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

public class SignUpActivity extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etName;
    private EditText etCity;
    private EditText etContacts;
    private EditText etAddress;
    private String emailText;
    private String passwordText;
    private String nameText;
    private String cityText;
    private String contactText;
    private String addressText;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getSupportActionBar().setTitle("SignUp");
        session = new SessionManager(SignUpActivity.this);
        if(session.isUserLoggedIn()){
            Intent intent = new Intent(SignUpActivity.this,HomeActivity.class);
            startActivity(intent);
            finish();
        }

        etName=(EditText) findViewById(R.id.name_signup);
        etEmail=(EditText) findViewById(R.id.email_signup);
        etPassword=(EditText) findViewById(R.id.password_signup);
        etCity=(EditText) findViewById(R.id.city_signup);
        etContacts=(EditText) findViewById(R.id.contact_signup);
        etAddress=(EditText) findViewById(R.id.address_signup);

        Button signupButton = (Button) findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameText=etName.getText().toString();
                emailText=etEmail.getText().toString();
                passwordText=etPassword.getText().toString();
                cityText=etCity.getText().toString();
                contactText=etContacts.getText().toString();
                addressText=etAddress.getText().toString();

                if(nameText.isEmpty()){
                    etName.setError("Name field cannot be empty.");
                }else if(emailText.isEmpty()){
                    etEmail.setError("E-mail field cannot be blank.");
                }else if(passwordText.isEmpty()){
                    etPassword.setError("Password field cannot be empty.");
                }else if(cityText.isEmpty()){
                    etCity.setError("City field cannot be empty.");
                }else if(contactText.isEmpty()){
                    etContacts.setError("Contacts field cannot be empty.");
                }else if(addressText.isEmpty()){
                    etAddress.setError("Address field cannot be empty.");
                }else if(!isValidEmailAddress(emailText)){
                    etEmail.setError("Not a valid e-mail address.");
                }else if(!isValidPassword(passwordText)){
                    etPassword.setError("Password should be strong.");
                }else{
                    signup();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signup,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.login:
                Intent intent = new Intent(SignUpActivity.this,LoginActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void signup(){
        emailText=etEmail.getText().toString().trim();
        nameText=etName.getText().toString().trim();
        passwordText=etPassword.getText().toString().trim();
        cityText=etCity.getText().toString().trim();
        contactText=etContacts.getText().toString().trim();
        addressText=etAddress.getText().toString().trim();
        new AsyncSignUp().execute(nameText,emailText,passwordText,cityText,contactText,addressText);
    }

    private class AsyncSignUp extends AsyncTask<String,String,String>{

        ProgressDialog pDialog = new ProgressDialog(SignUpActivity.this);
        HttpURLConnection connection;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog.setMessage("\t Registering....");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                url = new URL("https://puneetbhardwajdevil.000webhostapp.com/hey/signup.php");
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
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("name",params[0])
                        .appendQueryParameter("email",params[1])
                        .appendQueryParameter("password",params[2])
                        .appendQueryParameter("city",params[3])
                        .appendQueryParameter("contact",params[4])
                        .appendQueryParameter("address",params[5]);
                String query = builder.build().getEncodedQuery();
                Log.v("Query Sent",query);

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
//                    Toast.makeText(SignUpActivity.this, "User result returned.", Toast.LENGTH_LONG).show();
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
                Toast.makeText(SignUpActivity.this, "User registered.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SignUpActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
            else if(result.equalsIgnoreCase("already")){
                // If username and password does not match display a error message
                Toast.makeText(SignUpActivity.this, "User already exists.", Toast.LENGTH_LONG).show();
            }else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {
                Toast.makeText(SignUpActivity.this, "Oops! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
            }
        }
    }
    private final static boolean isValidEmailAddress(String target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        if(password.length()< 8 ){
            return false;
        }
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();

    }
}
