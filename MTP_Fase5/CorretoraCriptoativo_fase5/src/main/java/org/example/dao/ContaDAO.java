package org.example.dao;

import org.example.factory.ConnectionFactory;
import org.example.model.Autenticador;
import org.example.model.Conta;
import org.example.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe para acesso a dados da entidade Conta.
 * Implementa AutoCloseable para permitir uso em try-with-resources.
 */
public class ContaDAO implements AutoCloseable {
    private Connection connection;
    private UsuarioDAO usuarioDAO; // Para buscar o titular via autenticador
    
    /**
     * Construtor que inicializa a conexão com o banco de dados.
     * @throws SQLException Se houver erro ao obter conexão.
     */
    public ContaDAO() throws SQLException {
        this.connection = ConnectionFactory.getConnection();
        this.usuarioDAO = new UsuarioDAO(); // Instancia o DAO necessário
    }
    
    /**
     * Salva uma nova conta no banco de dados.
     * Assume que conta.getNumeroConta() já está definido e é a PK.
     * Busca o ID do autenticador associado ao usuário titular.
     * @param conta O objeto Conta a ser salvo (com numeroConta e titular definidos).
     * @throws SQLException Se ocorrer um erro no banco de dados, se o titular for nulo ou se o autenticador não for encontrado.
     */
    public void salvar(Conta conta) throws SQLException {
        if (conta.getTitular() == null || conta.getTitular().getId() == null) {
            throw new SQLException("Titular da conta não pode ser nulo ou não ter ID definido para buscar autenticador.");
        }
        if (conta.getNumeroConta() <= 0) { // Validar número da conta (PK)
            throw new SQLException("Número da conta inválido.");
        }

        // 1. Buscar o ID do autenticador associado ao usuário titular
        Long idAutenticador = usuarioDAO.buscarIdAutenticadorPorUsuarioId(conta.getTitular().getId())
                .orElseThrow(() -> new SQLException("Autenticador não encontrado para o usuário: " + conta.getTitular().getId()));

        // 2. Inserir a conta incluindo colunas com valores padrão (assumindo NOT NULL)
        String sql = "INSERT INTO t_mtp_conta (num_conta, idt_autenticador1, qtd_conta_registrada, qtd_reserva_criptoativo, val_saldo_conta) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, conta.getNumeroConta()); // PK
            stmt.setLong(2, idAutenticador); // FK
            stmt.setInt(3, 0); // Valor padrão para qtd_conta_registrada
            stmt.setInt(4, 0); // Valor padrão para qtd_reserva_criptoativo
            stmt.setBigDecimal(5, java.math.BigDecimal.ZERO); // Valor padrão para val_saldo_conta
            
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao inserir conta, nenhuma linha afetada.");
            }
        }
    }
    
    /**
     * Busca uma conta pelo seu número único (chave primária).
     * @param numeroConta O número da conta (PK).
     * @return Um Optional contendo a conta se encontrada, ou vazio caso contrário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Optional<Conta> buscarPorNumeroConta(int numeroConta) throws SQLException {
        // Seleciona num_conta e idt_autenticador1. idt_conta não existe.
        String sql = "SELECT c.num_conta, c.idt_autenticador1 " +
                     "FROM t_mtp_conta c WHERE c.num_conta = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, numeroConta);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Conta conta = mapearConta(rs);
                    return Optional.of(conta);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Busca todas as contas associadas a um determinado usuário (pelo ID do usuário).
     * Realiza a busca em duas etapas: busca idt_autenticador, depois busca contas.
     * @param usuarioId O ID do usuário titular.
     * @return Uma lista contendo as contas do usuário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public List<Conta> buscarPorUsuarioId(Long usuarioId) throws SQLException {
        List<Conta> contas = new ArrayList<>();

        // 1. Buscar o ID do autenticador associado ao usuário
        Optional<Long> idAutenticadorOpt = usuarioDAO.buscarIdAutenticadorPorUsuarioId(usuarioId);

        if (idAutenticadorOpt.isPresent()) {
            Long idAutenticador = idAutenticadorOpt.get();

            // 2. Buscar as contas usando o idt_autenticador1
            // Selecionar num_conta e idt_autenticador1
            String sql = "SELECT c.num_conta, c.idt_autenticador1 " +
                         "FROM t_mtp_conta c WHERE c.idt_autenticador1 = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, idAutenticador);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Conta conta = mapearConta(rs);
                        // Adicional: Se o titular já foi buscado uma vez, podemos reaproveitar?
                     ,      // Ou garantimos que mapearConta busque o titular correto sempre.
                        contas.add(conta);
                    }
                }
            }
        } // Se o autenticador não for encontrado, retorna lista vazia
        
        return contas;
    }
    
    /**
     * Atualiza os dados de uma conta existente.
     * IMPORTANTE: A chave primária NUM_CONTA geralmente não deve ser atualizada.
     * Este método pode precisar ser revisto dependendo do que PODE ser atualizado na conta.
     * Por enquanto, vamos assumir que NADA pode ser atualizado diretamente na T_MTP_CONTA além da FK (o que seria estranho).
     * Se for necessário atualizar o titular, a lógica seria via UsuarioDAO/Autenticador.
     * @param conta O objeto Conta com os dados atualizados (deve conter o numeroConta).
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void atualizar(Conta conta) throws SQLException {
         if (conta.getNumeroConta() <= 0) {
             throw new SQLException("Número da conta inválido para atualização.");
         }
         // Exemplo: Se quiséssemos atualizar o autenticador (o que não é comum)
         /*
         Long idAutenticador = usuarioDAO.buscarIdAutenticadorPorUsuarioId(conta.getTitular().getId())
                 .orElseThrow(() -> new SQLException("Autenticador não encontrado para o usuário: " + conta.getTitular().getId()));

         String sql = "UPDATE t_mtp_conta SET idt_autenticador1 = ? WHERE num_conta = ?";

         try (PreparedStatement stmt = connection.prepareStatement(sql)) {
             stmt.setLong(1, idAutenticador); // Novo autenticador
             stmt.setInt(2, conta.getNumeroConta()); // PK para o WHERE

             int linhasAfetadas = stmt.executeUpdate();
             if (linhasAfetadas == 0) {
                 throw new SQLException("Falha ao atualizar conta, nenhum registro afetado para o número: " + conta.getNumeroConta());
             }
         }
         */
         // No momento, este método não faz nada útil, pois a PK não deve mudar
         // e a FK idt_autenticador1 relaciona ao usuário, que não deveria mudar de conta.
         System.out.println("Aviso: Método atualizar() em ContaDAO atualmente não realiza operações.");
         // Lançar exceção ou logar um aviso pode ser mais apropriado.
         // throw new UnsupportedOperationException("Atualização de dados diretos na T_MTP_CONTA não suportada/implementada.");
    }
    
    /**
     * Exclui uma conta do banco de dados pelo seu número (PK).
     * (Considerar as implicações em carteiras e transações associadas - CASCADE ou validação prévia).
     * @param numeroConta O número da conta a ser excluída.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void excluir(int numeroConta) throws SQLException {
        if (numeroConta <= 0) {
            throw new SQLException("Número da conta inválido para exclusão.");
        }
        // TODO: Implementar lógica de exclusão segura (verificar carteiras/transações)
        String sql = "DELETE FROM t_mtp_conta WHERE num_conta = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, numeroConta);
            
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                // Considerar se isso é um erro ou apenas informativo
                 System.out.println("Nenhuma conta encontrada com o número " + numeroConta + " para exclusão.");
                // throw new SQLException("Conta com número " + numeroConta + " não encontrada para exclusão.");
            }
        }
    }
    
    /**
     * Utilitário para mapear ResultSet para objeto Conta.
     * Busca o titular usando o ID do Autenticador.
     * @param rs O ResultSet posicionado no registro a ser mapeado (deve conter num_conta, idt_autenticador1).
     * @return O objeto Conta com dados do ResultSet.
     * @throws SQLException Se ocorrer erro ao acessar os dados do ResultSet ou buscar titular.
     */
    private Conta mapearConta(ResultSet rs) throws SQLException {
        // Long idConta = rs.getLong("idt_conta"); // Coluna não existe
        int numeroConta = rs.getInt("num_conta"); // Obter a PK
        Long idAutenticador = rs.getLong("idt_autenticador1");
        
        // Buscar o usuário associado a este ID de autenticador
        Usuario titular = usuarioDAO.buscarPorIdAutenticador(idAutenticador)
            .orElseThrow(() -> new SQLException("Titular da conta não encontrado para o autenticador ID: " + idAutenticador + " associado à conta " + numeroConta));
            
        // Usar o construtor que não recebe mais o ID
        Conta conta = new Conta(numeroConta, titular);
        
        return conta;
    }
    
    /**
     * Fecha a conexão com o banco de dados.
     * @throws SQLException Se ocorrer erro ao fechar a conexão.
     */
    @Override
    public void close() throws SQLException {
        if (usuarioDAO != null) usuarioDAO.close(); // Fechar o DAO dependente
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
} 