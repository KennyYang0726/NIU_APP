package com.niu.csie.edu.app;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


public class Activity6_Contact_Us_Fragment2 extends Fragment {

    /**Element*/
    private EditText text_input1, text_input2;
    private CheckBox checkBox;
    private Button Submit;
    private WebView webView;

    /**Components*/


    /**Variable*/
    private String DeviceInfo = "";
    private String AppInfo = "";



    public Activity6_Contact_Us_Fragment2() {
        // Required empty public constructor
    }


    public static Activity6_Contact_Us_Fragment2 getInstance() {
        Activity6_Contact_Us_Fragment2 fragment = new Activity6_Contact_Us_Fragment2();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity6_contact_us_fragment2, container, false);
        // 主畫面UI
        text_input1 = v.findViewById(R.id.text_input1);
        text_input2 = v.findViewById(R.id.text_input2);
        checkBox = v.findViewById(R.id.checkBox);
        Submit = v.findViewById(R.id.button_submit);
        webView = v.findViewById(R.id.web);
        // 初始化設置
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        // 網站Load
        webView.loadUrl("https://forms.gle/FzdopLvAhnAumQ6F9");
        // 取得裝置資訊
        DeviceInfo = getDeviceInformation();
        // 取得app資訊 (版本, 版本號)
        android.content.pm.PackageInfo pinfo = null;
        try {
            pinfo = getActivity().getPackageManager().getPackageInfo("com.niu.csie.edu.app", android.content.pm.PackageManager.GET_ACTIVITIES);
            AppInfo = "app版本：" + pinfo.versionName + "\\napp版本號：" + pinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }


        //事件
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (text_input1.getText().toString().equals("") || text_input2.getText().toString().equals("")) {
                    showMessage(getActivity().getString(R.string.EmptyContent));
                } else {
                    String jsCode;
                    // 標題
                    jsCode = "var textarea = document.getElementsByClassName('KHxj8b tL9Q4c')[0];" +
                            "textarea.value = '" + text_input1.getText().toString() + "';" +
                            "textarea.dispatchEvent(new Event('input', { bubbles: true }));" +
                            "textarea.dispatchEvent(new Event('change', { bubbles: true }));";
                    // 內容描述
                    jsCode = jsCode +
                            "var textarea = document.getElementsByClassName('KHxj8b tL9Q4c')[1];" +
                            "textarea.value = '" + text_input2.getText().toString().replace("\n", "\\n") + "';" +
                            "textarea.dispatchEvent(new Event('input', { bubbles: true }));" +
                            "textarea.dispatchEvent(new Event('change', { bubbles: true }));";
                    if (checkBox.isChecked()) {
                        // 傳送設備資訊
                        jsCode = jsCode +
                                "var textarea = document.getElementsByClassName('KHxj8b tL9Q4c')[2];" +
                                "textarea.value = '" + DeviceInfo + "';" +
                                "textarea.dispatchEvent(new Event('input', { bubbles: true }));" +
                                "textarea.dispatchEvent(new Event('change', { bubbles: true }));";
                    }
                    // app 資訊
                    jsCode = jsCode +
                            "var textarea = document.getElementsByClassName('KHxj8b tL9Q4c')[3];" +
                            "textarea.value = '" + AppInfo + "';" +
                            "textarea.dispatchEvent(new Event('input', { bubbles: true }));" +
                            "textarea.dispatchEvent(new Event('change', { bubbles: true }));";
                    // 發送
                    jsCode = jsCode + "document.querySelector('div[aria-label=\"Submit\"]').click();";
                    webView.evaluateJavascript(jsCode,
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    showMessage(getActivity().getString(R.string.Submit_Successful));
                                    requireActivity().finish();
                                    requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                }
                            });
                }
            }
        });

        return v;
    }

    private String getDeviceInformation() {
        StringBuilder deviceInfo = new StringBuilder();

        // 裝置名稱
        deviceInfo.append("裝置名稱：").append(Build.DEVICE).append("\\n");
        // 製造商
        deviceInfo.append("製造商：").append(Build.MANUFACTURER).append("\\n");
        // 型號
        deviceInfo.append("型號：").append(Build.MODEL).append("\\n");
        // 品牌
        deviceInfo.append("品牌：").append(Build.BRAND).append("\\n");
        // 安卓版本
        deviceInfo.append("Android 版本：").append(Build.VERSION.RELEASE).append("\\n");
        // API等級
        deviceInfo.append("API等級：").append(Build.VERSION.SDK_INT).append("\\n");
        // 建置ID
        deviceInfo.append("建置 ID：").append(Build.ID).append("\\n");
        // 硬體
        deviceInfo.append("硬體：").append(Build.HARDWARE).append("\\n");
        // 主機板
        deviceInfo.append("主機板：").append(Build.BOARD).append("\\n");
        // 使用者名稱
        deviceInfo.append("使用者：").append(Build.USER).append("\\n");
        // 引導程式版本
        deviceInfo.append("Bootloader: ").append(Build.BOOTLOADER).append("\\n");

        return deviceInfo.toString();
    }

    private void showMessage(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }
}
