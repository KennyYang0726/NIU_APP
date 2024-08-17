package com.niu.csie.edu.app;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.niu.csie.edu.app.Event_Registration_FragmentAdapter.Event_Registration_Fragment2Adapter;



public class Activity5_Event_Registration_Fragment2 extends Fragment implements Event_Registration_Fragment2Adapter.OnDetailButtonClickListener, Activity5_Event_Registration.RefreshableFragment {

    /**Element*/
    private WebView webView;
    private ListView listView;
    private View ProgressOverlay;
    private EditText editTextTel, editTextMail, editTextRemark;
    private RadioGroup radiogroup1, radiogroup2;
    private RadioButton radio_1_1, radio_1_2,radio_1_3,radio_2_1,radio_2_2;

    /**Components*/
    private AlertDialog DialogEventDetail;
    private AlertDialog DialogEventModInfo;

    /**Variable*/
    private String url_bottom = "https://ccsys.niu.edu.tw/MvcTeam/Act/ApplyMe";
    private String Method = "GetData";
    private String EventID = "";
    private List<JSONObject> readData = new ArrayList<>();
    private boolean isPostHandled = false; // 新增標誌位



    public Activity5_Event_Registration_Fragment2() {
        // Required empty public constructor
    }

    public static Activity5_Event_Registration_Fragment2 getInstance() {
        Activity5_Event_Registration_Fragment2 fragment = new Activity5_Event_Registration_Fragment2();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity5_event_registration_fragment2, container, false);
        // 主畫面UI
        webView = v.findViewById(R.id.web);
        ProgressOverlay = v.findViewById(R.id.progress_overlay);
        listView = v.findViewById(R.id.listview);

        // 初始化設置
        webView.getSettings().setJavaScriptEnabled(true);
        // 網站Load
        webView.loadUrl(url_bottom);

        showProgressOverlay();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 頁面加載完成，
                if (Method.equals("ModInfo")) {
                    // 修改資訊
                    GetRegisterInfo();
                } else {
                    // 獲取已報名活動資訊
                    GetData();
                }

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
    public void onModInfoButtonClick(String eventID) {
        showProgressOverlay(); // 避免手賤連按
        Method = "ModInfo";
        String url = "https://ccsys.niu.edu.tw/MvcTeam/Act/RegData/" + eventID;
        webView.loadUrl(url);
        EventID = eventID;
    }



    @Override
    public void refresh() {
        GetData();
    }

    private void GetData() {
        String jsCode =
                "(function() { " +
                "    var data = []; " +
                "    var count = document.querySelector('.col-md-11.col-md-offset-1.col-sm-10.col-xs-12.col-xs-offset-0').querySelectorAll('.row.enr-list-sec').length;" +
                "    for(let i=0; i<count; i++) {" +
                "        let row = document.querySelectorAll('.row.enr-list-sec')[i];" + // 報名項目 父容器
                "        let row_state = document.querySelectorAll('.row.bg-warning')[i];" + // 報名狀態 父容器
                "        let dialog = row.querySelector('.table');" + // 活動詳情 Dialog 父容器
                "        let name = row.querySelector('h3').innerText.trim();" + // 名稱
                "        let department = row.querySelector('.col-sm-3.text-center.enr-list-dep-nam.hidden-xs').title.split('：')[1].trim();" + // 主辦單位
                "        let state = row_state.querySelector('.text-danger.text-shadow').innerText.split('：')[1].trim();" + // 報名狀態 (正取/候補/現場)
                "        let event_state = row.querySelector('.btn.btn-danger').innerText.trim();" + // 活動狀態
                "        let eventSerialID = row.querySelector('p').innerText.split('：')[1].split(' ')[0].trim();" + // 活動編號
                "        let eventTime = row.querySelector('.fa-calendar').parentElement.innerText.replace(/\\s+/g,'').replace('~','~\\n').trim();" + // 活動時間
                "        let eventTime_formatted = eventTime.replace('~','起')+'止'.trim();" + // 活動時間 格式化版
                "        let eventLocation = row.querySelector('.fa-map-marker').parentElement.innerText.trim();" + // 活動地點
                "        let eventDetail = dialog.querySelectorAll('tr')[3].querySelectorAll('td')[1].textContent.replace(/\\s+/g,'').replace('<br>','\\n').replace('\"','').trim();" + // 活動說明
                "        let contactInfoText = dialog.querySelectorAll('tr')[5].querySelectorAll('td')[1].innerHTML;" + // 聯絡資訊(3項)
                "        let contactInfos = contactInfoText.split('<br>').map(function(info) {" +
                "            return info.replace(/<[^>]*>/g,'').trim();" +
                "        });" + // 以 [index] 抓取3項資訊
                "        let Related_links = dialog.querySelectorAll('tr')[6].querySelectorAll('td')[1].textContent.replace(/\\s+/g,'').trim();" + // 相關連結
                "        let Remark = dialog.querySelectorAll('tr')[7].querySelectorAll('td')[1].textContent.replace(/\\s+/g,'').replace('<br>','\\n').replace('\"','').trim();" + // 備註
                "        let Multi_factor_authentication = dialog.querySelectorAll('tr')[8].querySelectorAll('td')[1].textContent.replace(/\\s+/g,'').replace('<br>','\\n').replace('\"','').trim();" + // 多元認證
                "        let eventRegisterTime = dialog.querySelectorAll('tr')[9].querySelectorAll('td')[1].textContent.replace(/\\s+/g,'').replace('~','~\\n').trim();" + // 報名時間
                // "        let eventPeople = row.querySelector('.fa-user-plus').parentElement.innerText.replace(/\\s+/g,'').replace('，','人\\n')+'人'.trim();" + // 報名人數
                "        data[i] = {name, department, state, event_state, eventSerialID, eventTime, eventTime_formatted, eventLocation, eventDetail, contactInfoName: contactInfos[0], contactInfoTel: contactInfos[1], contactInfoMail: contactInfos[2], Related_links, Remark, Multi_factor_authentication, eventRegisterTime};" +
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
                    Event_Registration_Fragment2Adapter adapter = new Event_Registration_Fragment2Adapter(getContext(), readData, Activity5_Event_Registration_Fragment2.this);
                    listView.setAdapter(adapter);
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

    // 取得報名修改資訊
    private void GetRegisterInfo() {
        String jsCode =
                "(function() { " +
                "    let RequestVerificationToken = document.querySelector('[name=\"__RequestVerificationToken\"]').value;" +
                "    let SignId = document.getElementById('SignId').value;" +
                "    let role = document.querySelectorAll('.col-xs-8')[0].innerText.trim();" +
                "    let classes = document.querySelectorAll('.col-xs-8')[1].innerText.trim();" +
                "    let schnum = document.querySelectorAll('.col-xs-8')[2].innerText.trim();" +
                "    let name = document.querySelectorAll('.col-xs-8')[3].innerText.trim();" +
                "    let Tel = document.getElementById('SignTEL').value.toString();" +
                "    let Mail = document.getElementById('SignEmail').value;" +
                "    let Remark = document.getElementById('SignMemo').value;" +
                "    let selectedFood = document.querySelector('input[name=\"Food\"]:checked').value;" +
                "    let selectedProof = document.querySelector('input[name=\"Proof\"]:checked').value;" +

                "    let result = {" +
                "        'RequestVerificationToken': RequestVerificationToken," +
                "        'SignId': SignId," +
                "        'role': role," +
                "        'classes': classes," +
                "        'schnum': schnum," +
                "        'name': name," +
                "        'Tel': String(Tel)," +
                "        'Mail': Mail," +
                "        'selectedFood': selectedFood," +
                "        'selectedProof': selectedProof" +
                "    };" +
                "    if (Remark !== '') result['Remark'] = Remark;" + // 只有當 Remark 不為空時才加入
                "    return JSON.stringify(result);" +
                "})();";
        webView.evaluateJavascript(jsCode, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                value = value.replace("\"", "");
                value = value.replace("\\", "");
                try {
                    JSONObject jsonObject = new JSONObject(value);
                    String requestVerificationToken = jsonObject.getString("RequestVerificationToken");
                    String signID = jsonObject.getString("SignId");
                    String role = jsonObject.getString("role");
                    String classes = jsonObject.getString("classes");
                    String schnum = jsonObject.getString("schnum");
                    String name = jsonObject.getString("name");
                    String tel = String.format("0%d", (long) Double.parseDouble(jsonObject.getString("Tel")));
                    String mail = jsonObject.getString("Mail");
                    int selectedFood = jsonObject.getInt("selectedFood");
                    int selectedProof = jsonObject.getInt("selectedProof");
                    // 處理選擇第三項 公務人員時數
                    if (selectedProof == 3) {
                        selectedProof = 2; // 需要證明
                    }
                    String remark;
                    if (jsonObject.has("Remark")) {
                        remark = jsonObject.optString("Remark", null);
                    } else {
                        remark = "";
                    }
                    showModInfoDialog(EventID, requestVerificationToken, signID, role, classes, schnum, name, tel, mail, remark, selectedFood, selectedProof);
                } catch (Exception e) {
                    Log.d("asdfgggg", e.getMessage());
                    // showMessage(getString(R.string.Event_ShowMessageJsonERR));
                    // showMessage(e.getMessage());
                }
            }
        });
    }

    // 顯示修改資料 Dialog
    private void showModInfoDialog(String EventID, String requestVerificationToken, String signID, String role, String classes, String schnum, String name, String tel, String mail, String remark, int selectedFood, int selectedProof) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.custom_alert_dialog_event_modding_info, null);
        builder.setView(view);
        ((TextView) view.findViewById(R.id.text_Event_ModInfo_role)).setText(role);
        ((TextView) view.findViewById(R.id.text_Event_ModInfo_class)).setText(classes);
        ((TextView) view.findViewById(R.id.text_Event_ModInfo_schnum)).setText(schnum);
        ((TextView) view.findViewById(R.id.text_Event_ModInfo_name)).setText(name);
        editTextTel = view.findViewById(R.id.text_Event_ModInfo_tel);
        editTextMail = view.findViewById(R.id.text_Event_ModInfo_mail);
        editTextRemark = view.findViewById(R.id.text_Event_ModInfo_remark);
        radiogroup1 = view.findViewById(R.id.radiogroup1);
        radiogroup2 = view.findViewById(R.id.radiogroup2);

        editTextTel.setText(tel);
        editTextMail.setText(mail);
        editTextRemark.setText(remark);
        // 初始化 RadioGroup1 的預設選取項
        radiogroup1.check(radiogroup1.getChildAt(selectedFood - 1).getId());
        // 初始化 RadioGroup2 的預設選取項
        radiogroup2.check(radiogroup2.getChildAt(selectedProof - 1).getId());


        builder.setCancelable(true);
        DialogEventModInfo = builder.create();

        view.findViewById(R.id.buttonAction_Cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogEventModInfo.dismiss();
                int radioButtonIDFood = radiogroup1.getCheckedRadioButtonId();
                View radioButtonFood = radiogroup1.findViewById(radioButtonIDFood);
                int idxFood = radiogroup1.indexOfChild(radioButtonFood)+1; // index 從 0 計算，post 資訊從 1 計算
                int radioButtonIDProof = radiogroup2.getCheckedRadioButtonId();
                View radioButtonProof = radiogroup2.findViewById(radioButtonIDProof);
                int idxProof = radiogroup2.indexOfChild(radioButtonProof)+1; // index 從 0 計算，post 資訊從 1 計算
                HandlePost(EventID, requestVerificationToken, signID, editTextTel.getText().toString(), editTextMail.getText().toString(), editTextRemark.getText().toString(), idxFood, idxProof, "確定取消");
                Method = "Stop ModInfo";
            }
        });
        view.findViewById(R.id.buttonAction_Save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogEventModInfo.dismiss();
                int radioButtonIDFood = radiogroup1.getCheckedRadioButtonId();
                View radioButtonFood = radiogroup1.findViewById(radioButtonIDFood);
                int idxFood = radiogroup1.indexOfChild(radioButtonFood)+1; // index 從 0 計算，post 資訊從 1 計算
                int radioButtonIDProof = radiogroup2.getCheckedRadioButtonId();
                View radioButtonProof = radiogroup2.findViewById(radioButtonIDProof);
                int idxProof = radiogroup2.indexOfChild(radioButtonProof)+1; // index 從 0 計算，post 資訊從 1 計算
                HandlePost(EventID, requestVerificationToken, signID, editTextTel.getText().toString(), editTextMail.getText().toString(), editTextRemark.getText().toString(), idxFood, idxProof, "儲存修改");
                Method = "Stop ModInfo";
            }
        });

        if (DialogEventModInfo.getWindow() != null){
            DialogEventModInfo.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        DialogEventModInfo.show();
        hideProgressOverlay();
    }

    private void HandlePost(String EventID, String requestVerificationToken, String signID, String tel, String mail, String remark, int food, int proof, String action) {

        isPostHandled = true;

        showProgressOverlay();
        String formData;
        try {
            formData = "__RequestVerificationToken=" + URLEncoder.encode(requestVerificationToken, "UTF-8") +
                    "&ApplyId=" + URLEncoder.encode(EventID, "UTF-8") +
                    "&SignId=" + URLEncoder.encode(signID, "UTF-8") +
                    "&SignTEL=" + URLEncoder.encode(tel, "UTF-8") +
                    "&SignEmail=" + URLEncoder.encode(mail, "UTF-8") +
                    "&SignMemo=" + URLEncoder.encode(remark, "UTF-8") +
                    "&Food=" + URLEncoder.encode(String.valueOf(food), "UTF-8") +
                    "&Proof=" + URLEncoder.encode(String.valueOf(proof), "UTF-8") +
                    "&action=" + URLEncoder.encode(action, "UTF-8");
            formData = formData.replace("%22", ""); // 編碼處理產生了%22(不應該出現)

            byte[] postData = formData.getBytes(StandardCharsets.UTF_8);

            if (isPostHandled) {
                webView.postUrl("https://ccsys.niu.edu.tw/MvcTeam/Act/RegData/" + EventID, postData);
            }

            // 循環檢查POST請求是否完成
            Handler handler = new Handler();
            Runnable checkPostCompletion = new Runnable() {
                @Override
                public void run() {
                    webView.evaluateJavascript(
                            "(function() {" +
                                    "    var h1Element = document.querySelector('h1.text-danger.text-shadow');" +
                                    "    return h1Element ? h1Element.innerText.includes('報名') : false;" +
                                    "})();",
                            value -> {
                                if (Boolean.parseBoolean(value)) {
                                    hideProgressOverlay();
                                    isPostHandled = false;
                                    webView.loadUrl(url_bottom);
                                    if (action.equals("確定取消")) {
                                        showMessage(getString(R.string.Event_ModInfo_btn1_success));
                                    } else {
                                        showMessage(getString(R.string.Event_ModInfo_btn2_success));
                                    }
                                    webView.loadUrl(url_bottom);
                                    Method = "GetData";
                                    /*
                                    if (getActivity() instanceof Activity5_Event_Registration) {
                                        ((Activity5_Event_Registration) getActivity()).refreshFragments();
                                    }*/

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
