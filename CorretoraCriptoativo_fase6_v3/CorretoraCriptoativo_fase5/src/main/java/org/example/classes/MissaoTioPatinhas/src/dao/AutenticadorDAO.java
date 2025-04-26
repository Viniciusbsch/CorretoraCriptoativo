package org.example.classes.MissaoTioPatinhas.src.dao;

import org.example.factory.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AutenticadorDAO {
    
    public int salvar(String senha) throws SQLException {
        String sql = "INSERT INTO t_mtp_autenticador (des_senha) VALUES (?)";
        
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql, new String[]{"idt_autenticador"})) {
            
            // Limitar a senha a 10 caracteres conforme definido no banco de dados
            if (senha.length() > 10) {
                senha = senha.substring(0, 10);
            }
            
            stmt.setString(1, senha);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
            // Se não conseguir obter o ID via getGeneratedKeys, tenta via consulta
            try (PreparedStatement stmt2 = conexao.prepareStatement("SELECT MAX(idt_autenticador) FROM t_mtp_autenticador")) {
                ResultSet rs = stmt2.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
            throw new SQLException("Não foi possível obter o ID do autenticador");
        }
    }
    
    public boolean autenticar(String email, String senha) throws SQLException {
        String sql = "SELECT a.idt_autenticador, a.des_senha FROM t_mtp_autenticador a " +
                     "JOIN t_mtp_usuario u ON a.idt_autenticador = u.idt_autenticador " +
                     "WHERE u.des_email = ?";
        
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String senhaBD = rs.getString("des_senha");
                return senhaBD.equals(senha);
            }
            
            return false;
        }
    }
    
    public boolean atualizarSenha(int idAutenticador, String novaSenha) throws SQLException {
        String sql = "UPDATE t_mtp_autenticador SET des_senha = ? WHERE idt_autenticador = ?";
        
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            
            // Limitar a senha a 10 caracteres conforme definido no banco de dados
            if (novaSenha.length() > 10) {
                novaSenha = novaSenha.substring(0, 10);
            }
            
            stmt.setString(1, novaSenha);
            stmt.setInt(2, idAutenticador);
            
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        }
    }
    
    public boolean excluir(int idAutenticador) throws SQLException {
        String sql = "DELETE FROM t_mtp_autenticador WHERE idt_autenticador = ?";
        
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            
            stmt.setInt(1, idAutenticador);
            
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        }
    }
} 