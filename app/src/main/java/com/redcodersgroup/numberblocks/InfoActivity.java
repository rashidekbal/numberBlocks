package com.redcodersgroup.numberblocks;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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
        }
        StatusBarHelper.updateSystemBars(this, theme.backgroundColor);
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
