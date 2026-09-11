package com.numberblocksmerge;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;
import com.numberblocksmerge.ads.AdsManager;
import com.numberblocksmerge.audio.HapticManager;
import com.numberblocksmerge.audio.SoundManager;
import com.numberblocksmerge.storage.PreferencesManager;
import com.numberblocksmerge.theme.Theme;
import com.numberblocksmerge.theme.ThemeManager;

public class HomeActivity extends AppCompatActivity {
    private PreferencesManager prefs;
    private SoundManager soundManager;
    private HapticManager hapticManager;
    private ThemeManager themeManager;
    private AdsManager adsManager;

    private View rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Core Managers
        prefs = PreferencesManager.getInstance(this);
        soundManager = SoundManager.getInstance();
        soundManager.setEnabled(prefs.isSoundEnabled());
        hapticManager = HapticManager.getInstance();
        hapticManager.init(this);
        hapticManager.setEnabled(prefs.isHapticsEnabled());
        themeManager = ThemeManager.getInstance();
        themeManager.setTheme(prefs.getTheme());
        adsManager = AdsManager.getInstance();
        adsManager.init(this);

        rootLayout = findViewById(R.id.home_root);
        ViewGroup bannerContainer = findViewById(R.id.home_banner_container);

        applyTheme();

        // Initial screen: Only the Play Button
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.home_fragment_container, new StartPlayFragment())
                    .commit();
        }

        // Load banner
        adsManager.loadBanner(this, bannerContainer);
    }

    public void openHomeDashboard() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                                    android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.home_fragment_container, new HomeDashboardFragment())
                .addToBackStack("dashboard")
                .commit();
    }

    public void applyTheme() {
        Theme theme = themeManager.getCurrentTheme();
        if (rootLayout != null) {
            rootLayout.setBackgroundColor(theme.backgroundColor);
        }
    }
}
