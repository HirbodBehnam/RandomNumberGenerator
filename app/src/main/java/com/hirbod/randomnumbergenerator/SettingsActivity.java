package com.hirbod.randomnumbergenerator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
        //Vote
        findViewById(R.id.voteBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_EDIT);
                        intent.setData(Uri.parse("bazaar://details?id=com.hirbod.randomnumbergenerator"));
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
						mad.show();
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
