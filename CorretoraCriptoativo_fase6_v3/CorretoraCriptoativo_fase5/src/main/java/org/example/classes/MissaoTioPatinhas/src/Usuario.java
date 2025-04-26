package org.example.classes.MissaoTioPatinhas.src;

import org.example.classes.MissaoTioPatinhas.src.dao.UsuarioDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private String nome;
    private String cpf;
    private String email;
    private List<Conta> contas;
    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();

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
        try {
            usuarioDAO.salvar(this, senha);
            System.out.println("Usuário salvo no banco de dados!");
        } catch (SQLException e) {
            System.err.println("Erro ao salvar usuário: " + e.getMessage());
        }
    }

    // Método para buscar um usuário pelo CPF
    public static Usuario buscarPorCpf(String cpf) {
        try {
            return usuarioDAO.buscarPorCpf(cpf);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário: " + e.getMessage());
            return null;
        }
    }

    // Método para listar todos os usuários
    public static List<Usuario> listarTodos() {
        try {
            return usuarioDAO.listarTodos();
        } catch (SQLException e) {
            System.err.println("Erro ao listar usuários: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Método para atualizar um usuário
    public boolean atualizar() {
        try {
            return usuarioDAO.atualizar(this);
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar usuário: " + e.getMessage());
            return false;
        }
    }

    // Método para excluir um usuário
    public boolean excluir() {
        try {
            return usuarioDAO.excluir(this.cpf);
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