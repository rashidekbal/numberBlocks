package com.redcodersgroup.numberblocks.games;

import android.app.Activity;
import android.util.Log;
import android.net.Uri;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.redcodersgroup.numberblocks.R;
import com.redcodersgroup.numberblocks.storage.PreferencesManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Manages Google Play Games Services v2 integration, including
 * automatic sign-in prompt, score submission, leaderboard data fetching,
 * and launching leaderboard overlays.
 */
public class PlayGamesManager {

    private static final String TAG = "PlayGamesManager";
    public static final int RC_LEADERBOARDS = 9002;

    private static PlayGamesManager instance;

    public interface LeaderboardCallback {
        void onScoresLoaded(List<LeaderboardEntry> entries, LeaderboardEntry currentPlayerStanding);
        void onFailed(String errorMessage);
    }

    public interface PlayerProfileCallback {
        void onProfileLoaded(String displayName, Uri iconUri);
        void onFailed(String errorMessage);
    }

    public interface SignInCallback {
        void onComplete(boolean success);
    }

    private PlayGamesManager() {}

    public static synchronized PlayGamesManager getInstance() {
        if (instance == null) {
            instance = new PlayGamesManager();
        }
        return instance;
    }

    /**
     * Loads the authenticated Google Play Games player profile (name and avatar URI).
     */
    public void loadCurrentPlayerProfile(Activity activity, @NonNull PlayerProfileCallback callback) {
        if (activity == null) {
            callback.onFailed("Activity is null");
            return;
        }

        try {
            PlayGames.getPlayersClient(activity)
                    .getCurrentPlayer()
                    .addOnSuccessListener(player -> {
                        if (player != null) {
                            String name = player.getDisplayName();
                            Uri iconUri = player.getIconImageUri();
                            if (iconUri == null) {
                                iconUri = player.getHiResImageUri();
                            }
                            if (name != null) {
                                PreferencesManager.getInstance(activity).setGooglePlayerName(name);
                            }
                            if (iconUri != null) {
                                PreferencesManager.getInstance(activity).setGoogleAvatarUri(iconUri.toString());
                            }
                            callback.onProfileLoaded(name, iconUri);
                        } else {
                            callback.onFailed("Player is null");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Failed to load current player profile: " + e.getMessage());
                        callback.onFailed(e.getMessage());
                    });
        } catch (Exception e) {
            Log.w(TAG, "Error loading player profile: " + e.getMessage());
            callback.onFailed(e.getMessage());
        }
    }

    /**
     * Loads a player avatar Uri into an ImageView using Google Play Services ImageManager.
     */
    public void loadPlayerImage(Activity activity, ImageView imageView, Uri imageUri, int defaultDrawableRes) {
        if (activity == null || imageView == null) return;
        if (imageUri != null) {
            try {
                ImageManager.create(activity).loadImage(imageView, imageUri, defaultDrawableRes);
            } catch (Exception e) {
                imageView.setImageResource(defaultDrawableRes);
            }
        } else {
            imageView.setImageResource(defaultDrawableRes);
        }
    }

    /**
     * Checks if the user is authenticated; if not, triggers the Google Play Games
     * interactive sign-in flow.
     */
    public void ensureSignedIn(Activity activity) {
        ensureSignedIn(activity, null);
    }

    public void ensureSignedIn(Activity activity, SignInCallback callback) {
        if (activity == null) {
            if (callback != null) callback.onComplete(false);
            return;
        }
        try {
            PlayGames.getGamesSignInClient(activity)
                    .isAuthenticated()
                    .addOnCompleteListener(task -> {
                        boolean authenticated = task.isSuccessful()
                                && task.getResult() != null
                                && task.getResult().isAuthenticated();
                        if (!authenticated) {
                            Log.d(TAG, "User not authenticated, prompting Play Games sign-in");
                            PlayGames.getGamesSignInClient(activity)
                                    .signIn()
                                    .addOnCompleteListener(signInTask -> {
                                        boolean signedIn = signInTask.isSuccessful() && signInTask.getResult() != null && signInTask.getResult().isAuthenticated();
                                        if (signedIn) {
                                            Log.d(TAG, "Sign-in succeeded, syncing historical local scores");
                                            syncLocalScores(activity);
                                            loadCurrentPlayerProfile(activity, new PlayerProfileCallback() {
                                                @Override
                                                public void onProfileLoaded(String displayName, Uri iconUri) {}
                                                @Override
                                                public void onFailed(String errorMessage) {}
                                            });
                                        }
                                        if (callback != null) {
                                            callback.onComplete(signedIn);
                                        }
                                    });
                        } else {
                            Log.d(TAG, "User is already authenticated with Play Games, syncing historical local scores");
                            syncLocalScores(activity);
                            loadCurrentPlayerProfile(activity, new PlayerProfileCallback() {
                                @Override
                                public void onProfileLoaded(String displayName, Uri iconUri) {}
                                @Override
                                public void onFailed(String errorMessage) {}
                            });
                            if (callback != null) {
                                callback.onComplete(true);
                            }
                        }
                    });
        } catch (Exception e) {
            Log.w(TAG, "Failed in ensureSignedIn: " + e.getMessage());
            if (callback != null) {
                callback.onComplete(false);
            }
        }
    }

    /**
     * Syncs all existing local high scores (4x4, 5x5, 6x6) to Google Play Games Leaderboards.
     * Guarantees that players updating the app have their previous scores uploaded immediately.
     */
    public void syncLocalScores(Activity activity) {
        if (activity == null) return;
        PreferencesManager prefs = PreferencesManager.getInstance(activity);
        for (int size = 4; size <= 6; size++) {
            int localBest = prefs.getBestScore(size);
            if (localBest > 0) {
                submitScore(activity, size, localBest);
            }
        }
    }

    /**
     * Prompts for sign-in once on first launch after app installation to ensure
     * the user is signed into Play Games.
     */
    public void promptFirstLaunchSignIn(Activity activity) {
        if (activity == null) return;
        PreferencesManager prefs = PreferencesManager.getInstance(activity);
        if (!prefs.hasAttemptedPlayGamesFirstSignIn()) {
            prefs.setAttemptedPlayGamesFirstSignIn(true);
            ensureSignedIn(activity);
        }
    }

    /**
     * Submits a score for the given board size (4, 5, or 6).
     */
    public void submitScore(Activity activity, int boardSize, long score) {
        if (activity == null || score <= 0) return;

        String leaderboardId = getLeaderboardId(activity, boardSize);
        if (isPlaceholderOrEmpty(leaderboardId)) {
            Log.d(TAG, "Leaderboard ID for " + boardSize + "x" + boardSize + " is not configured yet (current: " + leaderboardId + ")");
            return;
        }

        try {
            PlayGames.getLeaderboardsClient(activity).submitScore(leaderboardId, score);
            Log.d(TAG, "Submitted score " + score + " to leaderboard: " + leaderboardId);
        } catch (Exception e) {
            Log.w(TAG, "Failed to submit score to leaderboard: " + e.getMessage());
        }
    }

    /**
     * Loads the top scores for the specified board size in-game.
     */
    public void loadScores(Activity activity, int boardSize, @NonNull LeaderboardCallback callback) {
        if (activity == null) {
            callback.onFailed("Activity is null");
            return;
        }

        String leaderboardId = getLeaderboardId(activity, boardSize);
        if (isPlaceholderOrEmpty(leaderboardId)) {
            int localBest = PreferencesManager.getInstance(activity).getBestScore(boardSize);
            List<LeaderboardEntry> list = new ArrayList<>();
            LeaderboardEntry localEntry = null;
            if (localBest > 0) {
                localEntry = new LeaderboardEntry("1", "You (Local)", localBest, String.format(Locale.getDefault(), "%,d", localBest), true);
                list.add(localEntry);
            }
            callback.onScoresLoaded(list, localEntry);
            return;
        }

        try {
            PlayGames.getLeaderboardsClient(activity)
                    .loadTopScores(leaderboardId, LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC, 15, true)
                    .addOnSuccessListener(annotatedData -> {
                        LeaderboardsClient.LeaderboardScores data = annotatedData.get();
                        List<LeaderboardEntry> list = new ArrayList<>();

                        if (data != null && data.getScores() != null) {
                            LeaderboardScoreBuffer buffer = data.getScores();
                            try {
                                for (int i = 0; i < buffer.getCount(); i++) {
                                    LeaderboardScore score = buffer.get(i);
                                    String rankStr = String.valueOf(score.getRank());
                                    String name = score.getScoreHolderDisplayName();
                                    long rawScore = score.getRawScore();
                                    String formatted = score.getDisplayScore();
                                    list.add(new LeaderboardEntry(rankStr, name, rawScore, formatted, false));
                                }
                            } finally {
                                buffer.release();
                            }
                        }

                        fetchPlayerScoreAndComplete(activity, leaderboardId, boardSize, list, callback);
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Failed to load top scores: " + e.getMessage());
                        int localBest = PreferencesManager.getInstance(activity).getBestScore(boardSize);
                        List<LeaderboardEntry> fallbackList = new ArrayList<>();
                        LeaderboardEntry localEntry = null;
                        if (localBest > 0) {
                            localEntry = new LeaderboardEntry("-", "You", localBest, String.format(Locale.getDefault(), "%,d", localBest), true);
                            fallbackList.add(localEntry);
                        }
                        callback.onScoresLoaded(fallbackList, localEntry);
                    });
        } catch (Exception e) {
            Log.w(TAG, "Exception loading scores: " + e.getMessage());
            int localBest = PreferencesManager.getInstance(activity).getBestScore(boardSize);
            List<LeaderboardEntry> fallbackList = new ArrayList<>();
            LeaderboardEntry localEntry = null;
            if (localBest > 0) {
                localEntry = new LeaderboardEntry("-", "You", localBest, String.format(Locale.getDefault(), "%,d", localBest), true);
                fallbackList.add(localEntry);
            }
            callback.onScoresLoaded(fallbackList, localEntry);
        }
    }

    private void fetchPlayerScoreAndComplete(Activity activity, String leaderboardId, int boardSize, List<LeaderboardEntry> list, LeaderboardCallback callback) {
        try {
            PlayGames.getLeaderboardsClient(activity)
                    .loadCurrentPlayerLeaderboardScore(leaderboardId, LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC)
                    .addOnSuccessListener(annotatedScore -> {
                        LeaderboardScore myScore = annotatedScore.get();
                        LeaderboardEntry myEntry = null;
                        if (myScore != null) {
                            myEntry = new LeaderboardEntry(
                                    String.valueOf(myScore.getRank()),
                                    myScore.getScoreHolderDisplayName(),
                                    myScore.getRawScore(),
                                    myScore.getDisplayScore(),
                                    true
                            );
                        } else {
                            int localBest = PreferencesManager.getInstance(activity).getBestScore(boardSize);
                            if (localBest > 0) {
                                myEntry = new LeaderboardEntry("-", "You", localBest, String.format(Locale.getDefault(), "%,d", localBest), true);
                            }
                        }
                        callback.onScoresLoaded(list, myEntry);
                    })
                    .addOnFailureListener(e -> {
                        int localBest = PreferencesManager.getInstance(activity).getBestScore(boardSize);
                        LeaderboardEntry myEntry = null;
                        if (localBest > 0) {
                            myEntry = new LeaderboardEntry("-", "You", localBest, String.format(Locale.getDefault(), "%,d", localBest), true);
                        }
                        callback.onScoresLoaded(list, myEntry);
                    });
        } catch (Exception e) {
            callback.onScoresLoaded(list, null);
        }
    }

    /**
     * Opens the native Google Play Games overlay displaying all leaderboards.
     */
    public void showAllLeaderboards(Activity activity) {
        if (activity == null) return;

        try {
            PlayGames.getGamesSignInClient(activity)
                    .isAuthenticated()
                    .addOnCompleteListener(authTask -> {
                        boolean authenticated = authTask.isSuccessful()
                                && authTask.getResult() != null
                                && authTask.getResult().isAuthenticated();

                        if (authenticated) {
                            launchAllLeaderboardsIntent(activity);
                        } else {
                            Log.d(TAG, "User not authenticated in Play Games, prompting signIn()");
                            promptSignInAndLaunch(activity);
                        }
                    });
        } catch (Exception e) {
            Log.w(TAG, "Error checking authentication: " + e.getMessage());
            launchAllLeaderboardsIntent(activity);
        }
    }

    private void promptSignInAndLaunch(Activity activity) {
        try {
            PlayGames.getGamesSignInClient(activity)
                    .signIn()
                    .addOnCompleteListener(signInTask -> {
                        if (signInTask.isSuccessful() && signInTask.getResult() != null && signInTask.getResult().isAuthenticated()) {
                            Log.d(TAG, "Play Games sign-in successful, opening leaderboards");
                            launchAllLeaderboardsIntent(activity);
                        } else {
                            Log.w(TAG, "Play Games sign-in failed or cancelled by user");
                        }
                    });
        } catch (Exception e) {
            Log.w(TAG, "Failed to prompt Play Games sign-in: " + e.getMessage());
        }
    }

    private void launchAllLeaderboardsIntent(Activity activity) {
        try {
            PlayGames.getLeaderboardsClient(activity)
                    .getAllLeaderboardsIntent()
                    .addOnSuccessListener(intent -> {
                        try {
                            activity.startActivityForResult(intent, RC_LEADERBOARDS);
                        } catch (Exception e) {
                            Log.w(TAG, "Failed to launch all leaderboards activity: " + e.getMessage());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Failed to fetch all leaderboards intent: " + e.getMessage());
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            if (apiException.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
                                promptSignInAndLaunch(activity);
                            }
                        }
                    });
        } catch (Exception e) {
            Log.w(TAG, "PlayGames leaderboards client error: " + e.getMessage());
        }
    }

    /**
     * Opens the native Google Play Games overlay displaying a specific leaderboard.
     */
    public void showLeaderboard(Activity activity, int boardSize) {
        if (activity == null) return;

        String leaderboardId = getLeaderboardId(activity, boardSize);
        if (isPlaceholderOrEmpty(leaderboardId)) {
            showAllLeaderboards(activity);
            return;
        }

        try {
            PlayGames.getLeaderboardsClient(activity)
                    .getLeaderboardIntent(leaderboardId)
                    .addOnSuccessListener(intent -> {
                        try {
                            activity.startActivityForResult(intent, RC_LEADERBOARDS);
                        } catch (Exception e) {
                            Log.w(TAG, "Failed to launch leaderboard activity: " + e.getMessage());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Failed to fetch leaderboard intent, falling back to all leaderboards: " + e.getMessage());
                        showAllLeaderboards(activity);
                    });
        } catch (Exception e) {
            Log.w(TAG, "PlayGames client error: " + e.getMessage());
            showAllLeaderboards(activity);
        }
    }

    /**
     * Resolves the leaderboard ID resource for the given grid size.
     */
    public String getLeaderboardId(Activity activity, int boardSize) {
        switch (boardSize) {
            case 4:
                return activity.getString(R.string.leaderboard_4x4_id);
            case 5:
                return activity.getString(R.string.leaderboard_5x5_id);
            case 6:
                return activity.getString(R.string.leaderboard_6x6_id);
            default:
                return null;
        }
    }

    private boolean isPlaceholderOrEmpty(String id) {
        return id == null || id.trim().isEmpty() || id.startsWith("YOUR_");
    }
}
