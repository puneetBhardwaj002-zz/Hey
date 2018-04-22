package com.example.android.hey;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    private EditText etEmail;
    private EditText etPassword;
    private String emailText;
    private String passwordText;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(LoginActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login");
        if(sessionManager.isUserLoggedIn()){
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        etEmail = (EditText) findViewById(R.id.user_name);
        etPassword = (EditText) findViewById(R.id.password);

        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailText=etEmail.getText().toString();
                passwordText=etPassword.getText().toString();
                if(emailText.isEmpty()){
                    etEmail.setError("The e-mail field cannot be blank.");
                }
                else if(passwordText.isEmpty()){
                    etPassword.setError("The password field cannot be blank.");
                }
                else if(!isValidEmailAddress(emailText)){
                    etEmail.setError("Not a valid e-mail address.");
                }
                else if(!isValidPassword(passwordText)){
                    etPassword.setError("Password should be strong.");
                }
                else {
                    login();
                }
            }
        });

        TextView textView = (TextView) findViewById(R.id.signup_login);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.signup:
                Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    private void login(){
        emailText=etEmail.getText().toString();
        passwordText=etPassword.getText().toString();
        new AsyncLogin().execute(emailText,passwordText);
        Log.v("Login button pressed","Clicked on login button and data sent");
    }

    private class AsyncLogin extends AsyncTask<String,String,String>{
        ProgressDialog pDialog = new ProgressDialog(LoginActivity.this);
        HttpURLConnection connection;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog.setMessage("\tLoading....");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                url = new URL("https://puneetbhardwajdevil.000webhostapp.com/hey/login.php");
            }catch (MalformedURLException e){
                e.printStackTrace();
                return "Exception";
            }try{
                //Setup connection through url to php file at localhost
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);

                //Setup methods to handle sending and receiving of data
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Append parameters to URL
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("email",params[0])
                        .appendQueryParameter("password",params[1]);
                String query = builder.build().getEncodedQuery();

                Log.v("Query Sent:",query);

                //Open connection for sending data
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                connection.disconnect();

            }catch (IOException e){
                e.printStackTrace();
                return "Exception";
            }
            try{
                int responseCode = connection.getResponseCode();
                //Check if response code is ok i.e. connection is made successful
                if(responseCode == HttpURLConnection.HTTP_OK){
                    //Read data from server
                    InputStream input = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while((line=reader.readLine()) != null){
                        result.append(line);
                    }
                    Log.v("Connected to php script","Result is returned after checking for variables supplied");
                    return (result.toString());
                }
                else{
                    return "Unsuccessful";
                }
            }catch(IOException e){
                e.printStackTrace();
                return "Exception";
            }finally {
                connection.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
            if(result.equalsIgnoreCase("true")){
                // If username and password match display home activity
                sessionManager.loginUser(sessionManager.getUserEmail(),true);
                Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
            }
            else if(result.equalsIgnoreCase("false")){
                // If username and password does not match display a error message
                Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_LONG).show();
                Log.v("Login Unsuccessful",result);
            }else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {

                Toast.makeText(LoginActivity.this, "Oops! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
                Log.v("Login Unsuccessful",result);

            }else if(result.equalsIgnoreCase("wrongpassword")){
                Toast.makeText(LoginActivity.this, "Oops! Wrong Password.", Toast.LENGTH_LONG).show();
                Log.v("Login Unsuccessful",result);
            }
        }
    }
    private final static boolean isValidEmailAddress(String target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        if(password.length()<8 ){
            return false;
        }
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();

    }

}
