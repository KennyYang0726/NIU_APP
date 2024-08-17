package com.niu.csie.edu.app.LoginManager;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Date;

public class LoginManager {
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_LOGIN_DATE = "loginDate";
    private static final String KEY_LOGIN_COUNT = "loginCount";

    private SharedPreferences prefs;
    private LoginListener listener;

    public LoginManager(Context context, LoginListener listener) {
        this.listener = listener;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public interface LoginListener {
        void onThirtyDaysLogin();
    }

    public void userLoggedIn() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int today = cal.get(Calendar.DAY_OF_YEAR);

        int lastLoginDay = prefs.getInt(KEY_LOGIN_DATE, -1);
        int loginCount = prefs.getInt(KEY_LOGIN_COUNT, 0);

        if (today == lastLoginDay) {
            // 使用者今天已經登錄，不執行任何操作
            return;
        } else if ((today-lastLoginDay == 1) || (today-lastLoginDay == -364) || (today-lastLoginDay == -365)) { // 正常, 跨年 1-365, 跨年閏年 1-366
            // 使用者連續登入日，遞增計數
            loginCount++;
        } else {
            // 用戶錯過了一天，重置計數
            loginCount = 1;
        }

        if (loginCount == 30) {
            // 使用者連續登入30天，觸發事件
            listener.onThirtyDaysLogin();
        }

        // 儲存今天的日期和新的登入計數
        prefs.edit()
                .putInt(KEY_LOGIN_DATE, today)
                .putInt(KEY_LOGIN_COUNT, loginCount)
                .apply();
    }
}

