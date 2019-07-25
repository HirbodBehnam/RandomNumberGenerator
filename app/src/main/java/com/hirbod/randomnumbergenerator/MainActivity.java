package com.hirbod.randomnumbergenerator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.URL;

import static com.hirbod.randomnumbergenerator.Functions.SetClipboard;
import static com.hirbod.randomnumbergenerator.Functions.fullRandomBig;

public class MainActivity extends Activity {
    static int resDialogMulti = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Max and min holder
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString("MaxS",preferences.getString("MaxS","100"));
        editor.putString("MinS",preferences.getString("MinS","1"));
        editor.putBoolean("Copy",preferences.getBoolean("Copy",true));
        editor.putBoolean("multiRandom",preferences.getBoolean("multiRandom",false));
        editor.putInt("Lang",preferences.getInt("Lang",0));
        editor.apply();
        //Set lang
        if(preferences.getInt("Lang",0) == 1)
            changeFarsi();
        //Disable edits on main text box
        findViewById(R.id.editText).setFocusable(false);
        //Auto copy
        ((CheckBox) findViewById(R.id.checkBox)).setChecked(preferences.getBoolean("Copy",true));
        ((CheckBox) findViewById(R.id.checkBox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    editor.putBoolean("Copy",isChecked);
                    editor.apply();
                }
        });
        //Multi-Random
        ((CheckBox) findViewById(R.id.multiRandomCheckbox)).setChecked(preferences.getBoolean("multiRandom",false));
        ((CheckBox) findViewById(R.id.multiRandomCheckbox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("multiRandom",isChecked);
                editor.apply();
            }
        });
        findViewById(R.id.multiplyHelp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mad = new AlertDialog.Builder(MainActivity.this);
                if(preferences.getInt("Lang",0) == 1){
                    mad.setTitle("راهنما")
                            .setMessage("این گزینه این قابلیت را به شما می دهد که چندین عدد تصادفی در یک محدوده بسازید. همچنین برنامه میانگین اعداد را به شما می دهد و می توانید آنرا به عنوان فایل متنی ذخیره کنید.")
                            .setPositiveButton("باشه",null);
                }else {
                    mad
                            .setTitle("Help")
                            .setMessage("Use this option to create multiply random numbers is specific range. App will also give you the average of numbers and write it as text.")
                            .setPositiveButton("OK",null);
                }
                mad.setIcon(R.drawable.ic_help_white_24dp);
                mad.show();
            }
        });
        //Settings
        findViewById(R.id.SettingsBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });
        //Copy
        findViewById(R.id.Copy_BTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetClipboard(MainActivity.this,((EditText) findViewById(R.id.editText)).getText().toString());
                Toast.makeText(MainActivity.this,"Copied",Toast.LENGTH_SHORT).show();
            }
        });
        //Set Max and Min
        ((EditText) findViewById(R.id.MaxNumber_EditText)).setText(preferences.getString("MaxS","100"));
        ((EditText) findViewById(R.id.MinNumber_EditText)).setText(preferences.getString("MinS","1"));
        //Generate Button
        findViewById(R.id.Generate_BTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strMin = ((EditText) findViewById(R.id.MinNumber_EditText)).getText().toString(), strMax = ((EditText) findViewById(R.id.MaxNumber_EditText)).getText().toString();
                if(preferences.getBoolean("multiRandom",false)){//Multi random
                    // Values
                    float Max;
                    float Min;
                    try {
                        Max = Float.valueOf(strMax);
                        Min = Float.valueOf(strMin);
                        if(Max <= Min) throw new Exception("Min number is bigger or equal to Max number.");
                        if(Max > Integer.MAX_VALUE)
                            throw new Exception("Max number is bigger than " + Integer.MAX_VALUE);
                        if(Min > Integer.MAX_VALUE)
                            throw new Exception("Min number is bigger than" + Integer.MAX_VALUE);
                        if(Max < Integer.MIN_VALUE)
                            throw new Exception("Max number is smaller than " + Integer.MIN_VALUE);
                        if(Min < Integer.MIN_VALUE)
                            throw new Exception("Min number is smaller than " + Integer.MIN_VALUE);

                    }catch (Exception ex){
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage(ex.toString().substring(ex.toString().indexOf(":") + 2))
                                .setTitle("Error")
                                .setPositiveButton("OK",null)
                                .show();
                        return;
                    }
                    //Float fix
                    ((EditText) findViewById(R.id.MinNumber_EditText)).setText(String.valueOf(Min));
                    ((EditText) findViewById(R.id.MaxNumber_EditText)).setText(String.valueOf(Max));
                    //Save
                    editor.putString("MaxS",((EditText) findViewById(R.id.MaxNumber_EditText)).getText().toString());
                    editor.putString("MinS",((EditText) findViewById(R.id.MinNumber_EditText)).getText().toString());
                    editor.apply();
                    //Check multi random
                    getNumberRandomDialog(Min,Max);
                }else {//Single random
                    // Values
                    BigInteger Max, Min;
                    if (((EditText) findViewById(R.id.MaxNumber_EditText)).getText().toString().contains(".") ||
                            ((EditText) findViewById(R.id.MinNumber_EditText)).getText().toString().contains(".")) {//Decimal Numbers
                        if(strMax.endsWith("."))
                            strMax += "0";
                        if(strMin.endsWith("."))
                            strMin += "0";
                        if(!strMax.contains("."))
                            strMax += ".0";
                        if(!strMin.contains("."))
                            strMin += ".0";
                        int decimalPlaces = Math.max(strMax.split("\\.")[1].length() , strMin.split("\\.")[1].length());
                        while (strMax.split("\\.")[1].length() < decimalPlaces)
                            strMax += "0";
                        while (strMin.split("\\.")[1].length() < decimalPlaces)
                            strMin += "0";
                        //Now both numbers have same decimal digits
                        strMax = strMax.replace(".","");
                        strMin = strMin.replace(".","");
                        try {
                            Max = new BigInteger(strMax);
                            strMax = null;
                            Min = new BigInteger(strMin);
                            strMin = null;
                            if (Max.compareTo(Min) <= 0)
                                throw new Exception("Min number is bigger or equal to Max number.");
                        } catch (Exception ex) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage(ex.toString().substring(ex.toString().indexOf(":") + 2))
                                    .setTitle("Error")
                                    .setPositiveButton("OK", null)
                                    .show();
                            return;
                        }
                        StringBuilder res = new StringBuilder(fullRandomBig(Min, Max).toString());
                        boolean negative = false;
                        if(res.charAt(0) == '-') {
                            res = new StringBuilder(res.substring(1));
                            negative = true;
                        }
                        if(res.length() <= decimalPlaces) {
                            while (res.length() < decimalPlaces)
                                res.insert(0, "0");
                            res.insert(0, "0.");
                        }else
                            res.insert(res.length() - decimalPlaces , '.');
                        if(negative)
                            res.insert(0,'-');
                        ((EditText) findViewById(R.id.editText)).setText(res);
                    } else { //Integer numbers
                        try {
                            Max = new BigInteger(strMax);
                            Min = new BigInteger(strMin);
                            if (Max.compareTo(Min) <= 0)
                                throw new Exception("Min number is bigger or equal to Max number.");
                        } catch (Exception ex) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage(ex.toString().substring(ex.toString().indexOf(":") + 2))
                                    .setTitle("Error")
                                    .setPositiveButton("OK", null)
                                    .show();
                            return;
                        }
                        ((EditText) findViewById(R.id.editText)).setText(fullRandomBig(Min,Max).toString());
                    }
                }
                //Save Values
                editor.putString("MaxS",((EditText) findViewById(R.id.MaxNumber_EditText)).getText().toString());
                editor.putString("MinS",((EditText) findViewById(R.id.MinNumber_EditText)).getText().toString());
                editor.apply();
                //Auto Copy
                if(((CheckBox) findViewById(R.id.checkBox)).isChecked())
                    SetClipboard(MainActivity.this,((EditText) findViewById(R.id.editText)).getText().toString());
            }
        });
        //Updater{
        try{
            int version = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            new CheckUpdates(this, version).execute();
        }catch (PackageManager.NameNotFoundException ex){
            ex.printStackTrace();
        }
    }
    private void getNumberRandomDialog(float Min,float Max){
        //Final
        final Context c = this;
        final float Min1 = Min;
        final float Max1 = Max;
        //Other
        resDialogMulti = -1;
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter numbers to generate:");
        if(preferences.getInt("Lang",0) == 1){builder.setTitle("لطفا تعداد اعداد را وارد کنید.");}
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    resDialogMulti = Integer.parseInt(input.getText().toString());
                    if(resDialogMulti > 5000000){
                        resDialogMulti = -1;
                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                        if(preferences.getInt("Lang",0) == 1){
                            builder.setTitle("خطا");
                            builder.setMessage("شما نمی توانید عددی بیشتر از پنج میلیون وارد کنید.");
                        }else{
                            builder.setTitle("Error");
                            builder.setMessage("You cannot enter number more than 5000000.");
                        }
                        builder.setPositiveButton("OK",null);
                        builder.show();
                    }else {
                        showNums(Min1,Max1);
                    }
                }catch (NumberFormatException ex){
                    resDialogMulti = -1;
                    showNums(Min1,Max1);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    private void showNums(float Min,float Max){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        if(resDialogMulti == 0 || resDialogMulti == -1) {//0 or nothing
            AlertDialog.Builder mad = new AlertDialog.Builder(MainActivity.this);
            if (preferences.getInt("Lang", 0) == 0) {
                mad
                        .setTitle("Error")
                        .setMessage("The value could not be 0 or empty.");
            } else {
                mad
                        .setTitle("خطا")
                        .setMessage("عدد وارد شده نمی تواند 0 یا خالی باشد.");
            }
            mad.setPositiveButton("OK", null);
            mad.show();
            return;
        }
        //Pass it to activity
        Bundle b = new Bundle();
        b.putInt("ToCreate",resDialogMulti);
        b.putFloat("Max",Max);
        b.putFloat("Min",Min);
        Intent i = new Intent(MainActivity.this, ShowNumsActivity.class);
        i.putExtras(b);
        MainActivity.this.startActivity(i);
    }
    private void changeFarsi(){
        ((Button) findViewById(R.id.Generate_BTN)).setText("بساز");
        ((Button) findViewById(R.id.Copy_BTN)).setText("کپی");
        ((Button) findViewById(R.id.SettingsBTN)).setText("تنظیمات");
        ((TextView) findViewById(R.id.textView2)).setText("بیشترین عدد");
        ((TextView) findViewById(R.id.textView3)).setText("کمترین عدد");
        ((CheckBox) findViewById(R.id.checkBox)).setText("کپی خودکار");
        ((CheckBox) findViewById(R.id.multiRandomCheckbox)).setText("ساخت چندین عدد تصادفی");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.HelpBTN) {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            if(preferences.getInt("Lang",0) == 1){
                ab.setTitle("راهنما");
                ab.setMessage("برای استفاده از بخش عدد تصادفی با رقم اعشار کافی است که بعد از عدد خود ممیز وارد کنید." + "\n" +  "مثلا اگر شما 1.00 را وارد کنید برنامه اعداد تصادفی شما را با دو رقم اعشار میسازد." + "\n\n"
                + "برای اینکه از این قابلیت در قسمت چندین عدد تصادفی استفاده کنید، باید همین کار را بکنید با این تفاوت که رقم آخر عدد صفر نباشد. مثلا 4.0001");
            }else{
                ab.setTitle("Help");
                ab.setMessage("To generate decimal numbers, add the decimal digits you want. For example if you use 0.32 the application will generate random numbers with 2 decimal points.\nIn multi number generator your last digit after decimal must not be zero. For example 3.00001 will generate numbers with 5 decimal digits.");
            }
            ab.setPositiveButton("OK",null);
            ab.setIcon(R.drawable.ic_help_white_24dp);
            ab.show();
        }else if(item.getItemId() == R.id.SequenceGenerator)
            startActivity(new Intent(getApplicationContext(), MultiStepGeneratorActivity.class));
        super.onOptionsItemSelected(item);
        return true;
    }
    private static class CheckUpdates extends AsyncTask<Void,Void,Integer>{
        private WeakReference<MainActivity> activityReference;
        private int currentVersion;
        // only retain a weak reference to the activity
        CheckUpdates(MainActivity context, int currentVersion) {
            activityReference = new WeakReference<>(context);
            this.currentVersion = currentVersion;
        }
        @Override
        protected Integer doInBackground(Void... voids) {
            //https://alvinalexander.com/blog/post/java/java-how-read-from-url-string-text
            int webVersion = Integer.MIN_VALUE;
            try
            {
                URL url = new URL("https://raw.githubusercontent.com/HirbodBehnam/RandomNumberGenerator/master/app/build.gradle");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    if(line.trim().startsWith("versionCode")){
                        webVersion = Integer.parseInt(line.trim().split(" ")[1]);
                        break;
                    }
                }
                bufferedReader.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
                return -1;
            }
            return webVersion > currentVersion ? webVersion : -1;
        }

        @Override
        protected void onPostExecute(Integer nextVersion) {
            super.onPostExecute(nextVersion);
            if(nextVersion == -1)
                return;
            //https://stackoverflow.com/a/46166223/4213397
            final MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing())
                return;
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            new AlertDialog.Builder(activity)
                    .setMessage(preferences.getInt("Lang",0) == 1 ? ("یک آپدیت برنامه به ورژن ساخت " + nextVersion + " موجود است.") : ("A new update to build version " + nextVersion + " is available. Do you want to update?"))
                    .setTitle(preferences.getInt("Lang",0) == 1 ? "آپدیت برنامه" : "Update Available")
                    .setPositiveButton(preferences.getInt("Lang",0) == 1 ? "آپدیت" :"Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://cafebazaar.ir/app/com.hirbod.randomnumbergenerator/"));
                            activity.startActivity(browserIntent);
                        }
                    })
                    .setNegativeButton(preferences.getInt("Lang",0) == 1 ? "بعدا" :"Later", null)
                    .show();
        }
    }
}