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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.redcodersgroup.numberblocks.R;
import com.redcodersgroup.numberblocks.ads.AdsManager;
import com.redcodersgroup.numberblocks.databinding.DialogEditProfileBinding;
import com.redcodersgroup.numberblocks.games.LeaderboardEntry;
import com.redcodersgroup.numberblocks.games.PlayGamesManager;
import com.redcodersgroup.numberblocks.profile.AvatarManager;
import com.redcodersgroup.numberblocks.databinding.DialogCareerStatsBinding;
import com.redcodersgroup.numberblocks.databinding.DialogLeaderboardBinding;
import com.redcodersgroup.numberblocks.databinding.DialogConfirmExitBinding;
import com.redcodersgroup.numberblocks.databinding.DialogFirstTimeInstructionBinding;
import com.redcodersgroup.numberblocks.databinding.DialogGameOverBinding;
import com.redcodersgroup.numberblocks.databinding.DialogMilestoneBinding;
import com.redcodersgroup.numberblocks.databinding.DialogModeResumeBinding;
import com.redcodersgroup.numberblocks.databinding.DialogPauseMenuBinding;
import com.redcodersgroup.numberblocks.databinding.DialogRestartBinding;
import com.redcodersgroup.numberblocks.databinding.DialogSettingsBinding;
import com.redcodersgroup.numberblocks.databinding.DialogThemeAdConfirmBinding;
import com.redcodersgroup.numberblocks.databinding.DialogThemeSelectorBinding;
import com.redcodersgroup.numberblocks.databinding.DialogTutorialBinding;
import com.redcodersgroup.numberblocks.databinding.ItemTutorialSlideGoalBinding;
import com.redcodersgroup.numberblocks.databinding.ItemTutorialSlideLeftRightBinding;
import com.redcodersgroup.numberblocks.databinding.ItemTutorialSlideTopDownBinding;
import com.redcodersgroup.numberblocks.storage.PreferencesManager;
import com.redcodersgroup.numberblocks.theme.Theme;
import com.redcodersgroup.numberblocks.theme.ThemeManager;
import java.util.Locale;

public class DialogHelper {

    public interface ThemeSelectListener {
        void onThemeSelected(String themeKey);
    }

    public interface SettingsListener {
        void onSoundToggled(boolean enabled);
        default void onMusicToggled(boolean enabled) {}
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
        applyDialogTheme(activity, contentView);
        return dialog;
    }

    private static void applyDialogTheme(Activity activity, View contentView) {
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        if (!theme.isDark) return;

        applyDarkDialogThemeRecursive(activity, contentView, theme);
    }

    private static boolean isInsideViewPager(View view) {
        android.view.ViewParent parent = view.getParent();
        while (parent instanceof View) {
            if (parent instanceof ViewPager2) return true;
            parent = parent.getParent();
        }
        return false;
    }

    private static void applyDarkDialogThemeRecursive(Activity activity, View view, Theme theme) {
        if (view instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) view;
            int id = card.getId();
            // Do not override theme selector preview cards or tutorial illustration cards
            if (id == R.id.card_theme_dark || id == R.id.card_theme_light
                    || id == R.id.card_theme_neon || id == R.id.card_theme_pastel
                    || isInsideViewPager(card)) {
                return;
            }
            if (card.getCardElevation() > 0 || card.getUseCompatPadding()) {
                card.setCardBackgroundColor(theme.cardBackgroundColor);
                card.setStrokeColor(theme.cardStrokeColor);
            } else {
                card.setCardBackgroundColor(theme.btnSurfaceColor);
                card.setStrokeColor(theme.btnStrokeColor);
            }
        } else if (view instanceof MaterialButton) {
            MaterialButton matBtn = (MaterialButton) view;
            int strokeWidth = matBtn.getStrokeWidth();
            ColorStateList bgTint = matBtn.getBackgroundTintList();
            int bgColor = bgTint != null ? bgTint.getDefaultColor() : 0;
            int textColor = matBtn.getCurrentTextColor();
            int viewId = matBtn.getId();

            boolean isTextOnlyButton = (strokeWidth == 0 && (
                    viewId == R.id.btn_dialog_stay ||
                    viewId == R.id.btn_dialog_restart_cancel ||
                    viewId == R.id.btn_dialog_cancel ||
                    viewId == R.id.btn_cancel_profile ||
                    viewId == R.id.btn_theme_confirm_cancel ||
                    viewId == R.id.btn_dialog_new_game ||
                    (textColor == Color.parseColor("#737680") && viewId != R.id.btn_save_profile) ||
                    ((bgColor == 0 || Color.alpha(bgColor) == 0) && viewId != R.id.btn_save_profile && viewId != R.id.btn_view_leaderboards)
            ));

            // 1. Text buttons (GameButton.Text: transparent background, no stroke)
            if (isTextOnlyButton) {
                matBtn.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                matBtn.setTextColor(theme.textSecondaryColor);
            }
            // 2. Outlined / Secondary buttons (has stroke)
            else if (strokeWidth > 0) {
                matBtn.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                matBtn.setStrokeColor(ColorStateList.valueOf(theme.btnStrokeColor));
                int r = Color.red(textColor);
                int g = Color.green(textColor);
                int b = Color.blue(textColor);
                if (r > 180 && g < 100 && b < 100) {
                    matBtn.setTextColor(Color.parseColor("#EF4444"));
                    if (matBtn.getIcon() != null) {
                        matBtn.setIconTint(ColorStateList.valueOf(Color.parseColor("#EF4444")));
                    }
                } else {
                    matBtn.setTextColor(theme.textPrimaryColor);
                    if (matBtn.getIcon() != null) {
                        matBtn.setIconTint(ColorStateList.valueOf(theme.textPrimaryColor));
                    }
                }
            }
            // 3. Solid Primary Buttons
            else {
                int r = Color.red(bgColor);
                int g = Color.green(bgColor);
                int b = Color.blue(bgColor);

                // Red confirm (e.g. Restart confirm)
                if (r > 180 && g < 100 && b < 100) {
                    matBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EF4444")));
                    matBtn.setTextColor(Color.WHITE);
                }
                // Green continue (e.g. Pause continue)
                else if (g > 150 && r < 100 && b < 160) {
                    matBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#10B981")));
                    matBtn.setTextColor(Color.WHITE);
                }
                // Gray secondary pills (#5A5D66 in pause menu)
                else if (bgColor == Color.parseColor("#5A5D66")) {
                    matBtn.setBackgroundTintList(ColorStateList.valueOf(theme.btnSurfaceColor));
                    matBtn.setStrokeColor(ColorStateList.valueOf(theme.btnStrokeColor));
                    matBtn.setStrokeWidth((int) (1 * activity.getResources().getDisplayMetrics().density));
                    matBtn.setTextColor(theme.textPrimaryColor);
                }
                // Primary action buttons (previously #1E2024) -> High contrast radiant amber gold
                else {
                    matBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F59E0B")));
                    matBtn.setTextColor(Color.parseColor("#181A1F"));
                }
            }
        } else if (view instanceof TextView && !(view instanceof Button)) {
            TextView tv = (TextView) view;
            if (isInsideViewPager(tv) && tv.getParent() instanceof android.widget.GridLayout) {
                return;
            }
            if (tv.getId() == R.id.tv_gameover_highest_tile) {
                return;
            }
            int currentColor = tv.getCurrentTextColor();
            double lum = (0.299 * Color.red(currentColor) + 0.587 * Color.green(currentColor) + 0.114 * Color.blue(currentColor)) / 255.0;
            if (lum < 0.45) {
                if (lum < 0.2) {
                    tv.setTextColor(theme.textPrimaryColor);
                } else {
                    tv.setTextColor(theme.textSecondaryColor);
                }
            }
        } else if (view instanceof ImageView) {
            ImageView iv = (ImageView) view;
            ColorStateList tint = iv.getImageTintList();
            if (tint != null) {
                int c = tint.getDefaultColor();
                double lum = (0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c)) / 255.0;
                if (lum < 0.5) {
                    iv.setImageTintList(ColorStateList.valueOf(theme.textSecondaryColor));
                }
            }
        } else if (view != null && "View".equals(view.getClass().getSimpleName())) {
            if (view.getLayoutParams() != null && view.getLayoutParams().height <= (int) (2 * activity.getResources().getDisplayMetrics().density)) {
                view.setBackgroundColor(theme.cardStrokeColor);
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyDarkDialogThemeRecursive(activity, vg.getChildAt(i), theme);
            }
        }
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

        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        float density = activity.getResources().getDisplayMetrics().density;

        int cardBg = theme.isDark ? theme.btnSurfaceColor : theme.hudCardColor;
        int cardStroke = theme.isDark ? theme.btnStrokeColor : theme.cardStrokeColor;

        if (binding.cardGameoverScore != null) {
            binding.cardGameoverScore.setCardBackgroundColor(cardBg);
            binding.cardGameoverScore.setStrokeColor(cardStroke);
        }
        if (binding.cardGameoverBest != null) {
            binding.cardGameoverBest.setCardBackgroundColor(cardBg);
            binding.cardGameoverBest.setStrokeColor(cardStroke);
        }
        binding.tvGameoverScore.setTextColor(theme.textPrimaryColor);
        binding.tvGameoverBest.setTextColor(theme.textPrimaryColor);

        android.graphics.drawable.GradientDrawable highestTilePill = new android.graphics.drawable.GradientDrawable();
        highestTilePill.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        highestTilePill.setCornerRadius(14 * density);
        highestTilePill.setColor(cardBg);
        highestTilePill.setStroke((int) (1 * density), cardStroke);
        binding.tvGameoverHighestTile.setTextColor(theme.textPrimaryColor);
        binding.tvGameoverHighestTile.setBackground(highestTilePill);

        // Prominent Full-Width Revive Action Button (Radiant Amber Gold)
        binding.btnGameoverRevive.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F59E0B")));
        binding.btnGameoverRevive.setTextColor(Color.WHITE);

        // Secondary Buttons: RETRY and HOME
        int btnSurface = theme.btnSurfaceColor;
        int btnStroke = theme.btnStrokeColor;

        binding.btnGameoverRetry.setBackgroundTintList(ColorStateList.valueOf(btnSurface));
        binding.btnGameoverRetry.setStrokeColor(ColorStateList.valueOf(btnStroke));
        binding.btnGameoverRetry.setStrokeWidth((int) (1 * density));
        binding.btnGameoverRetry.setTextColor(theme.textPrimaryColor);

        binding.btnGameoverHome.setBackgroundTintList(ColorStateList.valueOf(btnSurface));
        binding.btnGameoverHome.setStrokeColor(ColorStateList.valueOf(btnStroke));
        binding.btnGameoverHome.setStrokeWidth((int) (1 * density));
        binding.btnGameoverHome.setTextColor(theme.textPrimaryColor);

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

        boolean isPrism = "prism".equalsIgnoreCase(currentTheme) || "colorful".equalsIgnoreCase(currentTheme);
        boolean isAlabaster = "alabaster".equalsIgnoreCase(currentTheme) || "light".equalsIgnoreCase(currentTheme)
                || "golden".equalsIgnoreCase(currentTheme) || "golden_classic".equalsIgnoreCase(currentTheme);
        boolean isTitanium = "titanium".equalsIgnoreCase(currentTheme) || "neon".equalsIgnoreCase(currentTheme);
        boolean isNordic = "nordic".equalsIgnoreCase(currentTheme) || "pastel".equalsIgnoreCase(currentTheme);
        boolean isGraphite = "graphite".equalsIgnoreCase(currentTheme) || "dark".equalsIgnoreCase(currentTheme) || "charcoal".equalsIgnoreCase(currentTheme);

        Theme currentActiveTheme = ThemeManager.getInstance().getCurrentTheme();
        int activeStrokeColor = Color.parseColor("#F59E0B");
        int inactiveStrokeColor = currentActiveTheme.isDark ? Color.parseColor("#353942") : Color.parseColor("#D9D7CE");
        int activeWidth = (int) (2 * activity.getResources().getDisplayMetrics().density);
        int inactiveWidth = (int) (1 * activity.getResources().getDisplayMetrics().density);

        binding.cardThemePrism.setStrokeColor(isPrism ? activeStrokeColor : inactiveStrokeColor);
        binding.cardThemePrism.setStrokeWidth(isPrism ? activeWidth : inactiveWidth);

        binding.cardThemeDark.setStrokeColor(isAlabaster ? activeStrokeColor : inactiveStrokeColor);
        binding.cardThemeDark.setStrokeWidth(isAlabaster ? activeWidth : inactiveWidth);

        binding.cardThemeLight.setStrokeColor(isTitanium ? activeStrokeColor : inactiveStrokeColor);
        binding.cardThemeLight.setStrokeWidth(isTitanium ? activeWidth : inactiveWidth);

        binding.cardThemeNeon.setStrokeColor(isNordic ? activeStrokeColor : inactiveStrokeColor);
        binding.cardThemeNeon.setStrokeWidth(isNordic ? activeWidth : inactiveWidth);

        binding.cardThemePastel.setStrokeColor(isGraphite ? activeStrokeColor : inactiveStrokeColor);
        binding.cardThemePastel.setStrokeWidth(isGraphite ? activeWidth : inactiveWidth);

        // Update Badges - only Prism Pop is the default theme
        updateThemeBadge(binding.tvBadgeThemePrism, isPrism, true, currentActiveTheme);
        updateThemeBadge(binding.tvBadgeThemeDark, isAlabaster, false, currentActiveTheme);
        updateThemeBadge(binding.tvBadgeThemeLight, isTitanium, false, currentActiveTheme);
        updateThemeBadge(binding.tvBadgeThemeNeon, isNordic, false, currentActiveTheme);
        updateThemeBadge(binding.tvBadgeThemePastel, isGraphite, false, currentActiveTheme);

        binding.btnCloseTheme.setTextColor(currentActiveTheme.textPrimaryColor);

        binding.cardThemePrism.setOnClickListener(view -> {
            if (isPrism) {
                Toast.makeText(activity, activity.getString(R.string.theme_already_active, "Prism Pop"), Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            if (listener != null) listener.onThemeSelected("prism");
        });

        binding.cardThemeDark.setOnClickListener(view -> {
            if (isAlabaster) {
                Toast.makeText(activity, activity.getString(R.string.theme_already_active, "Golden Classic"), Toast.LENGTH_SHORT).show();
                return;
            }
            promptThemeAd(activity, dialog, "Golden Classic", "alabaster", listener);
        });

        binding.cardThemeLight.setOnClickListener(view -> {
            if (isTitanium) {
                Toast.makeText(activity, activity.getString(R.string.theme_already_active, "Titanium Slate"), Toast.LENGTH_SHORT).show();
                return;
            }
            promptThemeAd(activity, dialog, "Titanium Slate", "titanium", listener);
        });

        binding.cardThemeNeon.setOnClickListener(view -> {
            if (isNordic) {
                Toast.makeText(activity, activity.getString(R.string.theme_already_active, "Nordic Clay"), Toast.LENGTH_SHORT).show();
                return;
            }
            promptThemeAd(activity, dialog, "Nordic Clay", "nordic", listener);
        });

        binding.cardThemePastel.setOnClickListener(view -> {
            if (isGraphite) {
                Toast.makeText(activity, activity.getString(R.string.theme_already_active, "Charcoal Slate"), Toast.LENGTH_SHORT).show();
                return;
            }
            promptThemeAd(activity, dialog, "Charcoal Slate", "graphite", listener);
        });

        binding.btnCloseTheme.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    private static void updateThemeBadge(TextView badgeView, boolean isActive, boolean isDefault, Theme currentActiveTheme) {
        if (isActive) {
            badgeView.setText(R.string.theme_badge_active);
            badgeView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            badgeView.setTextColor(Color.WHITE);
        } else if (isDefault) {
            badgeView.setText(R.string.theme_badge_default);
            badgeView.setBackgroundTintList(ColorStateList.valueOf(currentActiveTheme.isDark ? Color.parseColor("#343845") : Color.parseColor("#EDE0C8")));
            badgeView.setTextColor(currentActiveTheme.isDark ? Color.parseColor("#E0E3EA") : Color.parseColor("#776E65"));
        } else {
            badgeView.setText(R.string.theme_badge_ad);
            badgeView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF3E0")));
            badgeView.setTextColor(Color.parseColor("#E65100"));
        }
    }

    private static void promptThemeAd(Activity activity, Dialog parentDialog, String themeName, String themeKey, ThemeSelectListener listener) {
        DialogThemeAdConfirmBinding confirmBinding = DialogThemeAdConfirmBinding.inflate(activity.getLayoutInflater());
        Dialog confirmDialog = createBaseDialog(activity, confirmBinding.getRoot(), true);

        confirmBinding.tvThemeConfirmMsg.setText(activity.getString(R.string.theme_change_msg, themeName));

        confirmBinding.btnThemeConfirmWatch.setOnClickListener(v -> {
            confirmDialog.dismiss();
            if (parentDialog != null && parentDialog.isShowing()) {
                parentDialog.dismiss();
            }
            AdsManager.getInstance().showRewarded(activity, rewardItem -> {
                if (listener != null) {
                    listener.onThemeSelected(themeKey);
                }
            });
        });

        confirmBinding.btnThemeConfirmCancel.setOnClickListener(v -> confirmDialog.dismiss());

        confirmDialog.show();
    }

    public static Dialog showSettings(Activity activity, boolean soundEnabled, boolean hapticsEnabled, SettingsListener listener) {
        boolean musicEnabled = PreferencesManager.getInstance(activity).isMusicEnabled();
        return showSettings(activity, soundEnabled, musicEnabled, hapticsEnabled, listener);
    }

    public static Dialog showSettings(Activity activity, boolean soundEnabled, boolean musicEnabled, boolean hapticsEnabled, SettingsListener listener) {
        DialogSettingsBinding binding = DialogSettingsBinding.inflate(activity.getLayoutInflater());
        Dialog dialog = createBaseDialog(activity, binding.getRoot(), true);

        binding.switchSound.setChecked(soundEnabled);
        if (binding.switchMusic != null) {
            binding.switchMusic.setChecked(musicEnabled);
            binding.switchMusic.setOnCheckedChangeListener((btn, isChecked) -> {
                if (listener != null) listener.onMusicToggled(isChecked);
            });
        }
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

        // Player Profile Card
        PreferencesManager prefs = PreferencesManager.getInstance(activity);
        if (binding.cardSettingsProfile != null) {
            String currentName = prefs.isUsingGoogleProfile() && prefs.getGooglePlayerName() != null
                    ? prefs.getGooglePlayerName()
                    : prefs.getPlayerName();
            String currentAvatarId = prefs.isUsingGoogleProfile()
                    ? AvatarManager.GOOGLE_AVATAR_ID
                    : prefs.getAvatarId();

            binding.tvSettingsPlayerName.setText(currentName);
            AvatarManager.loadAvatar(activity, binding.ivSettingsAvatar, currentAvatarId);

            Theme theme = ThemeManager.getInstance().getCurrentTheme();
            int profileCardBg = theme.isDark ? theme.backgroundColor : Color.parseColor("#FAF9F6");
            int profileCardStroke = theme.isDark ? theme.btnStrokeColor : Color.parseColor("#E3E1D8");
            binding.cardSettingsProfile.setCardBackgroundColor(profileCardBg);
            binding.cardSettingsProfile.setStrokeColor(profileCardStroke);
            binding.tvSettingsPlayerName.setTextColor(theme.textPrimaryColor);
            binding.tvSettingsProfileHint.setTextColor(theme.textSecondaryColor);

            binding.cardSettingsProfile.setOnClickListener(view -> {
                showEditProfileDialog(activity, (newName, newAvatarId, isGoogle) -> {
                    binding.tvSettingsPlayerName.setText(newName);
                    AvatarManager.loadAvatar(activity, binding.ivSettingsAvatar, newAvatarId);
                });
            });
        }

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

        if (binding.btnViewLeaderboards != null) {
            binding.btnViewLeaderboards.setOnClickListener(view -> {
                dialog.dismiss();
                showLeaderboardDialog(activity, 4);
            });
        }

        binding.btnCloseStats.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        return dialog;
    }

    public static Dialog showLeaderboardDialog(Activity activity, int initialSize) {
        DialogLeaderboardBinding binding = DialogLeaderboardBinding.inflate(activity.getLayoutInflater());
        Dialog dialog = createBaseDialog(activity, binding.getRoot(), true);

        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        float density = activity.getResources().getDisplayMetrics().density;

        // Apply theme styling
        int cardBg = theme.isDark ? theme.btnSurfaceColor : Color.parseColor("#FFFFFF");
        int cardStroke = theme.isDark ? theme.btnStrokeColor : Color.parseColor("#D9D7CE");
        binding.cardLeaderboardDialogRoot.setCardBackgroundColor(cardBg);
        binding.cardLeaderboardDialogRoot.setStrokeColor(cardStroke);

        binding.tvLbTitle.setTextColor(theme.textPrimaryColor);
        binding.tvLbSubtitle.setTextColor(theme.textSecondaryColor);

        // Segmented tabs container
        binding.containerLbTabs.setCardBackgroundColor(theme.hudCardColor);
        binding.containerLbTabs.setStrokeColor(theme.btnStrokeColor);

        // Standing card
        int standingCardBg = theme.isDark ? theme.backgroundColor : Color.parseColor("#FAF9F6");
        binding.cardPlayerStanding.setCardBackgroundColor(standingCardBg);
        binding.cardPlayerStanding.setStrokeColor(cardStroke);
        binding.tvStandingName.setTextColor(theme.textPrimaryColor);
        binding.tvStandingRank.setTextColor(theme.textSecondaryColor);
        binding.tvStandingScore.setTextColor(theme.textPrimaryColor);

        // Action buttons styling
        binding.btnCloseLb.setTextColor(theme.textPrimaryColor);
        binding.btnOpenFullLb.setTextColor(theme.textPrimaryColor);
        binding.btnOpenFullLb.setStrokeColor(ColorStateList.valueOf(theme.cardStrokeColor != 0 ? theme.cardStrokeColor : cardStroke));

        // RecyclerView setup
        LeaderboardAdapter adapter = new LeaderboardAdapter();
        binding.rvLbScores.setLayoutManager(new LinearLayoutManager(activity));
        binding.rvLbScores.setAdapter(adapter);

        final int[] currentSize = {initialSize >= 4 && initialSize <= 6 ? initialSize : 4};

        Runnable updateTabsVisuals = () -> {
            setDialogTabState(binding.tabLb4, binding.tvTabLb4, currentSize[0] == 4, theme, density);
            setDialogTabState(binding.tabLb5, binding.tvTabLb5, currentSize[0] == 5, theme, density);
            setDialogTabState(binding.tabLb6, binding.tvTabLb6, currentSize[0] == 6, theme, density);
        };

        interface LoadAction {
            void execute(int size);
        }

        LoadAction loadAction = new LoadAction() {
            @Override
            public void execute(int size) {
                currentSize[0] = size;
                updateTabsVisuals.run();

                // Show local score immediately
                int localBest = PreferencesManager.getInstance(activity).getBestScore(size);
                binding.tvStandingScore.setText(String.format(Locale.getDefault(), "%,d", localBest));
                binding.tvStandingRank.setText("Personal Record");

                binding.pbLbLoading.setVisibility(View.VISIBLE);
                binding.tvLbEmpty.setVisibility(View.GONE);
                binding.rvLbScores.setVisibility(View.GONE);

                PlayGamesManager.getInstance().loadScores(activity, size, new PlayGamesManager.LeaderboardCallback() {
                    @Override
                    public void onScoresLoaded(java.util.List<LeaderboardEntry> entries, LeaderboardEntry currentPlayerStanding) {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        activity.runOnUiThread(() -> {
                            binding.pbLbLoading.setVisibility(View.GONE);
                            if (currentPlayerStanding != null) {
                                binding.tvStandingScore.setText(currentPlayerStanding.getFormattedScore());
                                String rankText = "-".equals(currentPlayerStanding.getRank()) ? "Unranked" : "#" + currentPlayerStanding.getRank();
                                binding.tvStandingRank.setText("Rank: " + rankText);
                            } else {
                                binding.tvStandingRank.setText("Personal Record");
                            }

                            if (entries == null || entries.isEmpty()) {
                                binding.tvLbEmpty.setVisibility(View.VISIBLE);
                                binding.rvLbScores.setVisibility(View.GONE);
                            } else {
                                binding.tvLbEmpty.setVisibility(View.GONE);
                                binding.rvLbScores.setVisibility(View.VISIBLE);
                                adapter.setItems(entries);
                            }
                        });
                    }

                    @Override
                    public void onFailed(String errorMessage) {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        activity.runOnUiThread(() -> {
                            binding.pbLbLoading.setVisibility(View.GONE);
                            binding.tvLbEmpty.setVisibility(View.VISIBLE);
                            binding.rvLbScores.setVisibility(View.GONE);
                        });
                    }
                });
            }
        };

        // Bind profile avatar & name in standing card
        PreferencesManager prefs = PreferencesManager.getInstance(activity);
        String standingName = prefs.isUsingGoogleProfile() && prefs.getGooglePlayerName() != null
                ? prefs.getGooglePlayerName()
                : prefs.getPlayerName();
        String standingAvatar = prefs.isUsingGoogleProfile()
                ? AvatarManager.GOOGLE_AVATAR_ID
                : prefs.getAvatarId();
        binding.tvStandingName.setText(standingName);
        AvatarManager.loadAvatar(activity, binding.ivStandingAvatar, standingAvatar);

        binding.cardPlayerStanding.setOnClickListener(v -> {
            showEditProfileDialog(activity, (newName, newAvatarId, isGoogle) -> {
                binding.tvStandingName.setText(newName);
                AvatarManager.loadAvatar(activity, binding.ivStandingAvatar, newAvatarId);
            });
        });

        binding.tabLb4.setOnClickListener(v -> loadAction.execute(4));
        binding.tabLb5.setOnClickListener(v -> loadAction.execute(5));
        binding.tabLb6.setOnClickListener(v -> loadAction.execute(6));

        binding.btnOpenFullLb.setOnClickListener(v -> {
            PlayGamesManager.getInstance().showLeaderboard(activity, currentSize[0]);
        });

        binding.btnCloseLbIcon.setOnClickListener(v -> dialog.dismiss());
        binding.btnCloseLb.setOnClickListener(v -> dialog.dismiss());

        loadAction.execute(currentSize[0]);

        dialog.show();
        return dialog;
    }

    private static void setDialogTabState(MaterialCardView card, TextView tv, boolean isSelected, Theme theme, float density) {
        if (isSelected) {
            card.setCardBackgroundColor(theme.cardBackgroundColor);
            card.setStrokeColor(theme.cardStrokeColor != 0 ? theme.cardStrokeColor : Color.TRANSPARENT);
            card.setStrokeWidth((int) (1 * density));
            card.setCardElevation(2.5f * density);
            tv.setTextColor(theme.textPrimaryColor);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setAlpha(1.0f);
        } else {
            card.setCardBackgroundColor(Color.TRANSPARENT);
            card.setStrokeColor(Color.TRANSPARENT);
            card.setStrokeWidth(0);
            card.setCardElevation(0);
            tv.setTextColor(theme.textSecondaryColor);
            tv.setTypeface(null, Typeface.NORMAL);
            tv.setAlpha(0.70f);
        }
    }

    public interface ProfileUpdateListener {
        void onProfileUpdated(String newName, String newAvatarId, boolean isGoogle);
    }

    public static Dialog showEditProfileDialog(Activity activity, ProfileUpdateListener listener) {
        DialogEditProfileBinding binding = DialogEditProfileBinding.inflate(activity.getLayoutInflater());
        Dialog dialog = createBaseDialog(activity, binding.getRoot(), true);

        PreferencesManager prefs = PreferencesManager.getInstance(activity);
        Theme theme = ThemeManager.getInstance().getCurrentTheme();

        // Theme colors
        int cardBg = theme.isDark ? theme.btnSurfaceColor : Color.parseColor("#FFFFFF");
        int cardStroke = theme.isDark ? theme.btnStrokeColor : Color.parseColor("#D9D7CE");
        binding.cardProfileDialogRoot.setCardBackgroundColor(cardBg);
        binding.cardProfileDialogRoot.setStrokeColor(cardStroke);

        binding.tvProfileDialogTitle.setTextColor(theme.textPrimaryColor);
        binding.tvProfileDialogSubtitle.setTextColor(theme.textSecondaryColor);

        int inputBg = theme.isDark ? theme.backgroundColor : Color.parseColor("#FAF9F6");
        binding.cardNameInput.setCardBackgroundColor(inputBg);
        binding.cardNameInput.setStrokeColor(cardStroke);
        binding.etPlayerName.setTextColor(theme.textPrimaryColor);
        binding.etPlayerName.setHintTextColor(theme.textSecondaryColor);

        binding.cardGoogleProfileToggle.setCardBackgroundColor(inputBg);
        binding.cardGoogleProfileToggle.setStrokeColor(cardStroke);
        binding.tvGoogleToggleTitle.setTextColor(theme.textPrimaryColor);
        binding.tvGoogleToggleSubtitle.setTextColor(theme.textSecondaryColor);

        binding.cardSelectedAvatarPreview.setCardBackgroundColor(inputBg);
        binding.btnSaveProfile.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F59E0B")));
        binding.btnSaveProfile.setTextColor(Color.parseColor("#181A1F"));
        binding.btnCancelProfile.setTextColor(theme.textSecondaryColor);

        // Prepopulate current state
        String currentName = prefs.getPlayerName();
        String currentAvatarId = prefs.getAvatarId();
        boolean isGoogleProfile = prefs.isUsingGoogleProfile();

        if (isGoogleProfile) {
            String googleName = prefs.getGooglePlayerName();
            binding.etPlayerName.setText(googleName != null && !googleName.isEmpty() ? googleName : currentName);
            AvatarManager.loadAvatar(activity, binding.ivSelectedAvatarPreview, AvatarManager.GOOGLE_AVATAR_ID);
        } else {
            binding.etPlayerName.setText(currentName);
            AvatarManager.loadAvatar(activity, binding.ivSelectedAvatarPreview, currentAvatarId);
        }
        binding.switchUseGoogleProfile.setChecked(isGoogleProfile);

        // Avatars Grid Setup (12 avatars)
        binding.rvAvatarChoices.setLayoutManager(new GridLayoutManager(activity, 4));
        AvatarChoiceAdapter adapter = new AvatarChoiceAdapter(
                AvatarManager.getPredefinedAvatars(),
                isGoogleProfile ? AvatarManager.GOOGLE_AVATAR_ID : currentAvatarId,
                item -> {
                    binding.switchUseGoogleProfile.setChecked(false);
                    AvatarManager.loadAvatar(activity, binding.ivSelectedAvatarPreview, item.id);
                }
        );
        binding.rvAvatarChoices.setAdapter(adapter);

        // Google Profile Switch logic
        binding.switchUseGoogleProfile.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                PlayGamesManager.getInstance().loadCurrentPlayerProfile(activity, new PlayGamesManager.PlayerProfileCallback() {
                    @Override
                    public void onProfileLoaded(String displayName, Uri iconUri) {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        activity.runOnUiThread(() -> {
                            if (displayName != null && !displayName.isEmpty()) {
                                binding.etPlayerName.setText(displayName);
                            }
                            if (iconUri != null) {
                                PlayGamesManager.getInstance().loadPlayerImage(activity, binding.ivSelectedAvatarPreview, iconUri, R.drawable.ic_avatar_hero);
                            }
                            adapter.setSelectedAvatarId(AvatarManager.GOOGLE_AVATAR_ID);
                        });
                    }

                    @Override
                    public void onFailed(String errorMessage) {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        activity.runOnUiThread(() -> {
                            PlayGamesManager.getInstance().ensureSignedIn(activity, success -> {
                                if (activity.isFinishing() || activity.isDestroyed()) return;
                                activity.runOnUiThread(() -> {
                                    if (success) {
                                        PlayGamesManager.getInstance().loadCurrentPlayerProfile(activity, new PlayGamesManager.PlayerProfileCallback() {
                                            @Override
                                            public void onProfileLoaded(String displayName, Uri iconUri) {
                                                if (activity.isFinishing() || activity.isDestroyed()) return;
                                                activity.runOnUiThread(() -> {
                                                    if (displayName != null && !displayName.isEmpty()) {
                                                        binding.etPlayerName.setText(displayName);
                                                    }
                                                    if (iconUri != null) {
                                                        PlayGamesManager.getInstance().loadPlayerImage(activity, binding.ivSelectedAvatarPreview, iconUri, R.drawable.ic_avatar_hero);
                                                    }
                                                    adapter.setSelectedAvatarId(AvatarManager.GOOGLE_AVATAR_ID);
                                                });
                                            }

                                            @Override
                                            public void onFailed(String err) {
                                                if (activity.isFinishing() || activity.isDestroyed()) return;
                                                activity.runOnUiThread(() -> binding.switchUseGoogleProfile.setChecked(false));
                                            }
                                        });
                                    } else {
                                        binding.switchUseGoogleProfile.setChecked(false);
                                    }
                                });
                            });
                        });
                    }
                });
            } else {
                String selected = adapter.getSelectedAvatarId();
                if (AvatarManager.GOOGLE_AVATAR_ID.equals(selected)) {
                    selected = AvatarManager.DEFAULT_AVATAR_ID;
                    adapter.setSelectedAvatarId(selected);
                }
                AvatarManager.loadAvatar(activity, binding.ivSelectedAvatarPreview, selected);
            }
        });

        binding.btnSaveProfile.setOnClickListener(v -> {
            String newName = binding.etPlayerName.getText().toString().trim();
            if (newName.isEmpty()) {
                newName = "Player";
            }
            boolean useGoogle = binding.switchUseGoogleProfile.isChecked();
            String newAvatar = useGoogle ? AvatarManager.GOOGLE_AVATAR_ID : adapter.getSelectedAvatarId();

            prefs.setPlayerName(newName);
            prefs.setAvatarId(newAvatar);
            prefs.setUsingGoogleProfile(useGoogle);

            dialog.dismiss();
            if (listener != null) {
                listener.onProfileUpdated(newName, newAvatar, useGoogle);
            }
            Toast.makeText(activity, "Profile updated!", Toast.LENGTH_SHORT).show();
        });

        binding.btnCloseProfileIcon.setOnClickListener(v -> dialog.dismiss());
        binding.btnCancelProfile.setOnClickListener(v -> dialog.dismiss());

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
