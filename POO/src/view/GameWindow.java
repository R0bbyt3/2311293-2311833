/* ===========================================================
 * GameWindow ; janela principal do jogo.
 * Contém o tabuleiro, controles e informações do jogo.
 * =========================================================== */

package view;

import controller.GameController;
import controller.GameObserver;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import model.api.dto.OwnableInfo;
import model.api.dto.Ownables;
import model.api.dto.PlayerColor;
import view.ui.PlayerColorAwt;

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
    
    // Campos para mock de dados (teste)
    private JTextField dice1Field;
    private JTextField dice2Field;
    
    private List<OwnableInfo> currentProps = java.util.List.of(); // Propriedades do jogador atual
    
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
        infoPanel.setPreferredSize(new Dimension(230, 135));
        infoPanel.setMaximumSize(new Dimension(230, 135));

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

        // Botão para ver propriedades do jogador atual
        JButton seePropsBtn = createStyledButton("See Player Properties", new Color(180, 180, 180));
        seePropsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlayerPropertiesWindow dlg = new PlayerPropertiesWindow(GameWindow.this, controller, currentProps);
                dlg.setLocationRelativeTo(GameWindow.this);
                dlg.setVisible(true);
            }
        });
        infoPanel.add(seePropsBtn);
        infoPanel.add(Box.createVerticalStrut(6));


        
        // Painel de Mock de Dados (para testes)
        JPanel diceTestPanel = createDiceTestPanel();
        
        // Botões de controle
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        // Roll button tenta rolagem via controller
        JButton rollButton = createStyledButton("Roll Dice", new Color(180, 180, 180));
        rollButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Verifica se há valores mockados
                Integer d1 = parseDiceValue(dice1Field.getText());
                Integer d2 = parseDiceValue(dice2Field.getText());
                
                if (d1 != null && d2 != null) {
                    // Modo de teste: força valores específicos
                    controller.setMockedDiceValues(d1, d2);
                } else {
                    // Modo normal: valores aleatórios
                    controller.clearMockedDiceValues();
                }
                
                controller.rollDiceAndPlay();
            }
        });

        // Buy button tenta compra via controller
        JButton buyButton = createStyledButton("Buy",new Color(180, 180, 180));
        buyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.attemptBuy();
            }
        });

        // Build button tenta construção via controller
        JButton buildButton = createStyledButton("Build", new Color(180, 180, 180));
        buildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.attemptBuild();
            }
        });

        // End Turn button tenta finalizar turno via controller
        JButton endTurnButton = createStyledButton("End Turn", new Color(180, 180, 180));
        endTurnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.endTurn();
            }
        });
        
        // Toggle Log button mostra/oculta o log de eventos
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
	    buttonPanel.add(buyButton);
	    buttonPanel.add(Box.createVerticalStrut(10));
	    buttonPanel.add(buildButton);
	    buttonPanel.add(Box.createVerticalStrut(10));
	    buttonPanel.add(endTurnButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(toggleLogButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        
        // Log de eventos (vertical)
        bottomPanel = createLogPanel();
        
        panel.add(infoPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(diceTestPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(buttonPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(bottomPanel);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Cria o painel para mock/teste de dados.
     * Permite ao testador forçar valores específicos nos dados.
     */
    private JPanel createDiceTestPanel() {
        JPanel dicePanel = new JPanel();
        dicePanel.setLayout(new BoxLayout(dicePanel, BoxLayout.Y_AXIS));
        dicePanel.setBackground(Color.WHITE);
        dicePanel.setBorder(BorderFactory.createTitledBorder("Dice Value"));
        dicePanel.setPreferredSize(new Dimension(230, 100));
        dicePanel.setMaximumSize(new Dimension(230, 100));

        JLabel instructionLabel = new JLabel("Force dice values (1-6):");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel fieldsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        fieldsPanel.setBackground(Color.WHITE);
        
        JLabel d1Label = new JLabel("D1:");
        dice1Field = new JTextField(2);
        dice1Field.setFont(new Font("Arial", Font.BOLD, 14));
        dice1Field.setHorizontalAlignment(JTextField.CENTER);
        
        JLabel d2Label = new JLabel("D2:");
        dice2Field = new JTextField(2);
        dice2Field.setFont(new Font("Arial", Font.BOLD, 14));
        dice2Field.setHorizontalAlignment(JTextField.CENTER);
        
        fieldsPanel.add(d1Label);
        fieldsPanel.add(dice1Field);
        fieldsPanel.add(d2Label);
        fieldsPanel.add(dice2Field);
        
        JLabel statusLabel = new JLabel("(Empty = random)");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 9));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(Color.GRAY);
        
        dicePanel.add(Box.createVerticalStrut(5));
        dicePanel.add(instructionLabel);
        dicePanel.add(Box.createVerticalStrut(5));
        dicePanel.add(fieldsPanel);
        dicePanel.add(statusLabel);
        dicePanel.add(Box.createVerticalStrut(5));
        
        return dicePanel;
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
    public void onTurnStarted(int playerIndex, String playerName, PlayerColor playerColor, int playerMoney) {
        currentPlayerLabel.setText(playerName);
        diceLabel.setText("Dice: -");
        moneyLabel.setText("Money: $" + playerMoney);
        // Cor do jogador
        if (playerColor != null) {
            Color c = PlayerColorAwt.toColor(playerColor);
            if (c != null) currentPlayerLabel.setForeground(c);
        }
    }

    @Override
    public void onCurrentPlayerPropertyDataUpdated(List<OwnableInfo> items) {
        this.currentProps = (items != null) ? items : List.of();
    }

    @Override
    public void onPropertySold(int playerIndex) {
        handlePlayerMoneyUpdate(playerIndex);
    }

    @Override
    public void onDiceRolled(int dice1, int dice2, boolean isDouble) {
        String doubleText = isDouble ? " (DOUBLE!)" : "";
        diceLabel.setText("Dice: " + dice1 + " + " + dice2 + doubleText);
        boardPanel.setDiceValues(dice1, dice2);
    }
    
    @Override
    public void onPlayerMoved(int playerIndex, int fromPosition, int toPosition) {
        boardPanel.movePlayer(playerIndex, toPosition);
    }
    
    @Override
    public void onSquareLanded(int playerIndex, int squareIndex, String squareName, String squareType) {
    }

    @Override
    public void onChanceSquareLand(int playerIndex, int cardIndex) {
        boardPanel.setCard(cardIndex);
        handlePlayerMoneyUpdate(playerIndex);
    }

    @Override
    public void onStreetOwnableLand(int playerIndex, String propertyName, Ownables.Street streetInfo) {
        boardPanel.setPropertyInfo(propertyName, "street");
        boardPanel.setStreetInfo(streetInfo);
    }

    @Override
    public void onCompanyOwnableLand(int playerIndex, String companyName, Ownables.Company companyInfo) {
        boardPanel.setPropertyInfo(companyName, "company");
        boardPanel.setCompanyInfo(companyInfo);
    }
    
    @Override
    public void onStreetOwnableUpdate(int playerIndex, Ownables.Street streetInfo) {
        boardPanel.setStreetInfo(streetInfo);
        handlePlayerMoneyUpdate(playerIndex);
    }

    @Override
    public void onCompanyOwnableUpdate(int playerIndex, Ownables.Company companyInfo) {
        boardPanel.setCompanyInfo(companyInfo);
        handlePlayerMoneyUpdate(playerIndex);
    }

    /**
     * Helper: centraliza a atualização do label de dinheiro quando receber
     */
    private void handlePlayerMoneyUpdate(int playerIndex) {
        int money = controller.getPlayerMoney(playerIndex);
        if (moneyLabel != null) moneyLabel.setText("Money: $" + money);
    }
    
    @Override
    public void onTurnEnded() {
        boardPanel.setCard(-1);
        boardPanel.setPropertyInfo(null, null);
        if (moneyLabel != null) moneyLabel.setText("Money: $-");
    }
    
    @Override
    public void onGameMessage(String message) {
        addToLog(message);
    }
    
    /**
     * Tenta fazer parse de um valor de dado do campo de texto.
     * Retorna null se o campo estiver vazio ou inválido.
     */
    private Integer parseDiceValue(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            int value = Integer.parseInt(text.trim());
            if (value >= 1 && value <= 6) {
                return value;
            }
        } catch (NumberFormatException e) {
            // Ignora erros de parse
        }
        return null;
    }

}
