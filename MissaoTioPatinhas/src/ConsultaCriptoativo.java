import java.util.ArrayList;
import java.util.List;

public class ConsultaCriptoativo {
    private List<Criptoativo> criptoativos;

    public ConsultaCriptoativo() {
        this.criptoativos = new ArrayList<>();
    }

    public void adicionarCriptoativo(Criptoativo criptoativo) {
        criptoativos.add(criptoativo);
    }

    public Criptoativo buscarCriptoativo(String nome) {
        for (Criptoativo criptoativo : criptoativos) {
            if (criptoativo.getNomeCriptoativo().equalsIgnoreCase(nome)) {
                return criptoativo;
            }
        }
        return null; // Retorna null se o criptoativo não for encontrado
    }

    public void exibirTodosCriptoativos() {
        for (Criptoativo criptoativo : criptoativos) {
            System.out.println("Nome: " + criptoativo.getNomeCriptoativo() +
                    ", Sigla: " + criptoativo.getSigla());
        }
    }
}
