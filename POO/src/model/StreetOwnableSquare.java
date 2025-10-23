/* ===========================================================
 * StreetOwnableSquare ; ruas construtíveis; aluguel fixo por nível
 * =========================================================== */
package model;

final class StreetOwnableSquare extends OwnableSquare {

    private int houses;             // 0–4
    private boolean hasHotel;       // true = 1 hotel (só pode existir se houses == 4)
    private final int[] rentTable;  // níveis: [sem casa, 1, 2, 3, 4, hotel]
    private final int buildCost;    // custo fixo por construção
    
    StreetOwnableSquare(final int index,
                        final String name,
                        final String id,
                        final int price,
                        final int[] rentTable,
                        final int buildCost) {
        super(index, name, id, price);
        this.rentTable = expandTable(rentTable);
        this.buildCost = Math.max(buildCost, 0);
        this.houses = 0;
        this.hasHotel = false;
    }

    /** Garante tabela completa (6 níveis). */
    private int[] expandTable(int[] rents) {
        if (rents == null || rents.length == 0)
            throw new IllegalArgumentException("Tabela de aluguel vazia.");
        if (rents.length == 5) {
            int[] full = new int[6];
            full[0] = 0;
            System.arraycopy(rents, 0, full, 1, 5);
            return full;
        }
        if (rents.length == 6) return rents.clone();
        throw new IllegalArgumentException("Tabela de aluguel deve ter 5 ou 6 valores.");
    }

    // Pode construir mais 1 nível (até 4 casas e 1 hotel). 
    boolean canBuild() {
        return !hasHotel; // hotel = máximo possível
    }

    // Custo fixo de construção (para cada nível). 
    int getBuildCost() { return buildCost; }

    // Quantas casas a rua possui (0–4). 
    int getHouses() { return houses; }

    // Tem hotel? 
    boolean hasHotel() { return hasHotel; }

    // Constrói 1 nível: adiciona casa ou troca 4 casas por hotel. 
    void buildOne() {
        if (!canBuild()) throw new IllegalStateException("Não é possível construir mais aqui.");
        if (houses < 4) {
            houses++;
        } else {
            hasHotel = true;
            houses = 4; // mantém o valor de referência
        }
    }
    
    // Remove o dono (caso seja o atual) e reseta construções. 
    @Override
    void removeOwner(final Player target) {
        if (this.getOwner() != null && this.getOwner().equals(target)) {
            this.houses = 0;
            this.hasHotel = false;
            setOwner(null);
        }
    }

    // Retorna o valor total investido pelo dono atual (preço + construções). 
    @Override
    int getTotalInvestment() {
        if (this.getOwner() == null) return 0;

        int spentOnBuilds = (hasHotel ? 5 : houses + 1) * buildCost;
        return spentOnBuilds;
    }

    // Calcula o aluguel conforme nível atual. 
    @Override
    int calcRent(final GameEngine engine) {
        int level = hasHotel ? 5 : houses;
        return rentTable[level];
    }

    // Efeito ao cair na casa. 
    @Override
    void onLand(final Player player, final GameEngine engine, final EconomyService economy) {
      	
        economy.chargeRent(player, this, engine);
    }
}
