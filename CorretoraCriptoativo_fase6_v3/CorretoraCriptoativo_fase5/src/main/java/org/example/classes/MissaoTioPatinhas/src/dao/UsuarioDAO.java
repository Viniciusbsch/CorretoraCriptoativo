package org.example.classes.MissaoTioPatinhas.src.dao;

import org.example.classes.MissaoTioPatinhas.src.Usuario;
import org.example.factory.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
    
    public void salvar(Usuario usuario, String senha) throws SQLException {
        Connection conexao = null;
        try {
            conexao = ConnectionFactory.getConnection();
            conexao.setAutoCommit(false);
            
            // Primeiro, criar e salvar um autenticador com a senha fornecida
            AutenticadorDAO autenticadorDAO = new AutenticadorDAO();
            int idAutenticador = autenticadorDAO.salvar(senha);
            
            // Agora inserir na tabela de usuário com o ID do autenticador
            String sqlUsuario = "INSERT INTO t_mtp_usuario (num_cpf, des_nome, des_email, idt_autenticador) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmtUser = conexao.prepareStatement(sqlUsuario)) {
                long cpfNumerico = Long.parseLong(usuario.getCpf().replaceAll("[^0-9]", ""));
                stmtUser.setLong(1, cpfNumerico);
                stmtUser.setString(2, usuario.getNome());
                stmtUser.setString(3, usuario.getEmail());
                stmtUser.setInt(4, idAutenticador);
                
                stmtUser.executeUpdate();
            }
            
            conexao.commit();
        } catch (SQLException e) {
            if (conexao != null) {
                conexao.rollback();
            }
            throw e;
        } finally {
            if (conexao != null) {
                conexao.setAutoCommit(true);
                conexao.close();
            }
        }
    }
    
    public Usuario buscarPorCpf(String cpf) throws SQLException {
        String sql = "SELECT num_cpf, des_nome, des_email FROM t_mtp_usuario WHERE num_cpf = ?";
        
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            
            long cpfNumerico = Long.parseLong(cpf.replaceAll("[^0-9]", ""));
            stmt.setLong(1, cpfNumerico);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String nome = rs.getString("des_nome");
                String email = rs.getString("des_email");
                String cpfStr = String.valueOf(rs.getLong("num_cpf"));
                return new Usuario(nome, cpfStr, email);
            }
        }
        return null;
    }
    
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT num_cpf, des_nome, des_email FROM t_mtp_usuario";
        
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                long cpfNumerico = rs.getLong("num_cpf");
                String nome = rs.getString("des_nome");
                String email = rs.getString("des_email");
                String cpfStr = String.valueOf(cpfNumerico);
                
                usuarios.add(new Usuario(nome, cpfStr, email));
            }
        }
        return usuarios;
    }
    
    public boolean atualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE t_mtp_usuario SET des_nome = ?, des_email = ? WHERE num_cpf = ?";
        
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            
            long cpfNumerico = Long.parseLong(usuario.getCpf().replaceAll("[^0-9]", ""));
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setLong(3, cpfNumerico);
            
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        }
    }
    
    public boolean excluir(String cpf) throws SQLException {
        String sql = "DELETE FROM t_mtp_usuario WHERE num_cpf = ?";
        
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            
            long cpfNumerico = Long.parseLong(cpf.replaceAll("[^0-9]", ""));
            stmt.setLong(1, cpfNumerico);
            
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        }
    }
} 