package model.api.dto;

public sealed interface OwnableInfo permits Ownables.Street, Ownables.Company {
    Core core();

    // Parte comum a qualquer ownable
    public static final record Core(
            PlayerRef owner,
            String propertyName,
            int boardIndex,
            int propertyPrice,
            int propertySellValue 
    ) {
        public Core {
        	 if (boardIndex < 0) throw new IllegalArgumentException("boardIndex < 0");
            if (propertyPrice < 0) throw new IllegalArgumentException("price < 0");
            if (propertySellValue < 0) throw new IllegalArgumentException("sell < 0");       
        }
    }
}
