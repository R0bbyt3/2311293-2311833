/* ===========================================================
 * GameController ; controlador principal do padrão MVC.
 * Coordena interações entre Model (GameAPI) e View.
 * Implementa o padrão Observer para notificar a View sobre mudanças.
 * =========================================================== */

package controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import model.GameAPI;
import model.GameAPI.PlayerSpec;
import model.GameAPI.PlayersConfig;
import model.api.dto.OwnableInfo;
import model.api.dto.Ownables;
import model.api.dto.PlayerColor;
/**
 * Controller principal da aplicação.
 * Gerencia o ciclo do jogo e coordena a comunicação entre Model e View.
 */
public class GameController {
    
    private final GameAPI gameAPI;
    private final List<GameObserver> observers;
    private boolean gameStarted;
    
    // Mock de dados para testes
    private Integer mockedDice1;
    private Integer mockedDice2;
    
    // Configurações padrão
    private static final int INITIAL_PLAYER_MONEY = 1500;
    private static final int INITIAL_BANK_CASH = 100000;
    private static final String BOARD_CSV = "assets/dados/board.csv";
    private static final String DECK_CSV = "assets/dados/deck.csv";
    
    // Cores padrão para os jogadores (definidas pelo enum PlayerColor)
    private static final PlayerColor[] PLAYER_COLORS = PlayerColor.values();
    
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
    private void notifyTurnStarted(int playerIndex, String playerName, PlayerColor firstPlayerColor, int playerMoney) {
        for (GameObserver observer : observers) {
            observer.onTurnStarted(playerIndex, playerName, firstPlayerColor, playerMoney);
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
     * Notifica todos os observadores que um jogador caiu em uma casa específica.
     */
    private void notifySquareLanded(int playerIndex, int squareIndex, String squareName, String squareType) {
        for (GameObserver observer : observers) {
            observer.onSquareLanded(playerIndex, squareIndex, squareName, squareType);
        }
    }

    /**
     * Função auxiliar que notifica os observadores sobre a casa em que o jogador caiu
     * e executa ações específicas baseadas no tipo da casa.
     */
    private void callSquareNotification(int playerIndex, int squareIndex, String squareName, String squareType) {
        // Sempre notifica o pouso na casa
        notifySquareLanded(playerIndex, squareIndex, squareName, squareType);

        // Açoes específicas baseadas no tipo da casa
        switch (squareType) {
            case "ChanceSquare":
                notifyGameMessage("Drawing a chance card for " + gameAPI.getPlayerName(playerIndex));
                int cardIdx = gameAPI.getLastDrawedCardIndex();
                notifyChanceSquare(playerIndex, cardIdx);
                break;
            case "GoToJailSquare":
                notifyGameMessage("GoToJailSquare landed: player will be sent to jail.");
                break;
            case "JailSquare":
                notifyGameMessage("JailSquare: visiting jail.");
                break;
            case "MoneySquare":
                notifyGameMessage("MoneySquare: money-related effect applies.");
                break;
            case "StreetOwnableSquare":
                notifyGameMessage("Ownable property landed: " + squareName);
       
                var streetDto = gameAPI.getStreetOwnableInfo(squareIndex);
                notifyStreetOwnable(playerIndex, squareName, streetDto);
                break;
            case "CompanyOwnableSquare":
                notifyGameMessage("Company landed: " + squareName);
                var companyDto = gameAPI.getCompanyOwnableInfo(squareIndex);
                notifyCompanyOwnable(playerIndex, squareName, companyDto);
                break;
            case "StartSquare":
                notifyGameMessage("Start square landed: collecting rewards if any.");
                break;
            default:
                notifyGameMessage("Landed on square type: " + squareType);
        }
    }

    private void notifyChanceSquare(int playerIndex, int cardIndex) {
        for (GameObserver observer : observers) {
            observer.onChanceSquareLand(playerIndex, cardIndex);
        }
    }

    private void notifyStreetOwnable(int playerIndex, String propertyName, Ownables.Street streetInfo) {
        for (GameObserver observer : observers) {
            observer.onStreetOwnableLand(playerIndex, propertyName, streetInfo);
        }
    }

    private void notifyCompanyOwnable(int playerIndex, String companyName, Ownables.Company companyInfo) {
        for (GameObserver observer : observers) {
            observer.onCompanyOwnableLand(playerIndex, companyName, companyInfo);
        }
    }

    /**
     * Notifica atualização de uma rua (compra/construção)
     */
    private void notifyStreetOwnableUpdate(int playerIndex, Ownables.Street streetInfo) {
        for (GameObserver observer : observers) {
            observer.onStreetOwnableUpdate(playerIndex, streetInfo);
        }
    }

    /**
     * Notifica atualização de uma companhia (compra/efeito)
     */
    private void notifyCompanyOwnableUpdate(int playerIndex, Ownables.Company companyInfo) {
        for (GameObserver observer : observers) {
            observer.onCompanyOwnableUpdate(playerIndex, companyInfo);
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

    // Notificar observers com a lista de propriedades prontas
    private void notifyPropertyDataUpdated(List<OwnableInfo> items) {
        for (GameObserver observer : observers) {
            observer.onCurrentPlayerPropertyDataUpdated(items);
        }
    }

    // Notificar observers sobre venda de propriedade
    private void notifyPropertySold(int playerIndex) {
        for (GameObserver observer : observers) {
            observer.onPropertySold(playerIndex);
        }
    }

    /** Notifica observadores que o dinheiro de um jogador mudou. */
    // money notifications are now emitted via game messages or property updates
    
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
            // pass the color as the expected string name to the API (lowercase)
            PlayerColor color = PLAYER_COLORS[i];
            playerSpecs.add(new PlayerSpec(
                "P" + (i + 1),
                "Player " + (i + 1),
                color
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
            
            // Notifica o primeiro jogador (inclui cor)
            int firstPlayer = gameAPI.getCurrentPlayerIndex();
            String firstPlayerName = gameAPI.getPlayerName(firstPlayer);
            PlayerColor firstPlayerColor = gameAPI.getPlayerColor(firstPlayer);
            int firstPlayerMoney = gameAPI.getPlayerMoney(firstPlayer);
            notifyTurnStarted(firstPlayer, firstPlayerName, firstPlayerColor, firstPlayerMoney);
            notifyPropertyDataUpdated(gameAPI.getCurrentPlayerPropertyData());
            
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
        ensureGameStarted();
        
        try {
            int currentPlayer = gameAPI.getCurrentPlayerIndex();
            if (!gameAPI.isRollAllowed()) {
                String pname = gameAPI.getPlayerName(currentPlayer);
                notifyGameMessage(" '" + pname + "' tried to roll again, but was the last to play. Action blocked.");
                return;
            }

            // Obtém informações do jogador atual antes da jogada
            int positionBefore = gameAPI.getPlayerPosition(currentPlayer);
            
            // Se há valores mockados, aplica-os ao GameAPI antes do roll
            if (hasMockedDiceValues()) {
                gameAPI.setMockedDiceValues(mockedDice1, mockedDice2);
                // Limpa os valores após aplicá-los (single-use no controller também)
                clearMockedDiceValues();
            }
            
            // Executa a jogada através da API (move o jogador de verdade)
            gameAPI.rollAndResolve();
            
            // Obtém os valores dos dados reais que foram lançados via GameAPI
            GameAPI.DiceData lastRoll = gameAPI.getLastDiceData();
            int dice1 = lastRoll.d1();
            int dice2 = lastRoll.d2();
            boolean isDouble = lastRoll.isDouble();
            
            // Notifica sobre o lance de dados
            notifyDiceRolled(dice1, dice2, isDouble);
            
            // Obtém a posição real após o movimento
            int positionAfter = gameAPI.getPlayerPosition(currentPlayer);
            
            // Notifica sobre o movimento real
            notifyPlayerMoved(currentPlayer, positionBefore, positionAfter);

            // Notifica sobre a casa em que o jogador caiu
            String squareName = gameAPI.getSquareName(positionAfter);
            String squareType = gameAPI.getSquareType(positionAfter);
            // Use helper to notify observers and perform type-specific actions
            callSquareNotification(currentPlayer, positionAfter, squareName, squareType);


        } catch (Exception e) {
            notifyGameMessage("Error during turn: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Finaliza o turno atual e passa para o próximo jogador.
     */
    public void endTurn() {
        ensureGameStarted();
        
        try {
            // Finaliza o turno e obtém o próximo jogador
            gameAPI.endTurn();
            notifyTurnEnded();
            
            // Obtém informações do próximo jogador
            int nextPlayerIndex = gameAPI.getCurrentPlayerIndex();
            String nextPlayerName = gameAPI.getPlayerName(nextPlayerIndex);
            PlayerColor nextPlayerColor = gameAPI.getPlayerColor(nextPlayerIndex);
            int nextPlayerMoney = gameAPI.getPlayerMoney(nextPlayerIndex);

            notifyTurnStarted(nextPlayerIndex, nextPlayerName, nextPlayerColor, nextPlayerMoney);
            notifyGameMessage("Now it's " + nextPlayerName + "'s turn");
            notifyPropertyDataUpdated(gameAPI.getCurrentPlayerPropertyData());
            
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

    /**
     * Garante que o jogo foi iniciado; lança IllegalStateException caso contrário.
     */
    private void ensureGameStarted() {
        if (!gameStarted) {
            throw new IllegalStateException("Game has not been started yet");
        }
    }

    /**
     * Retorna a quantidade de dinheiro de um jogador (acesso de conveniência para a view).
     */
    public int getPlayerMoney(int playerIndex) {
        return gameAPI.getPlayerMoney(playerIndex);
    }

    /**
     * Tenta comprar a propriedade onde o jogador atual está.
     * Se não for possível, envia uma mensagem de debug explicando o motivo.
     */
    public void attemptBuy() {
        ensureGameStarted();

        try {
            final int currentPlayer = gameAPI.getCurrentPlayerIndex();

            if (!gameAPI.chooseBuy()) {
                String reason = gameAPI.getBuyNotAllowedReason();
                if (reason == null) reason = "Unknown reason";
                notifyGameMessage("Buy blocked: " + reason);
                return;
            }

            int pos = gameAPI.getPlayerPosition(currentPlayer);
            String propName = gameAPI.getSquareName(pos);

            notifyGameMessage(gameAPI.getPlayerName(currentPlayer) + " bought " + propName);

            String squareType = gameAPI.getSquareType(pos);
            // If it is a street ownable, notify an update (purchase) to the view without the "land" debug.
            if (squareType != null && squareType.equals("StreetOwnableSquare")) {
                Ownables.Street streetInfo = gameAPI.getStreetOwnableInfo(pos);
                notifyStreetOwnableUpdate(currentPlayer, streetInfo);
            } else {
                Ownables.Company companyInfo = gameAPI.getCompanyOwnableInfo(pos);
                notifyCompanyOwnableUpdate(currentPlayer,companyInfo);
            }
            notifyPropertyDataUpdated(gameAPI.getCurrentPlayerPropertyData());
            
        } catch (Exception e) {
            notifyGameMessage("Error while attempting buy: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tenta construir (uma casa) na propriedade atual. 
     * Emite debug se não for possível.
     */
    public void attemptBuild() {
        ensureGameStarted();

        try {
            final int currentPlayer = gameAPI.getCurrentPlayerIndex();

            if (!gameAPI.chooseBuild()) {
                String reason = gameAPI.getBuildNotAllowedReason();
                if (reason == null) reason = "Unknown reason";
                notifyGameMessage("Build blocked: " + reason);
                return;
            }

            int pos = gameAPI.getPlayerPosition(currentPlayer);
            String propName = gameAPI.getSquareName(pos);
            notifyGameMessage(gameAPI.getPlayerName(currentPlayer) + " built on " + propName);
            Ownables.Street streetInfo = gameAPI.getStreetOwnableInfo(pos);
            notifyStreetOwnableUpdate(currentPlayer, streetInfo);
            notifyPropertyDataUpdated(gameAPI.getCurrentPlayerPropertyData());
        } catch (Exception e) {
            notifyGameMessage("Error while attempting build: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Venda acionada pela View
    public void attemptSell(final int boardIndex) {
        ensureGameStarted();
        try {
            final int currentPlayer = gameAPI.getCurrentPlayerIndex();
            final String name = gameAPI.getSquareName(boardIndex);

            gameAPI.sellAtIndex(boardIndex);

            notifyGameMessage(gameAPI.getPlayerName(currentPlayer) + " sold " + name);
            notifyPropertySold(currentPlayer);
            notifyPropertyDataUpdated(gameAPI.getCurrentPlayerPropertyData());

        } catch (Exception e) {
            notifyGameMessage("Error while attempting sell: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Define valores mockados para os dados (modo de teste).
     * Quando definidos, o próximo rollDiceAndPlay usará estes valores.
     */
    public void setMockedDiceValues(int dice1, int dice2) {
        if (dice1 < 1 || dice1 > 6 || dice2 < 1 || dice2 > 6) {
            throw new IllegalArgumentException("Dice values must be between 1 and 6");
        }
        this.mockedDice1 = dice1;
        this.mockedDice2 = dice2;
    }
    
    /**
     * Remove valores mockados dos dados (volta ao modo normal/aleatório).
     */
    public void clearMockedDiceValues() {
        this.mockedDice1 = null;
        this.mockedDice2 = null;
    }
    
    /**
     * Verifica se há valores mockados definidos.
     */
    private boolean hasMockedDiceValues() {
        return mockedDice1 != null && mockedDice2 != null;
    }
}
