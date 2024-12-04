import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Corretora {
    private static Corretora instancia;
    private Map<String, Carteira> reservaCriptoativos;
    private List<Conta> contasRegistradas;
    
    private Corretora() {
        this.reservaCriptoativos = new HashMap<>();
        this.contasRegistradas = new ArrayList<>();
        inicializarReservaCriptoativos();
    }
    
    private void inicializarReservaCriptoativos() {
        // Criando criptoativos populares
        Criptoativo bitcoin = new Criptoativo("Bitcoin", "BTC");
        Criptoativo ethereum = new Criptoativo("Ethereum", "ETH");
        Criptoativo binanceCoin = new Criptoativo("Binance Coin", "BNB");
        Criptoativo cardano = new Criptoativo("Cardano", "ADA");
        Criptoativo dogecoin = new Criptoativo("Dogecoin", "DOGE");
        
        // Adicionando saldos iniciais para cada criptoativo
        adicionarCriptoativo(bitcoin, 1000.0);      // 1000 BTC
        adicionarCriptoativo(ethereum, 10000.0);    // 10000 ETH
        adicionarCriptoativo(binanceCoin, 50000.0); // 50000 BNB
        adicionarCriptoativo(cardano, 1000000.0);   // 1000000 ADA
        adicionarCriptoativo(dogecoin, 5000000.0);  // 5000000 DOGE
        
        System.out.println("Corretora inicializada com sucesso!");
        exibirReservaCriptoativos();
    }
    
    public static Corretora getInstancia() {
        if (instancia == null) {
            instancia = new Corretora();
        }
        return instancia;
    }
    
    public void adicionarCriptoativo(Criptoativo criptoativo, double quantidade) {
        Carteira carteira = new Carteira(null, criptoativo); // null porque é a carteira da própria corretora
        carteira.adicionarSaldo(quantidade);
        reservaCriptoativos.put(criptoativo.getNomeCriptoativo(), carteira);
    }
    
    public void registrarConta(Conta conta) {
        contasRegistradas.add(conta);
    }
    
    public boolean executarCompra(Conta conta, Criptoativo criptoativo, double quantidade, double precoNoMomento) {
        Carteira carteiraCorretora = reservaCriptoativos.get(criptoativo.getNomeCriptoativo());
        
        if (carteiraCorretora == null || carteiraCorretora.getSaldo() < quantidade) {
            System.out.println("Corretora não possui " + criptoativo.getNomeCriptoativo() + " suficiente para venda.");
            return false;
        }
        
        // Verifica se a conta já possui uma carteira para este criptoativo
        if (conta.getCarteira(criptoativo.getNomeCriptoativo()) == null) {
            conta.adicionarCarteira(criptoativo);
        }
        
        // Executa a compra
        Carteira carteiraCliente = conta.getCarteira(criptoativo.getNomeCriptoativo());
        carteiraCliente.adicionarSaldo(quantidade);
        carteiraCorretora.subtrairSaldo(quantidade);
        
        return true;
    }
    
    public boolean executarVenda(Conta conta, Criptoativo criptoativo, double quantidade, double precoNoMomento) {
        Carteira carteiraCliente = conta.getCarteira(criptoativo.getNomeCriptoativo());
        
        if (carteiraCliente == null || carteiraCliente.getSaldo() < quantidade) {
            System.out.println("Conta não possui " + criptoativo.getNomeCriptoativo() + " suficiente para venda.");
            return false;
        }
        
        Carteira carteiraCorretora = reservaCriptoativos.get(criptoativo.getNomeCriptoativo());
        if (carteiraCorretora == null) {
            carteiraCorretora = new Carteira(null, criptoativo);
            reservaCriptoativos.put(criptoativo.getNomeCriptoativo(), carteiraCorretora);
        }
        
        // Executa a venda
        carteiraCliente.subtrairSaldo(quantidade);
        carteiraCorretora.adicionarSaldo(quantidade);
        
        return true;
    }
    
    public double consultarSaldoCorretora(String nomeCriptoativo) {
        Carteira carteira = reservaCriptoativos.get(nomeCriptoativo);
        return carteira != null ? carteira.getSaldo() : 0.0;
    }
    
    public void exibirReservaCriptoativos() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║           CRIPTOATIVOS DISPONÍVEIS                       ║");
        System.out.println("╠════╦═══════════════════╦══════════╦══════════════════════╣");
        System.out.println("║ ID ║    Criptoativo    ║  Sigla   ║    Saldo             ║");
        System.out.println("╠════╬═══════════════════╬══════════╬══════════════════════╣");
        
        int id = 1;
        for (Map.Entry<String, Carteira> entry : reservaCriptoativos.entrySet()) {
            Carteira carteira = entry.getValue();
            System.out.printf("║ %-2d ║ %-17s ║ %-8s ║ %,11.0f          ║\n",
                id++,
                carteira.getCriptoativo().getNomeCriptoativo(),
                carteira.getCriptoativo().getSigla(),
                carteira.getSaldo());
        }
        System.out.println("╚════╩═══════════════════╩══════════╩══════════════════════╝\n");
    }
    
    public Criptoativo getCriptoativoPorId(int id) {
        if (id < 1 || id > reservaCriptoativos.size()) {
            return null;
        }
        
        int currentId = 1;
        for (Carteira carteira : reservaCriptoativos.values()) {
            if (currentId == id) {
                return carteira.getCriptoativo();
            }
            currentId++;
        }
        return null;
    }
} 