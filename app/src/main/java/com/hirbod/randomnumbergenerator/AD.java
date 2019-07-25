package com.hirbod.randomnumbergenerator;

import android.app.Activity;

import ir.tapsell.sdk.Tapsell;
import ir.tapsell.sdk.TapsellAd;
import ir.tapsell.sdk.TapsellAdRequestListener;
import ir.tapsell.sdk.TapsellAdRequestOptions;
import ir.tapsell.sdk.TapsellShowOptions;

class AD {
    static void InitAd(final Activity activity){

    }
    static void ShowFullScreenAD(final Activity activity){
        TapsellAdRequestOptions options = new TapsellAdRequestOptions();
        options.setCacheType(TapsellAdRequestOptions.CACHE_TYPE_STREAMED);
        Tapsell.requestAd(activity, "5cfd1c8daf72830001b77f5f", options, new TapsellAdRequestListener() {
            @Override
            public void onError(String s) {

            }

            @Override
            public void onAdAvailable(TapsellAd tapsellAd) {
                tapsellAd.show(activity,new TapsellShowOptions());
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
    static void LoadBanner(final Activity activity){

    }
}
