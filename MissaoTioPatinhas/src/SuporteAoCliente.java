import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SuporteAoCliente {
    private static SuporteAoCliente instancia;
    private List<Ticket> tickets;
    
    private SuporteAoCliente() {
        this.tickets = new ArrayList<>();
    }
    
    public static SuporteAoCliente getInstancia() {
        if (instancia == null) {
            instancia = new SuporteAoCliente();
        }
        return instancia;
    }
    
    public void criarTicket(Usuario usuario, String assunto, String descricao) {
        Ticket ticket = new Ticket(usuario, assunto, descricao);
        tickets.add(ticket);
        System.out.println("\n✅ Ticket criado com sucesso!");
        System.out.println("Número do ticket: " + ticket.getNumero());
    }
    
    public void listarTicketsUsuario(Usuario usuario) {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║              MEUS TICKETS DE SUPORTE             ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        
        boolean encontrou = false;
        for (Ticket ticket : tickets) {
            if (ticket.getUsuario().equals(usuario)) {
                encontrou = true;
                System.out.printf("║ Ticket #%-43s ║\n", ticket.getNumero());
                System.out.printf("║ Status: %-43s ║\n", ticket.getStatus());
                System.out.printf("║ Assunto: %-42s ║\n", ticket.getAssunto());
                System.out.printf("║ Data: %-44s ║\n", ticket.getDataCriacao().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                System.out.println("╠══════════════════════════════════════════════════╣");
            }
        }
        
        if (!encontrou) {
            System.out.println("║          Nenhum ticket encontrado                 ║");
        }
        System.out.println("╚══════════════════════════════════════════════════╝");
    }
    
    private static class Ticket {
        private static int ultimoNumero = 1000;
        private final String numero;
        private final Usuario usuario;
        private final String assunto;
        private final String descricao;
        private final LocalDateTime dataCriacao;
        private String status;
        
        public Ticket(Usuario usuario, String assunto, String descricao) {
            this.numero = "TK-" + (++ultimoNumero);
            this.usuario = usuario;
            this.assunto = assunto;
            this.descricao = descricao;
            this.dataCriacao = LocalDateTime.now();
            this.status = "Aberto";
        }
        
        public String getNumero() { return numero; }
        public Usuario getUsuario() { return usuario; }
        public String getAssunto() { return assunto; }
        public String getDescricao() { return descricao; }
        public LocalDateTime getDataCriacao() { return dataCriacao; }
        public String getStatus() { return status; }
    }
}
