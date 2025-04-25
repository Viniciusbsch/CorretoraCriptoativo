package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private Long id;
    private String nome;
    private String cpf;
    private String email;
    private List<Conta> contas;
    private List<Carteira> carteiras;

    public Usuario(Long id, String nome, String cpf, String email) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.contas = new ArrayList<>();
        this.carteiras = new ArrayList<>();
    }

    public Usuario(String nome, String cpf, String email) {
        this(null, nome, cpf, email);
    }

    public void adicionarConta(Conta conta) {
        if (conta != null) {
            this.contas.add(conta);
        }
    }

    public Conta getConta(int numeroConta) {
        for (Conta conta : contas) {
            if (conta.getNumeroConta() == numeroConta) {
                return conta;
            }
        }
        return null;
    }

    public List<Conta> getContas() {
        return new ArrayList<>(contas);
    }

    public String getInfoContas() {
        if (contas.isEmpty()) {
            return "Nenhuma conta associada.";
        } else {
            StringBuilder info = new StringBuilder();
            for (Conta conta : contas) {
                info.append("Conta: ").append(conta.getNumeroConta()).append("\n");
            }
            return info.toString();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public void setContas(List<Conta> contas) {
        this.contas = new ArrayList<>(contas);
    }

    public Carteira getCarteira(Long idCriptoativo) {
        for (Carteira carteira : carteiras) {
            if (carteira.getCriptoativo() != null && idCriptoativo.equals(carteira.getCriptoativo().id())) {
                return carteira;
            }
        }
        return null;
    }

    public void adicionarOuAtualizarCarteira(Carteira carteira) {
        // ... restante do código ...
    }

    @Override
    public String toString() {
        return "Usuario{" +
               "id=" + id +
               ", nome='" + nome + '\'' +
               ", cpf='" + cpf + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}