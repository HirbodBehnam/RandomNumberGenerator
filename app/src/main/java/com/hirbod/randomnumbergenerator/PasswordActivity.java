package com.hirbod.randomnumbergenerator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.security.SecureRandom;

public class PasswordActivity extends Activity {
    private boolean Farsi = false;
    private SecureRandom random = new SecureRandom();
    private static final String LOWERCASE = "qwertyuipasdfghjkzxcvbnm";
    private static final String UPPERCASE = "QWERTYUIPASDFGHJKZXCVBNM";
    private static final String NUMBERS = "23456789";
    private static final String SPECIAL = "!@#$%^&*()_+=-`[]{};':\"\\|/.,?><";
    private static final String EXCLUDED_LOWERCASE = "ol";
    private static final String EXCLUDED_UPPERCASE = "OL";
    private static final String EXCLUDED_NUMBERS = "01";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        setTitle("Password Generator");
        // get preferences for first run
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        if(preferences.getBoolean("FirstPassword",true)){
            new AlertDialog.Builder(this)
                    .setTitle("Password Generator")
                    .setMessage("For your security, I forced the app to use Secure Random (CRNG) to generate password. Also this app does not store anything. You should write down your passwords somewhere.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editor.putBoolean("FirstPassword",false);
                            editor.apply();
                        }
                    })
                    .show();
        }
        // Load status
        ((TextView) findViewById(R.id.PasswordLength)).setText(preferences.getString("PasswordLength","15"));
        ((CheckBox) findViewById(R.id.PasswordUseLowercase)).setChecked(preferences.getBoolean("PasswordUseLowercase",true));
        ((CheckBox) findViewById(R.id.PasswordUseUppercase)).setChecked(preferences.getBoolean("PasswordUseUppercase",false));
        ((CheckBox) findViewById(R.id.PasswordUseNumbers)).setChecked(preferences.getBoolean("PasswordUseNumbers",false));
        ((CheckBox) findViewById(R.id.PasswordUseSpecial)).setChecked(preferences.getBoolean("PasswordUseSpecial",false));
        ((CheckBox) findViewById(R.id.PasswordExcludeSimilar)).setChecked(preferences.getBoolean("PasswordExcludeSimilar",false));
        // Set persian
        Farsi = preferences.getInt("Lang",0) == 1;
        if(Farsi){
            setTitle("پسورد ساز");
            ((TextView) findViewById(R.id.PasswordLengthText)).setText("طول پسورد");
            ((CheckBox) findViewById(R.id.PasswordUseLowercase)).setText("استفاده از حروف کوچک");
            ((CheckBox) findViewById(R.id.PasswordUseUppercase)).setText("استفاده از حروف بزرگ");
            ((CheckBox) findViewById(R.id.PasswordUseNumbers)).setText("استفاده از اعداد");
            ((CheckBox) findViewById(R.id.PasswordUseSpecial)).setText("استفاده از حروف خاص");
            ((CheckBox) findViewById(R.id.PasswordExcludeSimilar)).setText("عدم استفاده از کارکتر های شبیه");
        }
        // set similar button help dialog
        findViewById(R.id.PasswordExcludeSimilarHelp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(PasswordActivity.this)
                        .setTitle(Farsi ? "راهنما" : "Help")
                        .setMessage(Farsi ?
                                "بعضی از کارکتر ها مثل i l 1 o O 0 شبیه هم هستند. برای اینکه حفظ پسورد برای شما راحت تر باشد، می توانید این گزینه را فعال کنید تا از این کارکتر ها استفاده نشود. این تنظیم چیزی از کارکتر های خاص را کم نمی کند.":
                                "Some characters like 1 l i 0 o O are similar. If you enable this option, your password will not contain any of these characters so reading it would be easier. This option does not exclude anything from special characters")
                        .setPositiveButton("OK",null)
                        .setIcon(R.drawable.ic_help_white_24dp)
                        .show();
            }
        });
        // Copy button
        findViewById(R.id.PasswordCopyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.SetClipboard(PasswordActivity.this,((TextView)findViewById(R.id.PasswordResult)).getText().toString());
                Toast.makeText(PasswordActivity.this,"Copied",Toast.LENGTH_SHORT).show();
            }
        });
        // Generate button
        findViewById(R.id.PasswordGenerateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hide keyboard
                View view = PasswordActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                // save the settings
                editor.putString("PasswordLength",((TextView) findViewById(R.id.PasswordLength)).getText().toString());
                editor.putBoolean("PasswordUseLowercase",((CheckBox) findViewById(R.id.PasswordUseLowercase)).isChecked());
                editor.putBoolean("PasswordUseUppercase",((CheckBox) findViewById(R.id.PasswordUseUppercase)).isChecked());
                editor.putBoolean("PasswordUseNumbers",((CheckBox) findViewById(R.id.PasswordUseNumbers)).isChecked());
                editor.putBoolean("PasswordUseSpecial",((CheckBox) findViewById(R.id.PasswordUseSpecial)).isChecked());
                editor.putBoolean("PasswordExcludeSimilar",((CheckBox) findViewById(R.id.PasswordExcludeSimilar)).isChecked());
                editor.apply();
                // generate the string of allowed characters
                StringBuilder chars = new StringBuilder(26 + 26 + 10 + 31);
                if(((CheckBox) findViewById(R.id.PasswordUseLowercase)).isChecked())
                    chars.append(LOWERCASE);
                if(((CheckBox) findViewById(R.id.PasswordUseUppercase)).isChecked())
                    chars.append(UPPERCASE);
                if(((CheckBox) findViewById(R.id.PasswordUseNumbers)).isChecked())
                    chars.append(NUMBERS);
                if(((CheckBox) findViewById(R.id.PasswordUseSpecial)).isChecked())
                    chars.append(SPECIAL);
                if(!((CheckBox) findViewById(R.id.PasswordExcludeSimilar)).isChecked()){
                    if(((CheckBox) findViewById(R.id.PasswordUseLowercase)).isChecked())
                        chars.append(EXCLUDED_LOWERCASE);
                    if(((CheckBox) findViewById(R.id.PasswordUseUppercase)).isChecked())
                        chars.append(EXCLUDED_UPPERCASE);
                    if(((CheckBox) findViewById(R.id.PasswordUseNumbers)).isChecked())
                        chars.append(EXCLUDED_NUMBERS);
                }
                // generate a password for user
                int length = 0;
                try{
                    length = Integer.parseInt(((TextView) findViewById(R.id.PasswordLength)).getText().toString());
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                if(length <= 0) {
                    new AlertDialog.Builder(PasswordActivity.this)
                            .setTitle(Farsi ? "خطا" : "Error")
                            .setMessage(Farsi ? "طول پسورد شما 0 یا نامعتبر هست." : "Your password length is 0 or invalid.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }
                StringBuilder password = new StringBuilder(length);
                for(int i = 0;i<length;i++)
                    password.append(chars.charAt(random.nextInt(chars.length())));
                // show password
                ((TextView) findViewById(R.id.PasswordResult)).setText(password.toString());
            }
        });
    }

}
