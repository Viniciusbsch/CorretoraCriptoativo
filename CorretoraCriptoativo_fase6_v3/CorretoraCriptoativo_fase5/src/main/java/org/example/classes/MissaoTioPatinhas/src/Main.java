package org.example.classes.MissaoTioPatinhas.src;

import org.example.classes.MissaoTioPatinhas.src.dao.AutenticadorDAO;
import org.example.classes.MissaoTioPatinhas.src.dao.UsuarioDAO;

import java.io.*;
import java.util.Scanner;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static DecimalFormat df = new DecimalFormat("#,##0.00");
    private static Corretora corretora = Corretora.getInstancia();
    private static Usuario usuarioAtual = null;
    private static Conta contaAtual = null;
    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final AutenticadorDAO autenticadorDAO = new AutenticadorDAO();

    private static ArrayList<Usuario> listaUsuarios = new ArrayList<>();
    private static HashMap<String, Usuario> mapaUsuarios = new HashMap<>();
    private static HashMap<String, String> credenciais = new HashMap<>();
    private static ArrayList<String> logAutenticacao = new ArrayList<>();

    private static final String DATA_DIR = "data";
    private static final String USUARIOS_FILE = DATA_DIR + "/usuarios.txt";
    private static final String LOG_FILE = DATA_DIR + "/log_autenticacao.txt";

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
            System.out.println("│ 4. Menu Banco de Dados                       │");
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
                    case 4:
                        menuBancoDeDados();
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

        try {
            if (autenticadorDAO.autenticar(email, senha)) {
                usuarioAtual = mapaUsuarios.get(email);
                logAutenticacao.add("Login: " + email + " - " + LocalDateTime.now());
                System.out.println("\n✅ Login realizado com sucesso!");
                menuPrincipal();
            } else {
                System.out.println("\n⚠️ Email ou senha incorretos!");
            }
        } catch (Exception e) {
            System.out.println("\n⚠️ Erro ao realizar login: " + e.getMessage());
        }
    }

    private static void cadastrarNovoUsuario() {
        System.out.println("\n═══ NOVO CADASTRO ═══");

        System.out.print("Nome completo: ");
        String nome = scanner.nextLine().trim();

        System.out.print("CPF: ");
        String cpf = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        if (nome.isEmpty() || cpf.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            System.out.println("\n⚠️ Todos os campos são obrigatórios!");
            return;
        }

        if (mapaUsuarios.containsKey(email)) {
            System.out.println("\n⚠️ Este email já está cadastrado!");
            return;
        }

        try {
            // Cria o novo usuário
            Usuario novoUsuario = new Usuario(nome, cpf, email);

            // Adiciona o usuário às listas e mapas
            listaUsuarios.add(novoUsuario);
            mapaUsuarios.put(email, novoUsuario);
            credenciais.put(email, senha);
            logAutenticacao.add("Cadastro: " + email + " - " + LocalDateTime.now());

            // Salva o usuário no banco de dados
            novoUsuario.salvarNoBanco(senha);

            System.out.println("\n✅ Usuário cadastrado com sucesso!");
            System.out.println("Deseja fazer login agora? (S/N)");

            if (scanner.nextLine().trim().equalsIgnoreCase("S")) {
                usuarioAtual = novoUsuario;
                menuPrincipal();
            }
        } catch (Exception e) {
            System.out.println("\n⚠️ Erro ao cadastrar usuário: " + e.getMessage());
        }
    }

    private static void listarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioDAO.listarTodos();
            if (usuarios.isEmpty()) {
                System.out.println("\n⚠️ Nenhum usuário cadastrado!");
                return;
            }

            System.out.println("\n═══ USUÁRIOS CADASTRADOS ═══");
            for (Usuario usuario : usuarios) {
                System.out.println("Nome: " + usuario.getNome());
                System.out.println("Email: " + usuario.getEmail());
                System.out.println("─────────────────────────");
            }
        } catch (Exception e) {
            System.out.println("\n⚠️ Erro ao listar usuários: " + e.getMessage());
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

    private static void menuBancoDeDados() {
        while (true) {
            System.out.println("\n┌─────────── MENU BANCO DE DADOS ───────────────┐");
            System.out.println("│ 1. Inserir Usuário                            │");
            System.out.println("│ 2. Buscar Usuário por CPF                     │");
            System.out.println("│ 3. Listar Todos os Usuários                   │");
            System.out.println("│ 4. Atualizar Usuário                          │");
            System.out.println("│ 5. Excluir Usuário                            │");
            System.out.println("│ 0. Voltar                                     │");
            System.out.println("└───────────────────────────────────────────────┘");
            System.out.print("Escolha uma opção: ");

            try {
                int opcao = scanner.nextInt();
                scanner.nextLine(); // Limpar buffer

                switch (opcao) {
                    case 1:
                        inserirUsuarioBD();
                        break;
                    case 2:
                        buscarUsuarioPorCpfBD();
                        break;
                    case 3:
                        listarTodosUsuariosBD();
                        break;
                    case 4:
                        atualizarUsuarioBD();
                        break;
                    case 5:
                        excluirUsuarioBD();
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

    private static void inserirUsuarioBD() {
        System.out.println("\n═══ INSERIR USUÁRIO ═══");
        System.out.print("Nome: ");
        String nome = scanner.nextLine().trim();
        System.out.print("CPF: ");
        String cpf = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();

        try {
            Usuario novoUsuario = new Usuario(nome, cpf, email);
            novoUsuario.salvarNoBanco(senha);
            System.out.println("\n✅ Usuário inserido com sucesso!");
        } catch (Exception e) {
            System.out.println("\n⚠️ Erro ao inserir usuário: " + e.getMessage());
        }
    }

    private static void buscarUsuarioPorCpfBD() {
        System.out.println("\n═══ BUSCAR USUÁRIO ═══");
        System.out.print("CPF: ");
        String cpf = scanner.nextLine().trim();

        try {
            Usuario usuario = usuarioDAO.buscarPorCpf(cpf);
            if (usuario != null) {
                System.out.println("\nUsuário encontrado:");
                System.out.println("Nome: " + usuario.getNome());
                System.out.println("Email: " + usuario.getEmail());
            } else {
                System.out.println("\n⚠️ Usuário não encontrado!");
            }
        } catch (Exception e) {
            System.out.println("\n⚠️ Erro ao buscar usuário: " + e.getMessage());
        }
    }

    private static void listarTodosUsuariosBD() {
        try {
            List<Usuario> usuarios = usuarioDAO.listarTodos();
            if (usuarios.isEmpty()) {
                System.out.println("\n⚠️ Nenhum usuário cadastrado!");
                return;
            }

            System.out.println("\n═══ USUÁRIOS CADASTRADOS ═══");
            for (Usuario usuario : usuarios) {
                System.out.println("Nome: " + usuario.getNome());
                System.out.println("CPF: " + usuario.getCpf());
                System.out.println("Email: " + usuario.getEmail());
                System.out.println("─────────────────────────");
            }
        } catch (Exception e) {
            System.out.println("\n⚠️ Erro ao listar usuários: " + e.getMessage());
        }
    }

    private static void atualizarUsuarioBD() {
        System.out.println("\n═══ ATUALIZAR USUÁRIO ═══");
        System.out.print("CPF do usuário a ser atualizado: ");
        String cpf = scanner.nextLine().trim();

        try {
            Usuario usuario = usuarioDAO.buscarPorCpf(cpf);
            if (usuario == null) {
                System.out.println("\n⚠️ Usuário não encontrado!");
                return;
            }

            System.out.print("Novo nome (ou Enter para manter o atual): ");
            String novoNome = scanner.nextLine().trim();
            if (!novoNome.isEmpty()) {
                usuario.setNome(novoNome);
            }

            System.out.print("Novo email (ou Enter para manter o atual): ");
            String novoEmail = scanner.nextLine().trim();
            if (!novoEmail.isEmpty()) {
                usuario.setEmail(novoEmail);
            }

            if (usuario.atualizar()) {
                System.out.println("\n✅ Usuário atualizado com sucesso!");
            } else {
                System.out.println("\n⚠️ Erro ao atualizar usuário!");
            }
        } catch (Exception e) {
            System.out.println("\n⚠️ Erro ao atualizar usuário: " + e.getMessage());
        }
    }

    private static void excluirUsuarioBD() {
        System.out.println("\n═══ EXCLUIR USUÁRIO ═══");
        System.out.print("CPF do usuário a ser excluído: ");
        String cpf = scanner.nextLine().trim();

        try {
            Usuario usuario = usuarioDAO.buscarPorCpf(cpf);
            if (usuario == null) {
                System.out.println("\n⚠️ Usuário não encontrado!");
                return;
            }

            System.out.print("Tem certeza que deseja excluir este usuário? (S/N): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("S")) {
                if (usuario.excluir()) {
                    System.out.println("\n✅ Usuário excluído com sucesso!");
                } else {
                    System.out.println("\n⚠️ Erro ao excluir usuário!");
                }
            }
        } catch (Exception e) {
            System.out.println("\n⚠️ Erro ao excluir usuário: " + e.getMessage());
        }
    }

    private static void criarDiretorioDados() {
        File diretorio = new File(DATA_DIR);
        if (!diretorio.exists()) {
            diretorio.mkdirs();
        }
    }

    private static void salvarUsuariosEmArquivo() {
        criarDiretorioDados();
        try (FileWriter fw = new FileWriter(USUARIOS_FILE);
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
        criarDiretorioDados();
        File arquivo = new File(USUARIOS_FILE);
        if (!arquivo.exists()) {
            return;
        }

        try (FileReader fr = new FileReader(arquivo);
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
        criarDiretorioDados();
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw)) {

            for (String log : logAutenticacao) {
                bw.write(log + "\n");
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar log: " + e.getMessage());
        }
    }

    private static void carregarLogAutenticacao() {
        criarDiretorioDados();
        File arquivo = new File(LOG_FILE);
        if (!arquivo.exists()) {
            return;
        }

        try (FileReader fr = new FileReader(arquivo);
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

