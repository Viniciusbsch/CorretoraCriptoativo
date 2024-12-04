import java.util.Map;
import java.text.DecimalFormat;

public class PainelAnalise {
    private static DecimalFormat df = new DecimalFormat("#,##0.00");
    
    public static void exibirAnalise(Conta conta) {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║               PAINEL DE ANÁLISE                           ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        
        Map<String, Carteira> carteiras = conta.getCarteiras();
        if (carteiras.isEmpty()) {
            System.out.println("║        Nenhum criptoativo para analisar               ║");
            System.out.println("╚═══════════════════════════════════════════════════════╝");
            return;
        }
        
        for (Carteira carteira : carteiras.values()) {
            double valorSimulado = simularValorAtual(carteira);
            double variacao = simularVariacao();
            
            System.out.printf("║       %-15s                                     ║\n", 
                carteira.getCriptoativo().getNomeCriptoativo());
            System.out.printf("║       Saldo: %-43s  ║\n", 
                df.format(carteira.getSaldo()));
            System.out.printf("║       Valor Atual: R$ %-35s ║\n", 
                df.format(valorSimulado));
            System.out.printf("║       Variação 24h: %-36s  ║\n", 
                (variacao >= 0 ? "+" : "") + df.format(variacao) + "%");
            System.out.println("╠═══════════════════════════════════════════════════════════╣");
        }
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
    }
    
    private static double simularValorAtual(Carteira carteira) {
        // Simulação simples de valor atual
        return carteira.getSaldo() * 50000.0;
    }
    
    private static double simularVariacao() {
        // Simulação de variação entre -5% e +5%
        return Math.random() * 10 - 5;
    }
} 