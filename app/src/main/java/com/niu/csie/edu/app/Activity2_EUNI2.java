package com.niu.csie.edu.app;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.*;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;



public class Activity2_EUNI2 extends AppCompatActivity {

	/**Element*/
	private Toolbar _toolbar;
	private WebView Web_M園區;
	private TextView None;
	private View ProgressOverlay;

	/**Components*/
	private Handler handler;
	private Runnable refreshRunnable;
	private Intent page = new Intent();

	/**Variable*/
	private String now_euni_url, url_bottom = "";
	private static final int FILE_CHOOSER_REQUEST_CODE = 1;
	private ValueCallback<Uri[]> filePathCallback;
	private static final int REFRESH_INTERVAL = 1800000; // 30 分鐘，防止 Euni 閒置自動登出
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.activity2_euni2);
		initialize(_savedInstanceState);
		initializeLogic();
	}


	@Override
	public void onBackPressed() {
		if (!now_euni_url.equals(url_bottom)) {
			Web_M園區.loadUrl(url_bottom);
		} else {
			super.onBackPressed();
			page.setClass(getApplicationContext(), Activity2_EUNI.class);
			startActivity(page);
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		}
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



	private void initialize(Bundle _savedInstanceState) {
		_toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		setActionBarTitle(getResources().getString(R.string.EUNI));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				page.setClass(getApplicationContext(), Activity2_EUNI.class);
				startActivity(page);
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			}
		});

		// 主畫面UI
		Web_M園區 = findViewById(R.id.web_EUNI);
		None = findViewById(R.id.text);
		ProgressOverlay = findViewById(R.id.progress_overlay);

		// 初始化設置
		Web_M園區.getSettings().setJavaScriptEnabled(true);
		// 挑選作業上傳
		Web_M園區.getSettings().setAllowFileAccess(true);
		Web_M園區.getSettings().setAllowFileAccessFromFileURLs(true);
		Web_M園區.getSettings().setAllowUniversalAccessFromFileURLs(true);
		// 禁用縮放控制，但啟用內部縮放功能
		Web_M園區.getSettings().setSupportZoom(true);
		Web_M園區.getSettings().setBuiltInZoomControls(true);
		Web_M園區.getSettings().setDisplayZoomControls(false);
		// 讓 WebView 的寬度適應屏幕寬度
		Web_M園區.getSettings().setUseWideViewPort(true);
		Web_M園區.getSettings().setLoadWithOverviewMode(true);
		//Web_M園區.setInitialScale(1); // 會出錯，因為M園區已對手機版做版型調整

		// M園區 網站Load
		Intent Info = getIntent();
		String Euni_url = Info.getStringExtra("Url_Final");
		url_bottom = Euni_url;
		Web_M園區.loadUrl(Euni_url);

		Web_M園區.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				// 頁面加載完成，隱藏無關元素
				hideElements();
				now_euni_url = url; // 用於 onBackPressed 之判斷
			}
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				view.loadUrl(request.getUrl().toString());
				return true; // 表示我們已經處理這個 URL，不需要再外部處理，eg: 跳轉外部瀏覽器
			}
		});

		Web_M園區.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				if (newProgress < 100) {
					showProgressOverlay();
					Web_M園區.setVisibility(View.GONE);
					None.setVisibility(View.GONE);
				}
			}
			@Override
			public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
				callback.invoke(origin, true, false);
			}
			@Override
			public boolean onConsoleMessage(ConsoleMessage consoleMessage) { // 可以從 logcat 中看到 webview 的 console.log
				Log.d("MyApplication", consoleMessage.message() + " -- From line " +
						consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
				return true;
			}
			// 挑選檔案
			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
				if (Activity2_EUNI2.this.filePathCallback != null) {
					Activity2_EUNI2.this.filePathCallback.onReceiveValue(null);
				}
				Activity2_EUNI2.this.filePathCallback = filePathCallback;
				Intent FilePick = fileChooserParams.createIntent();
				try {
					startActivityForResult(FilePick, FILE_CHOOSER_REQUEST_CODE);
				} catch (ActivityNotFoundException e) {
					Activity2_EUNI2.this.filePathCallback = null;
					showMessage(getResources().getString(R.string.OpenSAFFailed));
					return false;
				}
				return true;
			}
		});

		// 設置下載監聽器
		Web_M園區.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
				// 獲取 Cookie
				CookieManager cookieManager = CookieManager.getInstance();
				String cookies = cookieManager.getCookie(url);
				startDownload(url, userAgent, contentDisposition, mimeType, cookies);
			}
		});

	}

	private void startDownload(String url, String userAgent, String contentDisposition, String mimeType, String cookies) {
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		request.addRequestHeader("Cookie", cookies);
		request.setMimeType(mimeType);
		request.addRequestHeader("User-Agent", userAgent);
		//request.setDescription("Downloading file...");
		String FileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
		if (FileName.contains(".bin")) {
			int lastIndex0 = contentDisposition.lastIndexOf('.');
			int lastIndex1 = contentDisposition.lastIndexOf('\"');
			String Extention = contentDisposition.substring(lastIndex0, lastIndex1);
			FileName = FileName.split(".bin")[0]+Extention;
		}
		request.setTitle(FileName);
		request.allowScanningByMediaScanner();
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, FileName);

		DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		dm.enqueue(request);
	}


	private void hideElements() {

		String jsCode =
				"document.querySelector('.fixed-top.navbar.navbar-light.bg-white.navbar-expand.moodle-has-zindex').style.display = 'none';" +
				"document.getElementById('page-header').style.display = 'none';" +
				"var score_hide = document.querySelector('.nav.nav-tabs.mb-3');" + // 成績
				"if (score_hide) { " +
				"	score_hide.style.display = 'none';" +
				"}" +
				"var jump_to = document.querySelector('.mt-5.mb-1.activity-navigation.container-fluid');" + // 資源 跳至...
				"if (jump_to) { " +
				"	jump_to.style.display = 'none';" +
				"}" +
				"document.getElementById('page-footer').style.display = 'none';" +
				"var elements = document.getElementsByClassName('card-body p-3');" +
				"for (var i = 0; i < elements.length; i++) {" +
				"    elements[i].style.display = 'none';" +
				"}";

		Web_M園區.evaluateJavascript(jsCode, null);


		if (isDarkMode()) {
			// 暗黑模式更改 UI
			BlackModeEUNI();
		} else {
			// 如果不是暗黑模式，直接在主線程中設置可見性
			ShowWeb();
		}
	}

	private void BlackModeEUNI() {

		String js =
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'html {filter: invert(0.90) !important}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'video {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'img {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.image {filter: invert(100%);}';";// +
				//"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.s-i-t-b-wrapper {filter: invert(100%);}';";
		Web_M園區.evaluateJavascript(js, new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				// 確認是執行完 js 才隱藏請稍等
				ShowWeb();
			}
		});
	}

	@SuppressLint("SetTextI18n")
	private void ShowWeb() {
		// 判斷是否無資源，是的話以 textview 代替
		Web_M園區.evaluateJavascript(
				"(function() { " +
						"   var bodyText = document.body.innerText;" +
						"   return bodyText.includes('此課程沒有') || bodyText.includes('目前還沒有');" +
						"})();",
				value -> {
					if ("true".equals(value)) {
						Web_M園區.setVisibility(View.GONE);
						None.setVisibility(View.VISIBLE);
					} else {
						Web_M園區.setVisibility(View.VISIBLE);
						None.setVisibility(View.GONE);
					}
				});

		hideProgressOverlay();
	}

	// 設標題
	private void setActionBarTitle(String title) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(title);
		}
	}
	
	private void initializeLogic() {
		// 設置標題
		Intent Info = getIntent();
		setActionBarTitle(Info.getStringExtra("Title"));
		// 防止 Euni 閒置登出
		handler = new Handler();
		refreshRunnable = new Runnable() {
			@Override
			public void run() {
				//重新整理
				Web_M園區.reload();
				handler.postDelayed(this, REFRESH_INTERVAL);
			}
		};
		handler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(refreshRunnable);
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

	private void showMessage(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

}
