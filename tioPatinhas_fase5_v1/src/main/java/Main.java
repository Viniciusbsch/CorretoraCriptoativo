import java.sql.*;
import java.util.Scanner;

public class Main {

    // fazendo conexao com o oracle, sujeito a mudancao para factory

    private static final String URL = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl";
    private static final String USER = "rm554226";
    private static final String PASSWORD = "310704";

    private static Scanner sc = new Scanner(System.in);

    private static int usuarioLogadoId = -1; // importante, explicacao mais a frente

    public static void main(String[] args) {
        int op;
        do {
            System.out.println("┌─────────────────────────────────────┐");
            System.out.println("│ 1. Cadastrar Usuário                │");
            System.out.println("│ 2. Exibir Usuários                  │");
            System.out.println("│ 3. Autenticar Email e Senha         │");
            System.out.println("│ 0. Sair                             │");
            System.out.println("└─────────────────────────────────────┘");
            System.out.print("Opção: ");
            op = sc.nextInt();
            sc.nextLine();

            // pega a escolha do usuario e executa um metodo baseado nisso.
            switch (op) {
                case 0:
                    System.out.println("Saindo do Sistema...");
                    break;
                case 1:
                    cadastrarUsuario();
                    break;
                case 2:
                    exibirUsuarios();
                    break;
                case 3:
                    autenticarUsuario();
                    break;
                default:
                    System.out.println("⚠️ Opção Inválida!");
            }
        } while (op != 0);

        sc.close();
    }
    // CASO 1
    private static void cadastrarUsuario() {
        System.out.print("Digite o nome completo: ");
        String nome = sc.nextLine();
        System.out.print("Digite o email: ");
        String email = sc.nextLine();
        System.out.print("Digite a senha: ");
        String senha = sc.nextLine();

        // insere nas tabelas do banco de dados, na tabela usuarios e carteira.

        String sqlUsuario = "INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?)";
        String sqlCarteira = "INSERT INTO carteiras (id_usuario) VALUES ((SELECT id_usuario FROM usuarios WHERE email = ?))";

        // conexao com o banco
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false); // Garantir que ambas operações ocorram juntas

            try (PreparedStatement stmtUsuario = conn.prepareStatement(sqlUsuario);
                 PreparedStatement stmtCarteira = conn.prepareStatement(sqlCarteira)) {

                stmtUsuario.setString(1, nome);
                stmtUsuario.setString(2, email);
                stmtUsuario.setString(3, senha);
                stmtUsuario.executeUpdate();

                stmtCarteira.setString(1, email);
                stmtCarteira.executeUpdate();

                conn.commit();
                System.out.println("✅ Usuário cadastrado e carteira criada com sucesso!");

            } catch (SQLException e) {
                conn.rollback(); // Reverte a transação se houver erro
                System.out.println("❌ Erro ao cadastrar usuário: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao conectar ao banco de dados: " + e.getMessage());
        }
    }

    private static void exibirUsuarios() {
        String sql = "SELECT id_usuario, nome, email FROM usuarios";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n📋 Usuários cadastrados:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id_usuario") +
                        " | Nome: " + rs.getString("nome") +
                        " | Email: " + rs.getString("email"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Erro ao buscar usuários: " + e.getMessage());
        }
    }
    // parte importante, quando o usuario faz login.

    private static void autenticarUsuario() {
        System.out.print("Digite seu email: ");
        String email = sc.nextLine();
        System.out.print("Digite sua senha: ");
        String senha = sc.nextLine();


        String sql = "SELECT id_usuario FROM usuarios WHERE email = ? AND senha = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
        // substitui os ? do string sql pelos parametros
            stmt.setString(1, email);
            stmt.setString(2, senha);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // aqui ele altera o usuarioLogadoId para o ID do usuario. Fiz isso de modo que o menu da conta so abra se o usuario estiver logado.
                usuarioLogadoId = rs.getInt("id_usuario");
                System.out.println("✅ Autenticação bem-sucedida! Bem-vindo!");
                menuConta();
            } else {
                System.out.println("❌ Email ou senha incorretos!");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erro na autenticação: " + e.getMessage());
        }
    }

    private static void menuConta() {

        // primeira que o usuario vera ao logar na conta.

        while (true) {
            System.out.println("\n┌────────── MENU DA CONTA ──────────┐");
            System.out.println("│ 1. Comprar criptoativo              │");
            System.out.println("│ 2. Ver minha carteira               │");
            System.out.println("│ 3. Depositar R$                     │");
            System.out.println("│ 4. Saldo R$                         │");
            System.out.println("│ 0. Sair                             │");
            System.out.println("└─────────────────────────────────────┘");
            System.out.print("Escolha uma opção: ");

            int opcao = sc.nextInt();
            sc.nextLine(); // Limpar buffer

            switch (opcao) {
                case 1:
                    realizarCompra();
                    break;
                case 2:
                    exibirCarteira();
                    break;
                case 3:
                    adicionarSaldo();
                    break;
                case 4:
                    exibirSaldo();
                    break;
                case 0:
                    System.out.println("Saindo do menu da conta...");
                    return;
                default:
                    System.out.println("⚠️ Opção inválida!");
            }
        }
    }

    private static void realizarCompra() {
        System.out.println("\n═══ COMPRA DE CRIPTOATIVO ═══");

        String sqlCriptoativos = "SELECT id_criptoativo, nome, sigla, preco_atual FROM criptoativos";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlCriptoativos)) {

            System.out.println("\n📋 Criptoativos disponíveis:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id_criptoativo") +
                        " | Nome: " + rs.getString("nome") +
                        " | Sigla: " + rs.getString("sigla") +
                        " | Preço Atual: $" + rs.getDouble("preco_atual"));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao buscar criptoativos: " + e.getMessage());
            return;
        }

        System.out.print("Digite o ID do criptoativo desejado: ");
        int idCriptoativo = sc.nextInt();
        System.out.print("Digite a quantidade desejada: ");
        double quantidade = sc.nextDouble();
        sc.nextLine();

        String sqlPreco = "SELECT preco_atual FROM criptoativos WHERE id_criptoativo = ?";
        double precoAtual;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmtPreco = conn.prepareStatement(sqlPreco)) {

            stmtPreco.setInt(1, idCriptoativo);
            ResultSet rs = stmtPreco.executeQuery();
            if (rs.next()) {
                precoAtual = rs.getDouble("preco_atual");
            } else {
                System.out.println("❌ ID de criptoativo inválido!");
                return;
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao buscar preço do criptoativo: " + e.getMessage());
            return;
        }

        double totalCompra = precoAtual * quantidade;
        String sqlSaldo = "SELECT saldo FROM usuarios WHERE id_usuario = ?";
        double saldoAtual;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmtSaldo = conn.prepareStatement(sqlSaldo)) {

            stmtSaldo.setInt(1, usuarioLogadoId);
            ResultSet rs = stmtSaldo.executeQuery();
            if (rs.next()) {
                saldoAtual = rs.getDouble("saldo");
                if (saldoAtual < totalCompra) {
                    System.out.println("❌ Saldo insuficiente!");
                    return;
                }
            } else {
                System.out.println("❌ Usuário não encontrado!");
                return;
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao verificar saldo: " + e.getMessage());
            return;
        }

        String sqlCompra = "INSERT INTO carteiras (id_usuario, id_criptoativo, quantidade) VALUES (?, ?, ?)";
        String sqlDebitarSaldo = "UPDATE usuarios SET saldo = saldo - ? WHERE id_usuario = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmtCompra = conn.prepareStatement(sqlCompra);
             PreparedStatement stmtDebitarSaldo = conn.prepareStatement(sqlDebitarSaldo)) {

            stmtCompra.setInt(1, usuarioLogadoId);
            stmtCompra.setInt(2, idCriptoativo);
            stmtCompra.setDouble(3, quantidade);
            stmtCompra.executeUpdate();

            stmtDebitarSaldo.setDouble(1, totalCompra);
            stmtDebitarSaldo.setInt(2, usuarioLogadoId);
            stmtDebitarSaldo.executeUpdate();

            System.out.println("✅ Compra realizada com sucesso!");
            System.out.println("Total gasto: $" + totalCompra);

        } catch (SQLException e) {
            System.out.println("❌ Erro ao comprar criptoativo: " + e.getMessage());
        }
    }

    private static void exibirCarteira() {
        String sql = "SELECT * FROM carteiras WHERE id_usuario = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioLogadoId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n📋 Sua carteira:");
            while (rs.next()) {
                System.out.println("Criptoativo ID: " + rs.getInt("id_criptoativo") +
                        " | Quantidade: " + rs.getDouble("quantidade"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Erro ao exibir carteira: " + e.getMessage());
        }
    }

    private static void adicionarSaldo() {
        System.out.println("\n═══ ADICIONAR SALDO ═══");
        System.out.print("Digite o valor a ser adicionado: ");
        double valor = sc.nextDouble();
        sc.nextLine();

        String sqlAtualizarSaldo = "UPDATE usuarios SET saldo = saldo + ? WHERE id_usuario = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sqlAtualizarSaldo)) {

            stmt.setDouble(1, valor);
            stmt.setInt(2, usuarioLogadoId);
            stmt.executeUpdate();

            System.out.println("✅ Saldo adicionado com sucesso!");

        } catch (SQLException e) {
            System.out.println("❌ Erro ao adicionar saldo: " + e.getMessage());
        }
    }

    private static void exibirSaldo() {
        System.out.println("\n═══ EXIBIR SALDO ═══");

        String sqlConsultarSaldo = "SELECT saldo FROM usuarios WHERE id_usuario = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sqlConsultarSaldo)) {

            stmt.setInt(1, usuarioLogadoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double saldo = rs.getDouble("saldo");
                System.out.println("💰 Saldo disponível: $" + saldo);
            } else {
                System.out.println("❌ Usuário não encontrado.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erro ao exibir saldo: " + e.getMessage());
        }
    }

}
