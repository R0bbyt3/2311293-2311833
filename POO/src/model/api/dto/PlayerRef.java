package model.api.dto;

public record PlayerRef(String id, PlayerColor color) {
    public PlayerRef {
        if (id == null || !id.matches("P[1-6]"))
            throw new IllegalArgumentException("Id deve ser 'P1'..'P6'");
        if (color == null) throw new IllegalArgumentException("Color obrigatório");
    }

    public static PlayerRef of(int playerNumber, PlayerColor color) {
        return new PlayerRef("Player " + playerNumber, color);
    }
}
