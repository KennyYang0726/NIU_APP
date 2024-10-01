package com.niu.csie.edu.app;

import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.os.*;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;


public class Activity9_Bus extends AppCompatActivity {

	/**Element*/
	private Toolbar _toolbar;
	private WebView Web_公車動態;
	private View ProgressOverlay;

	/**Components*/
	private Intent page = new Intent();

	/**Variable*/
	private String now_bus_url = "";
	private String[] jsScripts = {
			"document.querySelector(\"#topNav\").style.display='none'",
			"document.querySelector(\"head\").style.display='none'",
			"document.querySelector(\"#main > div.container > nav\").style.display='none'",
			"document.querySelector(\"#main > div.container > div.srch-input\").style.display='none'",
			"document.querySelector(\"#main > div.page-title.page-title-srch\").style.display='none'",
			"document.querySelector(\"#main > a\").style.display='none'",
			"document.querySelector(\"#footer > a\").style.display='none'",
			"document.querySelector(\"#footer > div.footer-info\").style.display='none'",
			"document.querySelector(\"#btnFPMenuOpen\").style.display='none'",
			"document.querySelector(\"#MasterPageBodyTag > a\").style.display='none'",
			"document.querySelector(\"#MasterPageBodyTag > div\").style = 'padding-top: 10px'",
			"document.querySelector(\"#main > div.bus-header.container-md > div:nth-child(1) > div.bus-title.mb-1.mb-md-3 > div.bus-title__icon > i\").style.display='none'"
	};

	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.activity9_bus);
		initialize(_savedInstanceState);
		initializeLogic();
	}

	@Override
	public void onBackPressed() {
		if (!now_bus_url.contains("https://www.taiwanbus.tw/eBUSPage/Query/RouteQuery.aspx?key=%E5%AE%9C%E8%98%AD%E5%A4%A7%E5%AD%B8&lan=")) {
			LoadWeb();
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
		setActionBarTitle(getResources().getString(R.string.Bus));
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
		Web_公車動態 = findViewById(R.id.web_Bus);
		ProgressOverlay = findViewById(R.id.progress_overlay);

		// 初始化設置
		Web_公車動態.getSettings().setJavaScriptEnabled(true);
		Web_公車動態.getSettings().setDomStorageEnabled(true);
		Web_公車動態.getSettings().setMediaPlaybackRequiresUserGesture(false);
		Web_公車動態.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				Web_公車動態.setVisibility(View.GONE);
				showProgressOverlay();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				/** 檢測語系，由於僅對公車詳細頁面有區別，首頁無英文選項之 url */
				if (url.contains("rno=")) { // 若進入公車詳情頁面
					if (Locale.getDefault().toLanguageTag().contains("zh")) { // 裝置語言為中文
						if (url.contains("&lan=E")) { // 載入網址是英文
							url = url.replace("&lan=E","&lan=C");
							Web_公車動態.loadUrl(url);
						}
					} else { // 裝置語言為英文
						if (url.contains("&lan=C")) { // 載入網址是中文
							url = url.replace("&lan=C","&lan=E");
							Web_公車動態.loadUrl(url);
						}
					}
				}

				now_bus_url = url;
				if (url.contains("www.taiwanbus.tw/eBUSPage/")) {
					for (String script : jsScripts) {
						view.evaluateJavascript(script, null);
					}
					if (isDarkMode()) {
						view.evaluateJavascript(
								"document.lastElementChild.appendChild(document.createElement('style')).textContent = `html {filter: invert(0.90) !important}`;" +
										"document.lastElementChild.appendChild(document.createElement('style')).textContent = `video {filter: invert(100%)}`;" +
										"document.lastElementChild.appendChild(document.createElement('style')).textContent = `img {filter: invert(100%)}`;" +
										"document.lastElementChild.appendChild(document.createElement('style')).textContent = `div.image {filter: invert(100%)}`;" +
										"document.lastElementChild.appendChild(document.createElement('style')).textContent = `div.bus-header-section {filter: invert(100%)}`;" +
										"document.lastElementChild.appendChild(document.createElement('style')).textContent = `div.div.bus-title.mb-1.mb-md-3 {filter: invert(100%)}`;" +
										"document.lastElementChild.appendChild(document.createElement('style')).textContent = `i.fas.fa-bus::before  {filter: invert(100%)}`;" +
										"document.lastElementChild.appendChild(document.createElement('style')).textContent = `i.fas.fa-wheelchair::before  {filter: invert(100%)}`;" +
										"document.querySelector(\"#main > div.bus-header.container-md > div:nth-child(1) > div.bus-title.mb-1.mb-md-3 > h2\").style.color = 'white';" +
										"document.querySelector(\"#main > div.bus-header.container-md > div:nth-child(1) > div.bus-title.mb-1.mb-md-3 > div.bus-title__text\").style.color = 'white';"
								, new ValueCallback<String>() {
									@Override
									public void onReceiveValue(String value) {
										// 確認是執行完 js 才隱藏請稍等
										Web_公車動態.setVisibility(View.VISIBLE);
										hideProgressOverlay();
									}
								});
					} else {
						Web_公車動態.setVisibility(View.VISIBLE);
						hideProgressOverlay();
					}
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				String url = request.getUrl().toString();
				if (!url.startsWith("http") && !url.startsWith("https")) {
					return true;
				}
				return super.shouldOverrideUrlLoading(view, request);
			}
		});

		// 網站Load
		LoadWeb();

		// 事件
		
	}

	// 設標題
	private void setActionBarTitle(String title) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(title);
		}
	}
	
	private void initializeLogic() {

	}


	private void LoadWeb() {
		Web_公車動態.loadUrl("https://www.taiwanbus.tw/eBUSPage/Query/RouteQuery.aspx?key=%E5%AE%9C%E8%98%AD%E5%A4%A7%E5%AD%B8&lan=C");
		/**
		if (Locale.getDefault().toLanguageTag().contains("zh")) {
			Web_公車動態.loadUrl("https://www.taiwanbus.tw/eBUSPage/Query/RouteQuery.aspx?key=%E5%AE%9C%E8%98%AD%E5%A4%A7%E5%AD%B8&lan=C");
		} else  {
			// 英文網站
			Web_公車動態.loadUrl("https://www.taiwanbus.tw/eBUSPage/Query/RouteQuery.aspx?key=%E5%AE%9C%E8%98%AD%E5%A4%A7%E5%AD%B8&lan=E");
		}*/
	}

	private boolean isDarkMode() {
		int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
		return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
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
