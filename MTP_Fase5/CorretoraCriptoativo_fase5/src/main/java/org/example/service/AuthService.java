package org.example.service;

import org.example.dao.UsuarioDAO;
import org.example.model.Autenticador;
import org.example.model.Usuario;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável pelas operações de autenticação e cadastro de usuários.
 */
public class AuthService {

    private UsuarioDAO usuarioDAO;

    public AuthService() {
        try {
            // Instancia o DAO. Idealmente, isso seria feito por injeção de dependência.
            this.usuarioDAO = new UsuarioDAO();
        } catch (SQLException e) {
            // Tratar erro de inicialização do DAO. Lançar uma exceção específica
            // ou logar o erro pode ser apropriado.
            throw new RuntimeException("Erro ao inicializar UsuarioDAO", e);
        }
    }

    /**
     * Cadastra um novo usuário no sistema.
     *
     * @param nome  Nome do usuário.
     * @param cpf   CPF do usuário (sem formatação).
     * @param email Email do usuário (usado como login).
     * @param senha Senha do usuário.
     * @return O objeto Usuario criado e salvo no banco.
     * @throws SQLException      Se ocorrer um erro durante a operação no banco.
     * @throws IllegalArgumentException Se algum dado de entrada for inválido (ex: email já existe).
     */
    public Usuario cadastrarUsuario(String nome, String cpf, String email, String senha)
            throws SQLException, IllegalArgumentException {

        // TODO: Adicionar validações mais robustas (formato CPF, email, força da senha)

        // Verificar se o email já existe
        if (usuarioDAO.buscarPorEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado.");
        }
        // Verificar se o CPF já existe (se necessário, conforme regra de negócio)
        // Optional<Usuario> usuarioPorCpf = usuarioDAO.buscarPorCpf(cpf); // Descomentar e ajustar se necessário
        // if (usuarioPorCpf.isPresent()) {
        //     throw new IllegalArgumentException("CPF já cadastrado.");
        // }

        Usuario novoUsuario = new Usuario(nome, cpf, email); // ID será gerado pelo DAO/BD
        Autenticador novoAutenticador = new Autenticador(senha); // ID será gerado pelo DAO/BD

        // O método salvar do DAO cuida da transação e atribui os IDs
        usuarioDAO.salvar(novoUsuario, novoAutenticador);

        return novoUsuario; // Retorna o usuário com o ID atribuído
    }

    /**
     * Autentica um usuário com base no email e senha.
     * ALERTA: A senha está sendo comparada em texto plano! Implementar hashing.
     *
     * @param email Email do usuário.
     * @param senha Senha fornecida para autenticação.
     * @return Um Optional contendo o Usuario se a autenticação for bem-sucedida,
     *         ou Optional.empty() caso contrário.
     * @throws SQLException Se ocorrer um erro durante a operação no banco.
     */
    public Optional<Usuario> autenticarUsuario(String email, String senha) throws SQLException {
        Optional<Usuario> usuarioOpt = usuarioDAO.buscarPorEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            Optional<Autenticador> autenticadorOpt = usuarioDAO.buscarAutenticadorPorUsuarioId(usuario.getId());

            if (autenticadorOpt.isPresent()) {
                Autenticador autenticador = autenticadorOpt.get();
                // !! COMPARAÇÃO DE SENHA EM TEXTO PLANO - INSEGURO !!
                if (autenticador.getSenha().equals(senha)) {
                    return usuarioOpt; // Autenticação bem-sucedida
                }
            }
        }

        return Optional.empty(); // Falha na autenticação (usuário não encontrado ou senha incorreta)
    }

    /**
     * Lista todos os usuários cadastrados no sistema.
     *
     * @return Uma lista de todos os usuários.
     * @throws SQLException Se ocorrer um erro durante a operação no banco.
     */
    public List<Usuario> listarTodosUsuarios() throws SQLException {
        return usuarioDAO.buscarTodos();
    }

    // O DAO gerencia sua própria conexão, mas se precisarmos fechar explicitamente:
    // public void close() {
    //     try {
    //         if (usuarioDAO != null) {
    //             usuarioDAO.close();
    //         }
    //     } catch (SQLException e) {
    //         // Logar erro ao fechar DAO
    //         System.err.println("Erro ao fechar UsuarioDAO: " + e.getMessage());
    //     }
    // }
} 