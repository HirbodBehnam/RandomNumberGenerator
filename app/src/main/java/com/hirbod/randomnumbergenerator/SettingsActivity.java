package com.hirbod.randomnumbergenerator;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
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
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    private byte counter = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //Lang
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        if(preferences.getInt("Lang",0) == 1)
            changeFarsi();
        //AD
        AD.LoadBanner(this);
        //Spinner
        Spinner spinner = findViewById(R.id.LangSpinner);
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
        // Secure Random
        ((CheckBox)findViewById(R.id.SecureRandomCheckbox)).setChecked(preferences.getBoolean("UseSecureRandom",false));
        ((CheckBox)findViewById(R.id.SecureRandomCheckbox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("UseSecureRandom",isChecked);
                editor.apply();
                InnerRandom.UseSecureRandom = isChecked;
            }
        });
        //Vote
        findViewById(R.id.voteBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    Intent rateIntent = rateIntentForUrl("market://details");
                    startActivity(rateIntent);
                }
                catch (ActivityNotFoundException e)
                {
                    Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
                    startActivity(rateIntent);
                }
            }
        });
        //GitHub
        findViewById(R.id.ShowSourceBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/HirbodBehnam/RandomNumberGenerator/tree/google-play"));
                startActivity(intent);
            }
        });
        //Other apps
        findViewById(R.id.otherAppsBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Hirbod+Behnam"));
                startActivity(browserIntent);
            }
        });
        //Show build
        findViewById(R.id.textView5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    ((TextView) findViewById(R.id.textView5)).setText("Version " + pInfo.versionName + " Build " + pInfo.versionCode);
                }catch (Exception ex){
                    ex.printStackTrace();
                    ((TextView) findViewById(R.id.textView5)).setText(R.string.Settings_Creator);
                }
            }
        });
    }

    private Intent rateIntentForUrl(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
        {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
        else
        {
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onBackPressed();
        return true;
    }

    private void changeFarsi(){
        ((TextView) findViewById(R.id.textView4)).setText("زبان");
        ((Button) findViewById(R.id.voteBTN)).setText("رای دهید");
        ((Button) findViewById(R.id.otherAppsBTN)).setText("برنامه های دیگر");
    }
}
