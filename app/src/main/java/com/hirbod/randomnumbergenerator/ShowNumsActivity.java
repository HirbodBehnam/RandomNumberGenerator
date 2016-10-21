package com.hirbod.randomnumbergenerator;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class ShowNumsActivity extends Activity {
    private static String fileName = "";
    private static float FMax = 0;
    private static float FMin = 0;
    private static float summed = 0;
    private static float ave = 0;
    private static float max = 0;
    private static float min = 0;
    private static ArrayList<String> randoms = new ArrayList<>();
    private static int resDialogMulti = 0;
    private SharedPreferences preferences;
    private Generator g = new Generator();
    Random rand = new Random();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_nums);
        if(Build.VERSION.SDK_INT >= 11){try{getActionBar().setDisplayHomeAsUpEnabled(true);}catch (Exception e){e.printStackTrace();}}
        //Set array
        Bundle b = this.getIntent().getExtras();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        FMax = b.getFloat("Max");
        FMin = b.getFloat("Min");
        resDialogMulti = b.getInt("ToCreate");
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.activity_listview, new String[]{"Generating Numbers..."});
        ((ListView) findViewById(R.id.listView)).setAdapter(adapter);
        //Reset every thing
        randoms = new ArrayList<>();
        summed = 0;
        rand = new Random();
        //Generate
        g.execute();
        //Save
        findViewById(R.id.SaveBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!permission()){return;}
                fileName = "";
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowNumsActivity.this);
                builder.setTitle("Please enter filename:");
                if(preferences.getInt("Lang",0) == 1){builder.setTitle("لطفا اسم فایل را وارد کنید.");}
                final EditText input = new EditText(ShowNumsActivity.this);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            fileName = input.getText().toString();
                            if(fileName.matches("") || fileName == null){throw new Exception();}
                            write();
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        });
    }
    @Override
    public void onBackPressed() {
        g.cancel(true);
        super.onBackPressed();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        g.cancel(true);
        super.onBackPressed();
        return true;
    }
    public static boolean isInteger(float str) {return str % 1 == 0;}
    private boolean permission(){
        if(Build.VERSION.SDK_INT >= 23){
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    AlertDialog.Builder mad = new AlertDialog.Builder(this);
                    if(preferences.getInt("Lang",0) == 0) {
                        mad.setMessage("This permission is needed to save this list.");
                        mad.setTitle("Error");
                        mad.setNegativeButton("OK",null);
                        mad.setPositiveButton("Go To Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startApplicationDetailsActivity();
                            }
                        });
                    }else{
                        mad.setMessage("این دسترسی برای ذخیره کردن این لیست نیاز است.");
                        mad.setTitle("خطا");
                        mad.setNegativeButton("باشه",null);
                        mad.setPositiveButton("برو به تنظیمات", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startApplicationDetailsActivity();
                            }
                        });
                    }
                    mad.show();
                    return false;
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    AlertDialog.Builder mad = new AlertDialog.Builder(this);
                    if(preferences.getInt("Lang",0) == 0) {
                        mad.setMessage("This permission is needed to save this list.");
                        mad.setMessage("Error");
                        mad.setNegativeButton("OK",null);
                        mad.setPositiveButton("Go To Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startApplicationDetailsActivity();
                            }
                        });
                    }else{
                        mad.setMessage("این دسترسی برای ذخیره کردن این لیست نیاز است.");
                        mad.setTitle("خطا");
                        mad.setNegativeButton("باشه",null);
                        mad.setPositiveButton("برو به تنظیمات", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startApplicationDetailsActivity();
                            }
                        });
                    }
                    mad.show();
                }
            }
        }
    }
    private void startApplicationDetailsActivity() {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + "com.hirbod.randomnumbergenerator"));
            startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void write(){
        StringBuilder sb = new StringBuilder();
        sb.append("Range From ");
        sb.append(FMin);
        sb.append(" To ");
        sb.append(FMax);
        sb.append(" :\n\n");
        for(String i : randoms) sb.append(i);
        sb.append("=================\n");
        sb.append("Sum: ");
        sb.append(summed);
        sb.append("\n");
        sb.append("Average: ");
        sb.append(ave);
        sb.append("\n");
        sb.append("Max Number: ");
        sb.append(max);
        sb.append("\n");
        sb.append("Min Number: ");
        sb.append(min);
        try{
            //Create new folder
            if(!new File(Environment.getExternalStorageDirectory() + File.separator + "Random Numbers List").exists())
                new File(Environment.getExternalStorageDirectory() + File.separator + "Random Numbers List").mkdir();
            //Write
            FileOutputStream stream = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + File.separator + "Random Numbers List" + File.separator + fileName + ".txt"));
            stream.write(sb.toString().getBytes());
            stream.close();
            Toast.makeText(this,"File saved at Storage/Random Numbers List/" + fileName + ".txt",Toast.LENGTH_LONG).show();
        }catch (Exception e){
            new AlertDialog.Builder(ShowNumsActivity.this)
                    .setMessage(e.toString().substring(e.toString().indexOf(":") + 2))
                    .setTitle("Error")
                    .setPositiveButton("OK",null)
                    .show();
        }
    }
    private class Generator extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {

            float temp;
            temp = random(FMin, FMax);
            if(isInteger(FMax) && isInteger(FMin)) {
                randoms.add(String.valueOf(temp).replace(".0",""));
                summed += temp;
                max = temp;
                min = temp;
                for (int i = 1; i < resDialogMulti; i++) {
                    temp = random(FMin, FMax);
                    randoms.add(String.valueOf(temp).replace(".0",""));
                    summed += temp;
                    if(temp > max) max = temp;
                    if(temp < min) min = temp;
                }
            }else{
                randoms.add(String.valueOf(temp));
                summed += temp;
                max = temp;
                min = temp;
                for (int i = 1; i < resDialogMulti; i++) {
                    temp = random(FMin, FMax);
                    randoms.add(String.valueOf(temp));
                    summed += temp;
                    if(temp > max) max = temp;
                    if(temp < min) min = temp;
                }
            }
            ave = summed / randoms.size();
            //Reporting sum and ect.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.SaveBTN).setEnabled(true);
                    if(isInteger(summed)){
                        int summedInt = Integer.parseInt(String.valueOf(summed).replace(".0",""));
                        if(preferences.getInt("Lang",0) == 0) {
                            ((TextView) findViewById(R.id.SumView)).setText("Sum: " + summedInt);
                        }else{
                            ((TextView) findViewById(R.id.SumView)).setText("جمع: " + summedInt);
                            ((Button) findViewById(R.id.SaveBTN)).setText("ذخیره");
                        }
                    }else{
                        if(preferences.getInt("Lang",0) == 0) {
                            ((TextView) findViewById(R.id.SumView)).setText("Sum: " + summed);
                        }else{
                            ((TextView) findViewById(R.id.SumView)).setText("جمع: " + summed);
                            ((Button) findViewById(R.id.SaveBTN)).setText("ذخیره");
                        }
                    }
                    if(preferences.getInt("Lang",0) == 0) {
                        ((TextView) findViewById(R.id.minView)).setText(min + " :Min");
                        ((TextView) findViewById(R.id.maxView)).setText(max + " :Max");
                        ((TextView) findViewById(R.id.AverageView)).setText("Average: " + ave);
                    }else{
                        ((TextView) findViewById(R.id.minView)).setText("بیشترین عدد: " + max);
                        ((TextView) findViewById(R.id.maxView)).setText("کمترین عدد: " + min);
                        ((TextView) findViewById(R.id.AverageView)).setText("میانگین: " + ave);
                    }
                    ArrayAdapter adapter = new ArrayAdapter<>(ShowNumsActivity.this,R.layout.activity_listview, randoms.toArray());
                    ((ListView) findViewById(R.id.listView)).setAdapter(adapter);
                }
            });
            return null;
        }
    }
    public float random(float Min,float Max){
        float res;
        //Check Decimal
        if(Max % 1 != 0 || Min % 1 != 0){//Decimal
            float Max1 = Max;
            float Min1 = Min;
            int MaxDecimal;
            if(getDecimal(Min) > getDecimal(Max)){MaxDecimal = getDecimal(Min);}
            else if(getDecimal(Min) < getDecimal(Max)){MaxDecimal = getDecimal(Max);}
            else{MaxDecimal = getDecimal(Min);}
            float toPow = ((float)Math.pow(10,MaxDecimal - 1));
            while((MaxDecimal - 1) != 0){
                MaxDecimal--;
                Max1 *= 10;
                Min1 *= 10;
            }
            int res1 = randInt((int) Min1,(int) Max1);
            res = res1 / toPow;
        }else{//Int
            res = randInt((int) Min,(int) Max);
        }
        return res;
    }
    public int randInt(int min, int max) {return rand.nextInt((max - min) + 1) + min;}
    private int getDecimal(float value){return String.valueOf(value).substring(String.valueOf(value).indexOf(".")).length();}
}
