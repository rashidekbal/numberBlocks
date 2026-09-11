package com.numberblocksmerge.ads;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class AdsManager {
    private static AdsManager instance;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    private static final String BANNER_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final String INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712";
    private static final String REWARDED_ID = "ca-app-pub-3940256099942544/5224354917";

    private AdsManager() {}

    public static synchronized AdsManager getInstance() {
        if (instance == null) instance = new AdsManager();
        return instance;
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
            adView.setAdUnitId(BANNER_ID);
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
            InterstitialAd.load(context, INTERSTITIAL_ID, adRequest, new InterstitialAdLoadCallback() {
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
            RewardedAd.load(context, REWARDED_ID, adRequest, new RewardedAdLoadCallback() {
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
        }
    }
}
