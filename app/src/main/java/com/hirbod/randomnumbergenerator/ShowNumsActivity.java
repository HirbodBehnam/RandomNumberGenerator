package com.hirbod.randomnumbergenerator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;


import static com.hirbod.randomnumbergenerator.Functions.getDecimal;
import static com.hirbod.randomnumbergenerator.Functions.isInteger;

public class ShowNumsActivity extends Activity {
    private boolean Done = false;
    private String fileName = "";
    private float FMax = 0;
    private float FMin = 0;
    private double summed = 0;
    private double ave = 0;
    private float max = 0;
    private float min = 0;
    private ArrayList<String> randoms = new ArrayList<>();
    private int resDialogMulti = 0;
    private SharedPreferences preferences;
    private Generator g = new Generator();
    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_nums);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if(InnerRandom.nextBoolean())
            AD.ShowFullScreenAD(this);
        AD.LoadBanner(this);

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
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{"Generating Numbers...","Please Wait..."});
        ((ListView) findViewById(R.id.listView)).setAdapter(adapter);
        //Reset every thing
        Done = false;
        summed = 0;
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
        randoms = null;
        ((ListView) findViewById(R.id.listView)).setAdapter(null);
        //Running garbage collector to clear randoms array and ArrayAdapter
        System.gc();
        System.runFinalization();
        super.onBackPressed();
    }
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        if (keycode == KeyEvent.KEYCODE_MENU) {
            SAVE();
            return true;
        }

        return super.onKeyDown(keycode, e);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.SaveBTN)
            SAVE();
        else {
            g.cancel(true);
            randoms = new ArrayList<>();
            ((ListView) findViewById(R.id.listView)).setAdapter(null);
            //Running garbage collector to clear randoms array and ArrayAdapter
            System.gc();
            System.runFinalization();
            super.onBackPressed();
        }
        super.onOptionsItemSelected(item);
        return true;
    }
    private void SAVE(){
        if (Functions.permission(this)) {
            if (!Done)
                Toast.makeText(this, "Wait to create numbers...", Toast.LENGTH_SHORT).show();
            else {
                fileName = "";
                final AlertDialog.Builder builder = new AlertDialog.Builder(ShowNumsActivity.this);
                builder.setTitle("Please enter filename:");
                if (preferences.getInt("Lang", 0) == 1)
                    builder.setTitle("لطفا اسم فایل را وارد کنید.");
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == 0) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                AlertDialog.Builder mad = new AlertDialog.Builder(this);
                if (preferences.getInt("Lang", 0) == 0) {
                    mad.setMessage("This permission is needed to save this list.");
                    mad.setTitle("Error");
                    mad.setNegativeButton("OK", null);
                    mad.setPositiveButton("Go To Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Functions.startApplicationDetailsActivity(ShowNumsActivity.this);
                        }
                    });
                } else {
                    mad.setMessage("این دسترسی برای ذخیره کردن این لیست نیاز است.");
                    mad.setTitle("خطا");
                    mad.setNegativeButton("باشه", null);
                    mad.setPositiveButton("برو به تنظیمات", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           Functions.startApplicationDetailsActivity(ShowNumsActivity.this);
                        }
                    });
                }
                mad.show();
            }else{
                SAVE();
            }
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
            for(String i : randoms) {
                sb.append(i);
                sb.append("\n");
            }
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
                    if(new File(Environment.getExternalStorageDirectory() + File.separator + "Random Numbers List").mkdir())
                        throw new Exception("Cannot create new folder.");
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
            else
                new AlertDialog.Builder(ShowNumsActivity.this)
                        .setMessage(s)
                        .setTitle("Error")
                        .setPositiveButton("OK",null)
                        .show();
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
                    ArrayAdapter adapter = new ArrayAdapter<>(ShowNumsActivity.this,android.R.layout.simple_list_item_1, randoms.toArray());
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
    public int randInt(int min, int max)
    {
        return InnerRandom.nextInt((max - min) + 1) + min;
    }
}
