package com.niu.csie.edu.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.niu.csie.edu.app.Event_Registration_FragmentAdapter.Event_Registration_Fragment1Adapter;



public class Activity5_Event_Registration_Fragment1 extends Fragment implements Event_Registration_Fragment1Adapter.OnRegisterButtonClickListener, Activity5_Event_Registration.RefreshableFragment {

    /**Element*/
    private WebView webView;
    private ListView listview;
    private View ProgressOverlay;

    /**Components*/
    private AlertDialog DialogEventDetail;

    /**Variable*/
    private String url_bottom = "https://ccsys.niu.edu.tw/MvcTeam/Act";
    private List<JSONObject> readData = new ArrayList<>();
    private boolean isPostHandled = false; // 新增標誌位



    public Activity5_Event_Registration_Fragment1() {
        // Required empty public constructor
    }

    public static Activity5_Event_Registration_Fragment1 getInstance() {
        Activity5_Event_Registration_Fragment1 fragment = new Activity5_Event_Registration_Fragment1();
        return fragment;
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity5_event_registration_fragment1, container, false);
        // 主畫面UI
        webView = v.findViewById(R.id.web);
        listview = v.findViewById(R.id.listview);
        ProgressOverlay = v.findViewById(R.id.progress_overlay);
        // 初始化設置
        webView.getSettings().setJavaScriptEnabled(true);
        // 網站Load
        webView.loadUrl(url_bottom);

        showProgressOverlay();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 頁面加載完成，獲取活動資訊
                GetData();
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true; // 表示我們已經處理這個 URL，不需要再外部處理，eg: 跳轉外部瀏覽器
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) { // 可以從 logcat 中看到 webview 的 console.log
                Log.d("WebViewwwww", consoleMessage.message() + " -- From line " +
                        consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
                return true;
            }
        });
        return v;
    }

    @Override
    public void onDetailButtonClick(String name, String ID, String time, String location, String detail, String department, String contactInfoName, String contactInfoTel, String contactInfoMail, String Related_links, String Remark, String Multi_factor_authentication, String eventRegisterTime) {
        showDetailDialog(name, ID, time, location, detail, department, contactInfoName, contactInfoTel, contactInfoMail, Related_links, Remark, Multi_factor_authentication, eventRegisterTime);
    }

    @Override
    public void onRegisterButtonClick(String eventID) {
        // Call the method you want to execute and pass the eventID
        RegisterEvent(eventID);
    }


    @Override
    public void refresh() {
        GetData(); // 重新獲取資料並更新 ListView
    }


    private void GetData() {
        String jsCode =
                "(function() { " +
                "    var data = []; " +
                "    var skip = 0; " +
                "    var count = document.querySelector('.col-md-11.col-md-offset-1.col-sm-10.col-xs-12.col-xs-offset-0').querySelectorAll('.row.enr-list-sec').length;" +
                "    for(let i=0; i<count; i++) {" +
                "        let row = document.querySelectorAll('.row.enr-list-sec')[i];" +
                "        let dialog = row.querySelector('.table');" + // 活動詳情 Dialog 父容器
                "        let name = row.querySelector('h3').innerText.trim();" + // 名稱
                "        let department = row.querySelector('.col-sm-3.text-center.enr-list-dep-nam.hidden-xs').title.split('：')[1].trim();" + // 主辦單位
                "        let state = row.querySelector('.badge.alert-danger').innerText.trim();" + // 報名狀態
                "        if (state === '活動已結束') {count--;skip++;continue;}" + // 活動結束 直接跳過
                "        let targets = row.querySelector('.fa-id-badge').parentElement.innerText.trim();" + // 活動對象
                "        if (!targets.includes('本校在校生')) {count--;skip++;continue;}" + // 活動對象不包含學生 直接跳過
                "        let eventSerialID = row.querySelector('p').innerText.split('：')[1].split(' ')[0].trim();" + // 活動編號
                "        let eventTime = row.querySelector('.fa-calendar').parentElement.innerText.replace(/\\s+/g,'').replace('~','起\\n')+'止'.trim();" + // 活動時間
                "        let eventLocation = row.querySelector('.fa-map-marker').parentElement.innerText.trim();" + // 活動地點
                "        let eventRegisterTime = row.querySelector('.table').querySelectorAll('tr')[9].querySelectorAll('td')[1].textContent.replace(/\\s+/g,'').replace('~','起\\n')+'止'.trim();" + // 報名時間



                "        let eventDetail = dialog.querySelectorAll('tr')[3].querySelectorAll('td')[1].textContent.replace(/\\s+/g,'').replace('<br>','\\n').replace('\"','').trim();" + // 活動說明
                "        let contactInfoText = dialog.querySelectorAll('tr')[5].querySelectorAll('td')[1].innerHTML;" + // 聯絡資訊(3項)
                "        let contactInfos = contactInfoText.split('<br>').map(function(info) {" +
                "            return info.replace(/<[^>]*>/g,'').trim();" +
                "        });" + // 以 [index] 抓取3項資訊
                "        let Related_links = dialog.querySelectorAll('tr')[6].querySelectorAll('td')[1].textContent.replace(/\\s+/g,'').trim();" + // 相關連結
                "        let Remark = dialog.querySelectorAll('tr')[7].querySelectorAll('td')[1].textContent.replace(/\\s+/g,'').replace('<br>','\\n').replace('\"','').trim();" + // 備註



                "        let Multi_factor_authentication = dialog.querySelectorAll('tr')[8].querySelectorAll('td')[1].textContent.replace(/\\s+/g,'').replace('<br>','\\n').replace('已認證，','').replace('\"','').trim();" +  // 多元認證
                "        let eventPeople = row.querySelector('.fa-user-plus').parentElement.innerText.replace(/\\s+/g,'').replace('，','人\\n')+'人'.trim();" + // 報名人數
                //"        console.log(eventRegisterTime);" +
                //"        let eventDescription = row.querySelector('.small.hidden-xs').innerText.trim();" +
                "        data[i-skip] = {name, department, state, eventSerialID, eventTime, eventLocation, eventRegisterTime, eventDetail, contactInfoName: contactInfos[0], contactInfoTel: contactInfos[1], contactInfoMail: contactInfos[2], Related_links, Remark, Multi_factor_authentication, eventPeople};" +
                "    }" +
                "    return data; " +
                "})();";
        webView.evaluateJavascript(jsCode, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                hideProgressOverlay();
                try {
                    JSONArray jsonArray = new JSONArray(value);
                    readData.clear(); // 清空舊資料
                    for (int i = 0; i < jsonArray.length(); i++) {
                        readData.add(jsonArray.getJSONObject(i));
                    }
                    // ListView 設置 Adapter
                    Event_Registration_Fragment1Adapter adapter = new Event_Registration_Fragment1Adapter(getContext(), readData, Activity5_Event_Registration_Fragment1.this);
                    listview.setAdapter(adapter);
                    adapter.notifyDataSetChanged(); // 通知資料變更

                } catch (Exception e) {
                    Log.d("asdfg", e.getMessage());
                    // showMessage(getString(R.string.Event_ShowMessageJsonERR));
                    // showMessage(e.getMessage());
                }
            }
        });
    }


    // 顯示活動詳情 Dialog
    private void showDetailDialog(String name, String ID, String time, String location, String detail, String department, String contactInfoName, String contactInfoTel, String contactInfoMail, String Related_links, String Remark, String Multi_factor_authentication, String eventRegisterTime) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.custom_alert_dialog_event_detail, null);
        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTitle)).setText(name);
        ((TextView) view.findViewById(R.id.text_EventID)).setText(ID);
        ((TextView) view.findViewById(R.id.text_EventTime)).setText(time);
        ((TextView) view.findViewById(R.id.text_EventLocation)).setText(location);
        ((TextView) view.findViewById(R.id.text_EventDetail)).setText(detail);
        ((TextView) view.findViewById(R.id.text_EventDepartment)).setText(department);
        ((TextView) view.findViewById(R.id.text_EventContactInfoName)).setText(contactInfoName);
        ((TextView) view.findViewById(R.id.text_EventContactInfoTel)).setText(contactInfoTel);
        ((TextView) view.findViewById(R.id.text_EventContactInfoMail)).setText(contactInfoMail);
        ((TextView) view.findViewById(R.id.text_EventLink)).setText(Related_links);
        ((TextView) view.findViewById(R.id.text_EventRemark)).setText(Remark);
        ((TextView) view.findViewById(R.id.text_Event_FactorAuthentication)).setText(Multi_factor_authentication);
        ((TextView) view.findViewById(R.id.text_EventRegisterTime)).setText(eventRegisterTime);
        // 可互動之 TextView
        (view.findViewById(R.id.text_EventContactInfoTel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", ((TextView) view.findViewById(R.id.text_EventContactInfoTel)).getText().toString()));
            }
        });
        (view.findViewById(R.id.text_EventContactInfoMail)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail(contactInfoMail, getString(R.string.Event_Mail_Title), getString(R.string.Event_Mail_Content));
            }
        });
        (view.findViewById(R.id.text_EventLink)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Related_links.length() > 7) {
                    Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(Related_links));
                    startActivity(browser);
                }

            }
        });

        builder.setCancelable(true);
        DialogEventDetail = builder.create();

        view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogEventDetail.dismiss();
            }
        });

        if (DialogEventDetail.getWindow() != null){
            DialogEventDetail.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        DialogEventDetail.show();
    }

    private void sendEmail(String recipient, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // 只允許處理郵件的應用程式
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { recipient });
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        try {
            startActivity(intent);
        } catch (Exception e) {
            // 無 mail 程式
            showMessage(getString(R.string.Event_Mail_CannotHandle));
        }

    }


    private void RegisterEvent(String EventID) {

        isPostHandled = true;

        showProgressOverlay();
        String URL = "https://ccsys.niu.edu.tw/MvcTeam/Act/Apply/" + EventID;
        webView.loadUrl(URL);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.equals(URL)) {
                    if (isPostHandled) {
                        webView.evaluateJavascript("document.querySelector('[name=\"__RequestVerificationToken\"]').value", token -> {
                            token = token.replaceAll("\"", "");
                            handleRegisterEvent(token, EventID, URL);
                        });
                    }
                }
            }
        });
    }

    private void handleRegisterEvent(String token, String eventID, String PostURL) {
        String formData;
        try {
            formData = "__RequestVerificationToken=" + URLEncoder.encode(token, "UTF-8") +
                    "&id=" + eventID +
                    "&action=" + URLEncoder.encode("我要報名", "UTF-8");
            formData = formData.replace("%22", "");

            byte[] postData = formData.getBytes(StandardCharsets.UTF_8);
            webView.postUrl(PostURL, postData);

            // 循環檢查POST請求是否完成
            Handler handler = new Handler();
            Runnable checkPostCompletion = new Runnable() {
                @Override
                public void run() {
                    webView.evaluateJavascript(
                            "(function() {" +
                                    "    var h1Element = document.querySelector('h1.text-danger.text-shadow');" +
                                    "    return h1Element ? h1Element.innerText.includes('已報名') : false;" +
                                    "})();",
                            value -> {
                                if (Boolean.parseBoolean(value)) {
                                    hideProgressOverlay();
                                    isPostHandled = false;
                                    webView.loadUrl("https://ccsys.niu.edu.tw/MvcTeam/Act/");
                                    showMessage(getString(R.string.Event_Register_Success));
                                    if (getActivity() instanceof Activity5_Event_Registration) {
                                        ((Activity5_Event_Registration) getActivity()).refreshFragments();
                                    }

                                } else {
                                    handler.postDelayed(this, 100);
                                }
                            }
                    );
                }
            };
            handler.postDelayed(checkPostCompletion, 100);

        } catch (Exception e) {
            e.printStackTrace();
            isPostHandled = false;
        }
    }


    //顯示等待中
    private void showProgressOverlay() {
        if (ProgressOverlay != null) {
            ProgressOverlay.setVisibility(View.VISIBLE);
            // 旋轉動畫
            ImageView loadingImage = ProgressOverlay.findViewById(R.id.loading_image);
            Animation rotateAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.prog_rotate_icon);
            loadingImage.startAnimation(rotateAnimation);
        }
    }

    //隱藏等待中
    private void hideProgressOverlay() {
        if (ProgressOverlay != null && ProgressOverlay.getVisibility() == View.VISIBLE) {
            ProgressOverlay.setVisibility(View.GONE);
        }
    }

    private void showMessage(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }
}
