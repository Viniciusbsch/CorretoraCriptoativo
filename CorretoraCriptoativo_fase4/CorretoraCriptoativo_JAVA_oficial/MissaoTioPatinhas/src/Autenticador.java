public class Autenticador {
    private Usuario usuario;

    public Autenticador(Usuario usuario) {
        this.usuario = usuario;
    }

    public boolean autenticar(String email, String senha) {
        // Lógica de autenticação
        return true;
    }

    public void registrarUsuario(String nome, String email, String senha) {
        // Lógica de registro
    }
}
