package org.example.classes.MissaoTioPatinhas.src;

import org.example.factory.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

    // Método para salvar o usuário no banco de dados
    public void salvarNoBanco() {
        String sql = "INSERT INTO t_mtp_usuario (num_cpf, des_nome, des_email) VALUES (?, ?, ?)";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, this.cpf);
            stmt.setString(2, this.nome);
            stmt.setString(3, this.email);

            stmt.executeUpdate();
            System.out.println("Usuário salvo no banco de dados!");
        } catch (SQLException e) {
            System.err.println("Erro ao salvar usuário: " + e.getMessage());
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