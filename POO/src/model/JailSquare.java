/* ===========================================================
 * JailSquare ; casa da prisão (visita/permanência).
 * Estado de "preso" é mantido no Player; aqui não há efeito.
 * =========================================================== */

package model;

final class JailSquare extends Square {
    JailSquare(final int index, final String name) {
        super(index, name);
    }
    
    @Override
    void onLand(Player player, GameEngine engine, EconomyService economy) {
        // Nenhum efeito — apenas visita
    }

}