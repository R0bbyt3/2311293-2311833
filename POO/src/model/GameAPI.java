/* ===========================================================
 * GameAPI ; fachada pública do Model.
 * Expõe operações seguras para Controller/View e orquestra o ciclo de jogo via GameEngine.
 * =========================================================== */

package model;

import java.nio.file.Path;
import java.util.*;

public final class GameAPI {

    // ==== Estado principal mantido pela fachada ====
    private GameEngine engine;
    private boolean started;

    // ==== API pública ====

    /**
     * Inicia o jogo (boot do Model).
     * Cria regras, banco, dados, baralhos, tabuleiro, jogadores, economia e engine.
     *
     * playersConfig configuração dos jogadores
     * boardCsvPath  caminho do CSV contendo o tabuleiro
     * deckCsvPaths  lista de caminhos para os CSVs de baralhos
     * initialPlayerMoney dinheiro inicial de cada jogador
     * initialBankCash dinheiro inicial do banco
     */
    public void startGame(final PlayersConfig playersConfig,
                          final Path boardCsvPath,
                          final Path deckCsvPath,
                          final int initialPlayerMoney,
                          final int initialBankCash) {
        ensureNotStarted();
        Objects.requireNonNull(playersConfig, "playersConfig não pode ser nulo");
        Objects.requireNonNull(boardCsvPath, "boardCsvPath não pode ser nulo");
        Objects.requireNonNull(deckCsvPath, "deckCsvPath não pode ser nulo");

        // 1) Banco e economia
        final Bank bank = new Bank(initialBankCash);
        final EconomyService economy = new EconomyService(bank);

        // 2) Baralhos
        final Deck deck = DeckFactory.fromCSV(deckCsvPath);

        // 3) Jogadores
        validatePlayerCount(playersConfig);
        List<Player> players = new ArrayList<>(playersConfig.size());
        for (PlayerSpec spec : playersConfig.players()) {
            players.add(new Player(
                spec.id(),
                spec.name(),
                spec.color(),
                initialPlayerMoney
            ));
        }

        // 4) Tabuleiro
        final Board board = BoardFactory.fromCSV(boardCsvPath);

        // 5) Engine
    this.engine = new GameEngine(board, players, deck, economy, 0);

        // 7) Boot concluído
        this.started = true;
    }

    // ==== Métodos públicos ====

    /** Rola os dados e resolve tudo que não depende do usuário. */
    public void rollAndResolve() {
        ensureStarted();
        engine.rollAndResolve();
    }

    /** Solicita compra da propriedade atual (se aplicável). */
    public boolean chooseBuy() {
        ensureStarted();
        return engine.chooseBuy();
    }

    /** Solicita construção em propriedade do jogador. */
    public boolean chooseBuild() {
        ensureStarted();
        return engine.chooseBuild();
    }

    /** Encerra o turno atual e passa para o próximo jogador. */
    public void endTurn() {
        ensureStarted();
        engine.endTurn();
    }
    
    // ==== Métodos para obter informações do jogo ====
    
    /** Retorna o índice do jogador atual. */
    public int getCurrentPlayerIndex() {
        ensureStarted();
        return engine.currentPlayerIndex();
    }
    
    /** Retorna o número total de jogadores. */
    public int getNumberOfPlayers() {
        ensureStarted();
        return engine.allPlayers().size();
    }
    
    /** Retorna a posição de um jogador no tabuleiro. */
    public int getPlayerPosition(int playerIndex) {
        ensureStarted();
        validatePlayerIndex(playerIndex);
        return engine.allPlayers().get(playerIndex).getPosition();
    }
    
    /** Retorna o nome de um jogador. */
    public String getPlayerName(int playerIndex) {
        ensureStarted();
        validatePlayerIndex(playerIndex);
        return engine.allPlayers().get(playerIndex).getName();
    }
    
    /** Retorna o saldo de um jogador. */
    public int getPlayerMoney(int playerIndex) {
        ensureStarted();
        validatePlayerIndex(playerIndex);
        return engine.allPlayers().get(playerIndex).getMoney();
    }
    
    /** Retorna se um jogador está na prisão. */
    public boolean isPlayerInJail(int playerIndex) {
        ensureStarted();
        validatePlayerIndex(playerIndex);
        return engine.allPlayers().get(playerIndex).isInJail();
    }
    
    /** Retorna os valores do último lance de dados (após rollAndResolve). */
    public DiceData getLastDiceData() {
        ensureStarted();
        final int[] vals = engine.lastRollValues();
        return new DiceData(vals[0], vals[1], vals[2] == 1);
    }

    /** Retorna o índice do último jogador que rolou via rollAndResolve, ou -1 se nenhum. */
    public int getLastRollerIndex() {
        ensureStarted();
        return engine.lastRollerIndex();
    }

    /** Retorna a cor (string) de um jogador. */
    public String getPlayerColor(int playerIndex) {
        ensureStarted();
        validatePlayerIndex(playerIndex);
        return engine.allPlayers().get(playerIndex).getColor();
    }

    /** Retorna o nome da square no índice fornecido. */
    public String getSquareName(final int index) {
        ensureStarted();
        return engine.getSquareName(index);
    }

    /** Retorna o tipo (classe simples) da square no índice fornecido. */
    public String getSquareType(final int index) {
        ensureStarted();
        return engine.getSquareType(index);
    }

    /** Retorna o índice da última carta retirada do baralho (ou -1). */
    public int getLastDrawedCardIndex() {
        ensureStarted();
        return engine.lastDrawedCardIndex();
    }

    /** Retorna o nome da última propriedade/companhia em que um jogador caiu (ou null). */
    public String getLastLandedOwnableName() {
        ensureStarted();
        return engine.lastLandedOwnableName();
    }

    // ==== Auxiliares internas ====

    private void ensureStarted() {
        if (!started)
            throw new IllegalStateException("Jogo ainda não foi iniciado. Chame startGame().");
    }

    private void ensureNotStarted() {
        if (started)
            throw new IllegalStateException("Jogo já iniciado.");
    }

    private void validatePlayerCount(final PlayersConfig cfg) {
        final int n = cfg.size();
        if (n < 2 || n > 6)
            throw new IllegalArgumentException("Quantidade de jogadores inválida (precisa ser entre 2 e 6).");
    }

    /** Valida se o índice do jogador está no intervalo válido. */
    private void validatePlayerIndex(final int playerIndex) {
        final int n = engine.allPlayers().size();
        if (playerIndex < 0 || playerIndex >= n) {
            throw new IllegalArgumentException("Índice de jogador inválido: " + playerIndex);
        }
    }

    
    // ==== Tipos auxiliares ====

    /** Lista de jogadores com dados básicos. */
    public record PlayersConfig(List<PlayerSpec> players) {
        int size() { return players == null ? 0 : players.size(); }
    }

    /** Especificação mínima de um jogador. */
    public record PlayerSpec(String id, String name, String color) {}

    /** Retorna os valores do último lance de dados em um pequeno DTO. */
    public record DiceData(int d1, int d2, boolean isDouble) {}
}
