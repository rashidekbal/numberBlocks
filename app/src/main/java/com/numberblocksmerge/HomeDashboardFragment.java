package com.numberblocksmerge;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import java.util.Locale;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.gson.Gson;
import com.numberblocksmerge.audio.HapticManager;
import com.numberblocksmerge.audio.SoundManager;
import com.numberblocksmerge.engine.GameSnapshot;
import com.numberblocksmerge.storage.PreferencesManager;
import com.numberblocksmerge.theme.Theme;
import com.numberblocksmerge.theme.ThemeManager;
import com.numberblocksmerge.ui.DialogHelper;

public class HomeDashboardFragment extends Fragment {
    private PreferencesManager prefs;
    private SoundManager soundManager;
    private HapticManager hapticManager;
    private ThemeManager themeManager;
    private final Gson gson = new Gson();

    private View cardResume;
    private TextView tvResumeBadge, tvResumeScore;
    private TextView tvBest4, tvBest5, tvBest6;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = PreferencesManager.getInstance(requireContext());
        soundManager = SoundManager.getInstance();
        hapticManager = HapticManager.getInstance();
        themeManager = ThemeManager.getInstance();

        // Views
        ImageButton btnBack = view.findViewById(R.id.btn_dashboard_back);
        ImageButton btnSettings = view.findViewById(R.id.btn_dashboard_settings);

        cardResume = view.findViewById(R.id.card_dashboard_resume);
        tvResumeBadge = view.findViewById(R.id.tv_dashboard_resume_badge);
        tvResumeScore = view.findViewById(R.id.tv_dashboard_resume_score);
        Button btnResumeRun = view.findViewById(R.id.btn_dashboard_resume_run);

        View cardMode4 = view.findViewById(R.id.card_mode_4);
        View cardMode5 = view.findViewById(R.id.card_mode_5);
        View cardMode6 = view.findViewById(R.id.card_mode_6);

        tvBest4 = view.findViewById(R.id.tv_best_4);
        tvBest5 = view.findViewById(R.id.tv_best_5);
        tvBest6 = view.findViewById(R.id.tv_best_6);

        Button btnStats = view.findViewById(R.id.btn_dashboard_stats);
        Button btnThemes = view.findViewById(R.id.btn_dashboard_themes);
        Button btnHelp = view.findViewById(R.id.btn_dashboard_help);

        // Back to Start Play Screen
        btnBack.setOnClickListener(v -> {
            hapticManager.click();
            getParentFragmentManager().popBackStack();
        });

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
        btnSettings.setOnClickListener(v -> showSettingsDialog());
        btnStats.setOnClickListener(v -> showStatsDialog());
        btnThemes.setOnClickListener(v -> showThemeDialog());

        // Guide opens the same 3-slide first-time tutorial
        btnHelp.setOnClickListener(v -> showHelpDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDashboard();
    }

    private void updateDashboard() {
        if (!isAdded()) return;

        tvBest4.setText(getString(R.string.best_format, String.format(Locale.getDefault(), "%,d", prefs.getBestScore(4))));
        tvBest5.setText(getString(R.string.best_format, String.format(Locale.getDefault(), "%,d", prefs.getBestScore(5))));
        tvBest6.setText(getString(R.string.best_format, String.format(Locale.getDefault(), "%,d", prefs.getBestScore(6))));

        // Check for active in-progress game
        int lastSize = prefs.getLastPlayedSize();
        String activeState = prefs.getActiveGame(lastSize);
        if (activeState != null) {
            try {
                GameSnapshot snapshot = gson.fromJson(activeState, GameSnapshot.class);
                if (snapshot != null && !snapshot.isOver) {
                    cardResume.setVisibility(View.VISIBLE);
                    String modeName = lastSize == 4 ? getString(R.string.mode_4x4_badge) : lastSize == 5 ? getString(R.string.mode_5x5_badge) : getString(R.string.mode_6x6_badge);
                    tvResumeBadge.setText(getString(R.string.active_run_format, modeName));
                    tvResumeScore.setText(getString(R.string.score_format, String.format(Locale.getDefault(), "%,d", snapshot.score)));
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
                    DialogHelper.showResumePrompt(requireActivity(), size, snapshot.score,
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
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_BOARD_SIZE, size);
        intent.putExtra(MainActivity.EXTRA_RESUME, resume);
        startActivity(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requireActivity().overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void showThemeDialog() {
        hapticManager.click();
        DialogHelper.showThemePicker(requireActivity(), prefs.getTheme(), themeKey -> {
            themeManager.setTheme(themeKey);
            prefs.setTheme(themeKey);
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).applyTheme();
            }
        });
    }

    private void showSettingsDialog() {
        hapticManager.click();
        DialogHelper.showSettings(requireActivity(), soundManager.isEnabled(), hapticManager.isEnabled(),
                new DialogHelper.SettingsListener() {
                    @Override
                    public void onSoundToggled(boolean enabled) {
                        soundManager.setEnabled(enabled);
                        prefs.setSoundEnabled(enabled);
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
        DialogHelper.showCareerStats(requireActivity(), prefs.getGamesPlayed(), prefs.getHighestTile(),
                prefs.getBestScore(4), prefs.getBestScore(5), prefs.getBestScore(6));
    }

    private void showHelpDialog() {
        hapticManager.click();
        DialogHelper.showFirstTimeInstruction(requireActivity(), null);
    }
}
