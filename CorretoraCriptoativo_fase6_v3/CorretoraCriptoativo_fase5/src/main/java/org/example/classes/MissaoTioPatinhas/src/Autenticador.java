package org.example.classes.MissaoTioPatinhas.src;

import org.example.classes.MissaoTioPatinhas.src.dao.AutenticadorDAO;

import java.sql.SQLException;

public class Autenticador {
    private Usuario usuario;
    private int idAutenticador;
    private String senha;
    private static final AutenticadorDAO autenticadorDAO = new AutenticadorDAO();

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
        return autenticadorDAO.salvar(this.senha);
    }

    public boolean autenticar(String email, String senha) {
        try {
            return autenticadorDAO.autenticar(email, senha);
        } catch (SQLException e) {
            System.err.println("Erro ao autenticar usuário: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizarSenha(String novaSenha) {
        try {
            return autenticadorDAO.atualizarSenha(this.idAutenticador, novaSenha);
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar senha: " + e.getMessage());
            return false;
        }
    }

    public boolean excluir() {
        try {
            return autenticadorDAO.excluir(this.idAutenticador);
        } catch (SQLException e) {
            System.err.println("Erro ao excluir autenticador: " + e.getMessage());
            return false;
        }
    }

    public int getIdAutenticador() {
        return idAutenticador;
    }

    public void setIdAutenticador(int idAutenticador) {
        this.idAutenticador = idAutenticador;
    }
}
