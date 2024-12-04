public class Venda extends Transacao {

    public Venda(Conta conta, Criptoativo criptoativo, double quantidade, double precoNoMomento) {
        super(conta, criptoativo, quantidade, precoNoMomento);
    }

    @Override
    public void executar() {
        System.out.println("Executando venda de " + getQuantidade() + " " + getCriptoativo().getNomeCriptoativo());
        Conta conta = getConta();
        Carteira carteira = conta.getCarteira(getCriptoativo().getNomeCriptoativo());

        if (carteira != null) {
            carteira.subtrairSaldo(getQuantidade());
            // Adicionar lógica adicional, como creditar o dinheiro obtido na venda
        } else {
            System.out.println("Carteira para " + getCriptoativo().getNomeCriptoativo() + " não encontrada.");
        }
    }
} 