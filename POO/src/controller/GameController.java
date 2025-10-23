/* ===========================================================
 * GameController ; controlador principal do padrão MVC.
 * Coordena interações entre Model (GameAPI) e View.
 * Implementa o padrão Observer para notificar a View sobre mudanças.
 * =========================================================== */

package controller;

import model.GameAPI;
import model.GameAPI.PlayerSpec;
import model.GameAPI.PlayersConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller principal da aplicação.
 * Gerencia o ciclo do jogo e coordena a comunicação entre Model e View.
 */
public class GameController {
    
    private final GameAPI gameAPI;
    private final List<GameObserver> observers;
    private boolean gameStarted;
    
    // Configurações padrão
    private static final int INITIAL_PLAYER_MONEY = 1500;
    private static final int INITIAL_BANK_CASH = 100000;
    private static final String BOARD_CSV = "assets/dados/board.csv";
    private static final String DECK_CSV = "assets/dados/deck.csv";
    
    // Cores padrão para os jogadores
    private static final String[] PLAYER_COLORS = {
        "red", "blue", "green", "yellow", "purple", "orange"
    };
    
    public GameController() {
        this.gameAPI = new GameAPI();
        this.observers = new ArrayList<>();
        this.gameStarted = false;
    }
    
    /**
     * Adiciona um observador para receber notificações de eventos.
     */
    public void addObserver(GameObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    /**
     * Remove um observador.
     */
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * Notifica todos os observadores sobre o início de um turno.
     */
    private void notifyTurnStarted(int playerIndex, String playerName) {
        for (GameObserver observer : observers) {
            observer.onTurnStarted(playerIndex, playerName);
        }
    }
    
    /**
     * Notifica todos os observadores sobre um lance de dados.
     */
    private void notifyDiceRolled(int dice1, int dice2, boolean isDouble) {
        for (GameObserver observer : observers) {
            observer.onDiceRolled(dice1, dice2, isDouble);
        }
    }
    
    /**
     * Notifica todos os observadores sobre movimento de jogador.
     */
    private void notifyPlayerMoved(int playerIndex, int fromPosition, int toPosition) {
        for (GameObserver observer : observers) {
            observer.onPlayerMoved(playerIndex, fromPosition, toPosition);
        }
    }
    
    /**
     * Notifica todos os observadores sobre uma mensagem do jogo.
     */
    private void notifyGameMessage(String message) {
        for (GameObserver observer : observers) {
            observer.onGameMessage(message);
        }
    }
    
    /**
     * Notifica todos os observadores sobre o fim do turno.
     */
    private void notifyTurnEnded() {
        for (GameObserver observer : observers) {
            observer.onTurnEnded();
        }
    }
    
    /**
     * Inicia um novo jogo com o número especificado de jogadores.
     * @param numberOfPlayers número de jogadores (3 a 6)
     */
    public void startNewGame(int numberOfPlayers) {
        if (numberOfPlayers < 3 || numberOfPlayers > 6) {
            throw new IllegalArgumentException("Number of players must be between 3 and 6");
        }
        
        // Cria lista de jogadores
        List<PlayerSpec> playerSpecs = new ArrayList<>();
        for (int i = 0; i < numberOfPlayers; i++) {
            playerSpecs.add(new PlayerSpec(
                "P" + (i + 1),
                "Player " + (i + 1),
                PLAYER_COLORS[i]
            ));
        }
        
        PlayersConfig config = new PlayersConfig(playerSpecs);
        
        // Caminhos dos arquivos de configuração
        Path boardPath = Paths.get(BOARD_CSV);
        Path deckPath = Paths.get(DECK_CSV);
        
        try {
            System.out.println("DEBUG: Starting game with " + numberOfPlayers + " players");
            System.out.println("DEBUG: Board CSV: " + boardPath.toAbsolutePath());
            System.out.println("DEBUG: Deck CSV: " + deckPath.toAbsolutePath());
            
            // Inicia o jogo através da API
            gameAPI.startGame(config, boardPath, deckPath, INITIAL_PLAYER_MONEY, INITIAL_BANK_CASH);
            gameStarted = true;
            
            // Atualiza as posições iniciais de todos os jogadores
            for (int i = 0; i < numberOfPlayers; i++) {
                int position = gameAPI.getPlayerPosition(i);
                notifyPlayerMoved(i, -1, position); // -1 indica inicialização
            }
            
            notifyGameMessage("Game started with " + numberOfPlayers + " players!");
            System.out.println("DEBUG: Initial player positions notified");
            
            // Notifica o primeiro jogador
            int firstPlayer = gameAPI.getCurrentPlayerIndex();
            String firstPlayerName = gameAPI.getPlayerName(firstPlayer);
            notifyTurnStarted(firstPlayer, firstPlayerName);
            
        } catch (Exception e) {
            System.err.println("ERROR starting game:");
            e.printStackTrace();
            notifyGameMessage("Error starting game: " + e.getMessage());
            throw new RuntimeException("Failed to start game", e);
        }
    }
    
    /**
     * Executa um turno completo: rola dados e resolve todas as ações.
     * Este é o método principal que coordena a jogada.
     */
    public void rollDiceAndPlay() {
        if (!gameStarted) {
            throw new IllegalStateException("O jogo ainda não foi iniciado");
        }
        
        try {
            // Bloqueia rolagem se o jogador atual foi quem rolou por último
            int lastRoller = gameAPI.getLastRollerIndex();
            int currentPlayer = gameAPI.getCurrentPlayerIndex();
            if (lastRoller == currentPlayer) {
                String pname = gameAPI.getPlayerName(currentPlayer);
                notifyGameMessage(" '" + pname + "' tried to roll again, but was the last to play. Action blocked.");
                return;
            }

            // Obtém informações do jogador atual antes da jogada
            int positionBefore = gameAPI.getPlayerPosition(currentPlayer);
            
            // Executa a jogada através da API (move o jogador de verdade)
            gameAPI.rollAndResolve();
            
            // Obtém os valores dos dados reais que foram lançados
            model.DiceRoll lastRoll = gameAPI.getLastDiceRoll();
            int dice1 = lastRoll.getD1();
            int dice2 = lastRoll.getD2();
            boolean isDouble = lastRoll.isDouble();
            
            // Notifica sobre o lance de dados
            notifyDiceRolled(dice1, dice2, isDouble);
            
            // Obtém a posição real após o movimento
            int positionAfter = gameAPI.getPlayerPosition(currentPlayer);
            
            // Notifica sobre o movimento real
            notifyPlayerMoved(currentPlayer, positionBefore, positionAfter);
            
            String playerName = gameAPI.getPlayerName(currentPlayer);
            int steps = dice1 + dice2;
            notifyGameMessage(playerName + " rolled " + dice1 + " + " + dice2 + " = " + steps);
            notifyGameMessage(playerName + " moved from position " + positionBefore + " to " + positionAfter);

        } catch (Exception e) {
            notifyGameMessage("Error during turn: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Finaliza o turno atual e passa para o próximo jogador.
     */
    public void endTurn() {
        if (!gameStarted) {
            throw new IllegalStateException("Game has not been started yet");
        }
        
        try {
            // Finaliza o turno e obtém o próximo jogador
            gameAPI.endTurn();
            notifyTurnEnded();
            
            // Obtém informações do próximo jogador
            int nextPlayerIndex = gameAPI.getCurrentPlayerIndex();
            String nextPlayerName = gameAPI.getPlayerName(nextPlayerIndex);
            
            notifyTurnStarted(nextPlayerIndex, nextPlayerName);
            notifyGameMessage("Now it's " + nextPlayerName + "'s turn");
            
        } catch (Exception e) {
            notifyGameMessage("Error ending turn: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verifica se o jogo já foi iniciado.
     */
    public boolean isGameStarted() {
        return gameStarted;
    }
}
