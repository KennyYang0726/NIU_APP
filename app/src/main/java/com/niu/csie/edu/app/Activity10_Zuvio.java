package com.niu.csie.edu.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.provider.ProviderProperties;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.niu.csie.edu.app.Utils.FileUtil;

import java.util.ArrayList;


public class Activity10_Zuvio extends AppCompatActivity {

	/**Element*/
	private Toolbar _toolbar;
	private WebView Web_Zuvio;
	private View ProgressOverlay;

	/**Components*/
	private Intent page = new Intent();

	/**Variable*/
	private String now_zuvio_url = "";
	private double Latitude = 24.7454820; //緯度 //格致大樓預設
	private double Longitude = 121.7450088; //經度 //格致大樓預設

	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.activity10_zuvio);
		initialize(_savedInstanceState);
		initializeLogic();
	}


	@Override
	public void onBackPressed() {
		if (!now_zuvio_url.equals("https://irs.zuvio.com.tw/student5/irs/index")) {
			// 如果非 zuvio 主頁，切回主頁
			Web_Zuvio.loadUrl("https://irs.zuvio.com.tw/student5/irs/index");
		} else {
			super.onBackPressed();
			// 否則跳出
			page.setClass(getApplicationContext(), Activity1_HomeActivity.class);
			startActivity(page);
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		_toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		setActionBarTitle(getResources().getString(R.string.Zuvio));
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
		Web_Zuvio = findViewById(R.id.web_Zuvio);
		ProgressOverlay = findViewById(R.id.progress_overlay);

		// 初始化設置
		Web_Zuvio.getSettings().setJavaScriptEnabled(true);
		Web_Zuvio.setWebContentsDebuggingEnabled(true); //可編輯 html 成暗黑模式
		Web_Zuvio.getSettings().setGeolocationEnabled(true); //可讀取 GPS

		// 載入 URL
		String Zuvio_url = "https://irs.zuvio.com.tw/student5/irs/index";
		Web_Zuvio.loadUrl(Zuvio_url);

		Web_Zuvio.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				view.loadUrl(request.getUrl().toString());
				return true; // 表示我們已經處理這個 URL，不需要再外部處理，eg: 跳轉外部瀏覽器
			}
		});
		Web_Zuvio.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				// 頁面加載完成，隱藏無關元素
				hideElements();
				now_zuvio_url = url; // 用於 onBackPressed 之判斷 以及 GPS
				// GPS
				if (now_zuvio_url.contains("rollcall")) {
					// 注入自訂的JavaScript程式碼，覆蓋irs_getLocation函數，僅限關閉定位時生效
					String JSCode = "javascript:(function() { " +
							"function enableSubmitButton(){" +
							"    $(\"#submit-make-rollcall\").removeClass('i-r-f-b-disabled-button');" +
							"    $(\"#submit-make-rollcall\").addClass('i-r-f-b-make-rollcall-button');" +
							"    $(\"#submit-make-rollcall\").attr('onclick','makeRollcall(rollcall_id)');" +
							"    $('.open-gps-guidance-link').addClass('hidden');" +
							"    $('.open-gps-guidance-link-text-close').addClass('hidden');" +
							"    $('.open-gps-guidance-link-text-open').removeClass('hidden');" +
							"}" +
							"window.irs_getLocation = function(callback) { " +
							"    user_gps = true; " +
							"    user_latitude = " + Latitude + ";" + // 指定的緯度
							"    user_longitude = " + Longitude + ";" + // 指定的經度
							"    callback(); " +
							"};" +
							"window.makeRollcall = function(rollcall_id) {" +
							"    $(\"button#submit-make-rollcall\").disableBtn();" +
							"    google_ga_event('irs', 'IRS-學生簽到');" +
							//"        console.log(user_id);" +
							//"        console.log(accessToken);" +
							//"        console.log(rollcall_id);" +
							//"        console.log(user_latitude);" +
							//"        console.log(user_longitude);" +
							"    $.ajax({" +
							"        url: site_url + 'app_v2/makeRollcall'," +
							"        type: 'POST'," +
							"        data: {" +
							"            user_id: user_id," +
							"            accessToken: accessToken," +
							"            rollcall_id: rollcall_id," +
							"            device: 'WEB'," +
							"            lat: user_latitude," +
							"            lng: user_longitude" +
							"        }," +
							"        dataType: 'json'" +
							"    }).success(function (data) {" +
							"        if(data.status){" +
							"            rollcallFinishFcbx(data.ad.answer);" +
							"        } else {" +
							"            switch(data.msg) {" +
							"                case 'ROLLCALL IS ANSWERED':" +
							"                    student5Fancybox('#rollcall-refinish-fcbx-btn', 305);" +
							"                    break;" +
							"                case 'LOSE THE GPS LOCATION':" +
							"                    student5Fancybox('#rollcall-fail-fcbx-btn', 305);" +
							"                    break;" +
							"                case 'ROLLCALL IS NOT ONAIR':" +
							"                    student5Fancybox('#rollcall-unopen-fcbx-btn', 305);" +
							"                    break;" +
							"                default:" +
							"                    student5Fancybox('#rollcall-fail-fcbx-btn', 305);" +
							"                    break;" +
							"            }" +
							"        }" +
							"    });" +
							"};" +
							"irs_getLocation(enableSubmitButton);" +  // 重新呼叫修改後的函數
							"})()";
					Web_Zuvio.evaluateJavascript(JSCode, null);

					// Custom AlertDialog
					// 若是沒給權限 或 未開啟GPS -> 可以模擬 GPS
					String Permission_Location;
					String GPS_State;
					// 改變狀態文字
					if ((ContextCompat.checkSelfPermission(Activity10_Zuvio.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED))
						Permission_Location = getResources().getString(R.string.Permission_Location) + "❌";
					else
						Permission_Location = getResources().getString(R.string.Permission_Location) + "✅";
					if (!isGpsEnabled())
						GPS_State = getResources().getString(R.string.GPS_State) + "❌";
					else
						GPS_State = getResources().getString(R.string.GPS_State) + "✅";
					// show Dialog
					if ((ContextCompat.checkSelfPermission(Activity10_Zuvio.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) || !isGpsEnabled()) {
						if (FileUtil.isExistFile("/data/user/0/com.niu.csie.edu.app/jump zuvio dialog"))
							showAlertDialog(getResources().getString(R.string.State_NOW), Permission_Location + GPS_State + getResources().getString(R.string.Fake_GPS_OK));
					} else {
						if (FileUtil.isExistFile("/data/user/0/com.niu.csie.edu.app/jump zuvio dialog"))
							showAlertDialog(getResources().getString(R.string.State_NOW), Permission_Location + GPS_State + getResources().getString(R.string.Fake_GPS_NoWay));
					}
				}
				// 為了讓點名的 AlertDialog 不會因為 reload 再跳一次，這裡做一個寫入檔案動作，以此判定是否需跳
				// (不知為何，於點名畫面，展開地圖選完位置，仍會跳 dialog)
				if (now_zuvio_url.contains("rollcall")) {
					FileUtil.deleteFile("/data/user/0/com.niu.csie.edu.app/jump zuvio dialog");
				} else {
					FileUtil.writeFile("/data/user/0/com.niu.csie.edu.app/jump zuvio dialog", "");
				}

			}
		});
		Web_Zuvio.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				if (newProgress < 100) {
					showProgressOverlay();
					Web_Zuvio.setVisibility(View.GONE);
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
		});

	}

	private void hideElements() {

        String jsCode =
                "var Elements_NotImportant = document.querySelectorAll('.g-f-button-box[data-type=\"forum\"]');" +  //底下 footer 不相干元素1 隱藏
                "Elements_NotImportant.forEach(function(element) {" +
                "    element.style.display = 'none';" +
                "});" + 
                "var Elements_NotImportant = document.querySelectorAll('.g-f-button-box[data-type=\"zook\"]');" +  //底下 footer 不相干元素2 隱藏
                "Elements_NotImportant.forEach(function(element) {" +
                "    element.style.display = 'none';" +
                "});" +
                "var Elements_NotImportant = document.querySelectorAll('.g-f-button-box[data-type=\"direno\"]');" +  //底下 footer 不相干元素4 隱藏
                "Elements_NotImportant.forEach(function(element) {" +
                "    element.style.display = 'none';" +
                "});" +
                "var Elements_NotImportant = document.querySelectorAll('.g-f-button-box[data-type=\"match\"]');" +  //底下 footer 不相干元素5 隱藏
                "Elements_NotImportant.forEach(function(element) {" +
                "    element.style.display = 'none';" +
                "});" +
                "var Elements_NotImportant = document.querySelectorAll('.g-f-button-box[data-type=\"setting\"]');" +  //底下 footer 不相干元素6 隱藏
                "Elements_NotImportant.forEach(function(element) {" +    //不讓你登出啊~~  ㄌㄩㄝ~~   .by:茅場晶彥
                "    element.style.display = 'none';" +
                "});" +
                "document.querySelector('.i-m-p-wisdomhall-area').style.display='none';" + //自修專區廣告 隱藏
				"var Elements_399868 = document.querySelectorAll('.i-m-p-c-a-c-l-course-box[data-course-id=\"399868\"]');" + //大學生快問快答 隱藏
				"Elements_399868.forEach(function(element) {" +
				"    element.style.display = 'none';" +
				"});";

		Web_Zuvio.evaluateJavascript(jsCode, null);
        if (isDarkMode()) {
            // 暗黑模式更改 UI
            BlackModeZuvio();
        } else {
            // 如果不是暗黑模式，直接在主線程中設置可見性
            Web_Zuvio.setVisibility(View.VISIBLE);
			hideProgressOverlay();
        }
	}

	private void BlackModeZuvio() {
		String js =
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'html {filter: invert(0.90) !important}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'video {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'img {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.image {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.s-i-t-b-wrapper {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.i-c-l-reload-button {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.g-f-button-box {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.i-a-c-q-t-q-b-top-box {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.button {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.i-r-reload-button {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.i-f-f-f-a-post-feedback-button {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.i-m-p-c-a-c-l-c-b-green-block {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.i-m-p-c-a-c-l-c-b-t-star {filter: invert(100%) opacity(80%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.s-i-top-box {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.s-i-t-b-i-b-icon {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.p-m-c-icon-box {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.user-icon-switch {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.c-pm-c-chat-wrapper.message-box {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.c-pm-c-send-message {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.c-pm-c-receive-message {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.c-pm-c-r-text {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.c-pm-c-s-text {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.c-pm-c-r-icon {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.c-pm-c-r-redirect {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.c-pm-c-chat-topic-card-list {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.i-h-r-rollcall-row.i-h-r-r-r-nonarrival {filter: invert(100%);}';" +
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = 'div.i-h-r-rollcall-row.i-h-r-r-r-punctual {filter: invert(100%);}';";
		Web_Zuvio.evaluateJavascript(js, new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				// 確認是執行完 js 才隱藏請稍等
				Web_Zuvio.setVisibility(View.VISIBLE);
				hideProgressOverlay();
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
		// 處理 ChooseLocation 傳回的 List
		Intent intent = getIntent();
		if (intent.hasExtra("Location_Info")) {
			// 選完地圖回來要來重新加載 url
			// 照理說去地圖前已經存好 url 於 /data/user/0/pkgname/ 了
			if (FileUtil.isExistFile("/data/user/0/com.niu.csie.edu.app/Zuvio_url"))
				now_zuvio_url = FileUtil.readFile("/data/user/0/com.niu.csie.edu.app/Zuvio_url");
			else
				now_zuvio_url = "https://irs.zuvio.com.tw/student5/irs/index"; // 否則讓他回主頁
						// 取得額外數據
			ArrayList<Double> Location_Info = (ArrayList<Double>) intent.getSerializableExtra("Location_Info");
			setMockLocation(Location_Info.get(0), Location_Info.get(1));
		}
	}

	private boolean isGpsEnabled() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	private void showAlertDialog(String Title, String content) {
		AlertDialog.Builder builder = new AlertDialog.Builder(Activity10_Zuvio.this, R.style.AlertDialogTheme);
		View view = LayoutInflater.from(Activity10_Zuvio.this).inflate(
				R.layout.custom_alert_dialog,
				findViewById(R.id.layoutDialogContainer)
		);
		builder.setView(view);
		((TextView) view.findViewById(R.id.textTitle)).setText(Title);
		((TextView) view.findViewById(R.id.textMessage)).setText(content);
		((Button) view.findViewById(R.id.buttonAction)).setText(getResources().getString(R.string.Dialog_OK));
		((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_location_white);

		builder.setCancelable(false);
		AlertDialog alertDialog = builder.create();

		view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				alertDialog.dismiss();
			}
		});

		if (alertDialog.getWindow() != null){
			alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		}
		alertDialog.show();
	}

	public void SetLocation(View view) {
		if ((ContextCompat.checkSelfPermission(Activity10_Zuvio.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) || !isGpsEnabled()) {
			FileUtil.writeFile("/data/user/0/com.niu.csie.edu.app/Zuvio_url", now_zuvio_url);
			page.setClass(getApplicationContext(), Activity10_ChooseLocation.class);
			startActivity(page);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		} else {
			showMessage(getResources().getString(R.string.Close_GPS_Tips));
		}
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


	// 設定自訂位置
	private void setMockLocation(double latitude, double longitude) {
		Latitude = latitude;
		Longitude = longitude;
		Web_Zuvio.loadUrl(now_zuvio_url);
	}


	/**程式中新增MenuItem選項*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/**itemId為稍後判斷點擊事件要用的*/
		menu.add(0,0,0,getResources().getString(R.string.ECE_Building));
		menu.add(0,1,1,getResources().getString(R.string.Engineering_Building));
		menu.add(0,2,2,getResources().getString(R.string.Bioresources_Building));
		menu.add(0,3,3,getResources().getString(R.string.Teaching_Building));
		menu.add(0,4,4,getResources().getString(R.string.Comprehensive_Building));
		menu.add(0,5,5,getResources().getString(R.string.Guishan));
		return super.onCreateOptionsMenu(menu);
	}
	/**此處為設置點擊事件*/
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		/*取得Item的ItemId，判斷點擊到哪個元件*/
		if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) || !isGpsEnabled()) {
			switch (item.getItemId()){
				case 0:
					showMessage(getResources().getString(R.string.showMessage0)+getResources().getString(R.string.ECE_Building)+getResources().getString(R.string.showMessage1));
					setMockLocation(24.7454820, 121.7450088);
					break;
				case 1:
					showMessage(getResources().getString(R.string.showMessage0)+getResources().getString(R.string.Engineering_Building)+getResources().getString(R.string.showMessage1));
					setMockLocation(24.7454690, 121.7440559);
					break;
				case 2:
					showMessage(getResources().getString(R.string.showMessage0)+getResources().getString(R.string.Bioresources_Building)+getResources().getString(R.string.showMessage1));
					setMockLocation(24.7468043, 121.7455621);
					break;
				case 3:
					showMessage(getResources().getString(R.string.showMessage0)+getResources().getString(R.string.Teaching_Building)+getResources().getString(R.string.showMessage1));
					setMockLocation(24.7461405, 121.7457680);
					break;
				case 4:
					showMessage(getResources().getString(R.string.showMessage0)+getResources().getString(R.string.Comprehensive_Building)+getResources().getString(R.string.showMessage1));
					setMockLocation(24.7462449, 121.7471765);
					break;
				case 5:
					showMessage(getResources().getString(R.string.showMessage0)+getResources().getString(R.string.Guishan)+getResources().getString(R.string.showMessage1));
					setMockLocation(24.8464381, 121.9489986);
					break;
			}
			return super.onOptionsItemSelected(item);
		} else {
			showMessage(getResources().getString(R.string.Close_GPS_Tips));
		}
		return true; // 返回 true 表示事件已處理
	}


	private void showMessage(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
}
