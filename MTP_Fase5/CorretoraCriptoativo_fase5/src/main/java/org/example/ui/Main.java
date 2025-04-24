package org.example.ui;

import org.example.model.Carteira;
import org.example.model.Conta;
import org.example.model.Criptoativo;
import org.example.model.Transacao;
import org.example.model.Usuario;
import org.example.exception.CorretoraException;
import org.example.service.AuthService;
import org.example.service.CarteiraService;
import org.example.service.ContaService;
import org.example.service.CriptoativoService;
import org.example.service.TransacaoService;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;
import java.text.DecimalFormat;
import java.util.List;
import java.math.BigDecimal;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static DecimalFormat df = new DecimalFormat("#,##0.00");
    private static Usuario usuarioAtual = null;
    private static Conta contaAtual = null;
    private static AuthService authService;
    private static ContaService contaService;
    private static CriptoativoService criptoativoService;
    private static CarteiraService carteiraService;
    private static TransacaoService transacaoService;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║         CORRETORA DE CRIPTOATIVOS        ║");
        System.out.println("╚══════════════════════════════════════════╝");

        try {
            authService = new AuthService();
            contaService = new ContaService();
            criptoativoService = new CriptoativoService();
            carteiraService = new CarteiraService();
            transacaoService = new TransacaoService();
        } catch (RuntimeException e) {
            System.err.println("Erro fatal ao inicializar serviços: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        menuAutenticacao();
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
                scanner.nextLine();

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
                System.out.println("\n⚠️ Entrada inválida! Por favor, insira um número.");
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
            Optional<Usuario> usuarioOpt = authService.autenticarUsuario(email, senha);

            if (usuarioOpt.isPresent()) {
                usuarioAtual = usuarioOpt.get();
                System.out.println("\n✅ Login realizado com sucesso! Bem-vindo(a), " + usuarioAtual.getNome() + "!");
                contaAtual = null;
                menuPrincipal();
                usuarioAtual = null;
                contaAtual = null;
            } else {
                System.out.println("\n⚠️ Email ou senha incorretos!");
            }
        } catch (SQLException e) {
            System.out.println("\n❌ Erro no banco de dados durante o login: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n❌ Erro inesperado durante o login: " + e.getMessage());
        }
    }

    private static void cadastrarNovoUsuario() {
        System.out.println("\n═══ NOVO CADASTRO ═══");

        System.out.print("Nome completo: ");
        String nome = scanner.nextLine().trim();

        System.out.print("CPF (apenas números): ");
        String cpf = scanner.nextLine().trim().replaceAll("[^0-9]", "");

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Senha (máx 10 caracteres): ");
        String senha = scanner.nextLine().trim();

        if (nome.isEmpty() || cpf.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            System.out.println("\n⚠️ Todos os campos são obrigatórios!");
            return;
        }

        try {
            Usuario novoUsuario = authService.cadastrarUsuario(nome, cpf, email, senha);
            System.out.println("\n✅ Usuário cadastrado com sucesso! ID: " + novoUsuario.getId());

            System.out.println("Deseja fazer login agora? (S/N)");
            if (scanner.nextLine().trim().equalsIgnoreCase("S")) {
                Optional<Usuario> usuarioOpt = authService.autenticarUsuario(email, senha);
                if (usuarioOpt.isPresent()) {
                    usuarioAtual = usuarioOpt.get();
                    System.out.println("\n✅ Login realizado com sucesso! Bem-vindo(a), " + usuarioAtual.getNome() + "!");
                    contaAtual = null;
                    menuPrincipal();
                    usuarioAtual = null;
                    contaAtual = null;
                } else {
                    System.out.println("\n⚠️ Falha ao fazer login automático. Por favor, tente manualmente.");
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("\n⚠️ Erro ao cadastrar: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("\n❌ Erro no banco de dados durante o cadastro: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n❌ Erro inesperado durante o cadastro: " + e.getMessage());
        }
    }

    private static void listarUsuarios() {
        System.out.println("\n═══ USUÁRIOS CADASTRADOS ═══");
        try {
            List<Usuario> usuarios = authService.listarTodosUsuarios();

            if (usuarios.isEmpty()) {
                System.out.println("\nℹ️ Nenhum usuário cadastrado no momento.");
                return;
            }

            for (Usuario usuario : usuarios) {
                System.out.println("ID: " + usuario.getId());
                System.out.println("Nome: " + usuario.getNome());
                System.out.println("Email: " + usuario.getEmail());
                System.out.println("CPF: " + usuario.getCpf());
                System.out.println("─────────────────────────");
            }
        } catch (SQLException e) {
            System.out.println("\n❌ Erro no banco de dados ao listar usuários: " + e.getMessage());
        } catch (UnsupportedOperationException e) {
             System.out.println("\n⚠️ Funcionalidade 'listarTodosUsuarios' ainda não implementada no AuthService.");
        } catch (Exception e) {
            System.out.println("\n❌ Erro inesperado ao listar usuários: " + e.getMessage());
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
                scanner.nextLine();

                switch (opcao) {
                    case 1:
                        criarConta();
                        break;
                    case 2:
                        acessarConta();
                        break;
                    case 3:
                        try {
                            List<Criptoativo> criptoativos = criptoativoService.listarTodosCriptoativos();
                            if (criptoativos.isEmpty()) {
                                System.out.println("\nℹ️ Nenhum criptoativo disponível no momento.");
                            } else {
                                System.out.println("\n═══ CRIPTOATIVOS DISPONÍVEIS ═══");
                                for (Criptoativo cripto : criptoativos) {
                                    System.out.println("ID: " + cripto.id());
                                    System.out.println("Nome: " + cripto.nomeCriptoativo());
                                    System.out.println("Sigla: " + cripto.sigla());
                                    System.out.println("─────────────────────────");
                                }
                            }
                        } catch (SQLException e) {
                            System.out.println("\n❌ Erro ao listar criptoativos: " + e.getMessage());
                        }
                        break;
                    case 4:
                        if (usuarioAtual != null) {
                            listarContas();
                        } else {
                            System.out.println("\n⚠️ Você precisa criar uma conta primeiro!");
                        }
                        break;
                    case 5:
                        System.out.println("\nℹ️ Funcionalidade de suporte temporariamente indisponível.");
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
            scanner.nextLine();

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
                    exibirHistoricoTransacoes();
                    break;
                case 6:
                    System.out.println("\nℹ️ Funcionalidade de painel de análise temporariamente indisponível.");
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
        System.out.println("\n═══ CRIAR NOVA CONTA ═══");
        
        if (usuarioAtual == null) {
            System.out.println("\n⚠️ Usuário não está logado. Faça login primeiro.");
            return;
        }
        
        try {
            Conta novaConta = contaService.criarConta(usuarioAtual);
            System.out.println("\n✅ Conta criada com sucesso!");
            System.out.println("Número da conta: " + novaConta.getNumeroConta());
        } catch (SQLException e) {
            System.out.println("\n❌ Erro no banco de dados: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("\n⚠️ " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n❌ Erro inesperado: " + e.getMessage());
        }
    }

    private static void acessarConta() {
        System.out.println("\n═══ ACESSAR CONTA ═══");
        
        if (usuarioAtual == null) {
            System.out.println("\n⚠️ Usuário não está logado. Faça login primeiro.");
            return;
        }
        
        try {
            List<Conta> contas = contaService.buscarContasPorUsuario(usuarioAtual);
            
            if (contas.isEmpty()) {
                System.out.println("\nℹ️ Você não possui nenhuma conta. Crie uma nova conta primeiro.");
                return;
            }
            
            System.out.println("\nSuas contas disponíveis:");
            for (int i = 0; i < contas.size(); i++) {
                System.out.println((i + 1) + ". " + contas.get(i).getNumeroConta());
            }
            
            System.out.print("\nSelecione o número da conta: ");
            int opcao = scanner.nextInt();
            scanner.nextLine();
            
            if (opcao < 1 || opcao > contas.size()) {
                System.out.println("\n⚠️ Opção inválida!");
                return;
            }
            
            contaAtual = contas.get(opcao - 1);
            System.out.println("\n✅ Conta " + contaAtual.getNumeroConta() + " selecionada com sucesso!");
            menuConta();
        } catch (SQLException e) {
            System.out.println("\n❌ Erro no banco de dados: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n❌ Erro inesperado: " + e.getMessage());
        }
    }
    
    private static void listarContas() {
        System.out.println("\n═══ MINHAS CONTAS ═══");
        
        try {
            List<Conta> contas = contaService.buscarContasPorUsuario(usuarioAtual);
            
            if (contas.isEmpty()) {
                System.out.println("\nℹ️ Você não possui nenhuma conta. Crie uma nova conta primeiro.");
                return;
            }
            
            for (Conta conta : contas) {
                System.out.println("Número da conta: " + conta.getNumeroConta());
                System.out.println("ID: " + conta.getId());
                System.out.println("─────────────────────────");
            }
        } catch (SQLException e) {
            System.out.println("\n❌ Erro no banco de dados: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n❌ Erro inesperado: " + e.getMessage());
        }
    }

    private static void realizarCompra() {
        System.out.println("\n═══ COMPRAR CRIPTOATIVO ═══");
        
        try {
            // 1. Listar criptoativos disponíveis
            List<Criptoativo> criptoativos = criptoativoService.listarTodosCriptoativos();
            
            if (criptoativos.isEmpty()) {
                System.out.println("\nℹ️ Não há criptoativos disponíveis para compra no momento.");
                return;
            }
            
            System.out.println("\nCriptoativos disponíveis para compra:");
            for (Criptoativo cripto : criptoativos) {
                System.out.println(cripto.id() + ". " + cripto.nomeCriptoativo() + " (" + cripto.sigla() + ")");
            }
            
            // 2. Solicitar o ID do criptoativo
            System.out.print("\nDigite o ID do criptoativo que deseja comprar: ");
            long idCriptoativo = scanner.nextLong();
            scanner.nextLine();
            
            Optional<Criptoativo> criptoOpt = criptoativoService.buscarCriptoativoPorId(idCriptoativo);
            if (criptoOpt.isEmpty()) {
                System.out.println("\n⚠️ Criptoativo não encontrado!");
                return;
            }
            
            Criptoativo criptoativo = criptoOpt.get();
            
            // 3. Solicitar a quantidade e o preço
            System.out.print("Quantidade a comprar: ");
            double quantidadeDouble = scanner.nextDouble();
            scanner.nextLine();
            
            System.out.print("Preço atual (por unidade): ");
            double precoAtualDouble = scanner.nextDouble();
            scanner.nextLine();
            
            BigDecimal quantidade = BigDecimal.valueOf(quantidadeDouble);
            BigDecimal precoAtual = BigDecimal.valueOf(precoAtualDouble);
            
            // 4. Calcular o valor total
            BigDecimal valorTotal = quantidade.multiply(precoAtual);
            
            // 5. Confirmar a compra
            System.out.println("\nResumo da compra:");
            System.out.println("Criptoativo: " + criptoativo.nomeCriptoativo() + " (" + criptoativo.sigla() + ")");
            System.out.println("Quantidade: " + quantidade);
            System.out.println("Preço unitário: R$ " + df.format(precoAtual));
            System.out.println("Valor total: R$ " + df.format(valorTotal));
            
            System.out.print("\nConfirmar compra? (S/N): ");
            String confirmacao = scanner.nextLine();
            
            if (!confirmacao.equalsIgnoreCase("S")) {
                System.out.println("\nⓘ Compra cancelada pelo usuário.");
                return;
            }
            
            // 6. Realizar a compra
            transacaoService.comprarCriptoativo(contaAtual, criptoativo, quantidade, precoAtual);
            
            System.out.println("\n✅ Compra realizada com sucesso!");
            
        } catch (SQLException e) {
            System.out.println("\n❌ Erro no banco de dados: " + e.getMessage());
        } catch (CorretoraException e) {
            System.out.println("\n⚠️ " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("\n⚠️ " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n❌ Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void realizarVenda() {
        System.out.println("\n═══ VENDER CRIPTOATIVO ═══");
        
        try {
            // 1. Mostrar carteiras do usuário (para ele saber o que pode vender)
            List<Carteira> carteiras = carteiraService.buscarCarteirasPorConta(contaAtual);
            
            if (carteiras.isEmpty()) {
                System.out.println("\nℹ️ Você não possui criptoativos para vender.");
                return;
            }
            
            System.out.println("\nSeus criptoativos disponíveis para venda:");
            for (int i = 0; i < carteiras.size(); i++) {
                Carteira carteira = carteiras.get(i);
                System.out.println((i + 1) + ". " + 
                                   carteira.getCriptoativo().nomeCriptoativo() + 
                                   " (" + carteira.getCriptoativo().sigla() + ")" +
                                   " - Saldo: " + carteira.getSaldo());
            }
            
            // 2. Solicitar qual criptoativo vender
            System.out.print("\nSelecione o número do criptoativo que deseja vender: ");
            int opcao = scanner.nextInt();
            scanner.nextLine();
            
            if (opcao < 1 || opcao > carteiras.size()) {
                System.out.println("\n⚠️ Opção inválida!");
                return;
            }
            
            Carteira carteiraSelecionada = carteiras.get(opcao - 1);
            Criptoativo criptoativo = carteiraSelecionada.getCriptoativo();
            
            // 3. Solicitar a quantidade e o preço
            System.out.println("Saldo disponível: " + carteiraSelecionada.getSaldo());
            System.out.print("Quantidade a vender: ");
            double quantidadeDouble = scanner.nextDouble();
            scanner.nextLine();
            
            BigDecimal quantidade = BigDecimal.valueOf(quantidadeDouble);
            
            // Verificar se tem saldo suficiente
            if (quantidade.compareTo(carteiraSelecionada.getSaldo()) > 0) {
                System.out.println("\n⚠️ Saldo insuficiente para venda!");
                return;
            }
            
            System.out.print("Preço atual (por unidade): ");
            double precoAtualDouble = scanner.nextDouble();
            scanner.nextLine();
            
            BigDecimal precoAtual = BigDecimal.valueOf(precoAtualDouble);
            
            // 4. Calcular o valor total
            BigDecimal valorTotal = quantidade.multiply(precoAtual);
            
            // 5. Confirmar a venda
            System.out.println("\nResumo da venda:");
            System.out.println("Criptoativo: " + criptoativo.nomeCriptoativo() + " (" + criptoativo.sigla() + ")");
            System.out.println("Quantidade: " + quantidade);
            System.out.println("Preço unitário: R$ " + df.format(precoAtual));
            System.out.println("Valor total a receber: R$ " + df.format(valorTotal));
            
            System.out.print("\nConfirmar venda? (S/N): ");
            String confirmacao = scanner.nextLine();
            
            if (!confirmacao.equalsIgnoreCase("S")) {
                System.out.println("\nⓘ Venda cancelada pelo usuário.");
                return;
            }
            
            // 6. Realizar a venda
            transacaoService.venderCriptoativo(contaAtual, criptoativo, quantidade, precoAtual);
            
            System.out.println("\n✅ Venda realizada com sucesso!");
            
        } catch (SQLException e) {
            System.out.println("\n❌ Erro no banco de dados: " + e.getMessage());
        } catch (CorretoraException e) {
            System.out.println("\n⚠️ " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("\n⚠️ " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n❌ Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void realizarTransferencia() {
        System.out.println("\n═══ TRANSFERIR CRIPTOATIVO ═══");
        
        try {
            // 1. Mostrar carteiras do usuário (para ele saber o que pode transferir)
            List<Carteira> carteiras = carteiraService.buscarCarteirasPorConta(contaAtual);
            
            if (carteiras.isEmpty()) {
                System.out.println("\nℹ️ Você não possui criptoativos para transferir.");
                return;
            }
            
            System.out.println("\nSeus criptoativos disponíveis para transferência:");
            for (int i = 0; i < carteiras.size(); i++) {
                Carteira carteira = carteiras.get(i);
                System.out.println((i + 1) + ". " + 
                                   carteira.getCriptoativo().nomeCriptoativo() + 
                                   " (" + carteira.getCriptoativo().sigla() + ")" +
                                   " - Saldo: " + carteira.getSaldo());
            }
            
            // 2. Solicitar qual criptoativo transferir
            System.out.print("\nSelecione o número do criptoativo que deseja transferir: ");
            int opcao = scanner.nextInt();
            scanner.nextLine();
            
            if (opcao < 1 || opcao > carteiras.size()) {
                System.out.println("\n⚠️ Opção inválida!");
                return;
            }
            
            Carteira carteiraSelecionada = carteiras.get(opcao - 1);
            Criptoativo criptoativo = carteiraSelecionada.getCriptoativo();
            
            // 3. Solicitar a quantidade
            System.out.println("Saldo disponível: " + carteiraSelecionada.getSaldo());
            System.out.print("Quantidade a transferir: ");
            double quantidadeDouble = scanner.nextDouble();
            scanner.nextLine();
            
            BigDecimal quantidade = BigDecimal.valueOf(quantidadeDouble);
            
            // Verificar se tem saldo suficiente
            if (quantidade.compareTo(carteiraSelecionada.getSaldo()) > 0) {
                System.out.println("\n⚠️ Saldo insuficiente para transferência!");
                return;
            }
            
            // 4. Solicitar a conta de destino
            System.out.print("Número da conta de destino: ");
            String numeroContaDestino = scanner.nextLine();
            
            Optional<Conta> contaDestinoOpt = contaService.buscarContaPorNumero(numeroContaDestino);
            
            if (contaDestinoOpt.isEmpty()) {
                System.out.println("\n⚠️ Conta de destino não encontrada!");
                return;
            }
            
            Conta contaDestino = contaDestinoOpt.get();
            
            // Verificar se não é a mesma conta
            if (contaAtual.getId().equals(contaDestino.getId())) {
                System.out.println("\n⚠️ Não é possível transferir para a mesma conta!");
                return;
            }
            
            // 5. Confirmar a transferência
            System.out.println("\nResumo da transferência:");
            System.out.println("Criptoativo: " + criptoativo.nomeCriptoativo() + " (" + criptoativo.sigla() + ")");
            System.out.println("Quantidade: " + quantidade);
            System.out.println("Conta de destino: " + contaDestino.getNumeroConta());
            
            System.out.print("\nConfirmar transferência? (S/N): ");
            String confirmacao = scanner.nextLine();
            
            if (!confirmacao.equalsIgnoreCase("S")) {
                System.out.println("\nⓘ Transferência cancelada pelo usuário.");
                return;
            }
            
            // 6. Realizar a transferência
            transacaoService.transferirCriptoativo(contaAtual, contaDestino, criptoativo, quantidade);
            
            System.out.println("\n✅ Transferência realizada com sucesso!");
            
        } catch (SQLException e) {
            System.out.println("\n❌ Erro no banco de dados: " + e.getMessage());
        } catch (CorretoraException e) {
            System.out.println("\n⚠️ " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("\n⚠️ " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n❌ Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void exibirCarteiras() {
        System.out.println("\n═══ MINHAS CARTEIRAS ═══");
        
        try {
            List<Carteira> carteiras = carteiraService.buscarCarteirasPorConta(contaAtual);
            
            if (carteiras.isEmpty()) {
                System.out.println("\nℹ️ Você não possui criptoativos em sua carteira.");
                return;
            }
            
            for (Carteira carteira : carteiras) {
                System.out.println("Criptoativo: " + carteira.getCriptoativo().nomeCriptoativo() + 
                                  " (" + carteira.getCriptoativo().sigla() + ")");
                System.out.println("Saldo: " + carteira.getSaldo());
                System.out.println("─────────────────────────");
            }
            
        } catch (SQLException e) {
            System.out.println("\n❌ Erro no banco de dados: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n❌ Erro inesperado: " + e.getMessage());
        }
    }
    
    private static void exibirHistoricoTransacoes() {
        System.out.println("\n═══ HISTÓRICO DE TRANSAÇÕES ═══");
        
        try {
            List<Transacao> transacoes = transacaoService.listarTransacoesPorConta(contaAtual);
            
            if (transacoes.isEmpty()) {
                System.out.println("\nℹ️ Não há transações registradas para esta conta.");
                return;
            }
            
            for (Transacao transacao : transacoes) {
                System.out.println("ID: " + transacao.getIdTransacao());
                System.out.println("Tipo: " + transacao.getClass().getSimpleName());
                System.out.println("Criptoativo: " + transacao.getCriptoativo().nomeCriptoativo() + 
                                  " (" + transacao.getCriptoativo().sigla() + ")");
                System.out.println("Quantidade: " + transacao.getQuantidade());
                System.out.println("Preço no momento: " + df.format(transacao.getPrecoNoMomento()));
                System.out.println("Data/Hora: " + transacao.getDataHora());
                System.out.println("─────────────────────────");
            }
            
        } catch (SQLException e) {
            System.out.println("\n❌ Erro no banco de dados: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n❌ Erro inesperado: " + e.getMessage());
        }
    }
}

