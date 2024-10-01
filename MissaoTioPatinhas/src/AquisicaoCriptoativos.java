public class AquisicaoCriptoativos {


    public static boolean comprarCriptoativo(Conta conta, Criptoativo aquisicao) {
        double valorTotal = aquisicao.calcularValorTotal();
            if (conta.getSaldo() >= valorTotal) {
                conta.sacar(valorTotal);
                System.out.println("Compra de " + aquisicao.getNomeDoCriptoativo() + " realizada com sucesso.");
                return true;
            } else {
                System.out.println("Saldo insuficiente para realizar a compra.");
                return false;
        }
    }
}
