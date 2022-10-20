package com.directlending.dlapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LoginPage extends AppCompatActivity {

    private EditText eUsername;
    private EditText ePassword;
    private Button eLogin;
    SharedPreferences sp;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(task -> {
                                if (!task.isSuccessful()) {
                                    Log.w("FAILED FCM", "Fetching FCM registration token failed", task.getException());
                                    return;
                                }

                                // Get new FCM registration token
                                String token = task.getResult();
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("FCMtoken", token);
                                editor.commit();

                                Log.d("FCM TOKEN", token);
                            });
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_DLapp);
        setContentView(R.layout.login_page);

        eUsername = findViewById(R.id.username);
        ePassword = findViewById(R.id.password);
        eLogin = findViewById(R.id.logIn);

        sp = getApplicationContext().getSharedPreferences("DLuserPrefs", Context.MODE_PRIVATE);
        String Pusername = sp.getString("username","");
        String Ppassword = sp.getString("password","");

        askNotificationPermission();

        if (!Pusername.equals("") && !Ppassword.equals("")){
            eUsername.setText(Pusername);
            ePassword.setText(Ppassword);
        }
        eLogin.setOnClickListener(view -> {

            String inputUsername = eUsername.getText().toString();
            String inputPassword = ePassword.getText().toString();

            //validate form
            if(validateLogin(inputUsername, inputPassword)){
                //do login
                doLogin(inputUsername, inputPassword);
            }
        });
    }



    private boolean validateLogin(String inputUsername, String inputPassword){
        if(inputUsername == null || inputUsername.trim().length() == 0){
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(inputPassword == null || inputPassword.trim().length() == 0){
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void doLogin(String inputUsername, String inputPassword){

        OkHttpClient client = new OkHttpClient();

        JsonObject postData = new JsonObject();
        postData.addProperty("username", inputUsername);
        postData.addProperty("password", inputPassword);
        String postUrl= "https://dev.directlending.com.my/directlending/whatsapp/login";

        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody postBody = RequestBody.create(JSON, postData.toString());
        Request post = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(post).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    ResponseBody responseBody = response.body();
                    String stringResponse = responseBody.string();

                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    Log.i("data", stringResponse);
                    JSONObject jj = new JSONObject(stringResponse);
                    String status = jj.getString("success");
                    String inputToken = jj.getString("jwtToken");

                    SharedPreferences.Editor editor = sp.edit();
                    String Pusername = sp.getString("username","");
                    String Ppassword = sp.getString("password","");
                    String PJWTtoken = sp.getString("JWTtoken","");
                    String PFCMtoken = sp.getString("FCMtoken", "");
                    if(status.equals("true") && Pusername.equals("") && Ppassword.equals("") && PJWTtoken.equals("")){      //res.getCount() == 0
                        runOnUiThread(() -> showToastTrue());
                        editor.putString("username", inputUsername);
                        editor.putString("password", inputPassword);
                        editor.putString("JWTtoken", inputToken);
                        editor.commit();
                        openWhatsappURL();
                        String PJWTtoken2 = sp.getString("JWTtoken","");
                        PostFCMtoken(PJWTtoken2,PFCMtoken);

                    }else if(status.equals("true") && !Pusername.equals("") && !Ppassword.equals("")){
                        runOnUiThread(() -> showToastTrueRefresh());
                        editor.putString("JWTtoken", inputToken);
                        editor.commit();
                        openWhatsappURL();
                        String PJWTtoken2 = sp.getString("JWTtoken","");
                        PostFCMtoken(PJWTtoken2,PFCMtoken);

                    }else if (status.equals("false")){
                        runOnUiThread(() -> showToastFalse());
                        eUsername.setText("");
                        ePassword.setText("");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void PostFCMtoken(String PJWTtoken, String PFCMtoken) {

        OkHttpClient client = new OkHttpClient();

        JsonObject postData = new JsonObject();
        postData.addProperty("JWTtoken", PJWTtoken);
        postData.addProperty("FCMtoken", PFCMtoken);
        String postUrl = "https://dev.directlending.com.my/directlending/whatsapp/registerDevice";

        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody postBody = RequestBody.create(JSON, postData.toString());
        Request post = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(post).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    ResponseBody responseBody = response.body();
                    String stringResponse = responseBody.string();

                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    Log.i("FCMtoken POST data", stringResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showToastFalse() {
        Toast.makeText(LoginPage.this,"Wrong username or password. Please fill in the details correctly!", Toast.LENGTH_SHORT).show();
    }

    private void showToastTrue() {
        Toast.makeText(LoginPage.this,"Login Successfully", Toast.LENGTH_SHORT).show();
    }

    private void showToastTrueRefresh() {
        Toast.makeText(LoginPage.this,"Token Updated", Toast.LENGTH_SHORT).show();
    }

    public void openWhatsappURL(){
        Intent intent = new Intent(LoginPage.this, WhatsappURL.class);
        startActivity(intent);
        finish();
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