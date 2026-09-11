package com.numberblocksmerge.theme;

import android.graphics.Color;
import java.util.Map;

public class Theme {
    public final String id;
    public final String name;
    public final int backgroundColor;
    public final int boardColor;
    public final int emptyCellColor;
    public final int hudCardColor;
    public final int textPrimaryColor;
    public final int textSecondaryColor;
    public final Map<Integer, Integer> tileColors;

    public Theme(String id, String name, int backgroundColor, int boardColor, int emptyCellColor,
                 int hudCardColor, int textPrimaryColor, int textSecondaryColor, Map<Integer, Integer> tileColors) {
        this.id = id;
        this.name = name;
        this.backgroundColor = backgroundColor;
        this.boardColor = boardColor;
        this.emptyCellColor = emptyCellColor;
        this.hudCardColor = hudCardColor;
        this.textPrimaryColor = textPrimaryColor;
        this.textSecondaryColor = textSecondaryColor;
        this.tileColors = tileColors;
    }

    public int getTileColor(int value) {
        Integer c = tileColors.get(value);
        if (c != null) return c;
        return Color.parseColor("#2C2B28");
    }

    public int getTextColor(int value) {
        int tileColor = getTileColor(value);
        double luminance = (0.299 * Color.red(tileColor) + 0.587 * Color.green(tileColor) + 0.114 * Color.blue(tileColor)) / 255.0;
        return luminance > 0.52 ? Color.parseColor("#1E2024") : Color.parseColor("#FAF9F6");
    }
}
