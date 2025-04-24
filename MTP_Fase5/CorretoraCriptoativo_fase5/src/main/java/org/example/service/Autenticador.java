package org.example.classes.MissaoTioPatinhas.src;

import org.example.factory.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Autenticador {
    private Usuario usuario;
    private int idAutenticador;
    private String senha;

    public Autenticador(Usuario usuario) {
        this.usuario = usuario;
    }

    // Construtor para criar um novo autenticador com senha
    public Autenticador(String senha) {
        this.senha = senha;
        // Limitar a senha a 10 caracteres conforme definido no banco de dados
        if (this.senha.length() > 10) {
            this.senha = this.senha.substring(0, 10);
        }
    }

    // Método para salvar o autenticador no banco de dados e retornar o ID gerado
    public int salvarNoBanco() throws SQLException {
        String sql = "INSERT INTO t_mtp_autenticador (des_senha) VALUES (?)";
        
        Connection conexao = null;
        try {
            conexao = ConnectionFactory.getConnection();
            
            // Inserir o autenticador
            try (PreparedStatement stmt = conexao.prepareStatement(sql, new String[]{"idt_autenticador"})) {
                stmt.setString(1, this.senha);
                stmt.executeUpdate();
                
                // Tentar obter o ID gerado
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        this.idAutenticador = rs.getInt(1);
                        return this.idAutenticador;
                    }
                }
            } catch (SQLException e) {
                // Se não conseguir obter o ID via getGeneratedKeys, tenta via consulta
                try (PreparedStatement stmt = conexao.prepareStatement("SELECT MAX(idt_autenticador) FROM t_mtp_autenticador")) {
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        this.idAutenticador = rs.getInt(1);
                        return this.idAutenticador;
                    }
                }
            }
            
            throw new SQLException("Não foi possível obter o ID do autenticador");
        } finally {
            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
            }
        }
    }

    public boolean autenticar(String email, String senha) {
        // Implementação da lógica de autenticação com o banco de dados
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
        } catch (SQLException e) {
            System.err.println("Erro ao autenticar usuário: " + e.getMessage());
            return false;
        }
    }

    public int getIdAutenticador() {
        return idAutenticador;
    }
}
