package com.niu.csie.edu.app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;


public class Activity11_Take_Leave extends AppCompatActivity {

    /**Element*/
    private Toolbar _toolbar;
    private WebView Web_Take_Leave;
    private View ProgressOverlay;

    /**Components*/
    private Intent page = new Intent();

    /**Variable*/
    private static final int FILE_CHOOSER_REQUEST_CODE = 1;
    private ValueCallback<Uri[]> filePathCallback;



    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.activity11_take_leave);
        initialize(_savedInstanceState);
        initializeLogic();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        page.setClass(getApplicationContext(), Activity1_HomeActivity.class);
        startActivity(page);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    private void initialize(Bundle _savedInstanceState) {
        _toolbar = findViewById(R.id._toolbar);
        setSupportActionBar(_toolbar);
        setActionBarTitle(getResources().getString(R.string.Take_Leave));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        _toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _v) {
                page.setClass(getApplicationContext(), Activity1_HomeActivity.class);
                startActivity(page);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        // 主畫面UI
        Web_Take_Leave = findViewById(R.id.web_take_leave);
        ProgressOverlay = findViewById(R.id.progress_overlay);

        // 初始化設置
        Web_Take_Leave.getSettings().setJavaScriptEnabled(true);
        Web_Take_Leave.getSettings().setDomStorageEnabled(true);
        Web_Take_Leave.getSettings().setLoadWithOverviewMode(true);
        Web_Take_Leave.getSettings().setUseWideViewPort(true);
        // 禁用縮放控制，但啟用內部縮放功能
        Web_Take_Leave.getSettings().setSupportZoom(true);
        Web_Take_Leave.getSettings().setBuiltInZoomControls(true);
        Web_Take_Leave.getSettings().setDisplayZoomControls(false);
        // 讓 WebView 的寬度適應屏幕寬度
        Web_Take_Leave.setInitialScale(1);

        Web_Take_Leave.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (!uri.getScheme().matches("http|https|file|chrome|data|javascript|about") || !uri.toString().contains("niu.edu.tw")) {
                    // Implement canLaunch and launch
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.equals("https://acade.niu.edu.tw/NIU/Application/SEC/SEC20/SEC2010_01.aspx")) {
                    String js = "document.getElementById('QTable2').style.display = 'none';";
                    view.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            // 確認是執行完 js 才隱藏請稍等
                            hideProgressOverlay();
                            // GetValueOfAspx();
                        }
                    });
                    /*
                    String js = "javascript:(function() { " +
                            "document.body.innerHTML = document.body.innerHTML.replaceAll('XXX', '桐谷和人');" +
                            "document.body.innerHTML = document.body.innerHTML.replaceAll('BXXX', 'B1048763');" +
                            "})()";
                    view.evaluateJavascript(js, null);*/
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }
        });

        Web_Take_Leave.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (Activity11_Take_Leave.this.filePathCallback != null) {
                    Activity11_Take_Leave.this.filePathCallback.onReceiveValue(null);
                }
                Activity11_Take_Leave.this.filePathCallback = filePathCallback;
                Intent FilePick = fileChooserParams.createIntent();
                try {
                    startActivityForResult(FilePick, FILE_CHOOSER_REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    Activity11_Take_Leave.this.filePathCallback = null;
                    showMessage(getResources().getString(R.string.OpenSAFFailed));
                    return false;
                }
                return true;
            }
        });

        // 網站Load
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", "https://acade.niu.edu.tw/NIU/Application/SEC/SEC20/SEC2010_02.aspx");
        Web_Take_Leave.loadUrl("https://acade.niu.edu.tw/NIU/Application/SEC/SEC20/SEC2010_01.aspx", headers);

    }

    // 挑選檔案的 result 處理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (filePathCallback != null) {
                Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                filePathCallback.onReceiveValue(result);
                filePathCallback = null;
            }
        }
    }


    // 設標題
    private void setActionBarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    //顯示等待中
    private void showProgressOverlay() {
        if (ProgressOverlay != null) {
            ProgressOverlay.setVisibility(View.VISIBLE);
            // 旋轉動畫
            ImageView loadingImage = ProgressOverlay.findViewById(R.id.loading_image);
            Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.prog_rotate_icon);
            loadingImage.startAnimation(rotateAnimation);
        }
    }

    //隱藏等待中
    private void hideProgressOverlay() {
        if (ProgressOverlay != null && ProgressOverlay.getVisibility() == View.VISIBLE) {
            ProgressOverlay.setVisibility(View.GONE);
        }
    }

    private void initializeLogic() {
        showProgressOverlay();
    }

    public void WebView_Help(View view) {
        Intent page = new Intent(getApplicationContext(), Activity_WebviewProvider.class);
        startActivity(page);
    }


    private void showMessage(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
}