import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conta {

    private static int ultimoNumeroConta = 1000;
    private String numeroConta;
    private Map<String, Carteira> carteiras;
    private List<Transacao> historicoTransacoes;

    public Conta() {
        this.numeroConta = gerarNumeroConta();
        this.carteiras = new HashMap<>();
        this.historicoTransacoes = new ArrayList<>();
    }

    private static synchronized String gerarNumeroConta() {
        ultimoNumeroConta++;
        return String.format("CC-%06d", ultimoNumeroConta);
    }

    public Carteira getCarteira(String nomeCriptoativo) {
        return carteiras.get(nomeCriptoativo);
    }

    public void adicionarCarteira(Criptoativo criptoativo) {
        carteiras.put(criptoativo.getNomeCriptoativo(), new Carteira(this, criptoativo));
    }

    public boolean comprar(Criptoativo criptoativo, double quantidade, double precoNoMomento) {
        if (Corretora.getInstancia().executarCompra(this, criptoativo, quantidade, precoNoMomento)) {
            Compra compra = new Compra(this, criptoativo, quantidade, precoNoMomento);
            historicoTransacoes.add(compra);
            return true;
        }
        return false;
    }

    public boolean vender(Criptoativo criptoativo, double quantidade, double precoNoMomento) {
        if (Corretora.getInstancia().executarVenda(this, criptoativo, quantidade, precoNoMomento)) {
            Venda venda = new Venda(this, criptoativo, quantidade, precoNoMomento);
            adicionarTransacao(venda);
            return true;
        }
        return false;
    }

    public void transferir(Conta contaDestino, Criptoativo criptoativo, double quantidade, double precoNoMomento) {
        Transferencia transferencia = new Transferencia(this, contaDestino, criptoativo, quantidade, precoNoMomento);
        adicionarTransacao(transferencia);
    }

    private void adicionarTransacao(Transacao transacao) {
        Carteira carteira = getCarteira(transacao.getCriptoativo().getNomeCriptoativo());
        if (carteira != null) {
            historicoTransacoes.add(transacao);
        } else {
            System.out.println("Carteira para " + transacao.getCriptoativo().getNomeCriptoativo() + " não encontrada.");
        }
    }

    public String getNumeroConta() {
        return numeroConta;
    }

    public List<Transacao> getHistoricoTransacoes() {
        return new ArrayList<>(historicoTransacoes);
    }

    public void exibirHistoricoTransacoes() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║        HISTÓRICO DE TRANSAÇÕES - CONTA " + numeroConta + "              ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════╣");
        
        if (historicoTransacoes.isEmpty()) {
            System.out.println("║            Nenhuma transação encontrada                    ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            return;
        }

        for (Transacao transacao : historicoTransacoes) {
            System.out.println("║ ID: " + transacao.getIdTransacao() + "                      ║");
            System.out.printf("║ Tipo: %-45s           ║\n", transacao.getClass().getSimpleName());
            System.out.printf("║ Criptoativo: %-39s          ║\n", transacao.getCriptoativo().getNomeCriptoativo());
            System.out.printf("║ Quantidade: %-40.2f          ║\n", transacao.getQuantidade());
            System.out.printf("║ Preço: R$ %-41.2f           ║\n", transacao.getPrecoNoMomento());
            System.out.printf("║ Data: %-44s            ║\n", transacao.getDataHora());
            System.out.println("╠═══════════════════════════════════════════════════════════════╣");
        }
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
    }

    public Map<String, Carteira> getCarteiras() {
        return new HashMap<>(carteiras);
    }

    // ... outros métodos e getters/setters ...
}
