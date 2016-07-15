package com.hirbod.randomnumbergenerator;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
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

public class ShowNumsActivity extends Activity {
    private String fileName = "";
    private float FMax = 0;
    private float FMin = 0;
    private float summed = 0;
    private float ave = 0;
    private float max = 0;
    private float min = 0;
    private String[] array;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_nums);
        if(Build.VERSION.SDK_INT >= 11){try{getActionBar().setDisplayHomeAsUpEnabled(true);}catch (Exception e){e.printStackTrace();}}
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //Set array
        Bundle b = this.getIntent().getExtras();
        array = b.getStringArray("numbers");
        FMax = b.getFloat("Max");
        FMin = b.getFloat("Min");
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.activity_listview, array);
        ((ListView) findViewById(R.id.listView)).setAdapter(adapter);
        float arrayF[] = new float[array.length];
        //convert to float
        for(int i = 0; i < array.length;i++) arrayF[i] = Float.parseFloat(array[i]);
        //Sum
        summed = sum(arrayF);
        ave = summed / arrayF.length;
        max = max(arrayF);
        min = min(arrayF);
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

        //Save
        findViewById(R.id.SaveBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!permission()){return;}
                fileName = "";
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ShowNumsActivity.this);
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
    public boolean onOptionsItemSelected(MenuItem item){
        super.onBackPressed();
        return true;
    }
    private float sum(float array[]){
        float sum = 0;
        for (float i : array)
            sum += i;
        return sum;
    }
    private float max(float array[]){
        float max = array[0];
        for (float i : array) if(i > max) max = i;
        return max;
    }
    private float min(float array[]){
        float min = array[0];
        for (float i : array) if(i < min) min = i;
        return min;
    }
    public static boolean isInteger(float str) {
        return str % 1 == 0;
    }
    private boolean permission(){
        if(Build.VERSION.SDK_INT >= 23){
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
        String toWrite = "Range From " + FMin + " To " + FMax + " :\n\n";
        for(int i = 0;i < array.length;i++){
            toWrite += array[i] + "\n";
        }
        toWrite += "=================\n";
        toWrite += "Sum: " + summed + "\n";
        toWrite += "Average: " + ave + "\n";
        toWrite += "Max Number: " + max + "\n";
        toWrite += "Min Number: " + min;
        try{
            //Create new folder
            if(!new File(Environment.getExternalStorageDirectory() + File.separator + "Random Numbers List").exists())
                new File(Environment.getExternalStorageDirectory() + File.separator + "Random Numbers List").mkdir();
            //Write
            FileOutputStream stream = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + File.separator + "Random Numbers List" + File.separator + fileName + ".txt"));
            stream.write(toWrite.getBytes());
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
}
