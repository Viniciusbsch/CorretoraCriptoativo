package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa uma transação de transferência de criptoativo entre contas.
 */
public class Transferencia extends Transacao {
    private Conta contaDestino;

    /**
     * Construtor para uma nova Transferencia (sem ID, data/hora gerada).
     */
    public Transferencia(Conta contaOrigem, Conta contaDestino, Criptoativo criptoativo, BigDecimal quantidade, BigDecimal precoNoMomento) {
        super(contaOrigem, criptoativo, quantidade, precoNoMomento);
        if (contaDestino == null) {
            throw new IllegalArgumentException("Conta de destino não pode ser nula.");
        }
        this.contaDestino = contaDestino;
    }
    
    /**
     * Construtor para carregar uma Transferencia do banco (com ID e data/hora).
     */
    public Transferencia(Long id, Conta contaOrigem, Conta contaDestino, Criptoativo criptoativo, BigDecimal quantidade, BigDecimal precoNoMomento, LocalDateTime dataHora) {
        super(id, contaOrigem, criptoativo, quantidade, precoNoMomento, dataHora);
        if (contaDestino == null) {
            throw new IllegalArgumentException("Conta de destino não pode ser nula.");
        }
        this.contaDestino = contaDestino;
    }

    public Conta getContaDestino() {
        return contaDestino;
    }

    public void setContaDestino(Conta contaDestino) {
        this.contaDestino = contaDestino;
    }
} 