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
    private List<Player> players;
    private int currentPlayerIndex;
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
        this.players = new ArrayList<>(playersConfig.size());
        for (PlayerSpec spec : playersConfig.players()) {
            this.players.add(new Player(
                spec.id(),
                spec.name(),
                spec.color(),
                initialPlayerMoney
            ));
        }
        this.currentPlayerIndex = 0;

        // 4) Tabuleiro
        final Board board = BoardFactory.fromCSV(boardCsvPath);

        // 5) Engine
        this.engine = new GameEngine(board, players, deck, economy, currentPlayerIndex);

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
        this.currentPlayerIndex = engine.endTurn();
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

    
    // ==== Tipos auxiliares ====

    /** Lista de jogadores com dados básicos. */
    public record PlayersConfig(List<PlayerSpec> players) {
        int size() { return players == null ? 0 : players.size(); }
    }

    /** Especificação mínima de um jogador. */
    public record PlayerSpec(String id, String name, String color) {}
}
