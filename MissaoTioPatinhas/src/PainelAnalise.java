import java.util.ArrayList;
import java.util.List;

public class PainelAnalise {
    private List<Criptoativo> aquisicoes;

    public PainelAnalise() {
        this.aquisicoes = new ArrayList<>();
    }

    public void adicionarAquisicao(Criptoativo aquisicao) {
        aquisicoes.add(aquisicao);
    }

    public void exibirValores(double precoDeVenda) {
        for (Criptoativo aquisicao : aquisicoes) {
            double valorDeCompra = aquisicao.calcularValorTotal();
            double valorDeVenda = aquisicao.getQuantidade() * precoDeVenda;
            System.out.println("Criptoativo: " + aquisicao.getNomeDoCriptoativo());
            System.out.println("Quantidade: " + aquisicao.getQuantidade());
            System.out.println("Valor de Compra: " + valorDeCompra);
            System.out.println("Valor de Venda: " + valorDeVenda);
            System.out.println("Lucro/Prejuízo se efetivar a venda: " + (valorDeVenda - valorDeCompra));
        }
    }
}
