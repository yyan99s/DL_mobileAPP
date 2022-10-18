package com.directlending.dlapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        eUsername = findViewById(R.id.username);
        ePassword = findViewById(R.id.password);
        eLogin = findViewById(R.id.logIn);

        sp = getApplicationContext().getSharedPreferences("DLuserPrefs", Context.MODE_PRIVATE);
        String Pusername = sp.getString("username","");
        String Ppassword = sp.getString("password","");

        if (!Pusername.equals("") && !Ppassword.equals("")){
            eUsername.setText(Pusername);
            ePassword.setText(Ppassword);
            Log.i("weyhhhhhhhhhhhhh", Pusername);
            Log.i("oikkkkkkkkkkkkkkkk", Ppassword);
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
        String postUrl= "https://dev.directlending.com.my/directlending/demo/loginSuccess";

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
                    if(status.equals("true") && Pusername.equals("") && Ppassword.equals("") && PJWTtoken.equals("")){      //res.getCount() == 0
                        runOnUiThread(() -> showToastTrue());
                        editor.putString("username", inputUsername);
                        editor.putString("password", inputPassword);
                        editor.putString("JWTtoken", inputToken);
                        editor.commit();
                        openWhatsappURL();
                    }else if(status.equals("true") && !Pusername.equals("") && !Ppassword.equals("")){
                        runOnUiThread(() -> showToastTrueRefresh());
                        editor.putString("JWTtoken", inputToken);
                        editor.commit();
                        openWhatsappURL();
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

    private void showToastFalse() {
        Toast.makeText(LoginPage.this,"Wrong username or password. Please fill in the details correctly!", Toast.LENGTH_SHORT).show();
    }

    private void showToastTrue() {
        Toast.makeText(LoginPage.this,"Good", Toast.LENGTH_SHORT).show();
    }

    private void showToastTrueRefresh() {
        Toast.makeText(LoginPage.this,"Goodbye", Toast.LENGTH_SHORT).show();
    }

    public void openWhatsappURL(){
        Intent intent = new Intent(LoginPage.this, WhatsappURL.class);
        startActivity(intent);
    }
}