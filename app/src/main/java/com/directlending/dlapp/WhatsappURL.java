package com.directlending.dlapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

public class WhatsappURL extends AppCompatActivity {

    private WebView eWeb;
    private ProgressDialog progDailog;
    AppCompatActivity activity;
    SharedPreferences sp;

    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.whatsapp_url);

        Log.i("preferences", "hhhhh");
        sp = getApplicationContext().getSharedPreferences("DLuserPrefs", Context.MODE_PRIVATE);
        Log.i("preferences", "iiiiii");
        SharedPreferences.Editor editor = sp.edit();

        activity = this;
        progDailog = ProgressDialog.show(activity, "Loading","Please wait...", true);
        progDailog.setCancelable(false);

        String Pusername = sp.getString("username","");
        String Ppassword = sp.getString("password","");
        String PJWTtoken = sp.getString("JWTtoken","");
        Log.i("preferences", Pusername);
        Log.i("preferences", Ppassword);

        eWeb = (WebView) findViewById(R.id.whatsappWeb);
        eWeb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                progDailog.show();
                view.loadUrl(url);
                TokenCheck(url);
                return false;
            }
            @Override
            public void onPageFinished(WebView view, final String url) {
                progDailog.dismiss();
            }
        });
        if (!Pusername.equals("") && !Ppassword.equals("")){
            askNotificationPermission();
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w("FAILED FCM", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        editor.putString("FCMtoken", token);
                        editor.commit();

                        Log.d("FCM TOKEN OIKKKKK", token);
                        Toast.makeText(WhatsappURL.this, token, Toast.LENGTH_SHORT).show();
                    });
            String PFCMToken = sp.getString("FCMtoken", "");
            eWeb.setWebChromeClient(new WebChromeClient());
            eWeb.getSettings().setJavaScriptEnabled(true);
            Uri WhatsappURL = Uri.parse("https://dev.directlending.com.my/serv/servApplyMobile.html" + "?jwtToken=" + PJWTtoken + "&FCMToken=" + PFCMToken);
            String URL = String.valueOf(WhatsappURL);
            eWeb.loadUrl(URL);
            Log.i("WhatsappURL", String.valueOf(WhatsappURL));
            Log.i("OPEN2NDtime","Am I In?");
        }else{
            openLogin();
        }
    }

    public void openLogin(){
        Intent intent = new Intent(WhatsappURL.this, LoginPage.class);
        startActivity(intent);
    }

    public void TokenCheck(String url){
        boolean URLToken = url.matches("(.*)jwtToken(.*)");

        if (!URLToken) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("JWTtoken", null);
            editor.commit();
            //dbHandler.deleteToken(1);
            eWeb.clearCache(true);
            eWeb.clearHistory();
            eWeb.destroy();
            openLogin();
            Log.i("shitttttttttt", "why no token!!!!");
        }
    }

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}