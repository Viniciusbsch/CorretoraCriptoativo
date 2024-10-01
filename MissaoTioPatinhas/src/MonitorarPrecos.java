import java.util.HashMap;
import java.util.Map;

public class MonitorarPrecos {
    private Map<String, Double> criptoativos;

    public MonitorarPrecos() {
        this.criptoativos = new HashMap<>();
    }

    public void adicionarCriptoativo(String nome, double precoInicial) {
        criptoativos.put(nome, precoInicial);
    }

    public void atualizarPreco(String nome, double novoPreco) {
        if (criptoativos.containsKey(nome)) {
            criptoativos.put(nome, novoPreco);
        } else {
            System.out.println("Criptoativo não encontrado.");
        }
    }

    public void exibirVariacaoPrecos() {
        for (Map.Entry<String, Double> entry : criptoativos.entrySet()) {
            String nome = entry.getKey();
            double precoAtual = entry.getValue();
            System.out.println("Criptoativo: " + nome + " | Preço Atual: " + precoAtual);
        }
    }
}
