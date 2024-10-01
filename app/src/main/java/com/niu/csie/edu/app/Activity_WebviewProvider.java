package com.niu.csie.edu.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



public class Activity_WebviewProvider extends AppCompatActivity {

    /**Element*/
    private Toolbar _toolbar;
    private Button btn1, btn2;

    /**Components*/


    /**Variable*/



    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_webview_provider);
        initialize(_savedInstanceState);
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id._coordinator), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
    }

    private void initialize(Bundle _savedInstanceState) {
        _toolbar = findViewById(R.id._toolbar);
        setSupportActionBar(_toolbar);
        setActionBarTitle(getResources().getString(R.string.huh));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        _toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _v) {
                onBackPressed();
            }
        });

        // 主畫面 UI
        btn1 = findViewById(R.id.button1);
        btn2 = findViewById(R.id.button2);

        // 事件
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playstore = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.webview"));
                startActivity(playstore);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playstore = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.webview.beta"));
                startActivity(playstore);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(0, 0);
    }

    // 設標題
    private void setActionBarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}