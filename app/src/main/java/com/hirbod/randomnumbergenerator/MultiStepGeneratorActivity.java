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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;

import static com.hirbod.randomnumbergenerator.Functions.SetClipboard;
import static com.hirbod.randomnumbergenerator.Functions.fullRandomBig;

public class MultiStepGeneratorActivity extends Activity {
    private ArrayAdapter<String> arrayAdapter;
    private BigDecimal Sum = BigDecimal.ZERO;
    private SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_step_generator);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Sequential Random Generator");

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString("MaxSM",preferences.getString("MaxSM","100"));
        editor.putString("MinSM",preferences.getString("MinSM","1"));
        editor.apply();
        ((EditText) findViewById(R.id.MaxNumber_EditText)).setText(preferences.getString("MaxSM","100"));
        ((EditText) findViewById(R.id.MinNumber_EditText)).setText(preferences.getString("MinSM","1"));

        if(preferences.getInt("Lang",0) == 1)
            changeFarsi();

        ArrayList<String> listOfNums = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listOfNums);
        arrayAdapter.setNotifyOnChange(true);
        ((ListView)findViewById(R.id.listView)).setAdapter(arrayAdapter);

        Sum = Sum.setScale(15, RoundingMode.HALF_EVEN);

        findViewById(R.id.Generate_BTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strMin = ((EditText) findViewById(R.id.MinNumber_EditText)).getText().toString(), strMax = ((EditText) findViewById(R.id.MaxNumber_EditText)).getText().toString();
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
                        new AlertDialog.Builder(MultiStepGeneratorActivity.this)
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
                    arrayAdapter.add(res.toString());
                    updateAverage(res.toString(),false);
                } else { //Integer numbers
                    try {
                        Max = new BigInteger(strMax);
                        Min = new BigInteger(strMin);
                        if (Max.compareTo(Min) <= 0)
                            throw new Exception("Min number is bigger or equal to Max number.");
                    } catch (Exception ex) {
                        new AlertDialog.Builder(MultiStepGeneratorActivity.this)
                                .setMessage(ex.toString().substring(ex.toString().indexOf(":") + 2))
                                .setTitle("Error")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }
                    String Res = fullRandomBig(Min,Max).toString();
                    arrayAdapter.add(Res);
                    updateAverage(Res,false);
                }
                scrollListViewToBottom();
                //Save Values
                editor.putString("MaxSM",((EditText) findViewById(R.id.MaxNumber_EditText)).getText().toString());
                editor.putString("MinSM",((EditText) findViewById(R.id.MinNumber_EditText)).getText().toString());
                editor.apply();
            }
        });

        ((ListView)findViewById(R.id.listView)).setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String number = parent.getItemAtPosition(position).toString();
                AlertDialog.Builder builder = new AlertDialog.Builder(MultiStepGeneratorActivity.this);
                builder.setTitle("Selected " + number);
                builder.setItems(new String[]{"Copy","Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0) {
                            SetClipboard(MultiStepGeneratorActivity.this, number);
                            Toast.makeText(MultiStepGeneratorActivity.this,"Copied "+ number,Toast.LENGTH_SHORT).show();
                        }
                        else{
                            arrayAdapter.remove(number);
                            updateAverage(number,true);
                        }
                    }
                });
                builder.show();
                return false;
            }
        });
    }
    private void updateAverage(String number,boolean delete){
        BigDecimal num = new BigDecimal(number);
        int count = ((ListView) findViewById(R.id.listView)).getAdapter().getCount();
        if(delete)
            Sum = Sum.subtract(num);
        else
            Sum = Sum.add(num);
        if(count == 0){
            ((TextView) findViewById(R.id.SumView)).setText("Sum: ");
            ((TextView) findViewById(R.id.AverageView)).setText("Average: " );
            ((TextView) findViewById(R.id.CountView)).setText(" :Count");
            return;
        }
        ((TextView) findViewById(R.id.SumView)).setText("Sum: " + num.toString());
        ((TextView) findViewById(R.id.AverageView)).setText("Average: " + Sum.divide(BigDecimal.valueOf(count) ,15,RoundingMode.HALF_EVEN).toString());
        ((TextView) findViewById(R.id.CountView)).setText(count + " :Count");
    }
    //https://stackoverflow.com/a/7032341/4213397
    private void scrollListViewToBottom() {
        findViewById(R.id.listView).post(new Runnable() {
            @Override
            public void run() {
                ((ListView) findViewById(R.id.listView)).setSelection(arrayAdapter.getCount() - 1);
            }
        });
    }
    private void changeFarsi(){
        ((Button) findViewById(R.id.Generate_BTN)).setText("بساز");
        ((TextView) findViewById(R.id.textView2)).setText("بیشترین عدد");
        ((TextView) findViewById(R.id.textView3)).setText("کمترین عدد");
    }
    private void SAVE(){
        if (Functions.permission(this)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MultiStepGeneratorActivity.this);
            builder.setTitle("Please enter filename:");
            if (preferences.getInt("Lang", 0) == 1)
                builder.setTitle("لطفا اسم فایل را وارد کنید.");
            final EditText input = new EditText(MultiStepGeneratorActivity.this);
            builder.setView(input);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        String fileName = input.getText().toString();
                        if (fileName.matches("")) {
                            throw new Exception();
                        }
                        new MultiStepGeneratorActivity.Writer(MultiStepGeneratorActivity.this,Sum.toString(),
                                ((TextView) findViewById(R.id.AverageView)).getText().toString(),
                                arrayAdapter,fileName).execute();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        }
    }
    private static class Writer extends AsyncTask<Void,Void,String> {
        StringBuilder sb = new StringBuilder();
        ProgressDialog pd;
        WeakReference<MultiStepGeneratorActivity> activity;
        String Sum,Average,FileName;
        String[] Numbers;
        Writer(MultiStepGeneratorActivity TheActivity,String sum,String ave,ArrayAdapter<String> nums,String fileName){
            activity = new WeakReference<>(TheActivity);
            Sum = sum;
            Average = ave;
            FileName = fileName;
            Numbers = new String[nums.getCount()];
            for (int i = 0;i<nums.getCount();i++)
                Numbers[i] = nums.getItem(i);
        }
        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(activity.get());
            pd.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            for(String i : Numbers) {
                sb.append(i);
                sb.append("\n");
            }
            sb.append("=================\n");
            sb.append("Sum: ");
            sb.append(Sum);
            sb.append("\n");
            sb.append("Average: ");
            sb.append(Average);
            sb.append("\n");
            sb.append("Count: ");
            sb.append(Numbers.length);
            try{
                //Create new folder
                if(!new File(Environment.getExternalStorageDirectory() + File.separator + "Random Numbers List").exists())
                    if(new File(Environment.getExternalStorageDirectory() + File.separator + "Random Numbers List").mkdir())
                        throw new Exception("Cannot create new folder.");
                //Write
                FileOutputStream stream = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + File.separator + "Random Numbers List" + File.separator + FileName + ".txt"));
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
            MultiStepGeneratorActivity activity1 = activity.get();
            if(activity1 == null || activity1.isFinishing())
                return;
            if(s == null)
                Toast.makeText(activity1,"File saved at Storage/Random Numbers List/" + FileName + ".txt",Toast.LENGTH_LONG).show();
            else
                new AlertDialog.Builder(activity1)
                        .setMessage(s)
                        .setTitle("Error")
                        .setPositiveButton("OK",null)
                        .show();
            super.onPostExecute(s);
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
                            Functions.startApplicationDetailsActivity(MultiStepGeneratorActivity.this);
                        }
                    });
                } else {
                    mad.setMessage("این دسترسی برای ذخیره کردن این لیست نیاز است.");
                    mad.setTitle("خطا");
                    mad.setNegativeButton("باشه", null);
                    mad.setPositiveButton("برو به تنظیمات", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Functions.startApplicationDetailsActivity(MultiStepGeneratorActivity.this);
                        }
                    });
                }
                mad.show();
            }else
                SAVE();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.show_nums_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.SaveBTN)
            SAVE();
        return true;
    }
    @Override
    protected void onDestroy() {
        Sum = null;
        super.onDestroy();
    }
}
