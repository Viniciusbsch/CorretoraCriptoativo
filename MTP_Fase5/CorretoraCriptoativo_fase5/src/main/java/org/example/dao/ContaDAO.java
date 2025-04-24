package org.example.dao;

import org.example.factory.ConnectionFactory;
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
    private UsuarioDAO usuarioDAO; // Para mapear o titular
    
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
     * @param conta O objeto Conta a ser salvo (sem ID definido, mas com titular associado).
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void salvar(Conta conta) throws SQLException {
        if (conta.getTitular() == null || conta.getTitular().getId() == null) {
            throw new SQLException("Titular da conta não pode ser nulo ou não ter ID definido.");
        }
        
        String sql = "INSERT INTO t_mtp_conta (des_numero_conta, idt_usuario) VALUES (?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"idt_conta"})) {
            stmt.setString(1, conta.getNumeroConta());
            stmt.setLong(2, conta.getTitular().getId());
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    conta.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Falha ao obter ID da conta, nenhum ID retornado.");
                }
            }
        }
    }
    
    /**
     * Busca uma conta pelo seu ID.
     * @param id O ID da conta.
     * @return Um Optional contendo a conta se encontrada, ou vazio caso contrário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Optional<Conta> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT c.idt_conta, c.des_numero_conta, c.idt_usuario " +
                    // "u.des_nome, u.num_cpf, u.des_email " +
                    "FROM t_mtp_conta c " +
                    // "JOIN t_mtp_usuario u ON c.idt_usuario = u.idt_usuario " +
                    "WHERE c.idt_conta = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
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
     * Busca uma conta pelo seu número único.
     * @param numeroConta O número da conta.
     * @return Um Optional contendo a conta se encontrada, ou vazio caso contrário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Optional<Conta> buscarPorNumeroConta(String numeroConta) throws SQLException {
        String sql = "SELECT c.idt_conta, c.des_numero_conta, c.idt_usuario " +
                    "FROM t_mtp_conta c WHERE c.des_numero_conta = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, numeroConta);
            
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
     * Busca todas as contas pertencentes a um determinado usuário.
     * @param usuarioId O ID do usuário titular.
     * @return Uma lista contendo as contas do usuário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public List<Conta> buscarPorUsuarioId(Long usuarioId) throws SQLException {
        List<Conta> contas = new ArrayList<>();
        
        String sql = "SELECT c.idt_conta, c.des_numero_conta, c.idt_usuario " +
                    "FROM t_mtp_conta c WHERE c.idt_usuario = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, usuarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Conta conta = mapearConta(rs);
                    contas.add(conta);
                }
            }
        }
        
        return contas;
    }
    
    /**
     * Atualiza os dados de uma conta existente (ex: número da conta, se permitido).
     * @param conta O objeto Conta com os dados atualizados (deve conter o ID).
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void atualizar(Conta conta) throws SQLException {
        if (conta.getId() == null) {
            throw new SQLException("ID da conta não pode ser nulo para atualização.");
        }
        
        String sql = "UPDATE t_mtp_conta SET des_numero_conta = ? WHERE idt_conta = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, conta.getNumeroConta());
            stmt.setLong(2, conta.getId());
            
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar conta, nenhum registro afetado.");
            }
        }
    }
    
    /**
     * Exclui uma conta do banco de dados pelo seu ID.
     * (Considerar as implicações em carteiras e transações associadas).
     * @param id O ID da conta a ser excluída.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void excluir(Long id) throws SQLException {
        // TODO: Implementar lógica de exclusão segura (verificar carteiras/transações)
        String sql = "DELETE FROM t_mtp_conta WHERE idt_conta = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                // Considerar se isso deve ser um erro ou apenas um aviso
                // throw new SQLException("Falha ao excluir conta, nenhum registro afetado.");
                System.out.println("Nenhuma conta encontrada com o ID " + id + " para exclusão.");
            }
        }
    }
    
    /**
     * Utilitário para mapear ResultSet para objeto Conta.
     * Busca o titular usando UsuarioDAO.
     * @param rs O ResultSet posicionado no registro a ser mapeado.
     * @return O objeto Conta com dados do ResultSet.
     * @throws SQLException Se ocorrer erro ao acessar os dados do ResultSet ou buscar titular.
     */
    private Conta mapearConta(ResultSet rs) throws SQLException {
        Long idConta = rs.getLong("idt_conta");
        String numeroConta = rs.getString("des_numero_conta");
        Long idUsuario = rs.getLong("idt_usuario");
        
        // Buscar o titular completo
        Usuario titular = usuarioDAO.buscarPorId(idUsuario)
            .orElseThrow(() -> new SQLException("Titular da conta não encontrado: ID " + idUsuario));
            
        Conta conta = new Conta(idConta, numeroConta, titular);
        
        // Não carrega carteiras aqui, isso é feito pelo CarteiraDAO/Service
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