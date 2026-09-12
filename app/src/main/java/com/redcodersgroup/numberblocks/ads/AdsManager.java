package com.redcodersgroup.numberblocks.ads;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import com.redcodersgroup.numberblocks.R;

public class AdsManager {
    private static AdsManager instance;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    private AdsManager() {}

    public static synchronized AdsManager getInstance() {
        if (instance == null) instance = new AdsManager();
        return instance;
    }

    private String getBannerId(Context context) {
        return context.getString(R.string.admob_banner_id);
    }

    private String getInterstitialId(Context context) {
        return context.getString(R.string.admob_interstitial_id);
    }

    private String getRewardedId(Context context) {
        return context.getString(R.string.admob_rewarded_id);
    }

    public void init(Context context) {
        try {
            MobileAds.initialize(context, initializationStatus -> {});
            loadInterstitial(context);
            loadRewarded(context);
        } catch (Exception ignored) {}
    }

    public void loadBanner(Context context, ViewGroup container) {
        try {
            AdView adView = new AdView(context);
            adView.setAdUnitId(getBannerId(context));
            adView.setAdSize(AdSize.BANNER);
            container.removeAllViews();
            container.addView(adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } catch (Exception ignored) {}
    }

    public void loadInterstitial(Context context) {
        try {
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(context, getInterstitialId(context), adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd ad) { interstitialAd = ad; }
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) { interstitialAd = null; }
            });
        } catch (Exception ignored) {}
    }

    public void showInterstitial(Activity activity) {
        if (interstitialAd != null) {
            interstitialAd.show(activity);
            loadInterstitial(activity);
        }
    }

    public void loadRewarded(Context context) {
        try {
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(context, getRewardedId(context), adRequest, new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull RewardedAd ad) { rewardedAd = ad; }
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) { rewardedAd = null; }
            });
        } catch (Exception ignored) {}
    }

    public void showRewarded(Activity activity, OnUserEarnedRewardListener listener) {
        if (rewardedAd != null) {
            rewardedAd.show(activity, listener);
            loadRewarded(activity);
        } else {
            // If ad is not ready or offline, fulfill reward directly so player isn't left stuck in Game Over
            if (listener != null) {
                listener.onUserEarnedReward(new com.google.android.gms.ads.rewarded.RewardItem() {
                    @Override
                    public int getAmount() { return 1; }
                    @NonNull
                    @Override
                    public String getType() { return "revive"; }
                });
            }
            loadRewarded(activity);
        }
    }
}
