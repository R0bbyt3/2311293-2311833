/* ===========================================================
 * FreeParkingSquare — casa de parada livre.
 * Não realiza nenhuma ação no jogador.
 * =========================================================== */

package model;

final class FreeParkingSquare extends Square {
    FreeParkingSquare(final int index, final String name) {
        super(index, name);
    }

    @Override
    void onLand(Player player, GameEngine engine, EconomyService economy) {
        // Nenhum efeito automático.
    }
}