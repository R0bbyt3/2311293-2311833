/* ===========================================================
 * GameObserver ; interface para o padrão Observer.
 * Permite que a View seja notificada sobre mudanças no Model.
 * =========================================================== */

package controller;

/**
 * Interface para objetos que observam mudanças no estado do jogo.
 * Implementa o padrão Observer para desacoplar Model da View.
 */
public interface GameObserver {
    
    /**
     * Notifica que um novo turno começou.
     * @param playerIndex índice do jogador atual
     * @param playerName nome do jogador atual
     */
    void onTurnStarted(int playerIndex, String playerName, String playerColor, int playerMoney);
    
    /**
     * Notifica que os dados foram lançados.
     * @param dice1 valor do primeiro dado
     * @param dice2 valor do segundo dado
     * @param isDouble se os dados são iguais
     */
    void onDiceRolled(int dice1, int dice2, boolean isDouble);
    
    /**
     * Notifica que um jogador se moveu no tabuleiro.
     * @param playerIndex índice do jogador
     * @param fromPosition posição anterior
     * @param toPosition nova posição
     */
    void onPlayerMoved(int playerIndex, int fromPosition, int toPosition);
    
    /**
     * Notifica que um jogador caiu em uma casa.
     * @param playerIndex índice do jogador
     * @param squareIndex índice da casa
     * @param squareName nome da casa
     * @param squareType tipo/classe da casa (ex.: ChanceSquare, GoToJailSquare, JailSquare, MoneySquare, StreetOwnableSquare, StartSquare)
     */
    void onSquareLanded(int playerIndex, int squareIndex, String squareName, String squareType);

    /**
     * Notifica que um jogador caiu em uma ChanceSquare com a carta sorteada.
     * @param playerIndex índice do jogador
     * @param cardIndex índice da carta sorteada (0-based)
     */
    void onChanceSquareLand(int playerIndex, int cardIndex);
    
    /** Notifica que o jogador caiu em uma rua própria (nome da propriedade).
     * @param playerIndex índice do jogador
     * @param propertyName nome da propriedade
    */
    void onStreetOwnableLand(int playerIndex, String propertyName);

    /** Notifica que o jogador caiu em uma companhia própria (nome da companhia).
     * @param playerIndex índice do jogador
     * @param companyName nome da companhia
    */
    void onCompanyOwnableLand(int playerIndex, String companyName);
    
    /**
     * Notifica que o turno terminou.
     */
    void onTurnEnded();
    
    /**
     * Notifica sobre uma mensagem/evento do jogo.
     * @param message mensagem a exibir
     */
    void onGameMessage(String message);
}
