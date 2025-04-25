package org.example.dao;

import org.example.factory.ConnectionFactory;
import org.example.model.Criptoativo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe para acesso a dados da entidade Criptoativo.
 * Implementa AutoCloseable para permitir uso em try-with-resources.
 */
public class CriptoativoDAO implements AutoCloseable {
    private Connection connection;
    
    /**
     * Construtor que inicializa a conexão com o banco de dados.
     * @throws SQLException Se houver erro ao obter conexão.
     */
    public CriptoativoDAO() throws SQLException {
        this.connection = ConnectionFactory.getConnection();
    }
    
    /**
     * Salva um novo criptoativo no banco de dados.
     * @param criptoativo O objeto Criptoativo a ser salvo (sem ID definido).
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Criptoativo salvar(Criptoativo criptoativo) throws SQLException {
        String sql = "INSERT INTO t_mtp_criptoativo (nom_criptoativo, sig_criptoativo) VALUES (?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"idt_criptoativo"})) {
            stmt.setString(1, criptoativo.nomeCriptoativo());
            stmt.setString(2, criptoativo.sigla());
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long idGerado = generatedKeys.getLong(1);
                    // Retornar um novo record com o ID, pois Criptoativo é imutável
                    return new Criptoativo(idGerado, criptoativo.nomeCriptoativo(), criptoativo.sigla());
                } else {
                    throw new SQLException("Falha ao obter ID do criptoativo, nenhum ID retornado.");
                }
            }
        }
    }
    
    /**
     * Busca um criptoativo pelo seu ID.
     * @param id O ID do criptoativo.
     * @return Um Optional contendo o criptoativo se encontrado, ou vazio caso contrário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Optional<Criptoativo> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT idt_criptoativo, nom_criptoativo, sig_criptoativo " +
                     "FROM t_mtp_criptoativo WHERE idt_criptoativo = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Criptoativo criptoativo = mapearCriptoativo(rs);
                    return Optional.of(criptoativo);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Busca um criptoativo pela sua sigla única (ex: BTC, ETH).
     * @param sigla A sigla do criptoativo.
     * @return Um Optional contendo o criptoativo se encontrado, ou vazio caso contrário.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public Optional<Criptoativo> buscarPorSigla(String sigla) throws SQLException {
        String sql = "SELECT idt_criptoativo, nom_criptoativo, sig_criptoativo " +
                     "FROM t_mtp_criptoativo WHERE sig_criptoativo = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sigla);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Criptoativo criptoativo = mapearCriptoativo(rs);
                    return Optional.of(criptoativo);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Lista todos os criptoativos disponíveis.
     * @return Uma lista contendo todos os criptoativos.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public List<Criptoativo> buscarTodos() throws SQLException {
        List<Criptoativo> criptoativos = new ArrayList<>();
        
        String sql = "SELECT idt_criptoativo, nom_criptoativo, sig_criptoativo FROM t_mtp_criptoativo";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Criptoativo criptoativo = mapearCriptoativo(rs);
                criptoativos.add(criptoativo);
            }
        }
        
        return criptoativos;
    }
    
    /**
     * Atualiza os dados de um criptoativo existente (nome, sigla).
     * Como Criptoativo é um record (imutável), esta operação cria um novo registro
     * e atualiza o existente no banco de dados.
     * @param criptoativo O objeto Criptoativo com os dados atualizados (deve conter o ID).
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void atualizar(Criptoativo criptoativo) throws SQLException {
        if (criptoativo.id() == null) {
            throw new SQLException("ID do criptoativo não pode ser nulo para atualização.");
        }
        
        String sql = "UPDATE t_mtp_criptoativo SET nom_criptoativo = ?, sig_criptoativo = ? " +
                    "WHERE idt_criptoativo = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, criptoativo.nomeCriptoativo());
            stmt.setString(2, criptoativo.sigla());
            stmt.setLong(3, criptoativo.id());
            
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar criptoativo, nenhum registro afetado.");
            }
        }
    }
    
    /**
     * Exclui um criptoativo do banco de dados pelo seu ID.
     * @param id O ID do criptoativo a ser excluído.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public void excluir(Long id) throws SQLException {
        String sql = "DELETE FROM t_mtp_criptoativo WHERE idt_criptoativo = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                // throw new SQLException("Falha ao excluir criptoativo, nenhum registro afetado.");
                 System.out.println("Nenhum criptoativo encontrado com o ID " + id + " para exclusão.");
            }
        }
    }
    
    /**
     * Utilitário para mapear ResultSet para objeto Criptoativo.
     * @param rs O ResultSet posicionado no registro a ser mapeado.
     * @return O objeto Criptoativo com dados do ResultSet.
     * @throws SQLException Se ocorrer erro ao acessar os dados do ResultSet.
     */
    private Criptoativo mapearCriptoativo(ResultSet rs) throws SQLException {
        Long id = rs.getLong("idt_criptoativo");
        String nome = rs.getString("nom_criptoativo");
        String sigla = rs.getString("sig_criptoativo");
        
        return new Criptoativo(id, nome, sigla);
    }
    
    /**
     * Fecha a conexão com o banco de dados.
     * @throws SQLException Se ocorrer erro ao fechar a conexão.
     */
    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
} 