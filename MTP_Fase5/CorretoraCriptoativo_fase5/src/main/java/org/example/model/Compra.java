package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa uma transação de compra de criptoativo.
 */
public class Compra extends Transacao {

    /**
     * Construtor para uma nova Compra (sem ID, data/hora gerada).
     */
    public Compra(Conta conta, Criptoativo criptoativo, BigDecimal quantidade, BigDecimal precoNoMomento) {
        super(conta, criptoativo, quantidade, precoNoMomento);
    }
    
    /**
     * Construtor para carregar uma Compra do banco (com ID e data/hora).
     */
    public Compra(Long id, Conta conta, Criptoativo criptoativo, BigDecimal quantidade, BigDecimal precoNoMomento, LocalDateTime dataHora) {
        super(id, conta, criptoativo, quantidade, precoNoMomento, dataHora);
    }
} 