package com.redcodersgroup.numberblocks.profile;

import android.app.Activity;
import android.net.Uri;
import android.widget.ImageView;
import com.redcodersgroup.numberblocks.R;
import com.redcodersgroup.numberblocks.games.PlayGamesManager;
import com.redcodersgroup.numberblocks.storage.PreferencesManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the predefined in-game avatar choices and Google Play Games avatar loading.
 */
public class AvatarManager {

    public static final String DEFAULT_AVATAR_ID = "avatar_hero";
    public static final String GOOGLE_AVATAR_ID = "avatar_google";

    public static class AvatarItem {
        public final String id;
        public final String name;
        public final int drawableResId;
        public final boolean isGoogle;

        public AvatarItem(String id, String name, int drawableResId) {
            this(id, name, drawableResId, false);
        }

        public AvatarItem(String id, String name, int drawableResId, boolean isGoogle) {
            this.id = id;
            this.name = name;
            this.drawableResId = drawableResId;
            this.isGoogle = isGoogle;
        }
    }

    private static final List<AvatarItem> PREDEFINED_AVATARS;

    static {
        List<AvatarItem> list = new ArrayList<>();
        list.add(new AvatarItem("avatar_hero", "Bubble Hero", R.drawable.ic_avatar_hero));
        list.add(new AvatarItem("avatar_princess", "Princess", R.drawable.ic_avatar_princess));
        list.add(new AvatarItem("avatar_cat", "Lucky Cat", R.drawable.ic_avatar_cat));
        list.add(new AvatarItem("avatar_panda", "Bao Panda", R.drawable.ic_avatar_panda));
        list.add(new AvatarItem("avatar_fox", "Rusty Fox", R.drawable.ic_avatar_fox));
        list.add(new AvatarItem("avatar_frog", "Hoppy Frog", R.drawable.ic_avatar_frog));
        list.add(new AvatarItem("avatar_penguin", "Pip Penguin", R.drawable.ic_avatar_penguin));
        list.add(new AvatarItem("avatar_wizard", "Star Mage", R.drawable.ic_avatar_wizard));
        list.add(new AvatarItem("avatar_crown", "Crown Master", R.drawable.ic_avatar_crown));
        list.add(new AvatarItem("avatar_lightning", "Flash Surge", R.drawable.ic_avatar_lightning));
        list.add(new AvatarItem("avatar_rocket", "Sky Rocket", R.drawable.ic_avatar_rocket));
        list.add(new AvatarItem("avatar_star", "Super Star", R.drawable.ic_avatar_star));
        PREDEFINED_AVATARS = Collections.unmodifiableList(list);
    }

    public static List<AvatarItem> getPredefinedAvatars() {
        return PREDEFINED_AVATARS;
    }

    public static int getAvatarDrawable(String avatarId) {
        if (avatarId == null || avatarId.isEmpty()) {
            return R.drawable.ic_avatar_hero;
        }
        for (AvatarItem item : PREDEFINED_AVATARS) {
            if (item.id.equalsIgnoreCase(avatarId)) {
                return item.drawableResId;
            }
        }
        return R.drawable.ic_avatar_hero;
    }

    public static AvatarItem getAvatarById(String avatarId) {
        if (avatarId == null || avatarId.isEmpty()) {
            return PREDEFINED_AVATARS.get(0);
        }
        for (AvatarItem item : PREDEFINED_AVATARS) {
            if (item.id.equalsIgnoreCase(avatarId)) {
                return item;
            }
        }
        return PREDEFINED_AVATARS.get(0);
    }

    /**
     * Loads the avatar into the ImageView, handling both local predefined avatars
     * and the Google Play Games original profile image.
     */
    public static void loadAvatar(Activity activity, ImageView imageView, String avatarId) {
        if (activity == null || imageView == null) return;
        PreferencesManager prefs = PreferencesManager.getInstance(activity);

        if (GOOGLE_AVATAR_ID.equalsIgnoreCase(avatarId)) {
            String uriStr = prefs.getGoogleAvatarUri();
            if (uriStr != null && !uriStr.isEmpty()) {
                PlayGamesManager.getInstance().loadPlayerImage(activity, imageView, Uri.parse(uriStr), R.drawable.ic_avatar_hero);
                return;
            }
        }
        imageView.setImageResource(getAvatarDrawable(avatarId));
    }
}
