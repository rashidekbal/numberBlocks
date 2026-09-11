package com.redcodersgroup.numberblocks.ui;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class StatusBarHelper {

    /**
     * Applies system bar (status bar, navigation bar, cutout) insets to the given root view,
     * ensuring that screen content does not collide or interact with the status bar or navigation bar.
     */
    public static void applySystemBarInsets(View rootView) {
        if (rootView == null) return;

        final int initialStart = rootView.getPaddingStart();
        final int initialTop = rootView.getPaddingTop();
        final int initialEnd = rootView.getPaddingEnd();
        final int initialBottom = rootView.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            v.setPaddingRelative(
                initialStart + insets.left,
                initialTop + insets.top,
                initialEnd + insets.right,
                initialBottom + insets.bottom
            );
            return windowInsets;
        });

        // Request apply insets if attached
        ViewCompat.requestApplyInsets(rootView);
    }

    /**
     * Updates the status bar and navigation bar background colors and icon contrasts
     * based on the supplied background color.
     */
    public static void updateSystemBars(Activity activity, int backgroundColor) {
        if (activity == null) return;
        Window window = activity.getWindow();
        if (window == null) return;

        window.setStatusBarColor(backgroundColor);
        window.setNavigationBarColor(backgroundColor);

        double luminance = (0.299 * Color.red(backgroundColor)
                + 0.587 * Color.green(backgroundColor)
                + 0.114 * Color.blue(backgroundColor)) / 255.0;
        boolean isLight = luminance > 0.5;

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(isLight);
            controller.setAppearanceLightNavigationBars(isLight);
        }
    }
}
