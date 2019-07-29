package com.hirbod.randomnumbergenerator;

import android.app.Activity;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.Random;


class AD {
    private static InterstitialAd mInterstitialAd;

    static void InitAd(final Activity activity){
        MobileAds.initialize(activity, "ca-app-pub-4493333048205720~5460376984");
    }
    static void LoadFullScreenAd(final Activity activity){
        mInterstitialAd = new InterstitialAd(activity);
        mInterstitialAd.setAdUnitId("ca-app-pub-4493333048205720/6905320597");
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdLoaded() {
                Log.v("ADS","InterstitialAd Loaded");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.d("ADS","InterstitialAd error: "+ i);
            }
        });
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }
    static void ShowFullScreenAD(final Activity activity){
        if (mInterstitialAd.isLoaded())
            if(new Random().nextBoolean())
                mInterstitialAd.show();
    }
    static void LoadBanner(final Activity activity){
        AdRequest adRequest = new AdRequest.Builder().build();
        AdView adView = new AdView(activity);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-4493333048205720/5209095549");
        adView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        ((LinearLayout) activity.findViewById(R.id.BottomOfPageLayout)).addView(adView);
        adView.loadAd(adRequest);
    }
}
