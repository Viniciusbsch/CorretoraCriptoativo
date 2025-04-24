package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa uma transação de venda de criptoativo.
 */
public class Venda extends Transacao {

    /**
     * Construtor para uma nova Venda (sem ID, data/hora gerada).
     */
    public Venda(Conta conta, Criptoativo criptoativo, BigDecimal quantidade, BigDecimal precoNoMomento) {
        super(conta, criptoativo, quantidade, precoNoMomento);
    }
    
    /**
     * Construtor para carregar uma Venda do banco (com ID e data/hora).
     */
    public Venda(Long id, Conta conta, Criptoativo criptoativo, BigDecimal quantidade, BigDecimal precoNoMomento, LocalDateTime dataHora) {
        super(id, conta, criptoativo, quantidade, precoNoMomento, dataHora);
    }
} 