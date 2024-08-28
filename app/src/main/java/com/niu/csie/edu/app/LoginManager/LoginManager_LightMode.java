package com.niu.csie.edu.app.LoginManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Calendar;
import java.util.Date;

public class LoginManager_LightMode {
    private static final String PREFS_LIGHT_MODE_NAME = "LightModeLoginPrefs";
    private static final String KEY_LIGHT_MODE_LOGIN_DATE = "lightModeLoginDate";
    private static final String KEY_LIGHT_MODE_LOGIN_COUNT = "lightModeLoginCount";

    private SharedPreferences lightModePrefs;
    private LightModeListener lightModeListener;
    private Context context;

    public LoginManager_LightMode(Context context, LightModeListener lightModeListener) {
        this.context = context;
        this.lightModeListener = lightModeListener;
        this.lightModePrefs = context.getSharedPreferences(PREFS_LIGHT_MODE_NAME, Context.MODE_PRIVATE);
    }

    public interface LightModeListener {
        void on74DaysLogin_LightMode();
    }

    @SuppressLint("CommitPrefEdits")
    public void userLoggedIn() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int today = cal.get(Calendar.DAY_OF_YEAR);
        int lastLightModeLoginDay = lightModePrefs.getInt(KEY_LIGHT_MODE_LOGIN_DATE, -1);
        int lightModeLoginCount = lightModePrefs.getInt(KEY_LIGHT_MODE_LOGIN_COUNT, 0);

        if (!isDarkMode()) {
            if (today == lastLightModeLoginDay) {
                // 使用者今天已經登錄，不執行任何操作
                return;
            } else if ((today-lastLightModeLoginDay == 1) || (today-lastLightModeLoginDay == -364) || (today-lastLightModeLoginDay == -365)) { // 正常, 跨年 1-365, 跨年閏年 1-366
                // 使用者連續登入日，遞增計數
                lightModeLoginCount++;
            } else {
                // 用戶錯過了一天，重置計數
                lightModeLoginCount = 1;
            }

            if (lightModeLoginCount == 74) {
                // 使用者連續登入74天，觸發事件
                lightModeListener.on74DaysLogin_LightMode();
            }

            // 儲存今天的日期和新的登入計數
            lightModePrefs.edit()
                    .putInt(KEY_LIGHT_MODE_LOGIN_DATE, today)
                    .putInt(KEY_LIGHT_MODE_LOGIN_COUNT, lightModeLoginCount)
                    .apply();
        } else {
            // 用戶開啟暗黑模式
            lightModeLoginCount = 1;

            // 儲存今天的日期和新的登入計數
            lightModePrefs.edit()
                    .putInt(KEY_LIGHT_MODE_LOGIN_DATE, today)
                    .putInt(KEY_LIGHT_MODE_LOGIN_COUNT, lightModeLoginCount)
                    .apply();
        }
    }

    private boolean isDarkMode() {
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        boolean isSystemDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        boolean isAppDarkMode = nightMode == AppCompatDelegate.MODE_NIGHT_YES;
        boolean isFollowSystem = nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

        return isAppDarkMode || (isFollowSystem && isSystemDarkMode);
    }
}

