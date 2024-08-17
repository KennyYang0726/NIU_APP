package com.niu.csie.edu.app.application;

import android.app.Application;
import com.jakewharton.threetenabp.AndroidThreeTen;


/** app 常駐類，啟動 app 即註冊 */

public class NIU_Application extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
