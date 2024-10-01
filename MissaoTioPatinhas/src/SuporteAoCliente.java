import java.util.UUID;

public class SuporteAoCliente {
    private String nChamado;
    private String nome;
    private String email;
    private String solicitacao;

    public SuporteAoCliente(String nome, String email, String mensagem) {
        this.nChamado = UUID.randomUUID().toString(); // Gera um ID único
        this.nome = nome;
        this.email = email;
        this.solicitacao = mensagem;
    }

    public String getId() {
        return nChamado;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMensagem() {
        return solicitacao;
    }

    public void setMensagem(String mensagem) {
            this.solicitacao = solicitacao;
    }

    public void exibirInformacoes() {
        System.out.println("Numero do chamado: " + nChamado);
        System.out.println("Nome: " + nome);
        System.out.println("Email: " + email);
        System.out.println("Solicitação: " + solicitacao);
    }

}

