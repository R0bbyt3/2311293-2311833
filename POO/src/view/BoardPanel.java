/* ===========================================================
 * BoardPanel ; painel customizado para renderizar o tabuleiro.
 * Usa Graphics2D para desenhar o tabuleiro e os componentes do jogo.
 * =========================================================== */

package view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.awt.Rectangle;

/**
 * Painel customizado que renderiza o tabuleiro do Monopoly.
 * Utiliza Graphics2D para desenhar casas, jogadores e dados.
 */
public class BoardPanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    // Dimensões do painel
    private static final int PANEL_WIDTH = 1000;
    private static final int PANEL_HEIGHT = 800;
    
    // Configuração geométrica do tabuleiro (imagem 700x700)
    private static final int TOTAL_SQUARES = 40; // casas no tabuleiro
    private static final int BOARD_SIZE   = 700;   // lado da imagem
    private static final int CORNER_SIZE  = 100;   // 4 cantos 100x100
    private static final double EDGE_STEP = (BOARD_SIZE - 2.0 * CORNER_SIZE) / 9.0; // 500/9

    // SQUARE_SIZE fica obsoleto para a imagem real; mantenho só para fallback procedural, se quiser.
    private static final int SQUARE_SIZE = 64; // usado apenas no fallback sem imagem

    
    // Posições dos jogadores
    private final int[] playerPositions;
    private final Color[] playerColors;
    private int numberOfPlayers = 0;  // Número real de jogadores ativos
    
    // Valores dos dados
    private int dice1 = 0;
    private int dice2 = 0;
    
    // Cache de imagens
    private Map<String, BufferedImage> imageCache;
    
    public BoardPanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(200, 230, 200)); // Verde claro
        
        // Inicializa posições dos jogadores (máximo 6)
        playerPositions = new int[6];
        playerColors = new Color[] {
            Color.RED,
            Color.BLUE,
            new Color(0, 128, 0), // Verde
            Color.YELLOW,
            new Color(128, 0, 128), // Roxo
            Color.ORANGE
        };
        
        // Inicializa cache de imagens
        imageCache = new HashMap<>();
        loadImages();
    }
    
    /**
     * Carrega as imagens dos assets.
     */
    private void loadImages() {
        try {
            // Carrega tabuleiro
            File boardFile = new File("src/view/assets/tabuleiro.png");
            if (boardFile.exists()) {
                imageCache.put("board", ImageIO.read(boardFile));
                System.out.println("Board: " + boardFile.getAbsolutePath());
            } else {
                System.err.println("Board not found: " + boardFile.getAbsolutePath());
            }
            
            // Carrega dados (1-6)
            for (int i = 1; i <= 6; i++) {
                File diceFile = new File("src/view/assets/dados/die_face_" + i + ".png");
                if (diceFile.exists()) {
                    imageCache.put("dice" + i, ImageIO.read(diceFile));
                    System.out.println("Dice " + i + " loaded");
                }
            }
            
            // Carrega piões (0-5)
            for (int i = 0; i < 6; i++) {
                File pinFile = new File("src/view/assets/pinos/pin" + i + ".png");
                if (pinFile.exists()) {
                    imageCache.put("pin" + i, ImageIO.read(pinFile));
                    System.out.println("Pin " + i + " loaded");
                }
            }

            System.out.println("Total images loaded: " + imageCache.size());

        } catch (IOException e) {
            System.err.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Atualiza a posição de um jogador.
     */
    public void movePlayer(int playerIndex, int position) {
        if (playerIndex >= 0 && playerIndex < playerPositions.length) {
            playerPositions[playerIndex] = position % TOTAL_SQUARES;
            
            // Atualiza o número de jogadores se necessário
            if (playerIndex >= numberOfPlayers) {
                numberOfPlayers = playerIndex + 1;
            }
            
            repaint();
        }
    }
    
    /**
     * Define o número de jogadores no jogo.
     */
    public void setNumberOfPlayers(int count) {
        if (count >= 0 && count <= 6) {
            this.numberOfPlayers = count;
            repaint();
        }
    }
    
    /**
     * Define os valores dos dados exibidos.
     */
    public void setDiceValues(int d1, int d2) {
        this.dice1 = d1;
        this.dice2 = d2;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Ativa anti-aliasing para melhor qualidade
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Centraliza o tabuleiro
        int offsetX = (PANEL_WIDTH - BOARD_SIZE) / 2;
        int offsetY = (PANEL_HEIGHT - BOARD_SIZE) / 2;
        
        // Desenha o tabuleiro
        drawBoard(g2d, offsetX, offsetY);
        
        // Desenha os jogadores
        drawPlayers(g2d, offsetX, offsetY);
        
        // Desenha os dados
        drawDice(g2d, offsetX, offsetY);
    }
    
    /**
     * Desenha o tabuleiro (40 casas em formato quadrado).
     */
    private void drawBoard(Graphics2D g2d, int offsetX, int offsetY) {
        // Usa a imagem do tabuleiro se disponível
        if (imageCache.containsKey("board")) {
            BufferedImage boardImg = imageCache.get("board");
            // Desenha a imagem do tabuleiro ajustada ao tamanho
            g2d.drawImage(boardImg, offsetX, offsetY, BOARD_SIZE, BOARD_SIZE, null);
        } else {
            // Fallback: Desenha o tabuleiro proceduralmente
            
            // Fundo central
            g2d.setColor(new Color(220, 255, 220));
            int centerSize = BOARD_SIZE - 2 * SQUARE_SIZE;
            g2d.fillRect(offsetX + SQUARE_SIZE, offsetY + SQUARE_SIZE, centerSize, centerSize);
            
            // Desenha as 40 casas
            for (int i = 0; i < TOTAL_SQUARES; i++) {
                Point pos = getSquarePosition(i, offsetX, offsetY);
                drawSquare(g2d, pos.x, pos.y, i);
            }
        }
    }
    
    /**
     * Desenha uma casa individual do tabuleiro.
     */
    private void drawSquare(Graphics2D g2d, int x, int y, int squareIndex) {
        // Borda da casa
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
        
        // Cor de fundo varia por posição
        Color bgColor = getSquareColor(squareIndex);
        g2d.setColor(bgColor);
        g2d.fillRect(x + 1, y + 1, SQUARE_SIZE - 2, SQUARE_SIZE - 2);
        
        // Número da casa
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        String label = String.valueOf(squareIndex);
        FontMetrics fm = g2d.getFontMetrics();
        int labelWidth = fm.stringWidth(label);
        g2d.drawString(label, x + (SQUARE_SIZE - labelWidth) / 2, y + SQUARE_SIZE - 5);
    }
    
    /**
     * Retorna a cor de fundo de uma casa baseada em sua posição.
     */
    private Color getSquareColor(int index) {
        // Casas especiais
        if (index == 0) return new Color(255, 200, 200); // Início (vermelho claro)
        if (index == 10) return new Color(220, 220, 220); // Prisão (cinza)
        if (index == 20) return new Color(255, 255, 200); // Estacionamento (amarelo claro)
        if (index == 30) return new Color(200, 200, 255); // Vá para prisão (azul claro)
        
        // Casas normais (tons variados)
        int group = (index / 10) % 4;
        switch (group) {
            case 0: return new Color(255, 240, 240);
            case 1: return new Color(240, 255, 240);
            case 2: return new Color(240, 240, 255);
            case 3: return new Color(255, 255, 240);
            default: return Color.WHITE;
        }
    }

    /**
     * Retorna o retângulo (x,y,w,h) exato da casa idx (0..39),
     * alinhado à imagem 700x700 com cantos 100x100 e passos 500/9.
     */
    private Rectangle getCellRect(int idx, int offsetX, int offsetY) {
        final int BS = BOARD_SIZE;
        final int C  = CORNER_SIZE;
        final double STEP = EDGE_STEP;

        // Cantos (quadrados 100x100)
        if (idx == 0)  return new Rectangle(offsetX + BS - C, offsetY + BS - C, C, C); // inf-dir
        if (idx == 10) return new Rectangle(offsetX,          offsetY + BS - C, C, C); // inf-esq
        if (idx == 20) return new Rectangle(offsetX,          offsetY,          C, C); // sup-esq
        if (idx == 30) return new Rectangle(offsetX + BS - C, offsetY,          C, C); // sup-dir

        // Lado inferior (1..9) — direita -> esquerda
        if (idx < 10) {
            int k = 10 - idx;                // k = 9..1 (distância a partir do canto direito)
            int x1 = offsetX + C + (int)Math.round((k - 1) * STEP);
            int x2 = offsetX + C + (int)Math.round(k * STEP);
            int y  = offsetY + BS - C;
            return new Rectangle(x1, y, x2 - x1, C);
        }

        // Lado esquerdo (11..19) — baixo -> cima
        if (idx < 20) {
            int k = 20 - idx;                // k = 9..1 (de baixo pra cima)
            int y1 = offsetY + C + (int)Math.round((k - 1) * STEP);
            int y2 = offsetY + C + (int)Math.round(k * STEP);
            int x  = offsetX;
            return new Rectangle(x, y1, C, y2 - y1);
        }

        // Lado superior (21..29) — esquerda -> direita
        if (idx < 30) {
            int k = idx - 20;                // k = 1..9
            int x1 = offsetX + C + (int)Math.round((k - 1) * STEP);
            int x2 = offsetX + C + (int)Math.round(k * STEP);
            int y  = offsetY;
            return new Rectangle(x1, y, x2 - x1, C);
        }

        // Lado direito (31..39) — cima -> baixo
        int k = idx - 30;                    // k = 1..9
        int y1 = offsetY + C + (int)Math.round((k - 1) * STEP);
        int y2 = offsetY + C + (int)Math.round(k * STEP);
        int x  = offsetX + BS - C;
        return new Rectangle(x, y1, C, y2 - y1);
    }
    
    private Point getSquarePosition(int squareIndex, int offsetX, int offsetY) {
        Rectangle r = getCellRect(squareIndex, offsetX, offsetY);
        return new Point(r.x, r.y); // canto superior-esquerdo da casa real
    }
    
    /**
     * Desenha os peões dos jogadores centralizados num grid 2x3 dentro da casa real.
     */
    private void drawPlayers(Graphics2D g2d, int offsetX, int offsetY) {
        final int pinSize = 25;
        final int spacing = 5;

        for (int i = 0; i < numberOfPlayers; i++) {
            int idx = playerPositions[i] % TOTAL_SQUARES;
            Rectangle r = getCellRect(idx, offsetX, offsetY);

            // Tamanho do grid 2x3
            int totalGridW = 2 * pinSize + spacing;
            int totalGridH = 3 * pinSize + 2 * spacing;

            // Base centralizada dentro do retângulo da casa
            int baseX = r.x + Math.max(0, (r.width  - totalGridW) / 2);
            int baseY = r.y + Math.max(0, (r.height - totalGridH) / 2);

            // Posição do peão i no grid
            int col = i % 2;        // 0..1
            int row = i / 2;        // 0..2  (até 6 jogadores)

            int px = baseX + col * (pinSize + spacing);
            int py = baseY + row * (pinSize + spacing);

            // Desenha a imagem do pino ou círculo fallback
            BufferedImage pinImg = imageCache.get("pin" + i);
            if (pinImg != null) {
                g2d.drawImage(pinImg, px, py, pinSize, pinSize, null);
            } else {
                g2d.setColor(playerColors[i]);
                g2d.fillOval(px, py, pinSize, pinSize);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(px, py, pinSize, pinSize);
            }
        }
    }


    
    /**
     * Desenha os dados no centro do tabuleiro.
     */
    private void drawDice(Graphics2D g2d, int offsetX, int offsetY) {
        if (dice1 == 0 || dice2 == 0) return;
        
        int centerX = offsetX + BOARD_SIZE / 2;
        int centerY = offsetY + BOARD_SIZE / 2;
        
        int diceSize = 60;
        int spacing = 15;
        
        // Usa imagens dos dados se disponíveis
        if (imageCache.containsKey("dice" + dice1) && imageCache.containsKey("dice" + dice2)) {
            BufferedImage dice1Img = imageCache.get("dice" + dice1);
            BufferedImage dice2Img = imageCache.get("dice" + dice2);
            
            // Desenha o primeiro dado
            g2d.drawImage(dice1Img, centerX - diceSize - spacing/2, centerY - diceSize/2, diceSize, diceSize, null);
            
            // Desenha o segundo dado
            g2d.drawImage(dice2Img, centerX + spacing/2, centerY - diceSize/2, diceSize, diceSize, null);
        } else {
            // Fallback: desenha dados proceduralmente
            drawSingleDice(g2d, centerX - diceSize - spacing/2, centerY - diceSize/2, diceSize, dice1);
            drawSingleDice(g2d, centerX + spacing/2, centerY - diceSize/2, diceSize, dice2);
        }
    }
    
    /**
     * Desenha um único dado com seus pontos.
     */
    private void drawSingleDice(Graphics2D g2d, int x, int y, int size, int value) {
        // Fundo branco com borda preta
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(x, y, size, size, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(x, y, size, size, 10, 10);
        
        // Desenha os pontos baseado no valor
        g2d.setColor(Color.BLACK);
        int dotSize = 8;
        int offset = size / 4;
        
        // Posições padrão (grid 3x3)
        int cx = x + size / 2;
        int cy = y + size / 2;
        int left = x + offset;
        int right = x + size - offset;
        int top = y + offset;
        int bottom = y + size - offset;
        
        // Padrão de pontos para cada valor (índices: 0=canto superior esq, 1=canto superior dir, etc.)
        // Formato: cada linha é [posX, posY] dos pontos a desenhar
        int[][][] dicePatterns = {
            {},  // 0 (não usado)
            {{cx, cy}},  // 1: centro
            {{left, top}, {right, bottom}},  // 2: diagonal
            {{left, top}, {cx, cy}, {right, bottom}},  // 3: diagonal + centro
            {{left, top}, {right, top}, {left, bottom}, {right, bottom}},  // 4: quatro cantos
            {{left, top}, {right, top}, {cx, cy}, {left, bottom}, {right, bottom}},  // 5: quatro cantos + centro
            {{left, top}, {right, top}, {left, cy}, {right, cy}, {left, bottom}, {right, bottom}}  // 6: dois por coluna
        };
        
        // Desenha os pontos do padrão correspondente
        if (value >= 1 && value <= 6) {
            for (int[] dot : dicePatterns[value]) {
                g2d.fillOval(dot[0] - dotSize/2, dot[1] - dotSize/2, dotSize, dotSize);
            }
        }
    }
}
