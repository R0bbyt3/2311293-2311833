package model.api.dto;

public final class PlayerRef {
    private final String id;
    private final PlayerColor color;

    public PlayerRef(String id, PlayerColor color) {
        if (id == null || !id.matches("P[1-6]|Player [1-6]"))
            throw new IllegalArgumentException("Id deve ser 'P1'..'P6' ou 'Player 1'..'Player 6'");
        if (color == null) throw new IllegalArgumentException("Color obrigatório");
        this.id = id;
        this.color = color;
    }

    public String id() { return id; }
    public PlayerColor color() { return color; }

    public static PlayerRef of(int playerNumber, PlayerColor color) {
        return new PlayerRef("Player " + playerNumber, color);
    }
}
