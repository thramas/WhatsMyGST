package com.pukingminion.whatsmygst;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.FirebaseApp;

/**
 * Created by Samarth on 23/06/18.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        initGSTData();
    }

    private void initGSTData() {
        SharedPreferences sharedPreferences = getSharedPreferences("GST", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("oil", "5%").apply();
        sharedPreferences.edit().putString("sugar", "5%").apply();
        sharedPreferences.edit().putString("tea", "5%").apply();
        sharedPreferences.edit().putString("coffee", "5%").apply();
        sharedPreferences.edit().putString("food", "5%").apply();
        sharedPreferences.edit().putString("pc", "12%").apply();
        sharedPreferences.edit().putString("laptop", "5%").apply();
        sharedPreferences.edit().putString("computer", "5%").apply();
        sharedPreferences.edit().putString("mobile phone", "5%").apply();
        sharedPreferences.edit().putString("car", "28%").apply();
    }
}
