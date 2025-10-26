package model.api.dto;

public sealed interface OwnableInfo permits Ownables.Street, Ownables.Company {
    Core core();

    // Parte comum a qualquer ownable
    public record Core(PlayerRef owner, int propertyPrice) {
        public Core {
            if (propertyPrice < 0) throw new IllegalArgumentException("propertyPrice >= 0");
            // owner pode ser null => sem dono
        }
    }
}
