package com.midagepro.logurl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mStatus;
    private TextView mLogs;
    private Button btnSet;
    public static String strLogs="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSet = (Button) findViewById(R.id.button);
        mStatus=(TextView)findViewById(R.id.textView);
        mLogs=(TextView)findViewById(R.id.textView3);
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                //   Uri uri = Uri.fromParts("package", getPackageName()+"/"+getPackageName()+".NotificationListener", null);
                //  intent.setData(uri);
                startActivity(intent);
            }
        });

        DoInit();


    }
    public static void onBrowserRecv(String str)
    {
        strLogs+=str+"\r\n\r\n";
    }
    void DoInit()
    {
        boolean bset = isAccessibilitySettingsOn(this);
        if (bset == true)
        {
            btnSet.setEnabled(false);
            mStatus.setText("Permission Granted");

        }
        else
        {
            btnSet.setEnabled(true);
            mStatus.setText("Permission Pending");


        }
    }
    @Override
    protected void onResume() {

        DoInit();
        if(mLogs!=null)
        {
            mLogs.setText(strLogs);
            mLogs.setMovementMethod(new ScrollingMovementMethod());
        }

        super.onResume();
    }

    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + LogUrlService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        } else {
        }

        return false;
    }
}