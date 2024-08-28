package com.niu.csie.edu.app.application;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

import com.jakewharton.threetenabp.AndroidThreeTen;


/** app 常駐類，啟動 app 即註冊 */

public class NIU_Application extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 設定主題
        SharedPreferences sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String themePreference = sharedPref.getString("themePreference", "system");
        switch (themePreference) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
        AndroidThreeTen.init(this);
    }
}
