package org.example.dao;

import org.example.factory.ConnectionFactory;
import org.example.model.Carteira;
import org.example.model.Conta;
import org.example.model.Criptoativo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe para acesso a dados da entidade Carteira.
 * Implementa AutoCloseable para permitir uso em try-with-resources.
 */
public class CarteiraDAO implements AutoCloseable {
    private Connection connection;
    private ContaDAO contaDAO; // Para mapear conta
    private CriptoativoDAO criptoativoDAO; // Para mapear criptoativo
    
    /**
     * Construtor que inicializa a conexão com o banco de dados.
     * @throws SQLException Se houver erro ao obter conexão.
     */
    public CarteiraDAO() throws SQLException {
        this.connection = ConnectionFactory.getConnection();
        // Instanciar DAOs dependentes
        this.contaDAO = new ContaDAO();
        this.criptoativoDAO = new CriptoativoDAO();
    }
    
    /**
     * Salva uma nova carteira no banco de dados.
     * @param carteira O objeto Carteira a ser salvo (sem ID definido).
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void salvar(Carteira carteira) throws SQLException {
        if (carteira.getConta() == null || carteira.getConta().getId() == null) {
            throw new SQLException("Conta da carteira não pode ser nula ou não ter ID definido.");
        }
        if (carteira.getCriptoativo() == null || carteira.getCriptoativo().id() == null) {
            throw new SQLException("Criptoativo da carteira não pode ser nulo ou não ter ID definido.");
        }
        
        String sql = "INSERT INTO t_mtp_carteira (idt_conta, idt_criptoativo, val_saldo) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"idt_carteira"})) {
            stmt.setLong(1, carteira.getConta().getId());
            stmt.setLong(2, carteira.getCriptoativo().id());
            stmt.setBigDecimal(3, carteira.getSaldo());
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    carteira.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Falha ao obter ID da carteira, nenhum ID retornado.");
                }
            }
        }
    }
    
    /**
     * Busca uma carteira pelo seu ID.
     * @param id O ID da carteira.
     * @return Um Optional contendo a carteira se encontrada, ou vazio caso contrário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Optional<Carteira> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT crt.idt_carteira, crt.idt_conta, crt.idt_criptoativo, crt.val_saldo " +
                     // "cnt.des_numero_conta, " +
                     // "crp.nom_criptoativo, crp.des_sigla " +
                     "FROM t_mtp_carteira crt " +
                     // "JOIN t_mtp_conta cnt ON crt.idt_conta = cnt.idt_conta " +
                     // "JOIN t_mtp_criptoativo crp ON crt.idt_criptoativo = crp.idt_criptoativo " +
                     "WHERE crt.idt_carteira = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Carteira carteira = mapearCarteira(rs);
                    return Optional.of(carteira);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Busca todas as carteiras associadas a uma conta.
     * @param contaId O ID da conta.
     * @return Uma lista contendo as carteiras da conta.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public List<Carteira> buscarPorContaId(Long contaId) throws SQLException {
        List<Carteira> carteiras = new ArrayList<>();
        
        String sql = "SELECT crt.idt_carteira, crt.idt_conta, crt.idt_criptoativo, crt.val_saldo " +
                     "FROM t_mtp_carteira crt WHERE crt.idt_conta = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, contaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Carteira carteira = mapearCarteira(rs);
                    carteiras.add(carteira);
                }
            }
        }
        
        return carteiras;
    }
    
    /**
     * Busca uma carteira específica pela combinação de conta e criptoativo.
     * @param contaId O ID da conta.
     * @param criptoativoId O ID do criptoativo.
     * @return Um Optional contendo a carteira se encontrada, ou vazio caso contrário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Optional<Carteira> buscarPorContaECriptoativo(Long contaId, Long criptoativoId) throws SQLException {
        String sql = "SELECT crt.idt_carteira, crt.idt_conta, crt.idt_criptoativo, crt.val_saldo " +
                     "FROM t_mtp_carteira crt WHERE crt.idt_conta = ? AND crt.idt_criptoativo = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, contaId);
            stmt.setLong(2, criptoativoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Carteira carteira = mapearCarteira(rs);
                    return Optional.of(carteira);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Atualiza apenas o saldo de uma carteira existente.
     * Útil para operações como compra, venda e transferência.
     * @param carteira O objeto Carteira com o novo saldo (deve conter o ID).
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void atualizarSaldo(Carteira carteira) throws SQLException {
        if (carteira.getId() == null) {
            throw new SQLException("ID da carteira não pode ser nulo para atualização de saldo.");
        }
        
        String sql = "UPDATE t_mtp_carteira SET val_saldo = ? WHERE idt_carteira = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, carteira.getSaldo());
            stmt.setLong(2, carteira.getId());
            
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar saldo da carteira, nenhum registro afetado.");
            }
        }
    }
    
    /**
     * Atualiza todos os dados de uma carteira.
     * @param carteira O objeto Carteira com os dados atualizados (deve conter o ID).
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void atualizar(Carteira carteira) throws SQLException {
        // Este método pode não ser necessário se apenas o saldo for atualizável
        // Se precisar atualizar conta ou criptoativo, a lógica seria mais complexa
        atualizarSaldo(carteira); // Por ora, apenas atualiza o saldo
    }
    
    /**
     * Exclui uma carteira do banco de dados pelo seu ID.
     * @param id O ID da carteira a ser excluída.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void excluir(Long id) throws SQLException {
        String sql = "DELETE FROM t_mtp_carteira WHERE idt_carteira = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                // throw new SQLException("Falha ao excluir carteira, nenhum registro afetado.");
                 System.out.println("Nenhuma carteira encontrada com o ID " + id + " para exclusão.");
            }
        }
    }
    
    /**
     * Utilitário para mapear ResultSet para objeto Carteira.
     * Busca a Conta e Criptoativo completos usando seus respectivos DAOs.
     * @param rs O ResultSet posicionado no registro a ser mapeado.
     * @return O objeto Carteira com dados do ResultSet.
     * @throws SQLException Se ocorrer erro ao acessar os dados do ResultSet ou buscar dependências.
     */
    private Carteira mapearCarteira(ResultSet rs) throws SQLException {
        Long idCarteira = rs.getLong("idt_carteira");
        Long idConta = rs.getLong("idt_conta");
        Long idCriptoativo = rs.getLong("idt_criptoativo");
        BigDecimal saldo = rs.getBigDecimal("val_saldo");
        
        // Buscar Conta completa
        Conta conta = contaDAO.buscarPorId(idConta)
            .orElseThrow(() -> new SQLException("Conta da carteira não encontrada: ID " + idConta));
        
        // Buscar Criptoativo completo
        Criptoativo criptoativo = criptoativoDAO.buscarPorId(idCriptoativo)
            .orElseThrow(() -> new SQLException("Criptoativo da carteira não encontrado: ID " + idCriptoativo));
        
        // Montar a carteira
        return new Carteira(idCarteira, conta, criptoativo, saldo);
    }
    
    /**
     * Fecha a conexão com o banco de dados.
     * @throws SQLException Se ocorrer erro ao fechar a conexão.
     */
    @Override
    public void close() throws SQLException {
        if (contaDAO != null) contaDAO.close(); // Fechar DAOs dependentes
        if (criptoativoDAO != null) criptoativoDAO.close();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
} 