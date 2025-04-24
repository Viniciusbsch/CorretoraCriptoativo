package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Classe abstrata que representa uma transação financeira.
 */
public abstract class Transacao {
    private Long idTransacao;
    private Conta conta;
    private Criptoativo criptoativo;
    private BigDecimal quantidade;
    private BigDecimal precoNoMomento;
    private LocalDateTime dataHora;

    /**
     * Construtor para carregar do banco de dados (com ID).
     */
    public Transacao(Long idTransacao, Conta conta, Criptoativo criptoativo, BigDecimal quantidade, BigDecimal precoNoMomento, LocalDateTime dataHora) {
        if (conta == null || criptoativo == null || quantidade == null || precoNoMomento == null || dataHora == null) {
            throw new IllegalArgumentException("Argumentos da transação não podem ser nulos.");
        }
        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
             throw new IllegalArgumentException("Quantidade da transação deve ser positiva.");
        }
         if (precoNoMomento.compareTo(BigDecimal.ZERO) < 0) {
             throw new IllegalArgumentException("Preço no momento não pode ser negativo.");
        }

        this.idTransacao = idTransacao;
        this.conta = conta;
        this.criptoativo = criptoativo;
        this.quantidade = quantidade;
        this.precoNoMomento = precoNoMomento;
        this.dataHora = dataHora;
    }

    /**
     * Construtor para criar uma nova transação (sem ID ainda, data/hora gerada).
     */
    public Transacao(Conta conta, Criptoativo criptoativo, BigDecimal quantidade, BigDecimal precoNoMomento) {
        this(null, conta, criptoativo, quantidade, precoNoMomento, LocalDateTime.now());
    }

    public Long getIdTransacao() {
        return idTransacao;
    }

    public Conta getConta() {
        return conta;
    }

    public Criptoativo getCriptoativo() {
        return criptoativo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoNoMomento() {
        return precoNoMomento;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setIdTransacao(Long idTransacao) {
        this.idTransacao = idTransacao;
    }

    public void exibirInformacoes() {
        System.out.println("ID da Transação: " + (idTransacao != null ? idTransacao : "N/A"));
        System.out.println("Conta: " + (conta != null ? conta.getNumeroConta() : "N/A"));
        System.out.println("Criptoativo: " + (criptoativo != null ? criptoativo.nomeCriptoativo() : "N/A"));
        System.out.println("Quantidade: " + (quantidade != null ? quantidade.toPlainString() : "N/A"));
        System.out.println("Preço no Momento: " + (precoNoMomento != null ? precoNoMomento.toPlainString() : "N/A"));
        System.out.println("Data e Hora: " + (dataHora != null ? dataHora : "N/A"));
    }
}