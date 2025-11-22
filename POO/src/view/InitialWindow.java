/* ===========================================================
 * InitialWindow ; janela de configuração inicial do jogo.
 * Permite escolher o número de jogadores (3 a 6).
 * =========================================================== */

package view;

import controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Janela inicial para configuração do jogo.
 * Permite ao usuário escolher o número de jogadores antes de iniciar.
 */
public class InitialWindow extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    private final GameController controller;
    private JComboBox<String> playerCountCombo;
    
    public InitialWindow(GameController controller) {
        this.controller = controller;
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Monopoly - Initial Setup");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null); // Centraliza na tela
        setResizable(false);
        
        // Painel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 248, 255));
        
        // Painel do título
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(240, 248, 255));
        JLabel titleLabel = new JLabel("WELCOME TO MONOPOLY!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(25, 25, 112));
        titlePanel.add(titleLabel);
        
        // Painel de seleção
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        selectionPanel.setBackground(new Color(240, 248, 255));
        
        JLabel selectLabel = new JLabel("Number of players:");
        selectLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        String[] options = {"3", "4", "5", "6"};
        playerCountCombo = new JComboBox<>(options);
        playerCountCombo.setSelectedIndex(1); // Padrão: 4 jogadores
        playerCountCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        
        selectionPanel.add(selectLabel);
        selectionPanel.add(playerCountCombo);
        
        // Painel de botões
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 248, 255));
        
        JButton startButton = new JButton("START GAME");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setBackground(new Color(180, 180, 180));
        startButton.setForeground(Color.BLACK);
        startButton.setFocusPainted(false);
        startButton.setPreferredSize(new Dimension(150, 40));
        
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });
        
        buttonPanel.add(startButton);
        
        // Monta o layout
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(selectionPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * Inicia o jogo com o número de jogadores selecionado.
     */
    private void startGame() {
        try {
            int numberOfPlayers = Integer.parseInt((String) playerCountCombo.getSelectedItem());
            
            // Cria a janela principal (registra como observador) antes de iniciar o jogo
            GameWindow gameWindow = new GameWindow(controller, numberOfPlayers);

            // Inicia o jogo através do controller
            controller.startNewGame(numberOfPlayers);

            // Fecha esta janela e mostra a janela principal
            this.dispose();
            gameWindow.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Error starting game: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Método principal para testar a janela independentemente.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GameController controller = new GameController();
                InitialWindow window = new InitialWindow(controller);
                window.setVisible(true);
            }
        });
    }
}
