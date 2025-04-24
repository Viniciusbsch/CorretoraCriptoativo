package org.example.model;

/**
 * Representa um Criptoativo como um Record imutável.
 * @param id Identificador único do criptoativo (chave primária).
 * @param nomeCriptoativo Nome completo do criptoativo.
 * @param sigla Sigla ou símbolo do criptoativo (ex: BTC).
 */
public record Criptoativo(
    Long id,
    String nomeCriptoativo,
    String sigla
) {
    // Construtores, getters, equals, hashCode e toString são gerados automaticamente.
    
    // Pode-se adicionar construtores compactos para validação, se necessário:
    public Criptoativo {
        if (nomeCriptoativo == null || nomeCriptoativo.isBlank()) {
            throw new IllegalArgumentException("Nome do criptoativo não pode ser nulo ou vazio.");
        }
        if (sigla == null || sigla.isBlank()) {
            throw new IllegalArgumentException("Sigla do criptoativo não pode ser nula ou vazia.");
        }
        // ID pode ser nulo antes de ser persistido
    }
    
    // Construtor adicional se precisar criar sem ID (antes de persistir)
    public Criptoativo(String nomeCriptoativo, String sigla) {
        this(null, nomeCriptoativo, sigla);
    }
}
