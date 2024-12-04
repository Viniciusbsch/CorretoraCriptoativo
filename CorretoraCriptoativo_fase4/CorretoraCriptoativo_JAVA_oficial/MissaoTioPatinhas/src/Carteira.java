import java.util.ArrayList;
import java.util.List;

public class Carteira {
    private Conta conta;
    private Criptoativo criptoativo;
    private double saldo;

    public Carteira(Conta conta, Criptoativo criptoativo) {
        this.conta = conta;
        this.criptoativo = criptoativo;
        this.saldo = 0.0;
    }

    public Conta getConta() {
        return conta;
    }

    public Criptoativo getCriptoativo() {
        return criptoativo;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public void setCriptoativo(Criptoativo criptoativo) {
        this.criptoativo = criptoativo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public void adicionarSaldo(double valor) {
        this.saldo += valor;
    }

    public void adicionarSaldo(String valor) {
        this.saldo += Double.parseDouble(valor);
    }

    public void subtrairSaldo(double valor) {
        this.saldo -= valor;
    }

    public void subtrairSaldo(String valor) {
        this.saldo -= Double.parseDouble(valor);
    }
} 