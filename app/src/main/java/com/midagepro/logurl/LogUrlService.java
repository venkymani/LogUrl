package com.midagepro.logurl;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogUrlService  extends AccessibilityService {
    public String browserApp="";
    public String browserUrl="";
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        switch(eventType) {


            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:


            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: {
                AccessibilityNodeInfo parentNodeInfo = event.getSource();
                if (parentNodeInfo == null) {
                    return;
                }

                String packageName = event.getPackageName().toString();
                SupportedBrowserConfig browserConfig = null;
                for (SupportedBrowserConfig supportedConfig: getSupportedBrowsers()) {
                    if (supportedConfig.packageName.equals(packageName)) {
                        browserConfig = supportedConfig;
                    }
                }
                //this is not supported browser, so exit
                if (browserConfig == null) {
                    return;
                }

                String capturedUrl = captureUrl(parentNodeInfo, browserConfig);
                parentNodeInfo.recycle();

                if (capturedUrl == null) {
                    return;
                }

                long eventTime = event.getEventTime();
                if(!packageName.equals(browserApp))
                {
                    if(android.util.Patterns.WEB_URL.matcher(capturedUrl).matches()) {
                        Log.d("Browser", packageName + "  :  " + capturedUrl);
                        MainActivity.onBrowserRecv(packageName + "  :  " + capturedUrl);
                        browserApp = packageName;
                        browserUrl = capturedUrl;
                    }
                }
                else
                {
                    if(!capturedUrl.equals(browserUrl))
                    {
                        if(android.util.Patterns.WEB_URL.matcher(capturedUrl).matches()) {
                            browserUrl = capturedUrl;
                            Log.d("Browser", packageName + "   " + capturedUrl);
                            MainActivity.onBrowserRecv(packageName + "  :  " + capturedUrl);

                        }

                    }
                }


            }
            break;


        }


    }


    private static class SupportedBrowserConfig {
        public String packageName, addressBarId;
        public SupportedBrowserConfig(String packageName, String addressBarId) {
            this.packageName = packageName;
            this.addressBarId = addressBarId;
        }
    }

    /** @return a list of supported browser configs
     * This list could be instead obtained from remote server to support future browser updates without updating an app */
    @NonNull
    private static List<SupportedBrowserConfig> getSupportedBrowsers() {
        List<SupportedBrowserConfig> browsers = new ArrayList<>();
        browsers.add( new SupportedBrowserConfig("com.android.chrome", "com.android.chrome:id/url_bar"));
        browsers.add( new SupportedBrowserConfig("org.mozilla.firefox", "org.mozilla.firefox:id/mozac_browser_toolbar_url_view"));
        browsers.add( new SupportedBrowserConfig("com.opera.browser", "com.opera.browser:id/url_field"));
        browsers.add( new SupportedBrowserConfig("com.opera.mini.native", "com.opera.mini.native:id/url_field"));
        browsers.add( new SupportedBrowserConfig("com.duckduckgo.mobile.android", "com.duckduckgo.mobile.android:id/omnibarTextInput"));
        browsers.add( new SupportedBrowserConfig("com.microsoft.emmx", "com.microsoft.emmx:id/url_bar"));


        return browsers;
    }
    private void getChild(AccessibilityNodeInfo info)
    {
        int i=info.getChildCount();
        for(int p=0;p<i;p++)
        {
            AccessibilityNodeInfo n=info.getChild(p);
            if(n!=null) {
                String strres = n.getViewIdResourceName();
                if (n.getText() != null) {
                    String txt = n.getText().toString();
                    Log.d("Track", strres + "  :  " + txt);
                }
                getChild(n);
            }
        }
    }
    private String captureUrl(AccessibilityNodeInfo info, SupportedBrowserConfig config) {

        //  getChild(info);
        List<AccessibilityNodeInfo> nodes = info.findAccessibilityNodeInfosByViewId(config.addressBarId);
        if (nodes == null || nodes.size() <= 0) {
            return null;
        }

        AccessibilityNodeInfo addressBarNodeInfo = nodes.get(0);
        String url = null;
        if (addressBarNodeInfo.getText() != null) {
            url = addressBarNodeInfo.getText().toString();
        }

        addressBarNodeInfo.recycle();
        return url;
    }


    @Override
    public void onInterrupt() {

    }
    @Override
    public void onServiceConnected() {

    }

}
