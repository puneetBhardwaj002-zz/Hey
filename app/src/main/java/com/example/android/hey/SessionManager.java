package com.example.android.hey;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    //key for username
    private String USERNAME="username";
    //key for email
    private String EMAIL="email";
    //key for preferences
    private String PREFERENCES="HeyPrefs";
    //key for is user login
    private String ISLOGIN="login";
    // Shared Preferences variable
    SharedPreferences prefs;
    //editor for shared preference
    SharedPreferences.Editor editor;

    public SessionManager(Context context)
    {
        prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        editor=prefs.edit();
        editor.apply();
    }

    // to login user
    public void loginUser(String email,boolean login)
    {
        editor.putString(EMAIL,email);
        editor.putBoolean(ISLOGIN,login);
        editor.commit();
    }
    //to get useremail
    public String getUserEmail()
    {
        return prefs.getString(EMAIL,"");
    }
    //to check whether user is login or not
    public boolean isUserLoggedIn()
    {
        return prefs.getBoolean(ISLOGIN,false);
    }
    // to delete the user and clear the preferences
    public void logOutUser()
    {
        editor.clear();
        editor.commit();
    }

}