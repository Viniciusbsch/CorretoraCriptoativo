import java.util.Scanner;

public class Main{
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Usuario cadastro = new Usuario();
        int op;

        do{
            System.out.println("Escolha uma opção: " +
                    "\n1 - Cadastrar Usuario " +
                    "\n2 - Exibir Usuario " +
                    "\n3 - Autenticar Email e Senha" +
                    "\n0 - Sair");
            op = sc.nextInt();

            switch (op){
                case 1:
                    System.out.println("Digite o nome completo: ");
                    String nome = sc.next() + sc.nextLine();
                    System.out.println("Digite o email: ");
                    String email = sc.next() + sc.nextLine();
                    System.out.println("Digite a senha: ");
                    String senha = sc.next() + sc.nextLine();
                    cadastro = new Usuario(nome, email, senha);

                    Usuario usuario = new Usuario();
                    System.out.println("Usuário criado com ID: " + usuario.getId());

                    if (Autenticacao.registrar(nome, email, senha)) {
                        System.out.println("Usuário registrado com sucesso!");
                    } else {
                        System.out.println("Falha no registro, execute novamente o processo.");
                    }

                    break;
                case 2:
                    cadastro.exibirInformacoes();
                    break;
                case 3:
                    System.out.println("Digite seu email: ");
                    String setEmail = sc.next() + sc.nextLine();
                    System.out.println("Digite sua senha: ");
                    String setSenha = sc.next() + sc.nextLine();

                    if (Autenticacao.autenticar(setEmail, setSenha)) {
                        System.out.println("Autenticação bem-sucedida!");
                    } else {
                        System.out.println("Falha na autenticação. Verifique seus dados e tente novamente.");
                    }

                    break;
                case 0:
                    System.out.println("Saindo do Sistema");
                default:
                    System.out.println("Opção Invalida!");
            }
        } while (op != 0);
        sc.close();
    }
}

