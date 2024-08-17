package com.niu.csie.edu.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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


public class Activity6_Contact_Us_Fragment1 extends Fragment {

    /**Element*/
    private EditText text_input, ContactInfo_input;
    private CheckBox checkBox;
    private Button Submit;
    private WebView webView;

    /**Components*/
    private SharedPreferences sharedPreferences; // 儲存登入資訊

    /**Variable*/
    private String Info = "";



    public Activity6_Contact_Us_Fragment1() {
        // Required empty public constructor
    }


    public static Activity6_Contact_Us_Fragment1 getInstance() {
        Activity6_Contact_Us_Fragment1 fragment = new Activity6_Contact_Us_Fragment1();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity6_contact_us_fragment1, container, false);
        // 主畫面UI
        text_input = v.findViewById(R.id.text_input);
        ContactInfo_input = v.findViewById(R.id.contactinfo_input);
        checkBox = v.findViewById(R.id.checkBox);
        Submit = v.findViewById(R.id.button_submit);
        webView = v.findViewById(R.id.web);
        // 初始化設置
        sharedPreferences = getActivity().getSharedPreferences("sp", Activity.MODE_PRIVATE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        // 網站Load
        webView.loadUrl("https://forms.gle/6vvtRmUKnRr7QHvr6");
        // 取得記名資訊
        Info = sharedPreferences.getString("account", null) + "\\n" + sharedPreferences.getString("Name", null);


        //事件
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (text_input.getText().toString().equals("")) {
                    showMessage(getActivity().getString(R.string.EmptyContent));
                } else {
                    String jsCode;
                    jsCode = "var textarea = document.getElementsByClassName('KHxj8b tL9Q4c')[0];" +
                            "textarea.value = '" + text_input.getText().toString().replace("\n", "\\n") + "';" +
                            "textarea.dispatchEvent(new Event('input', { bubbles: true }));" +
                            "textarea.dispatchEvent(new Event('change', { bubbles: true }));";
                    // 聯絡方式
                    if (!(ContactInfo_input.getText().toString().length() < 3)) {
                        jsCode = jsCode + "var textarea = document.getElementsByClassName('KHxj8b tL9Q4c')[1];" +
                                "textarea.value = '" + ContactInfo_input.getText().toString().replace("\n", "\\n") + "';" +
                                "textarea.dispatchEvent(new Event('input', { bubbles: true }));" +
                                "textarea.dispatchEvent(new Event('change', { bubbles: true }));";
                    }
                    if (checkBox.isChecked()) {
                        // 記名
                        jsCode = jsCode + "var textarea = document.getElementsByClassName('KHxj8b tL9Q4c')[2];" +
                                "textarea.value = '" + Info + "';" +
                                "textarea.dispatchEvent(new Event('input', { bubbles: true }));" +
                                "textarea.dispatchEvent(new Event('change', { bubbles: true }));";
                    }
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

    private void showMessage(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }
}
