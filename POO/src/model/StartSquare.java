/* ===========================================================
 * StartSquare ; casa de Partida.
 * Aplica bônus de início conforme valor determinado.
 * =========================================================== */

package model;

final class StartSquare extends Square {

    private final int bonus;

    StartSquare(final int index, final String name, final int bonus) {
        super(index, name);
        this.bonus = Math.max(bonus, 0);
    }

    @Override
    void onLand(final Player player, final GameEngine engine, final EconomyService economy) {
        economy.applyIncome(player, bonus);
    }
}

