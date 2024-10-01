import java.util.HashMap;
import java.util.Map;

public class Autenticacao {
    private static Map<String, Usuario> usersDb = new HashMap<>();

    public static boolean registrar(String nome, String email, String senha) {
        if (usersDb.containsKey(email)) {
            return false;
        }
        Usuario newUser = new Usuario(nome, email, senha);
        usersDb.put(email, newUser);
        return true;
    }

    public static boolean autenticar(String email, String senha) {
        if (usersDb.containsKey(email)) {
            return usersDb.get(email).getSenha().equals(senha);
        }
        return false;
    }
}