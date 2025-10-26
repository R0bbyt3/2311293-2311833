package model.api.dto;

/**
 * Lightweight, UI-agnostic helpers for PlayerColor enum.
 * No references to AWT or other UI libraries here.
 */
public final class PlayerColorUtil {
    private PlayerColorUtil() {}

    public static PlayerColor fromString(String s) {
        if (s == null) return null;
        try {
            return PlayerColor.valueOf(s.trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    public static String toName(PlayerColor c) {
        return c == null ? null : c.name().toLowerCase();
    }
}
