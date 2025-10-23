/* ===========================================================
 * GameEngine ; motor de regras/turno (camada Model)
 * =========================================================== */

package model;

import java.util.List;
import java.util.Objects;


final class GameEngine {

    // Dependências e estado do turno 
    private final Board board;
    private final List<Player> players;
    private final Deck deck;
    private final EconomyService economy;

    private int currentPlayerIndex;
    private DiceRoll lastRoll;

    GameEngine(final Board board,
               final List<Player> players,
               final Deck deck,
               final EconomyService economy,
               final int startIndex) {
        this.board   = Objects.requireNonNull(board, "board");
        this.players = Objects.requireNonNull(players, "players");
        this.deck    = Objects.requireNonNull(deck, "deck");
        this.economy = Objects.requireNonNull(economy, "economy");
        this.currentPlayerIndex = startIndex;
    }

    // Início do turno: limpa estado do dado. 
    void beginTurn() {
        this.lastRoll = null;
    }

    // Aplica regras de saída da prisão (dupla ou cartão). 
    void applyJailRules(final DiceRoll roll) {
        final Player p = currentPlayer();
        if (!p.isInJail()) return;

        if (roll.isDouble()) {
            p.setInJail(false);
            return;
        }
        
        if (p.consumeGetOutOfJailCard()) {
            p.setInJail(false);
            deck.returnGetOutOfJailCardToBottom();
        }
        // Sem multa nesta edição; se não saiu, permanece preso.
    }

    // Move o jogador da vez pelo tabuleiro. Ignora se estiver preso. 
    void moveBy(final int steps) {
        final Player p = currentPlayer();
        if (p.isInJail()) return;

        final int from = p.getPosition();
        final int to = board.nextPosition(from, steps);
        p.moveTo(to);
    }

    // Resolve o efeito da casa onde o jogador parou. 
    void onLand() {
        final Player p = currentPlayer();
        final Square sq = board.squareAt(p.getPosition());
        sq.onLand(p, this, economy);
    }
    
    // Tira uma carta do baralho e utiliza
    void drawAndUseCard(Player player) {
        final Card card = deck.draw();
        card.applyEffect(player, this, economy);
    }
    
    // Envia o jogador para a prisão. 
    void sendToJail(final Player player) {
        player.setInJail(true);
        player.moveTo(board.jailIndex());
    }

    // Final do turno: passa a vez para o próximo jogador ativo. 
    void finishTurn() {
        int n = players.size();
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % n;
        } while (!players.get(currentPlayerIndex).isAlive());
    }
    
    /* ===========================================================
     * Executa a jogada completa: rolar dados → aplicar prisão → mover → resolver casa.
     * =========================================================== */
    void rollAndResolve() {
        final Player p = currentPlayer();

        // Rola os dados e guarda
        final DiceRoll roll = roll();
        applyJailRules(roll);

        // Se estiver preso, não move
        if (p.isInJail()) {
            return;
        }

        // Move o jogador
        moveBy(roll.getSum());

        // Resolve efeito da casa
        onLand();
    }

    /* ===========================================================
     * Compra da propriedade atual (se aplicável).
     * =========================================================== */
    boolean chooseBuy() {
    	final Player player = currentPlayer();
        final StreetOwnableSquare property = (StreetOwnableSquare) board.squareAt(player.getPosition());
        
        return economy.attemptBuy(player, property);
    }

    /* ===========================================================
     * Construção em propriedade do jogador.
     * =========================================================== */
    boolean chooseBuild() {
    	final Player player = currentPlayer();
        final StreetOwnableSquare property = (StreetOwnableSquare) board.squareAt(player.getPosition());

        return economy.attemptBuild(player, property);
   
    }

    /* ===========================================================
     * Finaliza o turno e retorna o índice do próximo jogador.
     * =========================================================== */
    int endTurn() {
        finishTurn();
        return currentPlayerIndex;
    }

    /* Retorna a lista de todos os jogadores (imutável). */
    List<Player> allPlayers() {
        return List.copyOf(players);
    }

    // Utilitários

    Player currentPlayer() { return players.get(currentPlayerIndex); }

    DiceRoll roll() {
        this.lastRoll = new DiceRoll();
        return lastRoll;
    }

    public DiceRoll lastRoll() { return lastRoll; }

    EconomyService economy() { return economy; }
}

