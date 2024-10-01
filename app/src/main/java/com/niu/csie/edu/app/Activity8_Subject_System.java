package com.niu.csie.edu.app;

import android.content.*;
import android.os.*;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.HashMap;
import java.util.Map;


public class Activity8_Subject_System extends AppCompatActivity {

	/**Element*/
	private Toolbar _toolbar;
	private WebView Web_選課系統;
	private View ProgressOverlay;

	/**Components*/
	private Handler handler;
	private Runnable refreshRunnable;
	private Intent page = new Intent();

	/**Variable*/
	private static final int REFRESH_INTERVAL = 1800000; //  30 分鐘，防止 Acade 閒置自動登出

	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.activity8_subject_system);
		initialize(_savedInstanceState);
		initializeLogic();
	}

	@Override
	public void onBackPressed() {
		if (Web_選課系統.canGoBack()) {
			showProgressOverlay();
			// 網站Load
			Map<String, String> headers = new HashMap<>();
			headers.put("Referer", "https://acade.niu.edu.tw/NIU/Application/TKE/TKE20/TKE2011_.aspx?progcd=TKE2011");
			Web_選課系統.loadUrl("https://acade.niu.edu.tw/NIU/Application/TKE/TKE20/TKE2011_01.aspx", headers);
		} else {
			super.onBackPressed();
			page.setClass(getApplicationContext(), Activity1_HomeActivity.class);
			startActivity(page);
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		_toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		setActionBarTitle(getResources().getString(R.string.Subject_System));
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
		Web_選課系統 = findViewById(R.id.web_Subject_System);
		ProgressOverlay = findViewById(R.id.progress_overlay);

		// 初始化設置
		Web_選課系統.getSettings().setJavaScriptEnabled(true);
		Web_選課系統.setWebContentsDebuggingEnabled(true);
		Web_選課系統.getSettings().setDomStorageEnabled(true);
		Web_選課系統.getSettings().setLoadWithOverviewMode(true);
		Web_選課系統.getSettings().setUseWideViewPort(true);
		// 禁用縮放控制，但啟用內部縮放功能
		Web_選課系統.getSettings().setSupportZoom(true);
		Web_選課系統.getSettings().setBuiltInZoomControls(true);
		Web_選課系統.getSettings().setDisplayZoomControls(false);
		// 讓 WebView 的寬度適應屏幕寬度
		Web_選課系統.setInitialScale(1);
		// 顯示等待中
		showProgressOverlay();

		// 網站Load
		Map<String, String> headers = new HashMap<>();
		headers.put("Referer", "https://acade.niu.edu.tw/NIU/Application/TKE/TKE20/TKE2011_.aspx?progcd=TKE2011");
		Web_選課系統.loadUrl("https://acade.niu.edu.tw/NIU/Application/TKE/TKE20/TKE2011_01.aspx", headers);
		Web_選課系統.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				String url = request.getUrl().toString();
				if (url.contains("chart.googleapis.com")) {
					return true;
				}
				if (url.startsWith("about:blank")) {
					showMessage("Not Support");
				}
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (url.contains("niu.edu.tw")) {
					// 成功進入選課畫面
					hideProgressOverlay();
				}
			}
		});

		Web_選課系統.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onPermissionRequest(PermissionRequest request) {
				request.grant(request.getResources());
			}
			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				if (message.contains("選課期間")) {
					showMessage(getResources().getString(R.string.currently_not_a_course_selection_time));
					page.setClass(getApplicationContext(), Activity1_HomeActivity.class);
					startActivity(page);
					overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
					Web_選課系統.destroy();
					finish();
					return true;
				}
				return super.onJsAlert(view, url, message, result);
			}
		});
		
	}

	// 設標題
	private void setActionBarTitle(String title) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(title);
		}
	}
	
	private void initializeLogic() {
		// 防止 Acade 閒置登出
		handler = new Handler();
		refreshRunnable = new Runnable() {
			@Override
			public void run() {
				// 重新整理
				Web_選課系統.reload();
				handler.postDelayed(this, REFRESH_INTERVAL);
			}
		};
		handler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
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

	public void WebView_Help(View view) {
		Intent page = new Intent(getApplicationContext(), Activity_WebviewProvider.class);
		startActivity(page);
	}


	private void showMessage(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

}
