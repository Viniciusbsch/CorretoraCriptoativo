import java.util.Scanner;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static DecimalFormat df = new DecimalFormat("#,##0.00");
    private static Corretora corretora = Corretora.getInstancia();
    private static Usuario usuarioAtual = null;
    private static Conta contaAtual = null;

    private static ArrayList<Usuario> listaUsuarios = new ArrayList<>();
    private static HashMap<String, Usuario> mapaUsuarios = new HashMap<>();
    private static HashMap<String, String> credenciais = new HashMap<>();
    private static ArrayList<String> logAutenticacao = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║         CORRETORA DE CRIPTOATIVOS        ║");
        System.out.println("╚══════════════════════════════════════════╝");

        carregarDados(); // Carrega dados dos arquivos ao iniciar
        menuAutenticacao();
        salvarDados();   // Salva dados em arquivos ao encerrar
    }

    private static void menuAutenticacao() {
        while (true) {
            System.out.println("\n┌─────────── MENU DE ACESSO ──────────────────┐");
            System.out.println("│ 1. Fazer Login                              │");
            System.out.println("│ 2. Criar Novo Usuário                       │");
            System.out.println("│ 3. Listar Usuários Cadastrados              │");
            System.out.println("│ 0. Sair                                     │");
            System.out.println("└─────────────────────────────────────────────┘");
            System.out.print("Escolha uma opção: ");

            try {
                int opcao = scanner.nextInt();
                scanner.nextLine(); // Limpar buffer

                switch (opcao) {
                    case 1:
                        realizarLogin();
                        break;
                    case 2:
                        cadastrarNovoUsuario();
                        break;
                    case 3:
                        listarUsuarios();
                        break;
                    case 0:
                        System.out.println("\nObrigado por usar nossos serviços!");
                        return;
                    default:
                        System.out.println("\n⚠️ Opção inválida!");
                }
            } catch (Exception e) {
                System.out.println("\n⚠️ Entrada inválida!");
                scanner.nextLine();
            }
        }
    }

    private static void realizarLogin() {
        System.out.println("\n═══ LOGIN ═══");
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        if (credenciais.containsKey(email) && credenciais.get(email).equals(senha)) {
            usuarioAtual = mapaUsuarios.get(email);
            logAutenticacao.add("Login: " + email + " - " + LocalDateTime.now());
            System.out.println("\n✅ Login realizado com sucesso!");
            menuPrincipal();
        } else {
            System.out.println("\n⚠️ Email ou senha incorretos!");
        }
    }

    private static void cadastrarNovoUsuario() {
        System.out.println("\n═══ NOVO CADASTRO ═══");

        System.out.print("Nome completo: ");
        String nome = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            System.out.println("\n⚠️ Todos os campos são obrigatórios!");
            return;
        }

        if (mapaUsuarios.containsKey(email)) {
            System.out.println("\n⚠️ Este email já está cadastrado!");
            return;
        }

        Usuario novoUsuario = new Usuario(nome, email, senha);
        listaUsuarios.add(novoUsuario);
        mapaUsuarios.put(email, novoUsuario);
        credenciais.put(email, senha);
        logAutenticacao.add("Cadastro: " + email + " - " + LocalDateTime.now());

        System.out.println("\n✅ Usuário cadastrado com sucesso!");
        System.out.println("Deseja fazer login agora? (S/N)");

        if (scanner.nextLine().trim().equalsIgnoreCase("S")) {
            usuarioAtual = novoUsuario;
            menuPrincipal();
        }
    }

    private static void listarUsuarios() {
        if (listaUsuarios.isEmpty()) {
            System.out.println("\n⚠️ Nenhum usuário cadastrado!");
            return;
        }

        System.out.println("\n═══ USUÁRIOS CADASTRADOS ═══");
        for (Usuario usuario : listaUsuarios) {
            System.out.println("Nome: " + usuario.getNome());
            System.out.println("Email: " + usuario.getEmail());
            System.out.println("─────────────────────────");
        }
    }

    private static void menuPrincipal() {
        while (true) {
            System.out.println("┌─────────── MENU PRINCIPAL ──────────────────────┐");
            System.out.println("│ 1. Criar nova conta                             │");
            System.out.println("│ 2. Acessar conta existente                      │");
            System.out.println("│ 3. Ver reservas da corretora                    │");
            System.out.println("│ 4. Ver minhas contas                            │");
            System.out.println("│ 5. Suporte ao cliente                           │");
            System.out.println("│ 0. Sair                                         │");
            System.out.println("└─────────────────────────────────────────────────┘");
            System.out.print("Escolha uma opção: ");

            try {
                int opcao = scanner.nextInt();
                scanner.nextLine(); // Limpar buffer

                switch (opcao) {
                    case 1:
                        criarConta();
                        break;
                    case 2:
                        acessarConta();
                        break;
                    case 3:
                        corretora.exibirReservaCriptoativos();
                        break;
                    case 4:
                        if (usuarioAtual != null) {
                            usuarioAtual.exibirContas();
                        } else {
                            System.out.println("\n⚠️ Você precisa criar uma conta primeiro!");
                        }
                        break;
                    case 5:
                        menuSuporte();
                        break;
                    case 0:
                        System.out.println("\nObrigado por usar nossos serviços!");
                        return;
                    default:
                        System.out.println("\n⚠️ Opção inválida!");
                }
            } catch (Exception e) {
                System.out.println("\n⚠️ Entrada inválida!");
                scanner.nextLine(); // Limpar buffer
            }
        }
    }

    private static void menuConta() {
        while (true) {
            String menu = String.format("\n┌─────────── MENU DA CONTA %-12s ────────────────────────────┐\n", contaAtual.getNumeroConta()) +
                    "│ 1. Comprar criptoativo                                            │\n" +
                    "│ 2. Vender criptoativo                                             │\n" +
                    "│ 3. Transferir criptoativo                                         │\n" +
                    "│ 4. Ver minhas carteiras                                           │\n" +
                    "│ 5. Ver histórico de transações                                    │\n" +
                    "│ 6. Painel de análise                                              │\n" +
                    "│ 0. Voltar ao menu principal                                       │\n" +
                    "└───────────────────────────────────────────────────────────────────┘";
            System.out.println(menu);
            System.out.print("Escolha uma opção: ");

            int opcao = scanner.nextInt();
            scanner.nextLine(); // Limpar buffer

            switch (opcao) {
                case 1:
                    realizarCompra();
                    break;
                case 2:
                    realizarVenda();
                    break;
                case 3:
                    realizarTransferencia();
                    break;
                case 4:
                    exibirCarteiras();
                    break;
                case 5:
                    contaAtual.exibirHistoricoTransacoes();
                    break;
                case 6:
                    PainelAnalise.exibirAnalise(contaAtual);
                    break;
                case 0:
                    contaAtual = null;
                    return;
                default:
                    System.out.println("\n⚠️ Opção inválida!");
            }
        }
    }

    private static void criarConta() {
        if (usuarioAtual == null) {
            System.out.println("\nPrimeiro, vamos cadastrar seus dados:");

            System.out.print("Nome: ");
            String nome = scanner.nextLine();

            System.out.print("CPF: ");
            String cpf = scanner.nextLine();

            System.out.print("Email: ");
            String email = scanner.nextLine();

            usuarioAtual = new Usuario(nome, cpf, email);
        }

        usuarioAtual.criarConta();
        String numeroConta = usuarioAtual.getContas().get(usuarioAtual.getContas().size() - 1).getNumeroConta();

        System.out.println("\n✅ Conta criada com sucesso!");
        System.out.println("Número da conta: " + numeroConta);
        System.out.println("Deseja acessar esta conta agora? (S/N)");

        if (scanner.nextLine().equalsIgnoreCase("S")) {
            contaAtual = usuarioAtual.getConta(numeroConta);
            menuConta();
        }
    }

    private static void acessarConta() {
        if (usuarioAtual == null) {
            System.out.println("\n⚠️ Você precisa criar uma conta primeiro!");
            return;
        }

        usuarioAtual.exibirContas();

        System.out.print("\nDigite o número da conta que deseja acessar: ");
        String numeroConta = scanner.nextLine();

        Conta conta = usuarioAtual.getConta(numeroConta);
        if (conta != null) {
            contaAtual = conta;
            menuConta();
        } else {
            System.out.println("\n⚠️ Conta não encontrada!");
        }
    }

    private static void realizarCompra() {
        try {
            System.out.println("\n═══ COMPRA DE CRIPTOATIVO ═══");

            corretora.exibirReservaCriptoativos();

            System.out.print("\nDigite o ID do criptoativo desejado: ");
            String idInput = scanner.nextLine().trim();
            if (idInput.isEmpty()) {
                throw new CorretoraException("ID do criptoativo não pode estar vazio!");
            }

            int idCripto = Integer.parseInt(idInput);
            Criptoativo cripto = corretora.getCriptoativoPorId(idCripto);
            if (cripto == null) {
                throw new CorretoraException("ID de criptoativo inválido!");
            }

            System.out.print("Digite a quantidade desejada: ");
            String quantidadeInput = scanner.nextLine().trim();
            if (quantidadeInput.isEmpty()) {
                throw new CorretoraException("Quantidade não pode estar vazia!");
            }

            double quantidade;
            try {
                quantidade = Double.parseDouble(quantidadeInput);
            } catch (NumberFormatException e) {
                throw new CorretoraException("Quantidade inválida! Use apenas números e ponto decimal.");
            }

            if (quantidade <= 0) {
                throw new CorretoraException("A quantidade deve ser maior que zero!");
            }

            double precoAtual = 50000.0;

            contaAtual.adicionarCarteira(cripto);
            if (!contaAtual.comprar(cripto, quantidade, precoAtual)) {
                throw new CorretoraException("Não foi possível realizar a compra!");
            }

            System.out.println("\n✅ Compra realizada com sucesso!");
            System.out.println("Criptoativo: " + cripto.getNomeCriptoativo() + " (" + cripto.getSigla() + ")");
            System.out.println("Quantidade: " + df.format(quantidade));

        } catch (CorretoraException e) {
            System.out.println("\n⚠️ " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("\n⚠️ Erro: Entrada numérica inválida!");
        } catch (Exception e) {
            System.out.println("\n⚠️ Erro inesperado: " + e.getMessage());
        }
    }

    private static void realizarVenda() {
        try {
            System.out.println("\n═══ VENDA DE CRIPTOATIVO ═══");

            if (contaAtual.getCarteiras().isEmpty()) {
                throw new CorretoraException("Você não possui criptoativos para vender!");
            }

            exibirCarteiras();

            System.out.print("\nDigite o ID do criptoativo: ");
            String idInput = scanner.nextLine().trim();
            if (idInput.isEmpty()) {
                throw new CorretoraException("ID do criptoativo não pode estar vazio!");
            }

            int idCripto = Integer.parseInt(idInput);
            Criptoativo cripto = getCriptoativoPorId(idCripto, contaAtual);
            if (cripto == null) {
                throw new CorretoraException("ID de criptoativo inválido!");
            }

            Carteira carteira = contaAtual.getCarteira(cripto.getNomeCriptoativo());
            if (carteira == null || carteira.getSaldo() <= 0) {
                throw new CorretoraException("Você não possui " + cripto.getNomeCriptoativo() + " para vender!");
            }

            System.out.print("Digite a quantidade a vender: ");
            String quantidadeInput = scanner.nextLine().trim();
            if (quantidadeInput.isEmpty()) {
                throw new CorretoraException("Quantidade não pode estar vazia!");
            }

            double quantidade;
            try {
                quantidade = Double.parseDouble(quantidadeInput);
            } catch (NumberFormatException e) {
                throw new CorretoraException("Quantidade inválida! Use apenas números e ponto decimal.");
            }

            if (quantidade <= 0) {
                throw new CorretoraException("A quantidade deve ser maior que zero!");
            }

            if (quantidade > carteira.getSaldo()) {
                throw new CorretoraException("Saldo insuficiente! Você possui apenas " +
                        df.format(carteira.getSaldo()) + " " + cripto.getSigla());
            }

            double precoAtual = 50000.0;

            if (!contaAtual.vender(cripto, quantidade, precoAtual)) {
                throw new CorretoraException("Não foi possível realizar a venda!");
            }

            System.out.println("\n✅ Venda realizada com sucesso!");
            System.out.println("Criptoativo: " + cripto.getNomeCriptoativo() + " (" + cripto.getSigla() + ")");
            System.out.println("Quantidade: " + df.format(quantidade));

        } catch (CorretoraException e) {
            System.out.println("\n⚠️ " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("\n⚠️ Erro: Entrada numérica inválida!");
        } catch (Exception e) {
            System.out.println("\n⚠️ Erro inesperado: " + e.getMessage());
        }
    }

    private static Criptoativo getCriptoativoPorId(int id, Conta conta) {
        int currentId = 1;
        for (Carteira carteira : conta.getCarteiras().values()) {
            if (currentId == id) {
                return carteira.getCriptoativo();
            }
            currentId++;
        }
        return null;
    }

    private static void realizarTransferencia() {
        try {
            System.out.println("\n═══ TRANSFERÊNCIA DE CRIPTOATIVO ═══");

            List<Conta> contas = usuarioAtual.getContas();
            if (contas.size() < 2) {
                throw new CorretoraException("Você precisa ter mais de uma conta para realizar transferências!");
            }

            exibirCarteiras();
            if (contaAtual.getCarteiras().isEmpty()) {
                throw new CorretoraException("Você não possui criptoativos para transferir!");
            }

            System.out.println("\nContas disponíveis para transferência:");
            for (Conta conta : contas) {
                if (!conta.getNumeroConta().equals(contaAtual.getNumeroConta())) {
                    System.out.println("→ Conta: " + conta.getNumeroConta());
                }
            }

            System.out.print("\nDigite o número da conta destino: ");
            String numeroContaDestino = scanner.nextLine().trim();

            Conta contaDestino = usuarioAtual.getConta(numeroContaDestino);
            if (contaDestino == null) {
                throw new CorretoraException("Conta destino não encontrada!");
            }
            if (contaDestino.getNumeroConta().equals(contaAtual.getNumeroConta())) {
                throw new CorretoraException("Não é possível transferir para a mesma conta!");
            }

            System.out.print("Digite o ID do criptoativo: ");
            String idInput = scanner.nextLine().trim();
            if (idInput.isEmpty()) {
                throw new CorretoraException("ID do criptoativo não pode estar vazio!");
            }

            int idCripto = Integer.parseInt(idInput);
            Criptoativo cripto = corretora.getCriptoativoPorId(idCripto);
            if (cripto == null) {
                throw new CorretoraException("ID de criptoativo inválido!");
            }

            Carteira carteiraOrigem = contaAtual.getCarteira(cripto.getNomeCriptoativo());
            if (carteiraOrigem == null || carteiraOrigem.getSaldo() <= 0) {
                throw new CorretoraException("Você não possui " + cripto.getNomeCriptoativo() + " para transferir!");
            }

            System.out.print("Digite a quantidade a transferir: ");
            String quantidadeInput = scanner.nextLine().trim();
            if (quantidadeInput.isEmpty()) {
                throw new CorretoraException("Quantidade não pode estar vazia!");
            }

            double quantidade;
            try {
                quantidade = Double.parseDouble(quantidadeInput);
            } catch (NumberFormatException e) {
                throw new CorretoraException("Quantidade inválida! Use apenas números e ponto decimal.");
            }

            if (quantidade <= 0) {
                throw new CorretoraException("A quantidade deve ser maior que zero!");
            }

            if (quantidade > carteiraOrigem.getSaldo()) {
                throw new CorretoraException("Saldo insuficiente! Você possui apenas " +
                        df.format(carteiraOrigem.getSaldo()) + " " + cripto.getSigla());
            }

            contaDestino.adicionarCarteira(cripto);

            contaAtual.transferir(contaDestino, cripto, quantidade, 50000.0);

            System.out.println("\n✅ Transferência realizada com sucesso!");
            System.out.println("De: Conta " + contaAtual.getNumeroConta());
            System.out.println("Para: Conta " + contaDestino.getNumeroConta());
            System.out.println("Quantidade: " + df.format(quantidade) + " " + cripto.getSigla());

        } catch (CorretoraException e) {
            System.out.println("\n⚠️ " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("\n⚠️ Erro: Entrada numérica inválida!");
        } catch (Exception e) {
            System.out.println("\n⚠️ Erro inesperado: " + e.getMessage());
        }
    }

    private static void exibirCarteiras() {
        Map<String, Carteira> carteiras = contaAtual.getCarteiras();

        if (carteiras.isEmpty()) {
            System.out.println("\n═══ MINHAS CARTEIRAS ═══");
            System.out.println("Nenhuma carteira encontrada.");
            return;
        }

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                MINHAS CARTEIRAS                          ║");
        System.out.println("╠════╦═══════════════════╦══════════╦══════════════════════╣");
        System.out.println("║ ID ║    Criptoativo    ║  Sigla   ║     Saldo            ║");
        System.out.println("╠════╬═══════════════════╬══════════╬══════════════════════╣");

        int id = 1;
        for (Carteira carteira : contaAtual.getCarteiras().values()) {
            System.out.printf("║ %-2d ║ %-17s ║ %-8s ║ %14.2f       ║ \n",
                    id++,
                    carteira.getCriptoativo().getNomeCriptoativo(),
                    carteira.getCriptoativo().getSigla(),
                    carteira.getSaldo());
        }
        System.out.println("╚════╩═══════════════════╩══════════╩══════════════════════╝");
    }

    private static void menuSuporte() {
        if (usuarioAtual == null) {
            System.out.println("\n⚠️ Você precisa estar logado para acessar o suporte!");
            return;
        }

        while (true) {
            System.out.println("\n┌─────────── SUPORTE AO CLIENTE ──────────────────┐");
            System.out.println("│ 1. Abrir novo ticket                              │");
            System.out.println("│ 2. Ver meus tickets                               │");
            System.out.println("│ 0. Voltar ao menu principal                       │");
            System.out.println("└───────────────────────────────────────────────────┘");
            System.out.print("Escolha uma opção: ");

            try {
                int opcao = scanner.nextInt();
                scanner.nextLine();

                switch (opcao) {
                    case 1:
                        abrirTicket();
                        break;
                    case 2:
                        SuporteAoCliente.getInstancia().listarTicketsUsuario(usuarioAtual);
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("\n⚠️ Opção inválida!");
                }
            } catch (Exception e) {
                System.out.println("\n⚠️ Entrada inválida!");
                scanner.nextLine();
            }
        }
    }

    private static void abrirTicket() {
        System.out.println("\n═══ NOVO TICKET DE SUPORTE ═══");

        System.out.print("Assunto: ");
        String assunto = scanner.nextLine().trim();

        System.out.println("Descrição (digite sua mensagem e pressione Enter):");
        String descricao = scanner.nextLine().trim();

        if (assunto.isEmpty() || descricao.isEmpty()) {
            System.out.println("\n⚠️ Assunto e descrição não podem estar vazios!");
            return;
        }

        SuporteAoCliente.getInstancia().criarTicket(usuarioAtual, assunto, descricao);
    }

    // Métodos para persistência em arquivo (a serem implementados)
    private static void salvarUsuariosEmArquivo() {
        try (FileWriter fw = new FileWriter("usuarios.txt");
             BufferedWriter bw = new BufferedWriter(fw)) {

            for (Usuario usuario : listaUsuarios) {
                bw.write(String.format("%s;%s;%s\n",
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getCpf()
                ));
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar usuários: " + e.getMessage());
        }
    }

    private static void carregarUsuariosDoArquivo() {
        try (FileReader fr = new FileReader("usuarios.txt");
             BufferedReader br = new BufferedReader(fr)) {

            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length == 3) {
                    Usuario usuario = new Usuario(dados[0], dados[1], dados[2]);
                    listaUsuarios.add(usuario);
                    mapaUsuarios.put(dados[1], usuario);
                    credenciais.put(dados[1], dados[2]);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar usuários: " + e.getMessage());
        }
    }

    private static void salvarLogAutenticacao() {
        try (FileWriter fw = new FileWriter("log_autenticacao.txt", true);
             BufferedWriter bw = new BufferedWriter(fw)) {

            for (String log : logAutenticacao) {
                bw.write(log + "\n");
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar log: " + e.getMessage());
        }
    }

    private static void carregarLogAutenticacao() {
        try (FileReader fr = new FileReader("log_autenticacao.txt");
             BufferedReader br = new BufferedReader(fr)) {

            String linha;
            while ((linha = br.readLine()) != null) {
                logAutenticacao.add(linha);
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar log: " + e.getMessage());
        }
    }

    // Método auxiliar para garantir que os dados sejam salvos ao encerrar
    private static void salvarDados() {
        salvarUsuariosEmArquivo();
        salvarLogAutenticacao();
    }

    // Método auxiliar para carregar os dados ao iniciar
    private static void carregarDados() {
        carregarUsuariosDoArquivo();
        carregarLogAutenticacao();
    }
} 
