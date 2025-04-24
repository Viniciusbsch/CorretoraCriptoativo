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
        String sql = "INSERT INTO t_mtp_transacao (idt_conta, idt_criptoativo, tip_transacao, " +
                    "val_quantidade, val_preco_no_momento, dth_transacao, idt_conta_destino) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"idt_transacao"})) {
            stmt.setLong(1, transacao.getConta().getId());
            stmt.setLong(2, transacao.getCriptoativo().id());
            stmt.setString(3, transacao.getClass().getSimpleName()); // Compra, Venda, Transferencia
            stmt.setBigDecimal(4, transacao.getQuantidade());
            stmt.setBigDecimal(5, transacao.getPrecoNoMomento());
            stmt.setObject(6, OffsetDateTime.of(transacao.getDataHora(), ZoneOffset.UTC));

            if (transacao instanceof Transferencia) {
                Transferencia transf = (Transferencia) transacao;
                stmt.setLong(7, transf.getContaDestino().getId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transacao.setIdTransacao(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Falha ao obter ID da transação, nenhum ID retornado.");
                }
            }
        }
    }

    /**
     * Busca uma transação pelo seu ID.
     * @param id O ID da transação.
     * @return Um Optional contendo a transação se encontrada, ou vazio caso contrário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Optional<Transacao> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM t_mtp_transacao WHERE idt_transacao = ?";
        
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
     * @param contaId O ID da conta.
     * @return Uma lista contendo as transações da conta.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public List<Transacao> buscarPorContaId(Long contaId) throws SQLException {
        List<Transacao> transacoes = new ArrayList<>();
        String sql = "SELECT * FROM t_mtp_transacao WHERE idt_conta = ? OR idt_conta_destino = ? ORDER BY dth_transacao DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, contaId);
            stmt.setLong(2, contaId); // Inclui transferências recebidas
            
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
     * @param contaId O ID da conta.
     * @param inicio A data/hora inicial do intervalo.
     * @param fim A data/hora final do intervalo.
     * @return Uma lista contendo as transações dentro do período especificado.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public List<Transacao> buscarPorContaEPeriodo(Long contaId, LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        List<Transacao> transacoes = new ArrayList<>();
        String sql = "SELECT * FROM t_mtp_transacao " +
                     "WHERE (idt_conta = ? OR idt_conta_destino = ?) " +
                     "AND dth_transacao BETWEEN ? AND ? ORDER BY dth_transacao DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, contaId);
            stmt.setLong(2, contaId);
            stmt.setObject(3, OffsetDateTime.of(inicio, ZoneOffset.UTC));
            stmt.setObject(4, OffsetDateTime.of(fim, ZoneOffset.UTC));
            
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
        String sql = "SELECT * FROM t_mtp_transacao WHERE idt_criptoativo = ? ORDER BY dth_transacao DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, criptoativoId);
            
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
        String sql = "SELECT * FROM t_mtp_transacao WHERE tip_transacao = ? ORDER BY dth_transacao DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tipo);
            
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
        Long idTransacao = rs.getLong("idt_transacao");
        Long idConta = rs.getLong("idt_conta");
        Long idCriptoativo = rs.getLong("idt_criptoativo");
        String tipo = rs.getString("tip_transacao");
        BigDecimal quantidade = rs.getBigDecimal("val_quantidade");
        BigDecimal precoNoMomento = rs.getBigDecimal("val_preco_no_momento");
        Timestamp dthTransacaoTS = rs.getTimestamp("dth_transacao");
        LocalDateTime dataHoraLocal = dthTransacaoTS != null ? dthTransacaoTS.toLocalDateTime() : null;
        
        Long idContaDestino = rs.getLong("idt_conta_destino");
        if (rs.wasNull()) {
            idContaDestino = null;
        }

        Conta conta = contaDAO.buscarPorId(idConta)
                .orElseThrow(() -> new SQLException("Conta da transação não encontrada: " + idConta));
        Criptoativo criptoativo = criptoativoDAO.buscarPorId(idCriptoativo)
                .orElseThrow(() -> new SQLException("Criptoativo da transação não encontrado: " + idCriptoativo));

        switch (tipo) {
            case "Compra":
                return new Compra(idTransacao, conta, criptoativo, quantidade, precoNoMomento, dataHoraLocal);
            case "Venda":
                return new Venda(idTransacao, conta, criptoativo, quantidade, precoNoMomento, dataHoraLocal);
            case "Transferencia":
                if (idContaDestino == null) {
                    throw new SQLException("ID da conta de destino é nulo para uma transferência");
                }
                final Long finalIdContaDestino = idContaDestino;
                Conta contaDestino = contaDAO.buscarPorId(finalIdContaDestino)
                    .orElseThrow(() -> new SQLException("Conta destino da transação não encontrada: " + finalIdContaDestino));
                return new Transferencia(idTransacao, conta, contaDestino, criptoativo, quantidade, precoNoMomento, dataHoraLocal);
            default:
                throw new SQLException("Tipo de transação desconhecido: " + tipo);
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