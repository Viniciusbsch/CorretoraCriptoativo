package org.example.service;

import org.example.dao.TransacaoDAO;
import org.example.exception.CorretoraException;
import org.example.model.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para operações de transações (Compra, Venda, Transferência).
 */
public class TransacaoService {

    private TransacaoDAO transacaoDAO;
    private CarteiraService carteiraService;

    public TransacaoService() {
        try {
            this.transacaoDAO = new TransacaoDAO();
            this.carteiraService = new CarteiraService();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar serviços de Transação", e);
        }
    }

    /**
     * Realiza a compra de um criptoativo para uma conta.
     * 
     * @param conta A conta que está realizando a compra.
     * @param criptoativo O criptoativo a ser comprado.
     * @param quantidade A quantidade a ser comprada.
     * @param precoMomento O preço unitário do criptoativo no momento da compra.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     * @throws IllegalArgumentException Se os parâmetros forem inválidos.
     * @throws CorretoraException Se a operação não puder ser concluída.
     */
    public void comprarCriptoativo(Conta conta, Criptoativo criptoativo, BigDecimal quantidade, BigDecimal precoMomento)
            throws SQLException, IllegalArgumentException, CorretoraException {
        
        validarParametrosTransacao(conta, criptoativo, quantidade, precoMomento);
        
        try {
            // Adicionar saldo à carteira (cria se não existir)
            carteiraService.adicionarSaldoCarteira(conta, criptoativo, quantidade);
            
            // Registrar a transação
            Compra compra = new Compra(conta, criptoativo, quantidade, precoMomento);
            transacaoDAO.salvar(compra);
            
        } catch (SQLException e) {
            throw new SQLException("Erro ao processar compra de criptoativo: " + e.getMessage(), e);
        }
    }

    /**
     * Realiza a venda de um criptoativo de uma conta.
     * 
     * @param conta A conta que está realizando a venda.
     * @param criptoativo O criptoativo a ser vendido.
     * @param quantidade A quantidade a ser vendida.
     * @param precoMomento O preço unitário do criptoativo no momento da venda.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     * @throws IllegalArgumentException Se os parâmetros forem inválidos.
     * @throws CorretoraException Se a operação não puder ser concluída (ex: saldo insuficiente).
     */
    public void venderCriptoativo(Conta conta, Criptoativo criptoativo, BigDecimal quantidade, BigDecimal precoMomento)
            throws SQLException, IllegalArgumentException, CorretoraException {
        
        validarParametrosTransacao(conta, criptoativo, quantidade, precoMomento);
        
        try {
            // Verificar se há saldo suficiente e subtrair
            boolean sucesso = carteiraService.subtrairSaldoCarteira(conta, criptoativo, quantidade);
            
            if (!sucesso) {
                throw new CorretoraException("Saldo insuficiente para realizar a venda");
            }
            
            // Registrar a transação
            Venda venda = new Venda(conta, criptoativo, quantidade, precoMomento);
            transacaoDAO.salvar(venda);
            
        } catch (SQLException e) {
            throw new SQLException("Erro ao processar venda de criptoativo: " + e.getMessage(), e);
        }
    }

    /**
     * Transfere criptoativos entre contas.
     * 
     * @param contaOrigem A conta de origem.
     * @param contaDestino A conta de destino.
     * @param criptoativo O criptoativo a ser transferido.
     * @param quantidade A quantidade a ser transferida.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     * @throws IllegalArgumentException Se os parâmetros forem inválidos.
     * @throws CorretoraException Se a operação não puder ser concluída (ex: saldo insuficiente).
     */
    public void transferirCriptoativo(Conta contaOrigem, Conta contaDestino, Criptoativo criptoativo, BigDecimal quantidade)
            throws SQLException, IllegalArgumentException, CorretoraException {
        
        // Validações básicas
        if (contaOrigem == null) {
            throw new IllegalArgumentException("Conta de origem inválida");
        }
        if (contaDestino == null) {
            throw new IllegalArgumentException("Conta de destino inválida");
        }
        if (criptoativo == null || criptoativo.id() == null) {
            throw new IllegalArgumentException("Criptoativo inválido");
        }
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        if (contaOrigem.getNumeroConta() == contaDestino.getNumeroConta()) {
            throw new IllegalArgumentException("Não é possível transferir para a mesma conta");
        }
        
        try {
            // Verificar se há saldo suficiente e subtrair da origem
            boolean sucesso = carteiraService.subtrairSaldoCarteira(contaOrigem, criptoativo, quantidade);
            
            if (!sucesso) {
                throw new CorretoraException("Saldo insuficiente para realizar a transferência");
            }
            
            // Adicionar à carteira de destino
            carteiraService.adicionarSaldoCarteira(contaDestino, criptoativo, quantidade);
            
            // Registrar a transação
            // Preço no momento é zero para transferência, já que não há compra/venda
            BigDecimal precoMomento = BigDecimal.ZERO; 
            Transferencia transferencia = new Transferencia(contaOrigem, contaDestino, criptoativo, quantidade, precoMomento);
            transacaoDAO.salvar(transferencia);
            
        } catch (SQLException e) {
            throw new SQLException("Erro ao processar transferência de criptoativo: " + e.getMessage(), e);
        }
    }

    /**
     * Lista todas as transações de uma conta.
     * 
     * @param conta A conta cujas transações serão listadas.
     * @return Lista de transações da conta.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     * @throws IllegalArgumentException Se a conta for inválida.
     */
    public List<Transacao> listarTransacoesPorConta(Conta conta) throws SQLException {
        if (conta == null) {
            throw new IllegalArgumentException("Conta inválida para listar transações");
        }
        
        try {
            return transacaoDAO.buscarPorNumeroConta(conta.getNumeroConta());
        } catch (SQLException e) {
            throw new SQLException("Erro ao listar transações: " + e.getMessage(), e);
        }
    }
    
    /**
     * Valida os parâmetros básicos de uma transação.
     */
    private void validarParametrosTransacao(Conta conta, Criptoativo criptoativo, BigDecimal quantidade, BigDecimal precoMomento) {
        if (conta == null) {
            throw new IllegalArgumentException("Conta inválida");
        }
        if (criptoativo == null || criptoativo.id() == null) {
            throw new IllegalArgumentException("Criptoativo inválido");
        }
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        if (precoMomento == null || precoMomento.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo");
        }
    }
} 