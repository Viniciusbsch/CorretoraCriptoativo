package org.example.service;

import org.example.dao.CarteiraDAO;
import org.example.model.Carteira;
import org.example.model.Conta;
import org.example.model.Criptoativo;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para operações relacionadas a Carteiras de Criptoativos.
 */
public class CarteiraService {

    private CarteiraDAO carteiraDAO;

    public CarteiraService() {
        try {
            this.carteiraDAO = new CarteiraDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar CarteiraDAO", e);
        }
    }

    /**
     * Busca todas as carteiras de uma determinada conta.
     * 
     * @param conta A conta cujas carteiras serão buscadas.
     * @return Lista de carteiras da conta.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     * @throws IllegalArgumentException Se a conta for nula ou sem ID.
     */
    public List<Carteira> buscarCarteirasPorConta(Conta conta) throws SQLException {
        if (conta == null) {
            throw new IllegalArgumentException("Conta inválida para buscar carteiras");
        }
        
        return carteiraDAO.buscarPorNumeroConta(conta.getNumeroConta());
    }

    /**
     * Busca uma carteira específica pela combinação de conta e criptoativo.
     * 
     * @param conta A conta da carteira.
     * @param criptoativo O criptoativo da carteira.
     * @return Um Optional contendo a carteira se encontrada.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     * @throws IllegalArgumentException Se a conta ou o criptoativo forem inválidos.
     */
    public Optional<Carteira> buscarCarteiraPorContaECriptoativo(Conta conta, Criptoativo criptoativo) 
            throws SQLException {
        if (conta == null) {
            throw new IllegalArgumentException("Conta inválida para buscar carteira");
        }
        if (criptoativo == null || criptoativo.id() == null) {
            throw new IllegalArgumentException("Criptoativo inválido para buscar carteira");
        }
        
        return carteiraDAO.buscarPorNumeroContaECriptoativo(conta.getNumeroConta(), criptoativo.id());
    }

    /**
     * Atualiza o saldo de uma carteira. Se a carteira não existir, cria uma nova.
     * 
     * @param conta A conta da carteira.
     * @param criptoativo O criptoativo da carteira.
     * @param novoSaldo O novo saldo da carteira.
     * @return A carteira atualizada ou criada.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     * @throws IllegalArgumentException Se os parâmetros forem inválidos.
     */
    public Carteira atualizarSaldoCarteira(Conta conta, Criptoativo criptoativo, BigDecimal novoSaldo) 
            throws SQLException {
        if (conta == null) {
            throw new IllegalArgumentException("Conta inválida para atualizar carteira");
        }
        if (criptoativo == null || criptoativo.id() == null) {
            throw new IllegalArgumentException("Criptoativo inválido para atualizar carteira");
        }
        if (novoSaldo == null || novoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo não pode ser nulo ou negativo");
        }
        
        // Tentar buscar a carteira existente
        Optional<Carteira> carteiraOpt = carteiraDAO.buscarPorNumeroContaECriptoativo(
                conta.getNumeroConta(), criptoativo.id());
        
        Carteira carteira;
        if (carteiraOpt.isPresent()) {
            // Atualizar carteira existente
            carteira = carteiraOpt.get();
            carteira.setSaldo(novoSaldo);
            carteiraDAO.atualizarSaldo(carteira);
        } else {
            // Criar nova carteira
            carteira = new Carteira(conta, criptoativo);
            carteira.setSaldo(novoSaldo);
            carteiraDAO.salvar(carteira);
        }
        
        return carteira;
    }
    
    /**
     * Adiciona um valor ao saldo de uma carteira. Se a carteira não existir, cria uma nova.
     * 
     * @param conta A conta da carteira.
     * @param criptoativo O criptoativo da carteira.
     * @param valor O valor a ser adicionado ao saldo.
     * @return A carteira atualizada ou criada.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     * @throws IllegalArgumentException Se os parâmetros forem inválidos.
     */
    public Carteira adicionarSaldoCarteira(Conta conta, Criptoativo criptoativo, BigDecimal valor) 
            throws SQLException {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor a adicionar deve ser positivo");
        }
        
        // Buscar a carteira ou criar uma nova se não existir
        Optional<Carteira> carteiraOpt = buscarCarteiraPorContaECriptoativo(conta, criptoativo);
        
        Carteira carteira;
        if (carteiraOpt.isPresent()) {
            carteira = carteiraOpt.get();
            carteira.adicionarSaldo(valor);
            carteiraDAO.atualizarSaldo(carteira);
        } else {
            carteira = new Carteira(conta, criptoativo);
            carteira.setSaldo(valor);
            carteiraDAO.salvar(carteira);
        }
        
        return carteira;
    }
    
    /**
     * Subtrai um valor do saldo de uma carteira, se houver saldo suficiente.
     * 
     * @param conta A conta da carteira.
     * @param criptoativo O criptoativo da carteira.
     * @param valor O valor a ser subtraído do saldo.
     * @return true se o saldo foi subtraído com sucesso, false se não há saldo suficiente.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     * @throws IllegalArgumentException Se os parâmetros forem inválidos.
     */
    public boolean subtrairSaldoCarteira(Conta conta, Criptoativo criptoativo, BigDecimal valor) 
            throws SQLException {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor a subtrair deve ser positivo");
        }
        
        // Buscar a carteira
        Optional<Carteira> carteiraOpt = buscarCarteiraPorContaECriptoativo(conta, criptoativo);
        
        if (carteiraOpt.isPresent()) {
            Carteira carteira = carteiraOpt.get();
            
            // Verificar se há saldo suficiente
            if (carteira.getSaldo().compareTo(valor) >= 0) {
                carteira.subtrairSaldo(valor);
                carteiraDAO.atualizarSaldo(carteira);
                return true;
            }
        }
        
        return false; // Sem saldo suficiente ou carteira não existe
    }

    // Adicionar outros métodos (ex: buscarCarteiraPorCriptoativo, atualizarSaldoCarteira)

} 