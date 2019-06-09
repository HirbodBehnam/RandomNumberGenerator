package com.hirbod.randomnumbergenerator;

import android.app.Application;

import ir.tapsell.sdk.Tapsell;

public class AppClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Tapsell.initialize(this,"ggkhnrnpnlcljtdtgbjpqckgmqoqgrcifncpahthrlkqjcaapphifkogbegcbobrldpbif");
    }
}
