package org.example.classes.MissaoTioPatinhas.src;

public class Criptoativo {
    private int id; // opcional, se quiser manipular o id
    private String nome;
    private String sigla;
    private String descricao;
    private double cotacao;

    public Criptoativo(String nome, String sigla, String descricao, double cotacao) {
        this.nome = nome;
        this.sigla = sigla;
        this.descricao = descricao;
        this.cotacao = cotacao;
    }

    public Criptoativo(String nome, String sigla) {
        this(nome, sigla, "", 0.0);
    }

    public Criptoativo(String nome) {
        this(nome, nome.substring(0, 3).toUpperCase(), "", 0.0);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getSigla() { return sigla; }
    public void setSigla(String sigla) { this.sigla = sigla; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public double getCotacao() { return cotacao; }
    public void setCotacao(double cotacao) { this.cotacao = cotacao; }
    public String getNomeCriptoativo() {
        return nome;
    }
    public void setNomeCriptoativo(String nomeCriptoativo) {
        this.nome = nomeCriptoativo;
    }
}
