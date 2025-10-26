package model;

/**
 * DTO para informações de uma propriedade do tipo companhia.
 * Mantido mínimo e imutável para poder ser retornado com segurança ao controller/view.
 */
public record OwnableCompanyInfo(
    String name,
    String owner,    // null quando não possui dono
    int price,
    int multiplier 
) {}
