package com.redcodersgroup.numberblocks.theme;

import android.graphics.Color;
import java.util.Map;

public class Theme {
    public final String id;
    public final String name;
    public final boolean isDark;
    public final int backgroundColor;
    public final int boardColor;
    public final int emptyCellColor;
    public final int hudCardColor;
    public final int cardBackgroundColor;
    public final int cardStrokeColor;
    public final int btnSurfaceColor;
    public final int btnStrokeColor;
    public final int textPrimaryColor;
    public final int textSecondaryColor;
    public final Map<Integer, Integer> tileColors;

    public Theme(String id, String name, int backgroundColor, int boardColor, int emptyCellColor,
                 int hudCardColor, int textPrimaryColor, int textSecondaryColor, Map<Integer, Integer> tileColors) {
        this(id, name,
             ((0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(backgroundColor)) / 255.0) < 0.5,
             backgroundColor, boardColor, emptyCellColor, hudCardColor,
             ((0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(backgroundColor)) / 255.0) < 0.5 ? Color.parseColor("#23262F") : Color.parseColor("#FFFFFF"),
             ((0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(backgroundColor)) / 255.0) < 0.5 ? Color.parseColor("#343845") : Color.parseColor("#E4DFD7"),
             ((0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(backgroundColor)) / 255.0) < 0.5 ? Color.parseColor("#2A2E38") : Color.parseColor("#F0EFEA"),
             ((0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(backgroundColor)) / 255.0) < 0.5 ? Color.parseColor("#3D4352") : Color.parseColor("#E3E1D8"),
             textPrimaryColor, textSecondaryColor, tileColors);
    }

    public Theme(String id, String name, boolean isDark, int backgroundColor, int boardColor, int emptyCellColor,
                 int hudCardColor, int cardBackgroundColor, int cardStrokeColor,
                 int btnSurfaceColor, int btnStrokeColor,
                 int textPrimaryColor, int textSecondaryColor, Map<Integer, Integer> tileColors) {
        this.id = id;
        this.name = name;
        this.isDark = isDark;
        this.backgroundColor = backgroundColor;
        this.boardColor = boardColor;
        this.emptyCellColor = emptyCellColor;
        this.hudCardColor = hudCardColor;
        this.cardBackgroundColor = cardBackgroundColor;
        this.cardStrokeColor = cardStrokeColor;
        this.btnSurfaceColor = btnSurfaceColor;
        this.btnStrokeColor = btnStrokeColor;
        this.textPrimaryColor = textPrimaryColor;
        this.textSecondaryColor = textSecondaryColor;
        this.tileColors = tileColors;
    }

    public int getTileColor(int value) {
        Integer c = tileColors.get(value);
        if (c != null) return c;
        int exponent = (int) (Math.log(Math.max(2, value)) / Math.log(2));
        int[] fallbackPalette = new int[] {
            Color.parseColor("#1C1B18"), // Obsidian Charcoal
            Color.parseColor("#0D1B2A"), // Midnight Navy
            Color.parseColor("#1B263B"), // Slate Indigo
            Color.parseColor("#2C1820"), // Imperial Blackberry
            Color.parseColor("#1B382B"), // Deep Forest Emerald
            Color.parseColor("#3D2612")  // Smoked Bronze
        };
        return fallbackPalette[Math.abs(exponent) % fallbackPalette.length];
    }

    public int getTextColor(int value) {
        int tileColor = getTileColor(value);
        double luminance = (0.299 * Color.red(tileColor) + 0.587 * Color.green(tileColor) + 0.114 * Color.blue(tileColor)) / 255.0;
        return luminance > 0.52 ? textPrimaryColor : Color.parseColor("#FFFFFF");
    }
}
