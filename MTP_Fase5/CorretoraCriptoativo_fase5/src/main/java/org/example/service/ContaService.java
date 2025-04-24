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
     * 
     * @param usuario O usuário titular da conta.
     * @return A conta criada com ID gerado.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     * @throws IllegalArgumentException Se o usuário for nulo ou não tiver ID.
     */
    public Conta criarConta(Usuario usuario) throws SQLException {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("Usuário inválido para criar conta");
        }
        
        // Gerar número de conta único com formato: CC-XXXXXX (onde X são dígitos)
        String numeroConta = gerarNumeroConta();
        
        // Verificar se o número de conta já existe
        while (contaDAO.buscarPorNumeroConta(numeroConta).isPresent()) {
            numeroConta = gerarNumeroConta();
        }
        
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
     * Busca uma conta pelo seu número.
     * 
     * @param numeroConta O número da conta a ser buscada.
     * @return Um Optional contendo a conta se encontrada.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Optional<Conta> buscarContaPorNumero(String numeroConta) throws SQLException {
        if (numeroConta == null || numeroConta.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return contaDAO.buscarPorNumeroConta(numeroConta);
    }

    /**
     * Busca uma conta pelo seu ID.
     * 
     * @param idConta O ID da conta a ser buscada.
     * @return Um Optional contendo a conta se encontrada.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Optional<Conta> buscarContaPorId(Long idConta) throws SQLException {
        if (idConta == null) {
            return Optional.empty();
        }
        
        return contaDAO.buscarPorId(idConta);
    }

    /**
     * Gera um número de conta aleatório no formato CC-XXXXXX.
     * 
     * @return O número de conta gerado.
     */
    private String gerarNumeroConta() {
        Random random = new Random();
        int numero = 100000 + random.nextInt(900000); // Gera número entre 100000 e 999999
        return "CC-" + numero;
    }

    // Adicionar outros métodos necessários (ex: buscarContaPorNumero, getSaldo, etc.)

} 