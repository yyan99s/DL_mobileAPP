package com.directlending.dlapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;

import android.webkit.WebView;
import android.webkit.WebViewClient;


import com.google.firebase.messaging.FirebaseMessaging;

public class WhatsappURL extends AppCompatActivity {

    private WebView eWeb;
    private ProgressDialog progDailog;
    AppCompatActivity activity;
    SharedPreferences sp;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_DLapp);
        setContentView(R.layout.whatsapp_url);


        sp = getApplicationContext().getSharedPreferences("DLuserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        activity = this;
        progDailog = ProgressDialog.show(activity, "Loading","Please wait...", true);
        progDailog.setCancelable(false);

        String Pusername = sp.getString("username","");
        String Ppassword = sp.getString("password","");
        String PJWTtoken = sp.getString("JWTtoken","");
        String PFCMtoken = sp.getString("FCMtoken", "");
        Log.i("preferences", Pusername);
        Log.i("preferences", Ppassword);

        if(PFCMtoken.equals("")){
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

                        Log.d("FCM TOKEN", token);
                    });
        }

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
        if (!Pusername.equals("") && !Ppassword.equals("") && !PJWTtoken.equals("")){
            eWeb.setWebChromeClient(new WebChromeClient());
            eWeb.getSettings().setJavaScriptEnabled(true);
            Uri WhatsappURL = Uri.parse("https://dev.directlending.com.my/directlending/whatsapp/messagePage" + "?whatsappJwtToken=" + PJWTtoken);
            String URL = String.valueOf(WhatsappURL);
            eWeb.loadUrl(URL);
            Log.i("WhatsappURL", String.valueOf(WhatsappURL));
        }else{
            openLogin();
            progDailog.dismiss();
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    public void openLogin(){
        Intent intent = new Intent(WhatsappURL.this, LoginPage.class);
        startActivity(intent);
        finish();
    }

    public void TokenCheck(String url){
        boolean URLToken = url.matches("(.*)whatsappJwtToken(.*)");

        if (!URLToken) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("JWTtoken", null);
            editor.commit();
            eWeb.clearCache(true);
            eWeb.clearHistory();
            eWeb.destroy();
            progDailog.dismiss();
            openLogin();
            Log.i("NO JWT token", "why no token!!!!");
        }
    }
}