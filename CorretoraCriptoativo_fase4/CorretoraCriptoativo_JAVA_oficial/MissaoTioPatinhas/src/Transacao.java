import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Transacao {
    private String idTransacao; 
    private Conta conta;
    private Criptoativo criptoativo;
    private double quantidade;
    private double precoNoMomento;
    private LocalDateTime dataHora;

    public Transacao(Conta conta, Criptoativo criptoativo, double quantidade, double precoNoMomento) {
        this.idTransacao = UUID.randomUUID().toString();
        this.conta = conta;
        this.criptoativo = criptoativo;
        this.quantidade = quantidade;
        this.precoNoMomento = precoNoMomento;
        this.dataHora = LocalDateTime.now();
    }

    public String getIdTransacao() {
        return idTransacao;
    }

    public Conta getConta() {
        return conta;
    }

    public Criptoativo getCriptoativo() {
        return criptoativo;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public double getPrecoNoMomento() {
        return precoNoMomento;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    // Método abstrato que será implementado nas subclasses
    public abstract void executar();
    
    public void exibirInformacoes() {
        System.out.println("ID da Transação: " + idTransacao);
        System.out.println("Conta: " + conta.getNumeroConta());
        System.out.println("Criptoativo: " + criptoativo.getNomeCriptoativo());
        System.out.println("Quantidade: " + quantidade);
        System.out.println("Preço no Momento: " + precoNoMomento);
        System.out.println("Data e Hora: " + dataHora);
    }
}