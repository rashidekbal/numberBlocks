package com.numberblocksmerge.ui;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.content.res.ColorStateList;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.card.MaterialCardView;
import com.numberblocksmerge.R;

public class DialogHelper {

    public interface ThemeSelectListener {
        void onThemeSelected(String themeKey);
    }

    public interface SettingsListener {
        void onSoundToggled(boolean enabled);
        void onHapticsToggled(boolean enabled);
        void onChangeThemeRequested();
    }

    private static Dialog createBaseDialog(Activity activity, View contentView, boolean cancelable) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(contentView);
        dialog.setCancelable(cancelable);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

    public static Dialog showConfirmExit(Activity activity, Runnable onExit) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_confirm_exit, null);
        Dialog dialog = createBaseDialog(activity, v, true);

        v.findViewById(R.id.btn_dialog_save_exit).setOnClickListener(view -> {
            dialog.dismiss();
            if (onExit != null) onExit.run();
        });

        v.findViewById(R.id.btn_dialog_stay).setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public static Dialog showGameOver(Activity activity, int boardSize, int score, int bestScore,
                                      int highestTile, Runnable onRetry, Runnable onRevive, Runnable onHome) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_game_over, null);
        Dialog dialog = createBaseDialog(activity, v, false);

        TextView tvMode = v.findViewById(R.id.tv_gameover_mode);
        TextView tvScore = v.findViewById(R.id.tv_gameover_score);
        TextView tvBest = v.findViewById(R.id.tv_gameover_best);
        TextView tvTile = v.findViewById(R.id.tv_gameover_highest_tile);

        tvMode.setText(boardSize + "×" + boardSize + (boardSize == 4 ? " STANDARD" : boardSize == 5 ? " EXTENDED" : " EXPANDED"));
        tvScore.setText(String.format("%,d", score));
        tvBest.setText(String.format("%,d", bestScore));
        tvTile.setText("Best Block: " + highestTile);

        v.findViewById(R.id.btn_gameover_retry).setOnClickListener(view -> {
            dialog.dismiss();
            if (onRetry != null) onRetry.run();
        });

        v.findViewById(R.id.btn_gameover_revive).setOnClickListener(view -> {
            dialog.dismiss();
            if (onRevive != null) onRevive.run();
        });

        v.findViewById(R.id.btn_gameover_home).setOnClickListener(view -> {
            dialog.dismiss();
            if (onHome != null) onHome.run();
        });

        dialog.show();
        return dialog;
    }

    public static Dialog showThemePicker(Activity activity, String currentTheme, ThemeSelectListener listener) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_theme_selector, null);
        Dialog dialog = createBaseDialog(activity, v, true);

        MaterialCardView cardAlabaster = v.findViewById(R.id.card_theme_dark);
        MaterialCardView cardTitanium = v.findViewById(R.id.card_theme_light);
        MaterialCardView cardNordic = v.findViewById(R.id.card_theme_neon);
        MaterialCardView cardGraphite = v.findViewById(R.id.card_theme_pastel);

        boolean isAlabaster = "alabaster".equals(currentTheme) || "light".equals(currentTheme);
        boolean isTitanium = "titanium".equals(currentTheme) || "neon".equals(currentTheme);
        boolean isNordic = "nordic".equals(currentTheme) || "pastel".equals(currentTheme);
        boolean isGraphite = "graphite".equals(currentTheme) || "dark".equals(currentTheme);

        cardAlabaster.setStrokeColor(isAlabaster ? Color.parseColor("#1E2024") : Color.parseColor("#D9D7CE"));
        cardTitanium.setStrokeColor(isTitanium ? Color.parseColor("#1E2024") : Color.parseColor("#D9D7CE"));
        cardNordic.setStrokeColor(isNordic ? Color.parseColor("#1E2024") : Color.parseColor("#D9D7CE"));
        cardGraphite.setStrokeColor(isGraphite ? Color.parseColor("#EDEDF0") : Color.parseColor("#353942"));

        cardAlabaster.setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onThemeSelected("alabaster");
        });
        cardTitanium.setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onThemeSelected("titanium");
        });
        cardNordic.setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onThemeSelected("nordic");
        });
        cardGraphite.setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onThemeSelected("graphite");
        });

        v.findViewById(R.id.btn_close_theme).setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public static Dialog showSettings(Activity activity, boolean soundEnabled, boolean hapticsEnabled, SettingsListener listener) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_settings, null);
        Dialog dialog = createBaseDialog(activity, v, true);

        SwitchCompat switchSound = v.findViewById(R.id.switch_sound);
        SwitchCompat switchHaptics = v.findViewById(R.id.switch_haptics);

        switchSound.setChecked(soundEnabled);
        switchHaptics.setChecked(hapticsEnabled);

        switchSound.setOnCheckedChangeListener((btn, isChecked) -> {
            if (listener != null) listener.onSoundToggled(isChecked);
        });

        switchHaptics.setOnCheckedChangeListener((btn, isChecked) -> {
            if (listener != null) listener.onHapticsToggled(isChecked);
        });

        v.findViewById(R.id.btn_settings_change_theme).setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onChangeThemeRequested();
        });

        v.findViewById(R.id.btn_close_settings).setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public static Dialog showCareerStats(Activity activity, int gamesPlayed, int highestTile, int best4, int best5, int best6) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_career_stats, null);
        Dialog dialog = createBaseDialog(activity, v, true);

        TextView tvGames = v.findViewById(R.id.tv_stats_games_played);
        TextView tvHighest = v.findViewById(R.id.tv_stats_highest_tile);
        TextView tvBest4 = v.findViewById(R.id.tv_stats_best_4);
        TextView tvBest5 = v.findViewById(R.id.tv_stats_best_5);
        TextView tvBest6 = v.findViewById(R.id.tv_stats_best_6);

        tvGames.setText(String.valueOf(gamesPlayed));
        tvHighest.setText(String.valueOf(highestTile));
        tvBest4.setText(String.format("%,d", best4));
        tvBest5.setText(String.format("%,d", best5));
        tvBest6.setText(String.format("%,d", best6));

        v.findViewById(R.id.btn_close_stats).setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public static Dialog showMilestone(Activity activity, int milestone) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_milestone, null);
        Dialog dialog = createBaseDialog(activity, v, true);

        TextView tvValue = v.findViewById(R.id.tv_milestone_value);
        tvValue.setText(String.valueOf(milestone));

        v.findViewById(R.id.btn_milestone_continue).setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public static Dialog showTutorial(Activity activity) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_tutorial, null);
        Dialog dialog = createBaseDialog(activity, v, true);

        v.findViewById(R.id.btn_close_tutorial).setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public static Dialog showResumePrompt(Activity activity, int size, int score, Runnable onResume, Runnable onNewGame) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_mode_resume, null);
        Dialog dialog = createBaseDialog(activity, v, true);

        TextView tvTitle = v.findViewById(R.id.tv_resume_mode_title);
        TextView tvScore = v.findViewById(R.id.tv_resume_mode_score);

        tvTitle.setText("Resume " + size + "×" + size + "?");
        tvScore.setText("Score: " + String.format("%,d", score));

        v.findViewById(R.id.btn_dialog_resume_confirm).setOnClickListener(view -> {
            dialog.dismiss();
            if (onResume != null) onResume.run();
        });

        v.findViewById(R.id.btn_dialog_new_game).setOnClickListener(view -> {
            dialog.dismiss();
            if (onNewGame != null) onNewGame.run();
        });

        v.findViewById(R.id.btn_dialog_cancel).setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public static Dialog showRestartConfirm(Activity activity, Runnable onRestart) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_restart, null);
        Dialog dialog = createBaseDialog(activity, v, true);

        v.findViewById(R.id.btn_dialog_restart_confirm).setOnClickListener(view -> {
            dialog.dismiss();
            if (onRestart != null) onRestart.run();
        });

        v.findViewById(R.id.btn_dialog_restart_cancel).setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public interface PauseMenuListener {
        void onSoundToggled(boolean enabled);
        void onVibrationToggled(boolean enabled);
        void onHowToPlayClicked();
        void onRestartClicked();
        void onHomeClicked();
    }

    public static Dialog showPauseMenu(Activity activity, boolean soundEnabled, boolean vibrationEnabled, PauseMenuListener listener) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_pause_menu, null);
        Dialog dialog = createBaseDialog(activity, v, true);

        SwitchCompat switchSound = v.findViewById(R.id.switch_pause_sound);
        SwitchCompat switchVibrate = v.findViewById(R.id.switch_pause_vibration);

        switchSound.setChecked(soundEnabled);
        switchVibrate.setChecked(vibrationEnabled);

        switchSound.setOnCheckedChangeListener((btn, isChecked) -> {
            if (listener != null) listener.onSoundToggled(isChecked);
        });

        switchVibrate.setOnCheckedChangeListener((btn, isChecked) -> {
            if (listener != null) listener.onVibrationToggled(isChecked);
        });

        v.findViewById(R.id.row_pause_how_to_play).setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onHowToPlayClicked();
        });

        v.findViewById(R.id.btn_pause_close).setOnClickListener(view -> dialog.dismiss());
        v.findViewById(R.id.btn_pause_continue).setOnClickListener(view -> dialog.dismiss());

        v.findViewById(R.id.btn_pause_restart).setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onRestartClicked();
        });

        v.findViewById(R.id.btn_pause_home).setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onHomeClicked();
        });

        dialog.show();
        return dialog;
    }

    public static Dialog showFirstTimeInstruction(Activity activity, Runnable onLetsPlay) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_first_time_instruction, null);
        Dialog dialog = createBaseDialog(activity, v, true);

        ViewPager2 vpTutorial = v.findViewById(R.id.vp_tutorial);
        View dot1 = v.findViewById(R.id.dot_1);
        View dot2 = v.findViewById(R.id.dot_2);
        View dot3 = v.findViewById(R.id.dot_3);
        Button btnAction = v.findViewById(R.id.btn_tutorial_action);

        TutorialPagerAdapter adapter = new TutorialPagerAdapter(activity);
        vpTutorial.setAdapter(adapter);

        final int activeColor = Color.parseColor("#00C853");
        final int inactiveColor = Color.parseColor("#D6D6D6");

        Runnable updateDotsAndButton = () -> {
            int current = vpTutorial.getCurrentItem();
            dot1.setBackgroundTintList(ColorStateList.valueOf(current == 0 ? activeColor : inactiveColor));
            dot2.setBackgroundTintList(ColorStateList.valueOf(current == 1 ? activeColor : inactiveColor));
            dot3.setBackgroundTintList(ColorStateList.valueOf(current == 2 ? activeColor : inactiveColor));

            if (current == 2) {
                btnAction.setText("Let's play!");
            } else {
                btnAction.setText("Next");
            }
        };

        vpTutorial.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDotsAndButton.run();
            }
        });

        dot1.setOnClickListener(view -> vpTutorial.setCurrentItem(0, true));
        dot2.setOnClickListener(view -> vpTutorial.setCurrentItem(1, true));
        dot3.setOnClickListener(view -> vpTutorial.setCurrentItem(2, true));

        btnAction.setOnClickListener(view -> {
            int current = vpTutorial.getCurrentItem();
            if (current < 2) {
                vpTutorial.setCurrentItem(current + 1, true);
            } else {
                dialog.dismiss();
                if (onLetsPlay != null) onLetsPlay.run();
            }
        });

        v.findViewById(R.id.btn_tutorial_close).setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    private static class TutorialPagerAdapter extends RecyclerView.Adapter<TutorialViewHolder> {
        private final LayoutInflater inflater;

        TutorialPagerAdapter(Activity activity) {
            this.inflater = LayoutInflater.from(activity);
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public TutorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layoutId;
            if (viewType == 0) {
                layoutId = R.layout.item_tutorial_slide_top_down;
            } else if (viewType == 1) {
                layoutId = R.layout.item_tutorial_slide_left_right;
            } else {
                layoutId = R.layout.item_tutorial_slide_goal;
            }
            View view = inflater.inflate(layoutId, parent, false);
            return new TutorialViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TutorialViewHolder holder, int position) {
            // Layouts statically define the board and guide visuals
        }
    }

    private static class TutorialViewHolder extends RecyclerView.ViewHolder {
        TutorialViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
