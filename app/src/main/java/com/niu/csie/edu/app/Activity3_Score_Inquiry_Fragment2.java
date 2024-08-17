package com.niu.csie.edu.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.niu.csie.edu.app.ScoreQuote.ScoreQuote;
import com.niu.csie.edu.app.ScoreQuoteAdapter.ScoreQuoteAdapter;


public class Activity3_Score_Inquiry_Fragment2 extends Fragment {

    /**Element*/
    private WebView Web_分數查詢;
    private ListView listView;
    private TextView AVG, RANKING;
    private View ProgressOverlay;

    /**Components*/


    /**Variable*/
    private List<ScoreQuote> grades = new ArrayList<>();
    private String avg, rank = "";
    private ScoreQuoteAdapter adapter;


    public Activity3_Score_Inquiry_Fragment2() {
        // Required empty public constructor
    }


    public static Activity3_Score_Inquiry_Fragment2 getInstance() {
        Activity3_Score_Inquiry_Fragment2 fragment = new Activity3_Score_Inquiry_Fragment2();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity3_score_inquiry_fragment2, container, false);
        // 主畫面UI
        ProgressOverlay = v.findViewById(R.id.progress_overlay);
        Web_分數查詢 = v.findViewById(R.id.web_Score_Inquiry);
        listView = v.findViewById(R.id.listView);
        AVG = v.findViewById(R.id.text_avg);
        RANKING = v.findViewById(R.id.text_ranking);
        // 初始化設置
        Web_分數查詢.getSettings().setJavaScriptEnabled(true);
        Web_分數查詢.getSettings().setBlockNetworkImage(true);
        Web_分數查詢.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        Web_分數查詢.setWebViewClient(new WebViewClient());
        // 網站Load
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", "https://acade.niu.edu.tw/NIU/Application/GRD/GRD51/GRD5130_.aspx?progcd=GRD5130");
        Web_分數查詢.loadUrl("https://acade.niu.edu.tw/NIU/Application/GRD/GRD51/GRD5130_02.aspx", headers);

        Web_分數查詢.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 頁面加載完成，加載資訊
                loadGrades();
                loadAvgAndRank();
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true; // 表示我們已經處理這個 URL，不需要再外部處理，eg: 跳轉外部瀏覽器
            }
        });

        Web_分數查詢.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress < 100) {
                    showProgressOverlay();
                }
            }
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) { // 可以從 logcat 中看到 webview 的 console.log
                Log.d("MyApplication", consoleMessage.message() + " -- From line " +
                        consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
                return true;
            }
        });
        // 初始化 adapter
        adapter = new ScoreQuoteAdapter(getActivity(), grades);
        listView.setAdapter(adapter);

        return v;
    }


    private void loadGrades() {
        Web_分數查詢.evaluateJavascript("(function() { var result = ''; for (var i = 2; document.querySelector('#DataGrid > tbody > tr:nth-child(' + i + ')') != null; i++) { var type = document.querySelector('#DataGrid > tbody > tr:nth-child(' + i + ') > td:nth-child(4)').innerText; var lesson = document.querySelector('#DataGrid > tbody > tr:nth-child(' + i + ') > td:nth-child(5)').innerText; var score = document.querySelector('#DataGrid > tbody > tr:nth-child(' + i + ') > td:nth-child(6)').innerText; if (type.length < 2) { type = '必修'; } result += type + ',' + lesson + ',' + score + ';'; } return result; })();", value -> {
            if (value != null && !value.equals("null")) {
                String[] entries = value.replace("\"", "").split(";");
                for (String entry : entries) {
                    String[] parts = entry.split(",");
                    if (parts.length == 3) {
                        grades.add(new ScoreQuote(parts[0], parts[1], parts[2]));
                    }
                }
                sortGrades();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            hideProgressOverlay();
                        }
                    });
                }
            }
        });
    }

    private void sortGrades() {
        Collections.sort(grades, Comparator.comparing(ScoreQuote::getScore));
        Collections.sort(grades, (a, b) -> {
            if (a.getScore().contains("未上傳") || b.getScore().contains("未上傳")) {
                return -1;
            } else {
                return Double.compare(Double.parseDouble(b.getScore()), Double.parseDouble(a.getScore()));
            }
        });
    }

    // 載入平均和排名
    private void loadAvgAndRank() {
        Web_分數查詢.evaluateJavascript(
                "document.querySelector('#Q_CRS_AVG_MARK').innerText",
                new ValueCallback<String>() {
                    @SuppressLint({"StringFormatInvalid", "SetTextI18n"})
                    @Override
                    public void onReceiveValue(String value) {
                        double avg2; // 轉換用，若無法轉換代表尚未上傳計算完成
                        avg = value.replaceAll("\"", "");
                        try {
                            avg2 = Double.parseDouble(avg);
                        } catch (Exception e) {
                            avg = getActivity().getResources().getString(R.string.CanNotCalc);
                        }
                        AVG.setText(getActivity().getResources().getString(R.string.Avg) + avg);
                    }
                }
        );

        Web_分數查詢.evaluateJavascript(
                "document.querySelector('#QTable2 > tbody > tr:nth-child(2) > td:nth-child(2) > table > tbody > tr:nth-child(2) > td:nth-child(4)').innerText",
                new ValueCallback<String>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onReceiveValue(String value) {
                        rank = value.replaceAll("[^0-9]", "");
                        int rank2; // 轉換用，若無法轉換代表尚未上傳計算完成
                        try {
                            rank2 = Integer.parseInt(rank);
                            // 英文式轉換
                            if (!Locale.getDefault().toLanguageTag().contains("zh")) {
                                if (rank2%10==1 && rank2!=11) { // st
                                    rank = rank+"st";
                                } else if (rank2%10==2 && rank2!=12) { // nd
                                    rank = rank+"nd";
                                } else if (rank2%10==3 && rank2!=13) { // rd
                                    rank = rank+"rd";
                                } else {
                                    rank = rank+"th";
                                }
                            }
                        } catch (Exception e) {
                            rank = getActivity().getResources().getString(R.string.CanNotCalc);
                        }
                        RANKING.setText(getActivity().getResources().getString(R.string.Ranking_Text) + MessageFormat.format(getActivity().getResources().getString(R.string.Ranking), rank));
                    }
                }
        );
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
