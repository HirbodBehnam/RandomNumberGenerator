package com.hirbod.randomnumbergenerator;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    private static boolean Done = false;
    private static String fileName = "";
    private static float FMax = 0;
    private static float FMin = 0;
    private static double summed = 0;
    private static double ave = 0;
    private static float max = 0;
    private static float min = 0;
    private static ArrayList<String> randoms = new ArrayList<>();
    private static int resDialogMulti = 0;
    private SharedPreferences preferences;
    private Generator g = new Generator();
    private ProgressDialog pd;
    Random rand = new Random();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_nums);
        if(Build.VERSION.SDK_INT >= 11){try{getActionBar().setDisplayHomeAsUpEnabled(true);}catch (Exception e){e.printStackTrace();}}
        //ProgressDialog
        pd = new ProgressDialog(this);
        pd.setMessage("Writing To File...");
        pd.setIndeterminate(true);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        //Set max and min
        Bundle b = this.getIntent().getExtras();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        FMax = b.getFloat("Max");
        FMin = b.getFloat("Min");
        resDialogMulti = b.getInt("ToCreate");
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.activity_listview, new String[]{"Generating Numbers...","Please Wait..."});
        ((ListView) findViewById(R.id.listView)).setAdapter(adapter);
        //Reset every thing
        Done = false;
        summed = 0;
        rand = new Random();
        //Running garbage collector to clear randoms array and ArrayAdapter
        System.gc();
        System.runFinalization();
        //Generate
        g.execute();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.show_nums_menu, menu);
        return true;
    }
    @Override
    public void onBackPressed() {
        g.cancel(true);
        randoms = new ArrayList<>();
        ((ListView) findViewById(R.id.listView)).setAdapter(null);
        //Running garbage collector to clear randoms array and ArrayAdapter
        System.gc();
        System.runFinalization();
        super.onBackPressed();
    }
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
                SAVE();
                return true;
        }

        return super.onKeyDown(keycode, e);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.SaveBTN:
                SAVE();
                break;
            default:
                g.cancel(true);
                randoms = new ArrayList<>();
                ((ListView) findViewById(R.id.listView)).setAdapter(null);
                //Running garbage collector to clear randoms array and ArrayAdapter
                System.gc();
                System.runFinalization();
                super.onBackPressed();
                break;
        }
        super.onOptionsItemSelected(item);
        return true;
    }
    private void SAVE(){
        if (permission()) {
            if (!Done)
                Toast.makeText(this, "Wait to create numbers...", Toast.LENGTH_SHORT).show();
            else {
                fileName = "";
                final AlertDialog.Builder builder = new AlertDialog.Builder(ShowNumsActivity.this);
                builder.setTitle("Please enter filename:");
                if (preferences.getInt("Lang", 0) == 1) {
                    builder.setTitle("لطفا اسم فایل را وارد کنید.");
                }
                final EditText input = new EditText(ShowNumsActivity.this);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            fileName = input.getText().toString();
                            if (fileName.matches("") || fileName == null) {
                                throw new Exception();
                            }
                            new Writer().execute();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        }
    }
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
    private class Writer extends AsyncTask<Void,Void,String>{
        StringBuilder sb = new StringBuilder();

        @Override
        protected void onPreExecute() {
            pd.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
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
                if(!new File(Environment.getExternalStorageDirectory() + File.separator + "Random Numbers List").exists()){
                    if(new File(Environment.getExternalStorageDirectory() + File.separator + "Random Numbers List").mkdir()){
                        throw new Exception("Cannot create new folder.");
                    }
                }
                //Write
                FileOutputStream stream = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + File.separator + "Random Numbers List" + File.separator + fileName + ".txt"));
                stream.write(sb.toString().getBytes());
                stream.close();
            }catch (Exception e){
                return e.toString().substring(e.toString().indexOf(":") + 2);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            pd.dismiss();
            if(s == null)
                Toast.makeText(ShowNumsActivity.this,"File saved at Storage/Random Numbers List/" + fileName + ".txt",Toast.LENGTH_LONG).show();
            else {
                new AlertDialog.Builder(ShowNumsActivity.this)
                        .setMessage(s)
                        .setTitle("Error")
                        .setPositiveButton("OK",null)
                        .show();
            }
            super.onPostExecute(s);
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
                    Done = true;
                    if(isInteger(summed)){
                        if(preferences.getInt("Lang",0) == 0) {
                            ((TextView) findViewById(R.id.SumView)).setText("Sum: " + String.valueOf(summed).replace(".0",""));
                        }else{
                            ((TextView) findViewById(R.id.SumView)).setText("جمع: " + String.valueOf(summed).replace(".0",""));
                        }
                    }else{
                        if(preferences.getInt("Lang",0) == 0) {
                            ((TextView) findViewById(R.id.SumView)).setText("Sum: " + summed);
                        }else{
                            ((TextView) findViewById(R.id.SumView)).setText("جمع: " + summed);
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
    public static boolean isInteger(float str) {return str % 1 == 0;}
    public static boolean isInteger(double str) {return str % 1 == 0;}
}
