public class Transferencia extends Transacao {
    private Conta contaDestino;

    public Transferencia(Conta contaOrigem, Conta contaDestino, Criptoativo criptoativo, double quantidade, double precoNoMomento) {
        super(contaOrigem, criptoativo, quantidade, precoNoMomento);
        this.contaDestino = contaDestino;
    }

    public Conta getContaDestino() {
        return contaDestino;
    }

    @Override
    public void executar() {
        System.out.println("Executando transferência de " + getQuantidade() + " " + getCriptoativo().getNomeCriptoativo() + 
                           " da conta " + getConta().getNumeroConta() + " para a conta " + contaDestino.getNumeroConta());
        Conta contaOrigem = getConta();
        Carteira carteiraOrigem = contaOrigem.getCarteira(getCriptoativo().getNomeCriptoativo());
        Carteira carteiraDestino = contaDestino.getCarteira(getCriptoativo().getNomeCriptoativo());

        if (carteiraOrigem != null && carteiraDestino != null) {
            carteiraOrigem.subtrairSaldo(getQuantidade());
            carteiraDestino.adicionarSaldo(getQuantidade());
            // Adicionar lógica adicional, se necessário
        } else {
            System.out.println("Uma das carteiras não foi encontrada.");
        }
    }
} 