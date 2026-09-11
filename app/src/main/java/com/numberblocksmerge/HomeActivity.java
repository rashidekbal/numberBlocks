package com.numberblocksmerge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.numberblocksmerge.ads.AdsManager;
import com.numberblocksmerge.audio.HapticManager;
import com.numberblocksmerge.audio.SoundManager;
import com.numberblocksmerge.engine.GameSnapshot;
import com.numberblocksmerge.storage.PreferencesManager;
import com.numberblocksmerge.theme.Theme;
import com.numberblocksmerge.theme.ThemeManager;
import com.numberblocksmerge.ui.DialogHelper;

public class HomeActivity extends AppCompatActivity {
    private PreferencesManager prefs;
    private SoundManager soundManager;
    private HapticManager hapticManager;
    private ThemeManager themeManager;
    private AdsManager adsManager;
    private final Gson gson = new Gson();

    private View rootLayout;
    private View cardResume;
    private TextView tvResumeBadge, tvResumeScore;
    private TextView tvBest4, tvBest5, tvBest6;
    private ImageButton btnSound;

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

        // Views
        rootLayout = findViewById(R.id.home_root);
        cardResume = findViewById(R.id.card_resume);
        tvResumeBadge = findViewById(R.id.tv_resume_mode_badge);
        tvResumeScore = findViewById(R.id.tv_resume_score);
        Button btnResumeRun = findViewById(R.id.btn_resume_run);

        tvBest4 = findViewById(R.id.tv_best_4);
        tvBest5 = findViewById(R.id.tv_best_5);
        tvBest6 = findViewById(R.id.tv_best_6);

        View cardMode4 = findViewById(R.id.card_mode_4);
        View cardMode5 = findViewById(R.id.card_mode_5);
        View cardMode6 = findViewById(R.id.card_mode_6);

        btnSound = findViewById(R.id.btn_home_sound);
        ImageButton btnSettings = findViewById(R.id.btn_home_settings);
        Button btnStats = findViewById(R.id.btn_home_stats);
        Button btnThemes = findViewById(R.id.btn_home_themes);
        Button btnHelp = findViewById(R.id.btn_home_help);
        ViewGroup bannerContainer = findViewById(R.id.home_banner_container);

        // Mode clicks
        cardMode4.setOnClickListener(v -> onSelectMode(4));
        cardMode5.setOnClickListener(v -> onSelectMode(5));
        cardMode6.setOnClickListener(v -> onSelectMode(6));

        // Resume active game
        btnResumeRun.setOnClickListener(v -> {
            int lastSize = prefs.getLastPlayedSize();
            launchGame(lastSize, true);
        });

        // Quick Controls
        btnSound.setOnClickListener(v -> toggleSound());
        btnSettings.setOnClickListener(v -> showSettingsDialog());
        btnStats.setOnClickListener(v -> showStatsDialog());
        btnThemes.setOnClickListener(v -> showThemeDialog());
        btnHelp.setOnClickListener(v -> showHelpDialog());

        applyTheme();

        // Load banner
        adsManager.loadBanner(this, bannerContainer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboard();
    }

    private void updateDashboard() {
        tvBest4.setText(String.format("Best: %,d", prefs.getBestScore(4)));
        tvBest5.setText(String.format("Best: %,d", prefs.getBestScore(5)));
        tvBest6.setText(String.format("Best: %,d", prefs.getBestScore(6)));

        Theme theme = themeManager.getCurrentTheme();
        // Sound button tint
        btnSound.setImageResource(soundManager.isEnabled()
                ? android.R.drawable.ic_lock_silent_mode_off
                : android.R.drawable.ic_lock_silent_mode);
        btnSound.setColorFilter(theme.textPrimaryColor);

        // Check for active in-progress game
        int lastSize = prefs.getLastPlayedSize();
        String activeState = prefs.getActiveGame(lastSize);
        if (activeState != null) {
            try {
                GameSnapshot snapshot = gson.fromJson(activeState, GameSnapshot.class);
                if (snapshot != null && !snapshot.isOver) {
                    cardResume.setVisibility(View.VISIBLE);
                    String modeName = lastSize == 4 ? "4×4 STANDARD" : lastSize == 5 ? "5×5 EXTENDED" : "6×6 EXPANDED";
                    tvResumeBadge.setText("ACTIVE SESSION • " + modeName);
                    tvResumeScore.setText(String.format("Score: %,d", snapshot.score));
                    return;
                }
            } catch (Exception ignored) {}
        }
        cardResume.setVisibility(View.GONE);
    }

    private void onSelectMode(int size) {
        hapticManager.click();
        soundManager.playMove();

        String activeState = prefs.getActiveGame(size);
        if (activeState != null) {
            try {
                GameSnapshot snapshot = gson.fromJson(activeState, GameSnapshot.class);
                if (snapshot != null && !snapshot.isOver) {
                    DialogHelper.showResumePrompt(this, size, snapshot.score,
                            () -> launchGame(size, true),
                            () -> {
                                prefs.clearActiveGame(size);
                                launchGame(size, false);
                            });
                    return;
                }
            } catch (Exception ignored) {}
        }
        launchGame(size, false);
    }

    private void launchGame(int size, boolean resume) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_BOARD_SIZE, size);
        intent.putExtra(MainActivity.EXTRA_RESUME, resume);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void toggleSound() {
        boolean next = !soundManager.isEnabled();
        soundManager.setEnabled(next);
        prefs.setSoundEnabled(next);
        updateDashboard();
        Toast.makeText(this, "Sound: " + (next ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
    }

    private void applyTheme() {
        Theme theme = themeManager.getCurrentTheme();
        rootLayout.setBackgroundColor(theme.backgroundColor);
    }

    private void showThemeDialog() {
        hapticManager.click();
        DialogHelper.showThemePicker(this, prefs.getTheme(), themeKey -> {
            themeManager.setTheme(themeKey);
            prefs.setTheme(themeKey);
            applyTheme();
        });
    }

    private void showSettingsDialog() {
        hapticManager.click();
        DialogHelper.showSettings(this, soundManager.isEnabled(), hapticManager.isEnabled(),
                new DialogHelper.SettingsListener() {
                    @Override
                    public void onSoundToggled(boolean enabled) {
                        soundManager.setEnabled(enabled);
                        prefs.setSoundEnabled(enabled);
                        updateDashboard();
                    }

                    @Override
                    public void onHapticsToggled(boolean enabled) {
                        hapticManager.setEnabled(enabled);
                        prefs.setHapticsEnabled(enabled);
                    }

                    @Override
                    public void onChangeThemeRequested() {
                        showThemeDialog();
                    }
                });
    }

    private void showStatsDialog() {
        hapticManager.click();
        DialogHelper.showCareerStats(this, prefs.getGamesPlayed(), prefs.getHighestTile(),
                prefs.getBestScore(4), prefs.getBestScore(5), prefs.getBestScore(6));
    }

    private void showHelpDialog() {
        hapticManager.click();
        DialogHelper.showTutorial(this);
    }
}
