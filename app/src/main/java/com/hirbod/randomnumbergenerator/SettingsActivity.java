package com.hirbod.randomnumbergenerator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    private byte counter = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if(Build.VERSION.SDK_INT >= 11){try{getActionBar().setDisplayHomeAsUpEnabled(true);}catch (Exception e){e.printStackTrace();}}
        if(Build.VERSION.SDK_INT >= 7){((ImageButton) findViewById(R.id.multiplyHelp)).setImageResource(R.drawable.ic_help_white_24dp);}
        //Lang
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        if(preferences.getInt("Lang",0) == 1){changeFarsi();}
        //Multiply
        ((CheckBox) findViewById(R.id.multiRandomCheckbox)).setChecked(preferences.getBoolean("multiRandom",false));
        ((CheckBox) findViewById(R.id.multiRandomCheckbox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("multiRandom",isChecked);
                editor.commit();
            }
        });
        //Spinner
        Spinner spinner = (Spinner) findViewById(R.id.LangSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.lang_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(preferences.getInt("Lang",0));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                counter++;
                if(counter > 1) {
                    editor.putInt("Lang", position);
                    editor.commit();
                    Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        //Help
        findViewById(R.id.multiplyHelp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mad = new AlertDialog.Builder(SettingsActivity.this);
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
                if(Build.VERSION.SDK_INT >= 7){mad.setIcon(R.drawable.ic_help_white_24dp);}else{mad.setIcon(R.drawable.ic_help_white_24px);}
                mad.show();
            }
        });
        //Vote
        findViewById(R.id.voteBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_EDIT);
                        intent.setData(Uri.parse("bazaar://details?id=" + "com.hirbod.randomnumbergenerator"));
                        intent.setPackage("com.farsitel.bazaar");
                        startActivity(intent);
                    }catch (Exception ex){
                        AlertDialog.Builder mad = new AlertDialog.Builder(SettingsActivity.this);
                                mad
                                .setPositiveButton("Install", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://cafebazaar.ir/install/"));
                                        startActivity(browserIntent);
                                    }
                                })
                                .setNegativeButton("Close",null);
                        if(preferences.getInt("Lang",0) == 0){mad.setMessage("Please install bazaar to rate!").setTitle("Error");}
                        else{mad.setMessage("لطفا بازار را نصب کنید.").setTitle("خطا");}
                    }
            }
        });
        //GitHub
        findViewById(R.id.ShowSourceBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/HirbodBehnam/RandomNumberGenerator"));
                startActivity(intent);
            }
        });
        //Other apps
        findViewById(R.id.otherAppsBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("bazaar://collection?slug=by_author&aid=" + "hirbod_behnam"));
                    intent.setPackage("com.farsitel.bazaar");
                    startActivity(intent);
                }catch (Exception ex){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://cafebazaar.ir/developer/hirbod_behnam"));
                    startActivity(browserIntent);
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onBackPressed();
        return true;
    }

    private void changeFarsi(){
        ((TextView) findViewById(R.id.textView4)).setText("زبان");
        ((CheckBox) findViewById(R.id.multiRandomCheckbox)).setText("ساخت چندین عدد تصادفی");
        ((Button) findViewById(R.id.voteBTN)).setText("رای دهید");
        ((Button) findViewById(R.id.otherAppsBTN)).setText("برنامه های دیگر");
    }
}
