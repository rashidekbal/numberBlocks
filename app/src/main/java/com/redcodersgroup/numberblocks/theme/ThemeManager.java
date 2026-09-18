package com.redcodersgroup.numberblocks.theme;

import android.graphics.Color;
import java.util.HashMap;
import java.util.Map;

public class ThemeManager {
    private static ThemeManager instance;
    private final Map<String, Theme> themes = new HashMap<>();
    private Theme currentTheme;

    private ThemeManager() {
        initThemes();
        currentTheme = themes.get("prism");
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    private void initThemes() {
        // 1. Golden Arcade (Flagship Game Palette - Warm Cream, Coral & Radiant Gold)
        Map<Integer, Integer> alabasterTiles = new HashMap<>();
        alabasterTiles.put(2, Color.parseColor("#EEE4DA"));
        alabasterTiles.put(4, Color.parseColor("#EDE0C8"));
        alabasterTiles.put(8, Color.parseColor("#F2B179"));
        alabasterTiles.put(16, Color.parseColor("#F59563"));
        alabasterTiles.put(32, Color.parseColor("#F67C5F"));
        alabasterTiles.put(64, Color.parseColor("#F65E3B"));
        alabasterTiles.put(128, Color.parseColor("#EDCF72"));
        alabasterTiles.put(256, Color.parseColor("#EDCC61"));
        alabasterTiles.put(512, Color.parseColor("#EDC850"));
        alabasterTiles.put(1024, Color.parseColor("#EDC53F"));
        alabasterTiles.put(2048, Color.parseColor("#EDC22E"));
        alabasterTiles.put(4096, Color.parseColor("#3C3A32"));
        alabasterTiles.put(8192, Color.parseColor("#242320"));
        alabasterTiles.put(16384, Color.parseColor("#151412"));
        alabasterTiles.put(32768, Color.parseColor("#F59E0B"));
        alabasterTiles.put(65536, Color.parseColor("#EF4444"));
        alabasterTiles.put(131072, Color.parseColor("#8B5CF6"));
        Theme goldenTheme = new Theme("alabaster", "Golden Classic", false,
                Color.parseColor("#FAF8EF"), Color.parseColor("#BBADA0"),
                Color.parseColor("#CDC1B4"), Color.parseColor("#EDE0C8"),
                Color.parseColor("#FFFFFF"), Color.parseColor("#E8E2D2"),
                Color.parseColor("#EDE0C8"), Color.parseColor("#CDC1B4"),
                Color.parseColor("#776E65"), Color.parseColor("#8F7A66"), alabasterTiles);
        themes.put("alabaster", goldenTheme);
        themes.put("golden", goldenTheme);
        themes.put("golden_classic", goldenTheme);

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
        titaniumTiles.put(32768, Color.parseColor("#0A192F"));
        titaniumTiles.put(65536, Color.parseColor("#1B1A3A"));
        titaniumTiles.put(131072, Color.parseColor("#2A1224"));
        Theme titaniumTheme = new Theme("titanium", "Titanium Slate", false,
                Color.parseColor("#F2F3F5"), Color.parseColor("#DFE2E8"),
                Color.parseColor("#CFD3DB"), Color.parseColor("#DFE2E8"),
                Color.parseColor("#FFFFFF"), Color.parseColor("#CFD3DB"),
                Color.parseColor("#E7E9ED"), Color.parseColor("#D6D9E0"),
                Color.parseColor("#191B1F"), Color.parseColor("#717580"), titaniumTiles);
        themes.put("titanium", titaniumTheme);

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
        nordicTiles.put(32768, Color.parseColor("#1C1608"));
        nordicTiles.put(65536, Color.parseColor("#2E1C0C"));
        nordicTiles.put(131072, Color.parseColor("#3B1812"));
        Theme nordicTheme = new Theme("nordic", "Nordic Clay", false,
                Color.parseColor("#F8F6F2"), Color.parseColor("#E4DDD2"),
                Color.parseColor("#D6CDBD"), Color.parseColor("#E5DDD0"),
                Color.parseColor("#FFFFFF"), Color.parseColor("#D6CDBD"),
                Color.parseColor("#ECE7DF"), Color.parseColor("#DDD7CE"),
                Color.parseColor("#242220"), Color.parseColor("#7A7570"), nordicTiles);
        themes.put("nordic", nordicTheme);

        // 4. Charcoal Slate (High-Contrast Architectural Dark with Luminous Gem Progression)
        Map<Integer, Integer> graphiteTiles = new HashMap<>();
        graphiteTiles.put(2, Color.parseColor("#3D4353"));      // Slate blue-grey (distinct from #2A2E39 empty slot)
        graphiteTiles.put(4, Color.parseColor("#4E5669"));      // Cool steel grey
        graphiteTiles.put(8, Color.parseColor("#D97706"));      // Luminous amber topaz
        graphiteTiles.put(16, Color.parseColor("#EA580C"));     // Electric tangerine
        graphiteTiles.put(32, Color.parseColor("#DC2626"));     // Radiant ruby
        graphiteTiles.put(64, Color.parseColor("#9333EA"));     // Neon amethyst
        graphiteTiles.put(128, Color.parseColor("#0284C7"));    // Electric sapphire
        graphiteTiles.put(256, Color.parseColor("#059669"));    // Luminous emerald
        graphiteTiles.put(512, Color.parseColor("#0891B2"));    // Vibrant cyan
        graphiteTiles.put(1024, Color.parseColor("#C026D3"));   // Cyberpunk fuchsia
        graphiteTiles.put(2048, Color.parseColor("#F59E0B"));   // Crown radiant gold
        graphiteTiles.put(4096, Color.parseColor("#E11D48"));   // Mythic crimson rose
        graphiteTiles.put(8192, Color.parseColor("#38BDF8"));   // Celestial cyan
        graphiteTiles.put(16384, Color.parseColor("#A855F7"));  // Supernova purple
        graphiteTiles.put(32768, Color.parseColor("#10B981"));  // Matrix green
        graphiteTiles.put(65536, Color.parseColor("#F43F5E"));  // Hyper pink
        graphiteTiles.put(131072, Color.parseColor("#FFFFFF")); // Pure starlight
        Theme graphiteTheme = new Theme("graphite", "Charcoal Slate", true,
                Color.parseColor("#181A1F"), Color.parseColor("#20232B"),
                Color.parseColor("#2A2E39"), Color.parseColor("#1F222A"),
                Color.parseColor("#343A48"), Color.parseColor("#4A5266"),
                Color.parseColor("#2A2E38"), Color.parseColor("#3D4352"),
                Color.parseColor("#F1F3F7"), Color.parseColor("#9DA3B4"), graphiteTiles);
        themes.put("graphite", graphiteTheme);
        themes.put("charcoal", graphiteTheme);
        themes.put("dark", graphiteTheme);

        // 5. Prism Pop (Ultra Colorful Spectrum)
        Map<Integer, Integer> prismTiles = new HashMap<>();
        prismTiles.put(2, Color.parseColor("#FEE140"));
        prismTiles.put(4, Color.parseColor("#00E676"));
        prismTiles.put(8, Color.parseColor("#FF9100"));
        prismTiles.put(16, Color.parseColor("#FF3366"));
        prismTiles.put(32, Color.parseColor("#FF007F"));
        prismTiles.put(64, Color.parseColor("#9D4EDD"));
        prismTiles.put(128, Color.parseColor("#3A86FF"));
        prismTiles.put(256, Color.parseColor("#00F5D4"));
        prismTiles.put(512, Color.parseColor("#7000FF"));
        prismTiles.put(1024, Color.parseColor("#FF5400"));
        prismTiles.put(2048, Color.parseColor("#FFBE0B"));
        prismTiles.put(4096, Color.parseColor("#F72585"));
        prismTiles.put(8192, Color.parseColor("#4CC9F0"));
        prismTiles.put(16384, Color.parseColor("#7209B7"));
        Theme prismTheme = new Theme("prism", "Prism Pop", true,
                Color.parseColor("#0B0E17"), Color.parseColor("#171B2B"),
                Color.parseColor("#252B42"), Color.parseColor("#131624"),
                Color.parseColor("#262D47"), Color.parseColor("#3F4B75"),
                Color.parseColor("#1E2337"), Color.parseColor("#323B5C"),
                Color.parseColor("#FFFFFF"), Color.parseColor("#A5B4FC"), prismTiles);
        themes.put("prism", prismTheme);
        themes.put("colorful", prismTheme);

        // Aliases for backward compatibility
        themes.put("light", goldenTheme);
        themes.put("neon", titaniumTheme);
        themes.put("pastel", nordicTheme);
    }

    public Theme getCurrentTheme() { return currentTheme; }
    public void setTheme(String themeId) {
        if (themes.containsKey(themeId)) {
            currentTheme = themes.get(themeId);
        }
    }
}
