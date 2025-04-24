package org.example.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Classe Conta como POJO
public class Conta {

    // Remover geração estática de número de conta
    // private static int ultimoNumeroConta = 1000;
    private Long id; // ID do banco de dados
    private String numeroConta; // Número da conta (pode ser String)
    private Usuario titular; // Adicionar referência ao titular
    // Usar ID do criptoativo como chave pode ser melhor para mapeamento
    private Map<Long, Carteira> carteiras; 
    // Histórico pode ser carregado sob demanda pelo DAO/Service
    // private List<Transacao> historicoTransacoes; 

    // Construtor para carregar do BD
    public Conta(Long id, String numeroConta, Usuario titular) {
        this.id = id;
        this.numeroConta = numeroConta;
        this.titular = titular;
        this.carteiras = new HashMap<>();
        // this.historicoTransacoes = new ArrayList<>(); // Inicialização movida
    }

    // Construtor para nova conta (sem ID ainda)
    public Conta(String numeroConta, Usuario titular) {
        this(null, numeroConta, titular);
    }

    // Remover geração de número de conta
    // private static synchronized String gerarNumeroConta() { ... }

    // Método para obter carteira (chave pode ser ID do Criptoativo)
    public Carteira getCarteira(Long idCriptoativo) {
        return carteiras.get(idCriptoativo);
    }
    
    // Método para adicionar/atualizar carteira
    public void adicionarOuAtualizarCarteira(Carteira carteira) {
        if (carteira != null && carteira.getCriptoativo() != null) {
            carteiras.put(carteira.getCriptoativo().id(), carteira);
        }
    }

    // REMOVER MÉTODOS COM LÓGICA DE SERVIÇO/NEGÓCIO/UI:
    // public boolean comprar(Criptoativo criptoativo, double quantidade, double precoNoMomento) { ... }
    // public boolean vender(Criptoativo criptoativo, double quantidade, double precoNoMomento) { ... }
    // public void transferir(Conta contaDestino, Criptoativo criptoativo, double quantidade, double precoNoMomento) { ... }
    // private void adicionarTransacao(Transacao transacao) { ... }
    // public void exibirHistoricoTransacoes() { ... }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroConta() {
        return numeroConta;
    }

    public void setNumeroConta(String numeroConta) {
        this.numeroConta = numeroConta;
    }

    public Usuario getTitular() {
        return titular;
    }

    public void setTitular(Usuario titular) {
        this.titular = titular;
    }

    public Map<Long, Carteira> getCarteiras() {
        // Retorna cópia para segurança
        return new HashMap<>(carteiras);
    }

    public void setCarteiras(Map<Long, Carteira> carteiras) {
        // Recebe um mapa e cria uma cópia
        this.carteiras = new HashMap<>(carteiras);
    }

    // Remover getter/setter para historicoTransacoes se ele não for mantido aqui
    // public List<Transacao> getHistoricoTransacoes() { ... }
    // public void setHistoricoTransacoes(List<Transacao> historicoTransacoes) { ... }

    @Override
    public String toString() {
        return "Conta{" +
               "id=" + id +
               ", numeroConta='" + numeroConta + '\'' +
               ", titular=" + (titular != null ? titular.getNome() : "null") +
               ", numCarteiras=" + carteiras.size() +
               '}';
    }
}
