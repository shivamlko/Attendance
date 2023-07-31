package com.kieona.attendence;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity {
    private EditText username;
    private EditText password;
    private Button loginBtn;

    String user, pass;

    Context context;
    ProgressDialog progressDialog;
        SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEditText = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        context = MainActivity.this;

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        sharedPreferences = getSharedPreferences("USER",MODE_PRIVATE);


        if(sharedPreferences.getBoolean("isLogon",false))
        {
             Intent intent = new Intent(context, HomeScreen.class);
             startActivity(intent);
             finish();
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = usernameEditText.getText().toString();
                pass = password.getText().toString();

                if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass))

                {
                    if (user.length() == 10) {
                        checkLogin();
                    } else
                        Toast.makeText(context, "Enter valid username", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(context, "Enter valid username and password", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    void checkLogin() {

        LoginTask loginTask = new LoginTask();
        loginTask.execute();

    }


    private class LoginTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }


        @Override
        protected String doInBackground(String... strings) {
          /*  String user="1234567";
            String pass="1234567";*/
            StringBuilder total = new StringBuilder();

            try {
                URL url = new URL("https://apteral-bay.000webhostapp.com/attendanceapp/login.php?username=" + user + "&pass="
                        + pass + "&token=ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setRequestMethod("GET");

                int result = httpURLConnection.getResponseCode();
                Log.e("REQUEST CODE", result + "");

                if (result == HttpURLConnection.HTTP_OK) {
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        total.append(line);
                    }
                    if (reader != null) {
                        reader.close();
                    }

                    Log.e("RESULT", total.toString());


                } else
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show();
                        }
                    });


                progressDialog.dismiss();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return total.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean status = jsonObject.getBoolean("status");

                if (status) {

                    JSONObject data = jsonObject.getJSONObject("data");
                    Student student = new Student(data.getString("name"), data.getString("roll"),
                            data.getString("age"), data.getString("mobile"));

                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putBoolean("isLogon",true);
                    editor.commit();

                    Log.e("DATA", student.name + "");
                    Intent intent = new Intent(context, HomeScreen.class);
                    startActivity(intent);
                    finish();

                } else
                    Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
