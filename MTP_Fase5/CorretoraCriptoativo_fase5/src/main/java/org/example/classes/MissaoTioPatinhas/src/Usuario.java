package org.example.classes.MissaoTioPatinhas.src;

import org.example.factory.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private String nome;
    private String cpf;
    private String email;
    private List<Conta> contas;

    public Usuario(String nome, String cpf, String email) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.contas = new ArrayList<>();
    }

    public void criarConta() {
        Conta novaConta = new Conta();
        this.contas.add(novaConta);
        Corretora.getInstancia().registrarConta(novaConta);
    }

    public Conta getConta(String numeroConta) {
        for (Conta conta : contas) {
            if (conta.getNumeroConta().equals(numeroConta)) {
                return conta;
            }
        }
        return null;
    }

    public List<Conta> getContas() {
        return new ArrayList<>(contas);
    }

    public void exibirContas() {
        System.out.println("\n=== Contas de " + nome + " ===");
        if (contas.isEmpty()) {
            System.out.println("Nenhuma conta encontrada.");
        } else {
            for (Conta conta : contas) {
                System.out.println("Conta: " + conta.getNumeroConta());
            }
        }
    }

    // Método para salvar o usuário no banco de dados com senha específica
    public void salvarNoBanco(String senha) {
        Connection conexao = null;
        try {
            conexao = ConnectionFactory.getConnection();
            conexao.setAutoCommit(false); // Iniciar transação
            
            // Primeiro, criar e salvar um autenticador com a senha fornecida
            Autenticador autenticador = new Autenticador(senha);
            int idAutenticador = autenticador.salvarNoBanco();
            
            // Agora inserir na tabela de usuário com o ID do autenticador
            String sqlUsuario = "INSERT INTO t_mtp_usuario (num_cpf, des_nome, des_email, idt_autenticador) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmtUser = conexao.prepareStatement(sqlUsuario)) {
                // Converter CPF para número
                long cpfNumerico;
                try {
                    cpfNumerico = Long.parseLong(this.cpf.replaceAll("[^0-9]", ""));
                } catch (NumberFormatException e) {
                    System.err.println("CPF deve conter apenas números: " + e.getMessage());
                    conexao.rollback();
                    return;
                }
                
                stmtUser.setLong(1, cpfNumerico);
                stmtUser.setString(2, this.nome);
                stmtUser.setString(3, this.email);
                stmtUser.setInt(4, idAutenticador);
                
                stmtUser.executeUpdate();
            }
            
            conexao.commit(); // Confirmar transação
            System.out.println("Usuário salvo no banco de dados!");
        } catch (SQLException e) {
            try {
                if (conexao != null) {
                    conexao.rollback(); // Desfazer transação em caso de erro
                }
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
            }
            System.err.println("Erro ao salvar usuário: " + e.getMessage());
        } finally {
            try {
                if (conexao != null) {
                    conexao.setAutoCommit(true); // Restaurar autocommit
                    conexao.close();
                }
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }

    // Método para buscar um usuário pelo CPF
    public static Usuario buscarPorCpf(String cpf) {
        String sql = "SELECT num_cpf, des_nome, des_email FROM t_mtp_usuario WHERE num_cpf = ?";
        
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            
            // Converter CPF para número
            long cpfNumerico;
            try {
                cpfNumerico = Long.parseLong(cpf.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                System.err.println("CPF deve conter apenas números: " + e.getMessage());
                return null;
            }
            
            stmt.setLong(1, cpfNumerico);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String nome = rs.getString("des_nome");
                String email = rs.getString("des_email");
                String cpfStr = String.valueOf(rs.getLong("num_cpf"));
                return new Usuario(nome, cpfStr, email);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário: " + e.getMessage());
        }
        
        return null;
    }

    // Método para listar todos os usuários
    public static List<Usuario> listarTodos() {
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
        } catch (SQLException e) {
            System.err.println("Erro ao listar usuários: " + e.getMessage());
        }
        
        return usuarios;
    }

    // Método para atualizar um usuário
    public boolean atualizar() {
        String sql = "UPDATE t_mtp_usuario SET des_nome = ?, des_email = ? WHERE num_cpf = ?";
        
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            
            // Converter CPF para número
            long cpfNumerico;
            try {
                cpfNumerico = Long.parseLong(this.cpf.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                System.err.println("CPF deve conter apenas números: " + e.getMessage());
                return false;
            }
            
            stmt.setString(1, this.nome);
            stmt.setString(2, this.email);
            stmt.setLong(3, cpfNumerico);
            
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar usuário: " + e.getMessage());
            return false;
        }
    }

    // Método para excluir um usuário
    public boolean excluir() {
        String sql = "DELETE FROM t_mtp_usuario WHERE num_cpf = ?";
        
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            
            // Converter CPF para número
            long cpfNumerico;
            try {
                cpfNumerico = Long.parseLong(this.cpf.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                System.err.println("CPF deve conter apenas números: " + e.getMessage());
                return false;
            }
            
            stmt.setLong(1, cpfNumerico);
            
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir usuário: " + e.getMessage());
            return false;
        }
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}