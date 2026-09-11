package com.numberblocksmerge.theme;

import android.graphics.Color;
import java.util.HashMap;
import java.util.Map;

public class ThemeManager {
    private static ThemeManager instance;
    private final Map<String, Theme> themes = new HashMap<>();
    private Theme currentTheme;

    private ThemeManager() {
        initThemes();
        currentTheme = themes.get("alabaster");
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    private void initThemes() {
        // 1. Alabaster Minimal (Default - Warm Limestone & Tactile Stone)
        Map<Integer, Integer> alabasterTiles = new HashMap<>();
        alabasterTiles.put(2, Color.parseColor("#EFECE6"));
        alabasterTiles.put(4, Color.parseColor("#E4DFD5"));
        alabasterTiles.put(8, Color.parseColor("#D9D0C1"));
        alabasterTiles.put(16, Color.parseColor("#CFC3B0"));
        alabasterTiles.put(32, Color.parseColor("#C2B49E"));
        alabasterTiles.put(64, Color.parseColor("#B3A28B"));
        alabasterTiles.put(128, Color.parseColor("#9E8B72"));
        alabasterTiles.put(256, Color.parseColor("#87745B"));
        alabasterTiles.put(512, Color.parseColor("#705E47"));
        alabasterTiles.put(1024, Color.parseColor("#5A4A35"));
        alabasterTiles.put(2048, Color.parseColor("#2C2B28"));
        alabasterTiles.put(4096, Color.parseColor("#1C1B18"));
        alabasterTiles.put(8192, Color.parseColor("#12110E"));
        alabasterTiles.put(16384, Color.parseColor("#080807"));
        themes.put("alabaster", new Theme("alabaster", "Alabaster Minimal",
                Color.parseColor("#FAF9F6"), Color.parseColor("#E8E6E0"),
                Color.parseColor("#DDDBCF"), Color.parseColor("#F0EFEA"),
                Color.parseColor("#1E2024"), Color.parseColor("#737680"), alabasterTiles));

        // 2. Titanium Slate (Cool Architectural Neutral)
        Map<Integer, Integer> titaniumTiles = new HashMap<>();
        titaniumTiles.put(2, Color.parseColor("#E8EAEF"));
        titaniumTiles.put(4, Color.parseColor("#D9DCE3"));
        titaniumTiles.put(8, Color.parseColor("#C7CBD4"));
        titaniumTiles.put(16, Color.parseColor("#B4B9C4"));
        titaniumTiles.put(32, Color.parseColor("#9EA4B3"));
        titaniumTiles.put(64, Color.parseColor("#868D9E"));
        titaniumTiles.put(128, Color.parseColor("#6E768A"));
        titaniumTiles.put(256, Color.parseColor("#586175"));
        titaniumTiles.put(512, Color.parseColor("#434C61"));
        titaniumTiles.put(1024, Color.parseColor("#31394D"));
        titaniumTiles.put(2048, Color.parseColor("#1C212E"));
        titaniumTiles.put(4096, Color.parseColor("#121620"));
        titaniumTiles.put(8192, Color.parseColor("#0C0E14"));
        titaniumTiles.put(16384, Color.parseColor("#06070A"));
        themes.put("titanium", new Theme("titanium", "Titanium Slate",
                Color.parseColor("#F2F3F5"), Color.parseColor("#DFE2E8"),
                Color.parseColor("#CFD3DB"), Color.parseColor("#E7E9ED"),
                Color.parseColor("#191B1F"), Color.parseColor("#717580"), titaniumTiles));

        // 3. Nordic Clay (Warm Organic Sandstone)
        Map<Integer, Integer> nordicTiles = new HashMap<>();
        nordicTiles.put(2, Color.parseColor("#EEE7DC"));
        nordicTiles.put(4, Color.parseColor("#DFD5C7"));
        nordicTiles.put(8, Color.parseColor("#D0C1AF"));
        nordicTiles.put(16, Color.parseColor("#BFAD97"));
        nordicTiles.put(32, Color.parseColor("#AC977F"));
        nordicTiles.put(64, Color.parseColor("#968168"));
        nordicTiles.put(128, Color.parseColor("#816C54"));
        nordicTiles.put(256, Color.parseColor("#6C5840"));
        nordicTiles.put(512, Color.parseColor("#584530"));
        nordicTiles.put(1024, Color.parseColor("#453522"));
        nordicTiles.put(2048, Color.parseColor("#291E12"));
        nordicTiles.put(4096, Color.parseColor("#1A1208"));
        nordicTiles.put(8192, Color.parseColor("#100A04"));
        nordicTiles.put(16384, Color.parseColor("#050301"));
        themes.put("nordic", new Theme("nordic", "Nordic Clay",
                Color.parseColor("#F8F6F2"), Color.parseColor("#E4DDD2"),
                Color.parseColor("#D6CDBD"), Color.parseColor("#ECE7DF"),
                Color.parseColor("#242220"), Color.parseColor("#7A7570"), nordicTiles));

        // 4. Charcoal Slate (Architectural Dark, Matte Graphite, Zero Purple)
        Map<Integer, Integer> graphiteTiles = new HashMap<>();
        graphiteTiles.put(2, Color.parseColor("#363942"));
        graphiteTiles.put(4, Color.parseColor("#424652"));
        graphiteTiles.put(8, Color.parseColor("#535866"));
        graphiteTiles.put(16, Color.parseColor("#666C7D"));
        graphiteTiles.put(32, Color.parseColor("#7B8296"));
        graphiteTiles.put(64, Color.parseColor("#8F97AE"));
        graphiteTiles.put(128, Color.parseColor("#A6ADC4"));
        graphiteTiles.put(256, Color.parseColor("#BCC3DB"));
        graphiteTiles.put(512, Color.parseColor("#D0D6EC"));
        graphiteTiles.put(1024, Color.parseColor("#E2E6F5"));
        graphiteTiles.put(2048, Color.parseColor("#FFFFFF"));
        graphiteTiles.put(4096, Color.parseColor("#CFCFD6"));
        graphiteTiles.put(8192, Color.parseColor("#9E9EA8"));
        graphiteTiles.put(16384, Color.parseColor("#72727D"));
        themes.put("graphite", new Theme("graphite", "Charcoal Slate",
                Color.parseColor("#1C1E22"), Color.parseColor("#23252A"),
                Color.parseColor("#2D3037"), Color.parseColor("#25282E"),
                Color.parseColor("#EDEDF0"), Color.parseColor("#8C909C"), graphiteTiles));

        // Aliases for backward compatibility
        themes.put("light", themes.get("alabaster"));
        themes.put("dark", themes.get("graphite"));
        themes.put("neon", themes.get("titanium"));
        themes.put("pastel", themes.get("nordic"));
    }

    public Theme getCurrentTheme() { return currentTheme; }
    public void setTheme(String themeId) {
        if (themes.containsKey(themeId)) {
            currentTheme = themes.get(themeId);
        }
    }
}
