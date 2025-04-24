package org.example.service;

import org.example.dao.CriptoativoDAO;
import org.example.model.Criptoativo;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para operações relacionadas a Criptoativos.
 */
public class CriptoativoService {

    private CriptoativoDAO criptoativoDAO;

    public CriptoativoService() {
        try {
            this.criptoativoDAO = new CriptoativoDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar CriptoativoDAO", e);
        }
    }

    /**
     * Lista todos os criptoativos disponíveis no sistema.
     * 
     * @return Lista de todos os criptoativos cadastrados.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     */
    public List<Criptoativo> listarTodosCriptoativos() throws SQLException {
        return criptoativoDAO.buscarTodos();
    }

    /**
     * Busca um criptoativo específico pelo seu ID.
     * 
     * @param id O ID do criptoativo a ser buscado.
     * @return Um Optional contendo o criptoativo se encontrado.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     * @throws IllegalArgumentException Se o ID for nulo.
     */
    public Optional<Criptoativo> buscarCriptoativoPorId(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("ID do criptoativo não pode ser nulo");
        }
        
        return criptoativoDAO.buscarPorId(id);
    }

    /**
     * Busca um criptoativo pela sua sigla (ex: BTC, ETH).
     * 
     * @param sigla A sigla do criptoativo.
     * @return Um Optional contendo o criptoativo se encontrado.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     * @throws IllegalArgumentException Se a sigla for nula ou vazia.
     */
    public Optional<Criptoativo> buscarCriptoativoPorSigla(String sigla) throws SQLException {
        if (sigla == null || sigla.trim().isEmpty()) {
            throw new IllegalArgumentException("Sigla do criptoativo não pode ser nula ou vazia");
        }
        
        return criptoativoDAO.buscarPorSigla(sigla);
    }

    /**
     * Cadastra um novo criptoativo no sistema.
     * 
     * @param nome O nome completo do criptoativo.
     * @param sigla A sigla/símbolo do criptoativo.
     * @return O criptoativo cadastrado.
     * @throws SQLException Se ocorrer um erro no banco de dados.
     * @throws IllegalArgumentException Se o nome ou sigla forem inválidos.
     */
    public Criptoativo cadastrarCriptoativo(String nome, String sigla) throws SQLException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do criptoativo não pode ser nulo ou vazio");
        }
        
        if (sigla == null || sigla.trim().isEmpty()) {
            throw new IllegalArgumentException("Sigla do criptoativo não pode ser nula ou vazia");
        }
        
        // Verificar se já existe criptoativo com esta sigla
        Optional<Criptoativo> existente = criptoativoDAO.buscarPorSigla(sigla);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Já existe um criptoativo com a sigla: " + sigla);
        }
        
        // Criar e salvar o novo criptoativo
        Criptoativo novoCriptoativo = new Criptoativo(nome, sigla);
        criptoativoDAO.salvar(novoCriptoativo);
        
        // Como o criptoativo é um record imutável, precisamos buscar novamente para obter o ID
        return criptoativoDAO.buscarPorSigla(sigla)
                .orElseThrow(() -> new SQLException("Erro ao recuperar criptoativo após cadastro"));
    }
} 