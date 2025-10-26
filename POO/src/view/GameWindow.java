/* ===========================================================
 * GameWindow ; janela principal do jogo.
 * Contém o tabuleiro, controles e informações do jogo.
 * =========================================================== */

package view;

import controller.GameController;
import controller.GameObserver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Janela principal do jogo que exibe o tabuleiro e controles.
 * Implementa GameObserver para receber notificações do Controller.
 */
public class GameWindow extends JFrame implements GameObserver {
    
    private static final long serialVersionUID = 1L;
    
    // Dimensões da janela
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 800;
    
    private final GameController controller;
    private BoardPanel boardPanel;
    private JTextArea logArea;
    private JPanel bottomPanel;  // Referência para mostrar/ocultar
    private JLabel currentPlayerLabel;
    private JLabel diceLabel;
    private JLabel moneyLabel;
    private boolean logVisible = true;  // Estado do log
    
    public GameWindow(GameController controller, int numberOfPlayers) {
        this.controller = controller;
        this.controller.addObserver(this); // Registra como observador
        initializeUI(numberOfPlayers);
    }
    
    private void initializeUI(int numberOfPlayers) {
        setTitle("Monopoly Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Layout principal
        setLayout(new BorderLayout(10, 10));
        
        // Painel do tabuleiro (centro)
        boardPanel = new BoardPanel();
        boardPanel.setNumberOfPlayers(numberOfPlayers);  // Define número de jogadores
        add(boardPanel, BorderLayout.CENTER);
        
        // Painel lateral direito (informações, controles e log)
        JPanel sidePanel = createSidePanel();
        add(sidePanel, BorderLayout.EAST);
    }
    
    /**
     * Cria o painel lateral com controles e informações.
     */
    private JPanel createSidePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(250, WINDOW_HEIGHT));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Informações do jogador atual
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createTitledBorder("Current Turn"));
        infoPanel.setPreferredSize(new Dimension(230, 115));

        currentPlayerLabel = new JLabel("Player 1");
        currentPlayerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        currentPlayerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        diceLabel = new JLabel("Dice: -");
        diceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        diceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
    moneyLabel = new JLabel("Money: -");
    moneyLabel.setFont(new Font("Arial", Font.PLAIN, 14));
    moneyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(currentPlayerLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(diceLabel);
    infoPanel.add(Box.createVerticalStrut(6));
    infoPanel.add(moneyLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        
        // Botões de controle
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        JButton rollButton = createStyledButton("Roll Dice", new Color(34, 139, 34));
        rollButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.rollDiceAndPlay();
            }
        });

        JButton endTurnButton = createStyledButton("End Turn", new Color(70, 130, 180));
        endTurnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.endTurn();
            }
        });
        
        JButton toggleLogButton = createStyledButton("Toggle Log", new Color(180, 180, 180));
        toggleLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logVisible = !logVisible;
                bottomPanel.setVisible(logVisible);
                revalidate();
                repaint();
            }
        });
        
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(rollButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(endTurnButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(toggleLogButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        
        // Log de eventos (vertical)
        bottomPanel = createLogPanel();
        
        panel.add(infoPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(buttonPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(bottomPanel);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Cria o painel de log de eventos.
     */
    private JPanel createLogPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Event Log"));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        logArea.setBackground(new Color(250, 250, 250));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Cria um botão estilizado.
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        return button;
    }
    
    /**
     * Adiciona uma mensagem ao log.
     */
    private void addToLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    // ========== Implementação de GameObserver ==========
    
    @Override
    public void onTurnStarted(int playerIndex, String playerName, String playerColor, int playerMoney) {
        currentPlayerLabel.setText(playerName);
        addToLog("=== Turn of " + playerName + " ===");
        diceLabel.setText("Dice: -");
        moneyLabel.setText("Money: $" + playerMoney);
        // Aplica cor do jogador ao texto (identificação visual)
        if (playerColor != null) {
            Color c = parseColor(playerColor);
            if (c != null) currentPlayerLabel.setForeground(c);
        }
    }

    private Color parseColor(String colorStr) {
        switch (colorStr.toLowerCase()) {
            case "red": return Color.RED;
            case "blue": return Color.BLUE;
            case "green": return Color.GREEN;
            case "yellow": return Color.YELLOW;
            case "purple": return new Color(128, 0, 128);
            case "orange": return new Color(255, 165, 0);
            default:
                // Try hex like #RRGGBB
                try {
                    return Color.decode(colorStr);
                } catch (Exception ex) {
                    return null;
                }
        }
    }
    
    @Override
    public void onDiceRolled(int dice1, int dice2, boolean isDouble) {
        String doubleText = isDouble ? " (DOUBLE!)" : "";
        diceLabel.setText("Dice: " + dice1 + " + " + dice2 + doubleText);
        addToLog("Dice rolled: " + dice1 + " and " + dice2 + doubleText);
        boardPanel.setDiceValues(dice1, dice2);
    }
    
    @Override
    public void onPlayerMoved(int playerIndex, int fromPosition, int toPosition) {
        addToLog("Player moved from position " + fromPosition + " to " + toPosition);
        boardPanel.movePlayer(playerIndex, toPosition);
    }
    
    @Override
    public void onSquareLanded(int playerIndex, int squareIndex, String squareName, String squareType) {
        addToLog("Player landed on: " + squareName + " // Position: " + squareIndex + " // Type: " + squareType);
    }

    @Override
    public void onChanceSquareLand(int playerIndex, int cardIndex) {
        addToLog("Chance card drawn: index=" + cardIndex);
        boardPanel.setCard(cardIndex);
        // Refresh player's money in the UI when a chance card is drawn
        try {
            int money = controller.getPlayerMoney(playerIndex);
            if (moneyLabel != null) moneyLabel.setText("Money: " + money);
        } catch (Exception ex) {
            addToLog("Failed to refresh money: " + ex.getMessage());
        }
    }

    @Override
    public void onStreetOwnableLand(int playerIndex, String propertyName) {
        addToLog("Street ownable landed: " + propertyName + " (player=" + playerIndex + ")");
        boardPanel.setPropertyInfo(propertyName, "street");
    }

    @Override
    public void onCompanyOwnableLand(int playerIndex, String companyName) {
        addToLog("Company ownable landed: " + companyName + " (player=" + playerIndex + ")");
        boardPanel.setPropertyInfo(companyName, "company");
    }
    
    @Override
    public void onTurnEnded() {
        boardPanel.setCard(-1);
        boardPanel.setPropertyInfo(null, null);
        if (moneyLabel != null) moneyLabel.setText("Money: -");
        addToLog("Turn ended.\n");
    }
    
    @Override
    public void onGameMessage(String message) {
        addToLog(message);
    }
}
