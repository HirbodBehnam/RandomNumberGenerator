package com.hirbod.randomnumbergenerator;

import android.app.Activity;

import ir.tapsell.sdk.Tapsell;
import ir.tapsell.sdk.TapsellAd;
import ir.tapsell.sdk.TapsellAdRequestListener;
import ir.tapsell.sdk.TapsellAdRequestOptions;
import ir.tapsell.sdk.TapsellAdShowListener;
import ir.tapsell.sdk.TapsellShowOptions;

class AD {
    private static TapsellAd tapsellAd = null;
    static void InitAd(final Activity activity){

    }
    static void LoadFullScreenAd(final Activity activity){
        TapsellAdRequestOptions options = new TapsellAdRequestOptions();
        options.setCacheType(TapsellAdRequestOptions.CACHE_TYPE_STREAMED);
        Tapsell.requestAd(activity, "5cfd1c8daf72830001b77f5f", options, new TapsellAdRequestListener() {
            @Override
            public void onError(String s) {

            }

            @Override
            public void onAdAvailable(TapsellAd tapsellAd) {
                AD.tapsellAd = tapsellAd;
            }

            @Override
            public void onNoAdAvailable() {

            }

            @Override
            public void onNoNetwork() {

            }

            @Override
            public void onExpiring(TapsellAd tapsellAd) {

            }
        });
    }
    static void ShowFullScreenAD(final Activity activity){
        if(tapsellAd != null)
            tapsellAd.show(activity, new TapsellShowOptions(), new TapsellAdShowListener() {
                @Override
                public void onOpened(TapsellAd tapsellAd) {
                    AD.tapsellAd = null;
                    LoadFullScreenAd(activity);
                }

                @Override
                public void onClosed(TapsellAd tapsellAd) {
                    AD.tapsellAd = null;
                    LoadFullScreenAd(activity);
                }
            });
    }
    static void LoadBanner(final Activity activity){

    }
}
