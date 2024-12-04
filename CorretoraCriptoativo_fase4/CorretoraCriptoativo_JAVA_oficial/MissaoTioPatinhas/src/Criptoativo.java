public class Criptoativo {
    private String nomeCriptoativo;
    private String sigla;

    public Criptoativo(String nomeCriptoativo, String sigla) {
        this.nomeCriptoativo = nomeCriptoativo;
        this.sigla = sigla;
    }

    public String getNomeCriptoativo() {
        return nomeCriptoativo;
    }

    public String getSigla() {
        return sigla;
    }

    public void setNomeCriptoativo(String nomeCriptoativo) {
        this.nomeCriptoativo = nomeCriptoativo;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public Criptoativo(String nomeCriptoativo) {
        this(nomeCriptoativo, nomeCriptoativo.substring(0, 3).toUpperCase());
    }
}
