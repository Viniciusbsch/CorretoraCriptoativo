import java.util.UUID;
import java.time.LocalDate;

public class Criptoativo {
    private String codigo;
    private String nomeDoCriptoativo;
    private double quantidade;
    private double precoDeCompra;
    private LocalDate dataDeCompra;

    public Criptoativo(String nomeDoCriptoativo, double quantidade, double precoDeCompra) {
        this.codigo = UUID.randomUUID().toString();
        this.nomeDoCriptoativo = nomeDoCriptoativo;
        this.quantidade = quantidade;
        this.precoDeCompra = precoDeCompra;
        this.dataDeCompra = LocalDate.now();
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNomeDoCriptoativo() {
        return nomeDoCriptoativo;
    }

    public void setNomeDoCriptoativo(String nomeDoCriptoativo) {
        this.nomeDoCriptoativo = nomeDoCriptoativo;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public double getPrecoDeCompra() {
        return precoDeCompra;
    }

    public void setPrecoDeCompra(double precoDeCompra) {
        this.precoDeCompra = precoDeCompra;
    }

    public LocalDate getDataDeCompra() {
        return dataDeCompra;
    }

    public void setDataDeCompra(LocalDate dataDeCompra) {
        this.dataDeCompra = dataDeCompra;
    }

    public double calcularValorTotal() {
        return quantidade * precoDeCompra;
    }

    public void exibirInformacoes() {
        System.out.println("Código: " + codigo);
        System.out.println("Criptoativo: " + nomeDoCriptoativo);
        System.out.println("Quantidade: " + quantidade);
        System.out.println("Preço de Compra: " + precoDeCompra);
        System.out.println("Data de Compra: " + dataDeCompra);
        System.out.println("Valor Total: " + calcularValorTotal());
    }

}
