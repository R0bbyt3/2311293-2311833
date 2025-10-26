package model.api.dto;

public final class Ownables {
    private Ownables() {}

    // ==== Street ====
    public static record Street(OwnableInfo.Core core,
                                int propertyActualRent,
                                int propertyHouseNumber,
                                boolean propertyHasHotel)
            implements OwnableInfo {

        public Street {
            if (core == null) throw new IllegalArgumentException("Core obrigatório");
            if (propertyActualRent < 0) throw new IllegalArgumentException("Rent >= 0");
            if (propertyHouseNumber < 0 || propertyHouseNumber > 4)
                throw new IllegalArgumentException("HouseNumber deve ser 0..4");
            if (propertyHasHotel && propertyHouseNumber < 4)
                throw new IllegalArgumentException("Hotel exige houseNumber==4");
        }

        @Override public OwnableInfo.Core core() { return core; }
    }

    // ==== Company ====
    public static record Company(OwnableInfo.Core core,
                                 int propertyMultiplier)
            implements OwnableInfo {

        public Company {
            if (core == null) throw new IllegalArgumentException("Core obrigatório");
            if (propertyMultiplier <= 0) throw new IllegalArgumentException("Multiplier > 0");
        }

        @Override public OwnableInfo.Core core() { return core; }
    }
}
