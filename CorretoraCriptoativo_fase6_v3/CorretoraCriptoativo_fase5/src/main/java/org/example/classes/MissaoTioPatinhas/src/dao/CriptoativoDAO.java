package org.example.classes.MissaoTioPatinhas.src.dao;

import org.example.classes.MissaoTioPatinhas.src.Criptoativo;
import org.example.factory.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CriptoativoDAO {
    // Insere um novo criptoativo
    public void salvar(Criptoativo criptoativo) throws SQLException {
        String sql = "INSERT INTO T_MTP_CRIPTOATIVO (IDT_CRIPTOATIVO, SIG_CRIPTOATIVO, NOM_CRIPTOATIVO, DSC_CRIPTOATIVO, VLR_COTACAO_REAL) VALUES (SEQ_CRIPTOATIVO.NEXTVAL, ?, ?, ?, ?)";
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, criptoativo.getSigla());
            stmt.setString(2, criptoativo.getNome());
            stmt.setString(3, criptoativo.getDescricao());
            stmt.setDouble(4, criptoativo.getCotacao());
            stmt.executeUpdate();
        }
    }

    // Busca um criptoativo pelo ID
    public Criptoativo buscarPorId(int idCriptoativo) throws SQLException {
        String sql = "SELECT IDT_CRIPTOATIVO, NOM_CRIPTOATIVO, SIG_CRIPTOATIVO, DSC_CRIPTOATIVO, VLR_COTACAO_REAL FROM T_MTP_CRIPTOATIVO WHERE IDT_CRIPTOATIVO = ?";
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idCriptoativo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Criptoativo c = new Criptoativo(
                    rs.getString("NOM_CRIPTOATIVO"),
                    rs.getString("SIG_CRIPTOATIVO"),
                    rs.getString("DSC_CRIPTOATIVO"),
                    rs.getDouble("VLR_COTACAO_REAL")
                );
                c.setId(rs.getInt("IDT_CRIPTOATIVO"));
                return c;
            }
        }
        return null;
    }

    // Lista todos os criptoativos
    public List<Criptoativo> listarTodos() throws SQLException {
        List<Criptoativo> lista = new ArrayList<>();
        String sql = "SELECT IDT_CRIPTOATIVO, NOM_CRIPTOATIVO, SIG_CRIPTOATIVO, DSC_CRIPTOATIVO, VLR_COTACAO_REAL FROM T_MTP_CRIPTOATIVO";
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Criptoativo c = new Criptoativo(
                    rs.getString("NOM_CRIPTOATIVO"),
                    rs.getString("SIG_CRIPTOATIVO"),
                    rs.getString("DSC_CRIPTOATIVO"),
                    rs.getDouble("VLR_COTACAO_REAL")
                );
                c.setId(rs.getInt("IDT_CRIPTOATIVO"));
                lista.add(c);
            }
        }
        return lista;
    }

    // Atualiza um criptoativo pelo ID
    public boolean atualizar(int idCriptoativo, Criptoativo criptoativo) throws SQLException {
        String sql = "UPDATE T_MTP_CRIPTOATIVO SET NOM_CRIPTOATIVO = ?, SIG_CRIPTOATIVO = ?, DSC_CRIPTOATIVO = ?, VLR_COTACAO_REAL = ? WHERE IDT_CRIPTOATIVO = ?";
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, criptoativo.getNome());
            stmt.setString(2, criptoativo.getSigla());
            stmt.setString(3, criptoativo.getDescricao());
            stmt.setDouble(4, criptoativo.getCotacao());
            stmt.setInt(5, idCriptoativo);
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        }
    }

    // Exclui um criptoativo pelo ID
    public boolean excluir(int idCriptoativo) throws SQLException {
        String sql = "DELETE FROM T_MTP_CRIPTOATIVO WHERE IDT_CRIPTOATIVO = ?";
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idCriptoativo);
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        }
    }
} 