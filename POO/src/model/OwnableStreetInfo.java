package model;

/**
 * DTO para informações de uma propriedade do tipo rua.
 * Mantido mínimo e imutável para poder ser retornado com segurança ao controller/view.
 */
public record OwnableStreetInfo(
    String name,
    String owner,    // null quando não possui dono
    int price,
    int actualRent,
    int houses,
    boolean hasHotel
) {}
