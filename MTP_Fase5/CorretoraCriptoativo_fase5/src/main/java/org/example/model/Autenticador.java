package org.example.model;

public class Autenticador {
    private Long idAutenticador; // Usar Long para permitir null e consistência
    private String senha; // A senha em si (cuidado com armazenamento em memória)

    // Construtor para criar um novo autenticador (sem ID ainda)
    public Autenticador(String senha) {
        this.senha = senha;
        // Validação de tamanho pode permanecer aqui ou ser movida para validação de entrada
        if (this.senha != null && this.senha.length() > 10) {
            // Idealmente, lançar uma exceção ou tratar na camada de serviço/UI
            // System.err.println("Aviso: Senha truncada para 10 caracteres.");
            this.senha = this.senha.substring(0, 10);
        }
        this.idAutenticador = null; // ID não definido na criação
    }

    // Construtor para carregar um autenticador existente (com ID)
    public Autenticador(Long idAutenticador, String senha) {
        this.idAutenticador = idAutenticador;
        this.senha = senha;
        // Reaplicar validação se necessário
        if (this.senha != null && this.senha.length() > 10) {
            this.senha = this.senha.substring(0, 10);
        }
    }

    // Getters e Setters
    public Long getIdAutenticador() {
        return idAutenticador;
    }

    public void setIdAutenticador(Long idAutenticador) {
        this.idAutenticador = idAutenticador;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
        if (this.senha != null && this.senha.length() > 10) {
            this.senha = this.senha.substring(0, 10);
        }
    }
}
