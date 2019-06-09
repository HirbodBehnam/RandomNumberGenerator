package com.hirbod.randomnumbergenerator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends Activity {
    Random rand = new Random();
    static int resDialogMulti = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Max and min holder
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("Max",preferences.getFloat("Max",100));
        editor.putFloat("Min",preferences.getFloat("Min",1));
        editor.putBoolean("Copy",preferences.getBoolean("Copy",true));
        editor.putBoolean("multiRandom",preferences.getBoolean("multiRandom",false));
        editor.putInt("Lang",preferences.getInt("Lang",0));
        editor.apply();
        //Set lang
        if(preferences.getInt("Lang",0) == 1){changeFarsi();}
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
        ((EditText) findViewById(R.id.MaxNumber_EditText)).setText(String.valueOf(preferences.getFloat("Max",100)));
        ((EditText) findViewById(R.id.MinNumber_EditText)).setText(String.valueOf(preferences.getFloat("Min",1)));
        //Generate Button
        findViewById(R.id.Generate_BTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Values
                float Max;
                float Min;
                try {
                    Max = Float.valueOf(((EditText) findViewById(R.id.MaxNumber_EditText)).getText().toString());
                    Min = Float.valueOf(((EditText) findViewById(R.id.MinNumber_EditText)).getText().toString());
                    if(Max <= Min) throw new Exception("Min number is bigger or equal to Max number.");
                    if(Max > Integer.MAX_VALUE)
                    {
                        ((EditText) findViewById(R.id.MaxNumber_EditText)).setText(String.valueOf(preferences.getFloat("Max",100)));
                        throw new Exception("Max number is bigger than " + Integer.MAX_VALUE);
                    }
                    if(Min > Integer.MAX_VALUE)
                    {
                        ((EditText) findViewById(R.id.MinNumber_EditText)).setText(String.valueOf(preferences.getFloat("Min",1)));
                        throw new Exception("Min number is bigger than" + Integer.MAX_VALUE);
                    }
                    if(Max < Integer.MIN_VALUE)
                    {
                        ((EditText) findViewById(R.id.MaxNumber_EditText)).setText(String.valueOf(preferences.getFloat("Max",100)));
                        throw new Exception("Max number is smaller than " + Integer.MIN_VALUE);
                    }
                    if(Min < Integer.MIN_VALUE)
                    {
                        ((EditText) findViewById(R.id.MinNumber_EditText)).setText(String.valueOf(preferences.getFloat("Min",1)));
                        throw new Exception("Min number is smaller than " + Integer.MIN_VALUE);
                    }
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
                //Check multi random
                if(preferences.getBoolean("multiRandom",false)){//Multi random
                    getNumberRandomDialog(Min,Max);
                }else{//One random
                    float rnd = random(Min,Max);
                    if(isInteger(rnd)){
                        ((EditText) findViewById(R.id.editText)).setText(String.valueOf(rnd).replace(".0",""));}
                    else{
                        ((EditText) findViewById(R.id.editText)).setText(String.valueOf(rnd));}
                }
                //Save Values
                editor.putFloat("Max",Max);
                editor.putFloat("Min",Min);
                editor.apply();
                //Auto Copy
                if(((CheckBox) findViewById(R.id.checkBox)).isChecked())
                    SetClipboard(MainActivity.this,((EditText) findViewById(R.id.editText)).getText().toString());
            }
        });
    }
    public static void SetClipboard(Context context,String text){
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Random Number", text);
        clipboard.setPrimaryClip(clip);
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
    public int randInt(int min, int max) {return rand.nextInt((max - min) + 1) + min;}
    private int getDecimal(float value){return String.valueOf(value).substring(String.valueOf(value).indexOf(".")).length();}
    private void changeFarsi(){
        ((Button) findViewById(R.id.Generate_BTN)).setText("بساز");
        ((Button) findViewById(R.id.Copy_BTN)).setText("کپی");
        ((Button) findViewById(R.id.SettingsBTN)).setText("تنظیمات");
        ((TextView) findViewById(R.id.textView2)).setText("بیشترین عدد");
        ((TextView) findViewById(R.id.textView3)).setText("کمترین عدد");
        ((CheckBox) findViewById(R.id.checkBox)).setText("کپی خودکار");
    }
    public static boolean isInteger(float str) { return str % 1 == 0; }
}