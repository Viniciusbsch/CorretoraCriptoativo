package org.example.dao;

import org.example.factory.ConnectionFactory;
import org.example.model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe para acesso a dados da entidade Transacao.
 * Implementa AutoCloseable para permitir uso em try-with-resources.
 */
public class TransacaoDAO implements AutoCloseable {
    private Connection connection;
    private ContaDAO contaDAO;
    private CriptoativoDAO criptoativoDAO;

    public TransacaoDAO() throws SQLException {
        this.connection = ConnectionFactory.getConnection();
        // Instanciar outros DAOs necessários para mapeamento
        this.contaDAO = new ContaDAO(); 
        this.criptoativoDAO = new CriptoativoDAO();
    }

    /**
     * Salva uma nova transação no banco de dados.
     * @param transacao O objeto Transacao a ser salvo.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void salvar(Transacao transacao) throws SQLException {
        // SQL para inserir na tabela principal T_MTP_TRANSACAO
        // Colunas corrigidas: CONTA_ID, CRIPTOATIVO_ID, TIPO_TRANSACAO, QTD_UNIDADE_CRIPTOATIVO, PRECO_MOMENTO, DAT_TRANSACAO
        // IDT_TRANSACAO é a PK, esperamos que seja gerada pelo banco (SEQUENCE/IDENTITY)
        String sqlTransacao = "INSERT INTO t_mtp_transacao (conta_id, criptoativo_id, tipo_transacao, " +
                              "qtd_unidade_criptoativo, preco_momento, dat_transacao) " +
                              "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = this.connection; // Usar a conexão da classe
        PreparedStatement stmtTransacao = null;
        PreparedStatement stmtSubtipo = null;
        ResultSet generatedKeys = null;
        long idTransacaoGerado = -1;

        try {
            // Garantir que estamos numa transação controlada
            conn.setAutoCommit(false);

            // Preparar e executar a inserção na T_MTP_TRANSACAO
            stmtTransacao = conn.prepareStatement(sqlTransacao, new String[]{"idt_transacao"}); // Assume que a PK se chama IDT_TRANSACAO

            // Validar conta e criptoativo (se aplicável)
            if (transacao.getConta() == null) {
                 throw new SQLException("Conta da transação não pode ser nula.");
            }
             // Permitir Criptoativo nulo para Transferência? Por ora, vamos assumir que não.
            if (transacao.getCriptoativo() == null) {
                 throw new SQLException("Criptoativo da transação não pode ser nulo.");
            }

            stmtTransacao.setInt(1, transacao.getConta().getNumeroConta()); // CONTA_ID (FK para T_MTP_CONTA.NUM_CONTA)
            stmtTransacao.setLong(2, transacao.getCriptoativo().id());     // CRIPTOATIVO_ID (FK para T_MTP_CRIPTOATIVO.IDT_CRIPTOATIVO)
            stmtTransacao.setString(3, getTipoTransacaoString(transacao)); // TIPO_TRANSACAO ("COMPRA", "VENDA", "TRANSFERENCIA")
            stmtTransacao.setBigDecimal(4, transacao.getQuantidade());      // QTD_UNIDADE_CRIPTOATIVO
            stmtTransacao.setBigDecimal(5, transacao.getPrecoNoMomento());  // PRECO_MOMENTO
             // Usar setObject para TIMESTAMP WITH TIME ZONE se o driver/banco suportar
            stmtTransacao.setObject(6, OffsetDateTime.of(transacao.getDataHora(), ZoneOffset.UTC)); // DAT_TRANSACAO

            int linhasAfetadas = stmtTransacao.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir transação, nenhuma linha afetada.");
            }

            // Obter o IDT_TRANSACAO gerado
            generatedKeys = stmtTransacao.getGeneratedKeys();
            if (generatedKeys.next()) {
                idTransacaoGerado = generatedKeys.getLong(1);
                transacao.setIdTransacao(idTransacaoGerado); // Atualiza o objeto original
            } else {
                throw new SQLException("Falha ao obter ID da transação, nenhum ID retornado.");
            }

            // Inserir na tabela de subtipo correspondente
            String sqlSubtipo = null;
            if (transacao instanceof Compra) {
                sqlSubtipo = "INSERT INTO t_mtp_compra (idt_transacao) VALUES (?)";
                stmtSubtipo = conn.prepareStatement(sqlSubtipo);
                stmtSubtipo.setLong(1, idTransacaoGerado);
            } else if (transacao instanceof Venda) {
                sqlSubtipo = "INSERT INTO t_mtp_venda (idt_transacao) VALUES (?)";
                stmtSubtipo = conn.prepareStatement(sqlSubtipo);
                stmtSubtipo.setLong(1, idTransacaoGerado);
            } else if (transacao instanceof Transferencia) {
                sqlSubtipo = "INSERT INTO t_mtp_transferencia (idt_transacao, num_conta_destino) VALUES (?, ?)";
                Transferencia transf = (Transferencia) transacao;
                 if (transf.getContaDestino() == null) {
                     throw new SQLException("Conta de destino não pode ser nula para transferência.");
                 }
                stmtSubtipo = conn.prepareStatement(sqlSubtipo);
                stmtSubtipo.setLong(1, idTransacaoGerado);
                stmtSubtipo.setInt(2, transf.getContaDestino().getNumeroConta()); // NUM_CONTA_DESTINO
            } else {
                 // Se houver outros tipos no futuro ou erro
                 throw new SQLException("Tipo de transação não suportado para inserção de subtipo: " + transacao.getClass().getName());
            }

            if (stmtSubtipo != null) {
                linhasAfetadas = stmtSubtipo.executeUpdate();
                if (linhasAfetadas == 0) {
                    throw new SQLException("Falha ao inserir na tabela de subtipo " + transacao.getClass().getSimpleName() + ", nenhuma linha afetada.");
                }
            }

            conn.commit(); // Se tudo correu bem, comita a transação

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Em caso de erro, desfaz tudo
                } catch (SQLException ex) {
                    System.err.println("Erro ao fazer rollback: " + ex.getMessage());
                    // Logar ou tratar o erro de rollback
                }
            }
            // Re-lança a exceção original para ser tratada no nível superior
            throw new SQLException("Erro ao salvar transação: " + e.getMessage(), e);
        } finally {
            // Fechar recursos na ordem inversa de abertura
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { /* Log */ }
            if (stmtSubtipo != null) try { stmtSubtipo.close(); } catch (SQLException e) { /* Log */ }
            if (stmtTransacao != null) try { stmtTransacao.close(); } catch (SQLException e) { /* Log */ }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaura o autoCommit
                } catch (SQLException e) { /* Log */ }
                // Não fechar a conexão aqui, pois ela é gerenciada pelo DAO (close())
            }
        }
    }

    /**
     * Método auxiliar para obter a string do tipo de transação.
     * Pode ser útil para padronizar ou mapear nomes se necessário.
     */
    private String getTipoTransacaoString(Transacao transacao) {
        if (transacao instanceof Compra) return "COMPRA";
        if (transacao instanceof Venda) return "VENDA";
        if (transacao instanceof Transferencia) return "TRANSFERENCIA";
        return transacao.getClass().getSimpleName().toUpperCase(); // Fallback genérico
    }

    /**
     * Busca uma transação pelo seu ID.
     * @param id O ID da transação.
     * @return Um Optional contendo a transação se encontrada, ou vazio caso contrário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Optional<Transacao> buscarPorId(Long id) throws SQLException {
        // Selecionar colunas específicas com nomes corretos
        String sql = "SELECT IDT_TRANSACAO, CONTA_ID, CRIPTOATIVO_ID, TIPO_TRANSACAO, " +
                     "QTD_UNIDADE_CRIPTOATIVO, PRECO_MOMENTO, DAT_TRANSACAO " +
                     "FROM t_mtp_transacao WHERE idt_transacao = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Transacao transacao = mapearTransacao(rs);
                    return Optional.of(transacao);
                }
            }
        }
        
        return Optional.empty();
    }

    /**
     * Busca todas as transações associadas a uma conta.
     * @param numeroConta O número da conta.
     * @return Uma lista contendo as transações da conta.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public List<Transacao> buscarPorNumeroConta(int numeroConta) throws SQLException {
        List<Transacao> transacoes = new ArrayList<>();
        // Selecionar colunas específicas, usar CONTA_ID e DAT_TRANSACAO. 
        // Remoção da condição OR idt_conta_destino (requer query separada/join para recebidas)
        String sql = "SELECT IDT_TRANSACAO, CONTA_ID, CRIPTOATIVO_ID, TIPO_TRANSACAO, " +
                     "QTD_UNIDADE_CRIPTOATIVO, PRECO_MOMENTO, DAT_TRANSACAO " +
                     "FROM t_mtp_transacao WHERE conta_id = ? ORDER BY dat_transacao DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, numeroConta); // Usar CONTA_ID
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transacao transacao = mapearTransacao(rs);
                    transacoes.add(transacao);
                }
            }
        }
        
        return transacoes;
    }

    /**
     * Busca transações associadas a uma conta em um intervalo de datas.
     * @param numeroConta O número da conta.
     * @param inicio A data/hora inicial do intervalo.
     * @param fim A data/hora final do intervalo.
     * @return Uma lista contendo as transações dentro do período especificado.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public List<Transacao> buscarPorNumeroContaEPeriodo(int numeroConta, LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        List<Transacao> transacoes = new ArrayList<>();
        // Usar CONTA_ID e DAT_TRANSACAO. Remover OR idt_conta_destino.
        String sql = "SELECT IDT_TRANSACAO, CONTA_ID, CRIPTOATIVO_ID, TIPO_TRANSACAO, " +
                     "QTD_UNIDADE_CRIPTOATIVO, PRECO_MOMENTO, DAT_TRANSACAO " +
                     "FROM t_mtp_transacao " +
                     "WHERE conta_id = ? AND dat_transacao BETWEEN ? AND ? ORDER BY dat_transacao DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, numeroConta); // CONTA_ID
            stmt.setObject(2, OffsetDateTime.of(inicio, ZoneOffset.UTC)); // DAT_TRANSACAO inicio
            stmt.setObject(3, OffsetDateTime.of(fim, ZoneOffset.UTC));     // DAT_TRANSACAO fim
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transacao transacao = mapearTransacao(rs);
                    transacoes.add(transacao);
                }
            }
        }
        
        return transacoes;
    }
    
    /**
     * Busca transações associadas a um criptoativo específico.
     * @param criptoativoId O ID do criptoativo.
     * @return Uma lista contendo as transações do criptoativo.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public List<Transacao> buscarPorCriptoativoId(Long criptoativoId) throws SQLException {
        List<Transacao> transacoes = new ArrayList<>();
        // Usar CRIPTOATIVO_ID e DAT_TRANSACAO
        String sql = "SELECT IDT_TRANSACAO, CONTA_ID, CRIPTOATIVO_ID, TIPO_TRANSACAO, " +
                     "QTD_UNIDADE_CRIPTOATIVO, PRECO_MOMENTO, DAT_TRANSACAO " +
                     "FROM t_mtp_transacao WHERE criptoativo_id = ? ORDER BY dat_transacao DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, criptoativoId); // CRIPTOATIVO_ID
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transacao transacao = mapearTransacao(rs);
                    transacoes.add(transacao);
                }
            }
        }
        
        return transacoes;
    }
    
    /**
     * Busca transações por tipo (usando o nome da classe: "Compra", "Venda", "Transferencia").
     * @param tipo O nome do tipo/classe da transação.
     * @return Uma lista contendo as transações do tipo especificado.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public List<Transacao> buscarPorTipo(String tipo) throws SQLException {
        List<Transacao> transacoes = new ArrayList<>();
         // Usar TIPO_TRANSACAO e DAT_TRANSACAO. Usar UPPER para garantir consistência se necessário.
         // O tipo deve corresponder aos valores em TIPO_TRANSACAO ("COMPRA", "VENDA", "TRANSFERENCIA")
        String sql = "SELECT IDT_TRANSACAO, CONTA_ID, CRIPTOATIVO_ID, TIPO_TRANSACAO, " +
                     "QTD_UNIDADE_CRIPTOATIVO, PRECO_MOMENTO, DAT_TRANSACAO " +
                     "FROM t_mtp_transacao WHERE tipo_transacao = ? ORDER BY dat_transacao DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tipo); // TIPO_TRANSACAO
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transacao transacao = mapearTransacao(rs);
                    transacoes.add(transacao);
                }
            }
        }
        
        return transacoes;
    }

    /**
     * Mapeia um ResultSet para um objeto Transacao (Compra, Venda ou Transferencia).
     */
    private Transacao mapearTransacao(ResultSet rs) throws SQLException {
        // Ler dados da T_MTP_TRANSACAO com nomes corretos
        Long idTransacao = rs.getLong("idt_transacao");
        int numeroContaOrigem = rs.getInt("conta_id"); // Corrigido
        Long idCriptoativo = rs.getLong("criptoativo_id"); // Corrigido
        String tipo = rs.getString("tipo_transacao").toUpperCase(); // Corrigido e normalizado
        BigDecimal quantidade = rs.getBigDecimal("qtd_unidade_criptoativo"); // Corrigido
        BigDecimal precoNoMomento = rs.getBigDecimal("preco_momento"); // Corrigido
        
        // Tratamento da data/hora (DAT_TRANSACAO)
        LocalDateTime dataHoraLocal = null;
        try {
            OffsetDateTime odt = rs.getObject("dat_transacao", OffsetDateTime.class);
            if (odt != null) {
                dataHoraLocal = odt.toLocalDateTime(); // Converte para LocalDateTime (fuso horário local do servidor/aplicação)
            }
        } catch (SQLException e) {
            // Fallback ou log se getObject falhar ou tipo for inesperado
            System.err.println("Aviso: Não foi possível ler DAT_TRANSACAO como OffsetDateTime. Tentando como Timestamp. Erro: " + e.getMessage());
            Timestamp dthTransacaoTS = rs.getTimestamp("dat_transacao"); // Corrigido
             if (dthTransacaoTS != null) {
                 dataHoraLocal = dthTransacaoTS.toLocalDateTime();
             }
        }

        // Buscar objetos relacionados (Conta origem, Criptoativo)
        Conta contaOrigem = contaDAO.buscarPorNumeroConta(numeroContaOrigem)
                .orElseThrow(() -> new SQLException("Conta de origem da transação não encontrada: " + numeroContaOrigem + " para transação ID: " + idTransacao));
        
        // Criptoativo pode ser nulo em alguns cenários? Por ora, assumimos que não.
        Criptoativo criptoativo = criptoativoDAO.buscarPorId(idCriptoativo)
                .orElseThrow(() -> new SQLException("Criptoativo da transação não encontrado: " + idCriptoativo + " para transação ID: " + idTransacao));

        // Instanciar o tipo correto de Transacao
        switch (tipo) {
            case "COMPRA": // Usar constantes ou Enum seria mais seguro
                return new Compra(idTransacao, contaOrigem, criptoativo, quantidade, precoNoMomento, dataHoraLocal);
            case "VENDA":
                return new Venda(idTransacao, contaOrigem, criptoativo, quantidade, precoNoMomento, dataHoraLocal);
            case "TRANSFERENCIA":
                // Para transferência, buscar a conta destino na tabela T_MTP_TRANSFERENCIA
                int numeroContaDestino = buscarNumeroContaDestino(idTransacao);
                Conta contaDestino = contaDAO.buscarPorNumeroConta(numeroContaDestino)
                    .orElseThrow(() -> new SQLException("Conta destino da transferência não encontrada: " + numeroContaDestino + " para transação ID: " + idTransacao));
                return new Transferencia(idTransacao, contaOrigem, contaDestino, criptoativo, quantidade, precoNoMomento, dataHoraLocal);
            default:
                throw new SQLException("Tipo de transação desconhecido no mapeamento: " + tipo + " para transação ID: " + idTransacao);
        }
    }

    /**
     * Busca o número da conta de destino para uma dada transação de transferência.
     * @param idTransacao O ID da transação (PK da T_MTP_TRANSACAO).
     * @return O número da conta destino.
     * @throws SQLException Se a transação não for encontrada ou não for uma transferência, ou erro de DB.
     */
    private int buscarNumeroContaDestino(Long idTransacao) throws SQLException {
        String sql = "SELECT num_conta_destino FROM t_mtp_transferencia WHERE idt_transacao = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, idTransacao);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("num_conta_destino");
                } else {
                    throw new SQLException("Detalhes da transferência não encontrados para a transação ID: " + idTransacao + ". A transação existe mas não há registro correspondente em T_MTP_TRANSFERENCIA?");
                }
            }
        }
    }

    @Override
    public void close() throws SQLException {
        if (contaDAO != null) contaDAO.close();
        if (criptoativoDAO != null) criptoativoDAO.close();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
} 