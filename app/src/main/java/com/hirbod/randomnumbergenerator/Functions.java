package com.hirbod.randomnumbergenerator;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;

import java.math.BigInteger;

class Functions {
    private static BigInteger randBig(BigInteger upperLimit){
        BigInteger randomNumber;
        do {
            if(InnerRandom.UseSecureRandom)
                randomNumber = new BigInteger(upperLimit.bitLength(), InnerRandom.secureRandom);
            else
                randomNumber = new BigInteger(upperLimit.bitLength(), InnerRandom.random);
        } while (randomNumber.compareTo(upperLimit) >= 0);
        return randomNumber;
    }
    static BigInteger fullRandomBig(BigInteger Min,BigInteger Max){
        if(Min.compareTo(BigInteger.ZERO) >= 0){//Both numbers positive
            BigInteger delta = Max.subtract(Min);
            return randBig(delta).add(Min);
        }else if(Max.compareTo(BigInteger.ZERO) >= 0) //Max > 0 and Min < 0
        {
            Max = Max.subtract(Min);
            Max = randBig(Max); //Max in now the random number
            return Max.add(Min);
        }else //Both numbers less than 0
        {
            //after the abs(), the order of numbers will swap; So Min is the bigger value
            Min = Min.abs();
            Max = Max.abs();
            BigInteger delta = Min.subtract(Max);
            Min = randBig(delta).add(Max).add(BigInteger.ONE); //Min in now the random number
            return Min.negate();
        }
    }
    static void SetClipboard(Context context, String text){
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Random Number", text);
        clipboard.setPrimaryClip(clip);
    }
    static boolean isInteger(float str) { return str % 1 == 0; }
    static boolean isInteger(double str) {return str % 1 == 0;}
    static int getDecimal(float value){return String.valueOf(value).substring(String.valueOf(value).indexOf(".")).length();}
    static void startApplicationDetailsActivity(Activity activity) {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:com.hirbod.randomnumbergenerator"));
            activity.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    static boolean permission(final Activity activity){
        if(Build.VERSION.SDK_INT >= 23){
            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
                    AlertDialog.Builder mad = new AlertDialog.Builder(activity);
                    if(preferences.getInt("Lang",0) == 0) {
                        mad.setMessage("This permission is needed to save this list.");
                        mad.setTitle("Error");
                        mad.setNegativeButton("OK",null);
                        mad.setPositiveButton("Go To Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Functions.startApplicationDetailsActivity(activity);
                            }
                        });
                        mad.setNeutralButton("Request Again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                            }
                        });
                    }else{
                        mad.setMessage("این دسترسی برای ذخیره کردن این لیست نیاز است.");
                        mad.setTitle("خطا");
                        mad.setNegativeButton("باشه",null);
                        mad.setPositiveButton("برو به تنظیمات", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Functions.startApplicationDetailsActivity(activity);
                            }
                        });
                        mad.setNeutralButton("درخواست مجدد", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                            }
                        });
                    }
                    mad.show();
                    return false;
                } else {
                    activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    return false;
                }
            }
        }
        return true;
    }
}
