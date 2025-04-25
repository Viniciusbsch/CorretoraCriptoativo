package org.example.dao;

import org.example.factory.ConnectionFactory;
import org.example.model.Autenticador;
import org.example.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe para acesso a dados da entidade Usuario.
 * Implementa AutoCloseable para permitir uso em try-with-resources.
 */
public class UsuarioDAO implements AutoCloseable {
    private Connection connection;
    
    /**
     * Construtor que inicializa a conexão com o banco de dados.
     * @throws SQLException Se houver erro ao obter conexão.
     */
    public UsuarioDAO() throws SQLException {
        this.connection = ConnectionFactory.getConnection();
    }
    
    /**
     * Salva um novo usuário e seu autenticador associado no banco de dados.
     * @param usuario O objeto Usuario a ser salvo (sem ID definido).
     * @param autenticador O objeto Autenticador com a senha.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void salvar(Usuario usuario, Autenticador autenticador) throws SQLException {
        connection.setAutoCommit(false);
        
        try {
            // 1. Salvar o autenticador e obter o ID gerado
            String sqlAutenticador = "INSERT INTO t_mtp_autenticador (des_senha) VALUES (?)";
            try (PreparedStatement stmt = connection.prepareStatement(sqlAutenticador, new String[]{"idt_autenticador"})) {
                stmt.setString(1, autenticador.getSenha());
                stmt.executeUpdate();
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        autenticador.setIdAutenticador(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Falha ao obter ID do autenticador, nenhum ID retornado.");
                    }
                }
            }
            
            // 2. Salvar o usuário com o ID do autenticador.
            // O NUM_CPF é a chave primária e é fornecido, não gerado.
            String sqlUsuario = "INSERT INTO t_mtp_usuario (num_cpf, des_nome, des_email, idt_autenticador) VALUES (?, ?, ?, ?)";
            // Removido: new String[]{"idt_usuario"} - não esperamos chave gerada aqui
            try (PreparedStatement stmt = connection.prepareStatement(sqlUsuario)) { 
                long cpfNumerico = Long.parseLong(usuario.getCpf().replaceAll("[^0-9]", ""));
                
                stmt.setLong(1, cpfNumerico);
                stmt.setString(2, usuario.getNome());
                stmt.setString(3, usuario.getEmail());
                stmt.setLong(4, autenticador.getIdAutenticador());
                stmt.executeUpdate();
                
                // Definir o ID do usuário com base no CPF fornecido, que é a PK
                usuario.setId(cpfNumerico);

                // Removido: Bloco try (ResultSet generatedKeys...) que lia a chave gerada inexistente
            }
            
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    /**
     * Busca um usuário pelo seu ID.
     * @param id O ID do usuário.
     * @return Um Optional contendo o usuário se encontrado, ou vazio caso contrário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Optional<Usuario> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT num_cpf, des_nome, des_email " +
                     "FROM t_mtp_usuario WHERE num_cpf = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapearUsuario(rs);
                    return Optional.of(usuario);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Busca um usuário pelo seu CPF.
     * @param cpf O CPF do usuário.
     * @return Um Optional contendo o usuário se encontrado, ou vazio caso contrário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Optional<Usuario> buscarPorCpf(String cpf) throws SQLException {
        String sql = "SELECT num_cpf, des_nome, des_email " +
                     "FROM t_mtp_usuario WHERE num_cpf = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            long cpfNumerico = Long.parseLong(cpf.replaceAll("[^0-9]", ""));
            stmt.setLong(1, cpfNumerico);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapearUsuario(rs);
                    return Optional.of(usuario);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Busca um usuário pelo seu Email.
     * @param email O email do usuário.
     * @return Um Optional contendo o usuário se encontrado, ou vazio caso contrário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT num_cpf, des_nome, des_email " +
                     "FROM t_mtp_usuario WHERE des_email = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapearUsuario(rs);
                    return Optional.of(usuario);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Lista todos os usuários cadastrados.
     * @return Uma lista contendo todos os usuários.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public List<Usuario> buscarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT num_cpf, des_nome, des_email FROM t_mtp_usuario";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Usuario usuario = mapearUsuario(rs);
                usuarios.add(usuario);
            }
        }
        
        return usuarios;
    }
    
    /**
     * Atualiza os dados de um usuário existente (nome, email).
     * O CPF não deve ser alterado por este método.
     * @param usuario O objeto Usuario com os dados atualizados (deve conter o ID).
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void atualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE t_mtp_usuario SET des_nome = ?, des_email = ? WHERE num_cpf = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setLong(3, usuario.getId());
            
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar usuário, nenhum registro afetado.");
            }
        }
    }
    
    /**
     * Exclui um usuário do banco de dados pelo seu ID.
     * @param id O ID do usuário a ser excluído.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void excluir(Long id) throws SQLException {
        Long idAutenticador = null;
        String sqlBusca = "SELECT idt_autenticador FROM t_mtp_usuario WHERE num_cpf = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sqlBusca)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    idAutenticador = rs.getLong("idt_autenticador");
                } else {
                    throw new SQLException("Usuário não encontrado para exclusão.");
                }
            }
        }

        if (idAutenticador == null) {
             throw new SQLException("Não foi possível encontrar o autenticador associado ao usuário.");
        }
        
        connection.setAutoCommit(false);
        
        try {
            String sqlExcluirUsuario = "DELETE FROM t_mtp_usuario WHERE num_cpf = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sqlExcluirUsuario)) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
            }
            
            String sqlExcluirAutenticador = "DELETE FROM t_mtp_autenticador WHERE idt_autenticador = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sqlExcluirAutenticador)) {
                stmt.setLong(1, idAutenticador);
                stmt.executeUpdate();
            }
            
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    /**
     * Busca o autenticador associado a um usuário pelo ID do usuário.
     * @param usuarioId O ID do usuário.
     * @return Um Optional contendo o Autenticador se encontrado.
     * @throws SQLException Se ocorrer erro no banco.
     */
    public Optional<Autenticador> buscarAutenticadorPorUsuarioId(Long usuarioId) throws SQLException {
        String sqlUsuario = "SELECT idt_autenticador FROM t_mtp_usuario WHERE num_cpf = ?";
        Long idAutenticador = null;

        try (PreparedStatement stmtUsuario = connection.prepareStatement(sqlUsuario)) {
            stmtUsuario.setLong(1, usuarioId);
            try (ResultSet rsUsuario = stmtUsuario.executeQuery()) {
                if (rsUsuario.next()) {
                    idAutenticador = rsUsuario.getLong("idt_autenticador");
                } else {
                    return Optional.empty(); // Usuário não encontrado
                }
            }
        }
        
        if (idAutenticador == null) {
             return Optional.empty(); // ID do autenticador não encontrado
        }

        String sqlAutenticador = "SELECT des_senha FROM t_mtp_autenticador WHERE idt_autenticador = ?";
        try (PreparedStatement stmtAut = connection.prepareStatement(sqlAutenticador)) {
            stmtAut.setLong(1, idAutenticador);
            try (ResultSet rsAut = stmtAut.executeQuery()) {
                if (rsAut.next()) {
                    String senha = rsAut.getString("des_senha");
                    return Optional.of(new Autenticador(idAutenticador, senha));
                }
            }
        }
        return Optional.empty(); // Autenticador não encontrado
    }
    
    /**
     * Busca o ID do autenticador associado a um usuário pelo ID do usuário (num_cpf).
     * @param usuarioId O ID (num_cpf) do usuário.
     * @return Um Optional contendo o ID do Autenticador se encontrado.
     * @throws SQLException Se ocorrer erro no banco.
     */
    public Optional<Long> buscarIdAutenticadorPorUsuarioId(Long usuarioId) throws SQLException {
        String sql = "SELECT idt_autenticador FROM t_mtp_usuario WHERE num_cpf = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getLong("idt_autenticador"));
                }
            }
        }
        return Optional.empty(); // Usuário ou autenticador não encontrado
    }

    /**
     * Busca um usuário pelo ID do seu autenticador associado.
     * @param idAutenticador O ID do autenticador.
     * @return Um Optional contendo o usuário se encontrado.
     * @throws SQLException Se ocorrer erro no banco.
     */
    public Optional<Usuario> buscarPorIdAutenticador(Long idAutenticador) throws SQLException {
        String sql = "SELECT num_cpf, des_nome, des_email " +
                     "FROM t_mtp_usuario WHERE idt_autenticador = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, idAutenticador);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapearUsuario(rs);
                    return Optional.of(usuario);
                }
            }
        }
        return Optional.empty(); // Usuário não encontrado para este autenticador
    }
    
    /**
     * Utilitário para mapear ResultSet para objeto Usuario.
     * Assume que o ResultSet contém num_cpf, des_nome, des_email.
     * @param rs O ResultSet posicionado no registro a ser mapeado.
     * @return O objeto Usuario com dados do ResultSet.
     * @throws SQLException Se ocorrer erro ao acessar os dados do ResultSet.
     */
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        long id = rs.getLong("num_cpf");
        String nome = rs.getString("des_nome");
        String email = rs.getString("des_email");

        // Formatar o ID (num_cpf long) de volta para uma String CPF (11 dígitos)
        String cpfFormatado = String.format("%011d", id);
        // Opcional: adicionar formatação com pontos e traço se necessário no modelo
        // cpfFormatado = cpfFormatado.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");

        // Usar o construtor que aceita id, nome, cpf (String) e email
        Usuario usuario = new Usuario(id, nome, cpfFormatado, email);

        return usuario;
    }
    
    /**
     * Fecha a conexão com o banco de dados.
     * @throws SQLException Se ocorrer erro ao fechar a conexão.
     */
    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
} 