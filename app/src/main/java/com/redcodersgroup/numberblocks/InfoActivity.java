package com.redcodersgroup.numberblocks;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.redcodersgroup.numberblocks.audio.HapticManager;
import com.redcodersgroup.numberblocks.audio.SoundManager;
import com.redcodersgroup.numberblocks.databinding.ActivityInfoBinding;
import com.redcodersgroup.numberblocks.storage.PreferencesManager;
import com.redcodersgroup.numberblocks.theme.Theme;
import com.redcodersgroup.numberblocks.theme.ThemeManager;
import com.redcodersgroup.numberblocks.ui.StatusBarHelper;

public class InfoActivity extends AppCompatActivity {

    private ActivityInfoBinding binding;
    private PreferencesManager prefs;
    private ThemeManager themeManager;
    private HapticManager hapticManager;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = PreferencesManager.getInstance(this);
        themeManager = ThemeManager.getInstance();
        themeManager.setTheme(prefs.getTheme());
        hapticManager = HapticManager.getInstance();
        hapticManager.init(this);
        soundManager = SoundManager.getInstance();

        // Apply system bar insets and theme styling
        StatusBarHelper.hideSystemBars(this);
        StatusBarHelper.applySystemBarInsets(binding.getRoot());
        applyTheme();

        // Back button
        binding.btnInfoBack.setOnClickListener(v -> {
            hapticManager.click();
            finish();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE,
                        android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        // Legal buttons
        binding.btnInfoPrivacy.setOnClickListener(v -> {
            hapticManager.click();
            openUrl(getString(R.string.privacy_policy_url));
        });

        binding.btnInfoTerms.setOnClickListener(v -> {
            hapticManager.click();
            openUrl(getString(R.string.terms_url));
        });

        // Support buttons
        binding.btnInfoSupport.setOnClickListener(v -> {
            hapticManager.click();
            openUrl(getString(R.string.support_url));
        });

        binding.btnInfoEmail.setOnClickListener(v -> {
            hapticManager.click();
            try {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + getString(R.string.developer_email)));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Number Blocks 2048 - Support & Inquiries");
                startActivity(Intent.createChooser(emailIntent, getString(R.string.btn_email_developer)));
            } catch (Exception e) {
                openUrl(getString(R.string.support_url));
            }
        });

        // Portfolio button
        binding.btnInfoPortfolio.setOnClickListener(v -> {
            hapticManager.click();
            openUrl(getString(R.string.portfolio_url));
        });
    }

    private void applyTheme() {
        Theme theme = themeManager.getCurrentTheme();
        if (binding != null) {
            binding.getRoot().setBackgroundColor(theme.backgroundColor);

            // Back button
            android.graphics.drawable.GradientDrawable backCircle = new android.graphics.drawable.GradientDrawable();
            backCircle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            backCircle.setColor(theme.btnSurfaceColor);
            backCircle.setStroke((int) (1 * getResources().getDisplayMetrics().density), theme.btnStrokeColor);
            binding.btnInfoBack.setBackground(backCircle);
            binding.btnInfoBack.setImageTintList(ColorStateList.valueOf(theme.textPrimaryColor));

            binding.tvInfoHeader.setTextColor(theme.textPrimaryColor);

            applyThemeRecursive(binding.getRoot(), theme);

            // Specific button overrides
            styleOutlinedButton(binding.btnInfoPrivacy, theme);
            styleOutlinedButton(binding.btnInfoTerms, theme);
            styleOutlinedButton(binding.btnInfoSupport, theme);
            styleOutlinedButton(binding.btnInfoEmail, theme);

            if (theme.isDark) {
                binding.btnInfoPortfolio.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F3F7")));
                binding.btnInfoPortfolio.setTextColor(Color.parseColor("#181A1F"));
            } else {
                binding.btnInfoPortfolio.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1E2024")));
                binding.btnInfoPortfolio.setTextColor(Color.parseColor("#FFFFFF"));
            }
        }
        StatusBarHelper.updateSystemBars(this, theme.backgroundColor);
    }

    private void styleOutlinedButton(Button btn, Theme theme) {
        if (btn instanceof MaterialButton) {
            MaterialButton matBtn = (MaterialButton) btn;
            matBtn.setBackgroundColor(theme.cardBackgroundColor);
            matBtn.setTextColor(theme.textPrimaryColor);
            matBtn.setStrokeColor(ColorStateList.valueOf(theme.btnStrokeColor));
        }
    }

    private void applyThemeRecursive(View view, Theme theme) {
        if (view instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) view;
            if (card.getLayoutParams() != null && card.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) {
                card.setCardBackgroundColor(theme.cardBackgroundColor);
                card.setStrokeColor(theme.cardStrokeColor);
            }
        } else if (view instanceof TextView && !(view instanceof Button)) {
            TextView tv = (TextView) view;
            if (tv.getId() != R.id.tv_info_header) {
                if (tv.getTextSize() >= 14 * getResources().getDisplayMetrics().scaledDensity) {
                    tv.setTextColor(theme.textPrimaryColor);
                } else {
                    tv.setTextColor(theme.textSecondaryColor);
                }
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyThemeRecursive(vg.getChildAt(i), theme);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatusBarHelper.hideSystemBars(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            StatusBarHelper.hideSystemBars(this);
        }
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
        }
    }
}
