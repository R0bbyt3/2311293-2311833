package view.ui;

import java.awt.Color;
import model.api.dto.PlayerColor;

/**
 * Classe utilitária para conversão entre PlayerColor e Color do AWT.
 */

public final class PlayerColorAwt {

    private PlayerColorAwt() {}

    public static Color toColor(PlayerColor c) {
        if (c == null) return null;
        switch (c) {
            case RED: return Color.RED;
            case BLUE: return Color.BLUE;
            case GREEN: return new Color(0, 128, 0);
            case YELLOW: return Color.YELLOW;
            case PURPLE: return new Color(128, 0, 128);
            case ORANGE: return new Color(255, 165, 0);
            default: return Color.BLACK;
        }
    }


}
