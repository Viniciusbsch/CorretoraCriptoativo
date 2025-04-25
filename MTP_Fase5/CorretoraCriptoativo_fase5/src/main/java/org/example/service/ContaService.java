package org.example.service;

import org.example.dao.ContaDAO;
import org.example.model.Conta;
import org.example.model.Usuario;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Serviço para operações relacionadas a Contas.
 */
public class ContaService {

    private ContaDAO contaDAO;

    public ContaService() {
        try {
            this.contaDAO = new ContaDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar ContaDAO", e);
        }
    }

    /**
     * Cria uma nova conta para o usuário especificado.
     * Gera um número de conta (int) único.
     * @param usuario O usuário titular da conta.
     * @return A conta criada com ID gerado.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     * @throws IllegalArgumentException Se o usuário for nulo ou não tiver ID.
     */
    public Conta criarConta(Usuario usuario) throws SQLException {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("Usuário inválido para criar conta");
        }
        
        // Gerar número de conta (int) único
        int numeroConta = gerarNumeroContaUnico();
        
        // Criar e salvar a nova conta
        Conta novaConta = new Conta(numeroConta, usuario);
        contaDAO.salvar(novaConta);
        
        return novaConta;
    }

    /**
     * Busca todas as contas pertencentes ao usuário especificado.
     * 
     * @param usuario O usuário titular das contas.
     * @return Lista de contas do usuário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     * @throws IllegalArgumentException Se o usuário for nulo ou não tiver ID.
     */
    public List<Conta> buscarContasPorUsuario(Usuario usuario) throws SQLException {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("Usuário inválido para buscar contas");
        }
        
        return contaDAO.buscarPorUsuarioId(usuario.getId());
    }

    /**
     * Busca uma conta pelo seu número (int).
     * 
     * @param numeroConta O número da conta (int) a ser buscada.
     * @return Um Optional contendo a conta se encontrada.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Optional<Conta> buscarContaPorNumero(int numeroConta) throws SQLException {
        // Validação de número positivo pode ser adicionada se necessário
        // if (numeroConta <= 0) { return Optional.empty(); }
        
        return contaDAO.buscarPorNumeroConta(numeroConta);
    }

    /**
     * Busca uma conta pelo seu ID.
     * REMOVIDO: O ID da conta agora é o número da conta (int).
     * Use buscarContaPorNumero(int numeroConta) em vez disso.
     * 
     * @param idConta O ID da conta a ser buscada.
     * @return Um Optional contendo a conta se encontrada.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    /*
    public Optional<Conta> buscarContaPorId(Long idConta) throws SQLException {
        if (idConta == null) {
            return Optional.empty();
        }
        
        // Erro de compilação aqui: buscarPorId não existe mais em ContaDAO
        // return contaDAO.buscarPorId(idConta);
        throw new UnsupportedOperationException("Método buscarContaPorId(Long) foi removido. Use buscarContaPorNumero(int).");
    }
    */

    /**
     * Gera um número de conta (int) único de até 5 dígitos.
     * Verifica no banco se o número já existe.
     * @return O número de conta (int) gerado.
     * @throws SQLException Se ocorrer erro ao verificar existência no banco.
     */
    private int gerarNumeroContaUnico() throws SQLException {
        Random random = new Random();
        int numeroConta;
        int maxTentativas = 100; // Evitar loop infinito
        int tentativas = 0;
        
        do {
            numeroConta = random.nextInt(90000) + 10000; // Gera número entre 10000 e 99999
            tentativas++;
            if (tentativas > maxTentativas) {
                throw new SQLException("Não foi possível gerar um número de conta único após " + maxTentativas + " tentativas.");
            }
        } while (contaDAO.buscarPorNumeroConta(numeroConta).isPresent());
        
        return numeroConta;
    }

    // Adicionar outros métodos necessários (ex: getSaldo, etc.)

} 