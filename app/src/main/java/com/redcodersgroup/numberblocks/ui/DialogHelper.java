package com.redcodersgroup.numberblocks.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.redcodersgroup.numberblocks.R;
import com.redcodersgroup.numberblocks.databinding.DialogCareerStatsBinding;
import com.redcodersgroup.numberblocks.databinding.DialogConfirmExitBinding;
import com.redcodersgroup.numberblocks.databinding.DialogFirstTimeInstructionBinding;
import com.redcodersgroup.numberblocks.databinding.DialogGameOverBinding;
import com.redcodersgroup.numberblocks.databinding.DialogMilestoneBinding;
import com.redcodersgroup.numberblocks.databinding.DialogModeResumeBinding;
import com.redcodersgroup.numberblocks.databinding.DialogPauseMenuBinding;
import com.redcodersgroup.numberblocks.databinding.DialogRestartBinding;
import com.redcodersgroup.numberblocks.databinding.DialogSettingsBinding;
import com.redcodersgroup.numberblocks.databinding.DialogThemeSelectorBinding;
import com.redcodersgroup.numberblocks.databinding.DialogTutorialBinding;
import com.redcodersgroup.numberblocks.databinding.ItemTutorialSlideGoalBinding;
import com.redcodersgroup.numberblocks.databinding.ItemTutorialSlideLeftRightBinding;
import com.redcodersgroup.numberblocks.databinding.ItemTutorialSlideTopDownBinding;
import java.util.Locale;

public class DialogHelper {

    public interface ThemeSelectListener {
        void onThemeSelected(String themeKey);
    }

    public interface SettingsListener {
        void onSoundToggled(boolean enabled);
        void onHapticsToggled(boolean enabled);
        void onChangeThemeRequested();
        void onInfoClicked();
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
        DialogConfirmExitBinding binding = DialogConfirmExitBinding.inflate(activity.getLayoutInflater());
        Dialog dialog = createBaseDialog(activity, binding.getRoot(), true);

        binding.btnDialogSaveExit.setOnClickListener(view -> {
            dialog.dismiss();
            if (onExit != null) onExit.run();
        });

        binding.btnDialogStay.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public static Dialog showGameOver(Activity activity, int boardSize, int score, int bestScore,
                                      int highestTile, Runnable onRetry, Runnable onRevive, Runnable onHome) {
        DialogGameOverBinding binding = DialogGameOverBinding.inflate(activity.getLayoutInflater());
        Dialog dialog = createBaseDialog(activity, binding.getRoot(), false);

        String suffix = boardSize == 4 ? activity.getString(R.string.mode_4x4_badge)
                : boardSize == 5 ? activity.getString(R.string.mode_5x5_badge)
                : activity.getString(R.string.mode_6x6_badge);
        binding.tvGameoverMode.setText(suffix);
        binding.tvGameoverScore.setText(String.format(Locale.getDefault(), "%,d", score));
        binding.tvGameoverBest.setText(String.format(Locale.getDefault(), "%,d", bestScore));
        binding.tvGameoverHighestTile.setText(activity.getString(R.string.best_block_format, String.format(Locale.getDefault(), "%,d", highestTile)));

        binding.btnGameoverRetry.setOnClickListener(view -> {
            dialog.dismiss();
            if (onRetry != null) onRetry.run();
        });

        binding.btnGameoverRevive.setOnClickListener(view -> {
            dialog.dismiss();
            if (onRevive != null) onRevive.run();
        });

        binding.btnGameoverHome.setOnClickListener(view -> {
            dialog.dismiss();
            if (onHome != null) onHome.run();
        });

        dialog.show();
        return dialog;
    }

    public static Dialog showThemePicker(Activity activity, String currentTheme, ThemeSelectListener listener) {
        DialogThemeSelectorBinding binding = DialogThemeSelectorBinding.inflate(activity.getLayoutInflater());
        Dialog dialog = createBaseDialog(activity, binding.getRoot(), true);

        boolean isAlabaster = "alabaster".equals(currentTheme) || "light".equals(currentTheme);
        boolean isTitanium = "titanium".equals(currentTheme) || "neon".equals(currentTheme);
        boolean isNordic = "nordic".equals(currentTheme) || "pastel".equals(currentTheme);
        boolean isGraphite = "graphite".equals(currentTheme) || "dark".equals(currentTheme);

        binding.cardThemeDark.setStrokeColor(isAlabaster ? Color.parseColor("#1E2024") : Color.parseColor("#D9D7CE"));
        binding.cardThemeLight.setStrokeColor(isTitanium ? Color.parseColor("#1E2024") : Color.parseColor("#D9D7CE"));
        binding.cardThemeNeon.setStrokeColor(isNordic ? Color.parseColor("#1E2024") : Color.parseColor("#D9D7CE"));
        binding.cardThemePastel.setStrokeColor(isGraphite ? Color.parseColor("#EDEDF0") : Color.parseColor("#353942"));

        binding.cardThemeDark.setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onThemeSelected("alabaster");
        });
        binding.cardThemeLight.setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onThemeSelected("titanium");
        });
        binding.cardThemeNeon.setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onThemeSelected("nordic");
        });
        binding.cardThemePastel.setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onThemeSelected("graphite");
        });

        binding.btnCloseTheme.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public static Dialog showSettings(Activity activity, boolean soundEnabled, boolean hapticsEnabled, SettingsListener listener) {
        DialogSettingsBinding binding = DialogSettingsBinding.inflate(activity.getLayoutInflater());
        Dialog dialog = createBaseDialog(activity, binding.getRoot(), true);

        binding.switchSound.setChecked(soundEnabled);
        binding.switchHaptics.setChecked(hapticsEnabled);

        binding.switchSound.setOnCheckedChangeListener((btn, isChecked) -> {
            if (listener != null) listener.onSoundToggled(isChecked);
        });

        binding.switchHaptics.setOnCheckedChangeListener((btn, isChecked) -> {
            if (listener != null) listener.onHapticsToggled(isChecked);
        });

        binding.btnSettingsChangeTheme.setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onChangeThemeRequested();
        });

        binding.btnSettingsInfo.setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onInfoClicked();
        });

        binding.btnCloseSettings.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public static Dialog showCareerStats(Activity activity, int gamesPlayed, int highestTile, int best4, int best5, int best6) {
        DialogCareerStatsBinding binding = DialogCareerStatsBinding.inflate(activity.getLayoutInflater());
        Dialog dialog = createBaseDialog(activity, binding.getRoot(), true);

        binding.tvStatsGamesPlayed.setText(String.format(Locale.getDefault(), "%,d", gamesPlayed));
        binding.tvStatsHighestTile.setText(String.format(Locale.getDefault(), "%,d", highestTile));
        binding.tvStatsBest4.setText(String.format(Locale.getDefault(), "%,d", best4));
        binding.tvStatsBest5.setText(String.format(Locale.getDefault(), "%,d", best5));
        binding.tvStatsBest6.setText(String.format(Locale.getDefault(), "%,d", best6));

        binding.btnCloseStats.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public static Dialog showMilestone(Activity activity, int milestone) {
        DialogMilestoneBinding binding = DialogMilestoneBinding.inflate(activity.getLayoutInflater());
        Dialog dialog = createBaseDialog(activity, binding.getRoot(), true);

        binding.tvMilestoneValue.setText(String.valueOf(milestone));
        binding.btnMilestoneContinue.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public static Dialog showTutorial(Activity activity) {
        DialogTutorialBinding binding = DialogTutorialBinding.inflate(activity.getLayoutInflater());
        Dialog dialog = createBaseDialog(activity, binding.getRoot(), true);

        binding.btnCloseTutorial.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public static Dialog showResumePrompt(Activity activity, int size, int score, Runnable onResume, Runnable onNewGame) {
        DialogModeResumeBinding binding = DialogModeResumeBinding.inflate(activity.getLayoutInflater());
        Dialog dialog = createBaseDialog(activity, binding.getRoot(), true);

        binding.tvResumeModeTitle.setText(activity.getString(R.string.resume_mode_format, size, size));
        binding.tvResumeModeScore.setText(activity.getString(R.string.score_format, String.format(Locale.getDefault(), "%,d", score)));

        binding.btnDialogResumeConfirm.setOnClickListener(view -> {
            dialog.dismiss();
            if (onResume != null) onResume.run();
        });

        binding.btnDialogNewGame.setOnClickListener(view -> {
            dialog.dismiss();
            if (onNewGame != null) onNewGame.run();
        });

        binding.btnDialogCancel.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public static Dialog showRestartConfirm(Activity activity, Runnable onRestart) {
        DialogRestartBinding binding = DialogRestartBinding.inflate(activity.getLayoutInflater());
        Dialog dialog = createBaseDialog(activity, binding.getRoot(), true);

        binding.btnDialogRestartConfirm.setOnClickListener(view -> {
            dialog.dismiss();
            if (onRestart != null) onRestart.run();
        });

        binding.btnDialogRestartCancel.setOnClickListener(view -> dialog.dismiss());

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
        DialogPauseMenuBinding binding = DialogPauseMenuBinding.inflate(activity.getLayoutInflater());
        Dialog dialog = createBaseDialog(activity, binding.getRoot(), true);

        binding.switchPauseSound.setChecked(soundEnabled);
        binding.switchPauseVibration.setChecked(vibrationEnabled);

        binding.switchPauseSound.setOnCheckedChangeListener((btn, isChecked) -> {
            if (listener != null) listener.onSoundToggled(isChecked);
        });

        binding.switchPauseVibration.setOnCheckedChangeListener((btn, isChecked) -> {
            if (listener != null) listener.onVibrationToggled(isChecked);
        });

        binding.rowPauseHowToPlay.setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onHowToPlayClicked();
        });

        binding.btnPauseClose.setOnClickListener(view -> dialog.dismiss());
        binding.btnPauseContinue.setOnClickListener(view -> dialog.dismiss());

        binding.btnPauseRestart.setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onRestartClicked();
        });

        binding.btnPauseHome.setOnClickListener(view -> {
            dialog.dismiss();
            if (listener != null) listener.onHomeClicked();
        });

        dialog.show();
        return dialog;
    }

    public static Dialog showFirstTimeInstruction(Activity activity, Runnable onLetsPlay) {
        DialogFirstTimeInstructionBinding binding = DialogFirstTimeInstructionBinding.inflate(activity.getLayoutInflater());
        Dialog dialog = createBaseDialog(activity, binding.getRoot(), true);

        ViewPager2 vpTutorial = binding.vpTutorial;
        View dot1 = binding.dot1;
        View dot2 = binding.dot2;
        View dot3 = binding.dot3;

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
                binding.btnTutorialAction.setText("Let's play!");
            } else {
                binding.btnTutorialAction.setText("Next");
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

        binding.btnTutorialAction.setOnClickListener(view -> {
            int current = vpTutorial.getCurrentItem();
            if (current < 2) {
                vpTutorial.setCurrentItem(current + 1, true);
            } else {
                dialog.dismiss();
                if (onLetsPlay != null) onLetsPlay.run();
            }
        });

        binding.btnTutorialClose.setOnClickListener(view -> dialog.dismiss());

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
            View view;
            if (viewType == 0) {
                view = ItemTutorialSlideTopDownBinding.inflate(inflater, parent, false).getRoot();
            } else if (viewType == 1) {
                view = ItemTutorialSlideLeftRightBinding.inflate(inflater, parent, false).getRoot();
            } else {
                view = ItemTutorialSlideGoalBinding.inflate(inflater, parent, false).getRoot();
            }
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
