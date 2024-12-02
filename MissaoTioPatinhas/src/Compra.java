public class Compra extends Transacao {

    public Compra(Conta conta, Criptoativo criptoativo, double quantidade, double precoNoMomento) {
        super(conta, criptoativo, quantidade, precoNoMomento);
    }

    @Override
    public void executar() {
        System.out.println("Executando compra de " + getQuantidade() + " " + getCriptoativo().getNomeCriptoativo());
        Conta conta = getConta();
        Carteira carteira = conta.getCarteira(getCriptoativo().getNomeCriptoativo());

        if (carteira != null) {
            carteira.adicionarSaldo(getQuantidade());
            // Adicionar lógica adicional, como debitar o dinheiro utilizado para a compra
        } else {
            System.out.println("Carteira para " + getCriptoativo().getNomeCriptoativo() + " não encontrada.");
        }
    }
} 