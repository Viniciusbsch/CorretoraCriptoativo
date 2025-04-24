package org.example.model;

import java.math.BigDecimal;

public class Carteira {
    private Long id;
    private Conta conta;
    private Criptoativo criptoativo;
    private BigDecimal saldo;

    public Carteira(Long id, Conta conta, Criptoativo criptoativo, BigDecimal saldo) {
        this.id = id;
        this.conta = conta;
        this.criptoativo = criptoativo;
        this.saldo = saldo != null ? saldo : BigDecimal.ZERO;
    }

    public Carteira(Conta conta, Criptoativo criptoativo) {
        this(null, conta, criptoativo, BigDecimal.ZERO);
    }

    public Long getId() {
        return id;
    }

    public Conta getConta() {
        return conta;
    }

    public Criptoativo getCriptoativo() {
        return criptoativo;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public void setCriptoativo(Criptoativo criptoativo) {
        this.criptoativo = criptoativo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo != null ? saldo : BigDecimal.ZERO;
    }

    public void adicionarSaldo(BigDecimal valor) {
        if (valor != null && valor.compareTo(BigDecimal.ZERO) > 0) {
            this.saldo = this.saldo.add(valor);
        }
    }

    public boolean subtrairSaldo(BigDecimal valor) {
        if (valor != null && valor.compareTo(BigDecimal.ZERO) > 0 && this.saldo.compareTo(valor) >= 0) {
            this.saldo = this.saldo.subtract(valor);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Carteira{" +
               "id=" + id +
               ", conta=" + (conta != null ? conta.getNumeroConta() : "null") +
               ", criptoativo=" + (criptoativo != null ? criptoativo.sigla() : "null") +
               ", saldo=" + saldo +
               '}';
    }
} 