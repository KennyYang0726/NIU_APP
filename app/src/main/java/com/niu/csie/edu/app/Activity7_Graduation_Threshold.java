package com.niu.csie.edu.app;

import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import com.niu.csie.edu.app.CustomProgressBar.ProgressView;


public class Activity7_Graduation_Threshold extends AppCompatActivity {

	/**Element*/
	private Toolbar _toolbar;
	private WebView Web_畢業門檻;
	// 查詢畢業門檻
	private Button button_appbar;
	private ScrollView Linear_Page;
	// 多元時數, 服務, 多元, 專業, 綜合
	private TextView Diverse_Hours_Services_0, Diverse_Hours_Services_1, Diverse_Hours_Diverse_0, Diverse_Hours_Diverse_1, Diverse_Hours_Major_0, Diverse_Hours_Major_1, Diverse_Hours_Complex_0, Diverse_Hours_Complex_1;
	// 英語能力, 體適能, 應修學分總數, 學分學程
	private TextView English_Ability_Result, Physical_Fitness_Result, Total_Credits_Required_Result, Credit_Course_Result, Credit_Course_Result2;
	private ProgressView progressView_Services, progressView_Diverse, progressView_Major, progressView_Complex;
	private View ProgressOverlay;

	/**Components*/
	private Intent page = new Intent();

	/**Variable*/
	private boolean State = true; // true 是總綱，false 是詳細

	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.activity7_graduation_threshold);
		initialize(_savedInstanceState);
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
		setActionBarTitle(getResources().getString(R.string.Graduation_Threshold));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});
		
		// 主畫面UI
		Web_畢業門檻 = findViewById(R.id.web_Graduation_Threshold);
		button_appbar = findViewById(R.id.button_appbar);
		Linear_Page = findViewById(R.id.Linear_Page);
		progressView_Services = findViewById(R.id.progressBar_Services);
		progressView_Diverse = findViewById(R.id.progressBar_Diverse);
		progressView_Major = findViewById(R.id.progressBar_Major);
		progressView_Complex = findViewById(R.id.progressBar_Complex);
		Diverse_Hours_Services_0 = findViewById(R.id.Diverse_Hours_Services_0);
		Diverse_Hours_Services_1 = findViewById(R.id.Diverse_Hours_Services_1);
		Diverse_Hours_Diverse_0 = findViewById(R.id.Diverse_Hours_Diverse_0);
		Diverse_Hours_Diverse_1 = findViewById(R.id.Diverse_Hours_Diverse_1);
		Diverse_Hours_Major_0 = findViewById(R.id.Diverse_Hours_Major_0);
		Diverse_Hours_Major_1 = findViewById(R.id.Diverse_Hours_Major_1);
		Diverse_Hours_Complex_0 = findViewById(R.id.Diverse_Hours_Complex_0);
		Diverse_Hours_Complex_1 = findViewById(R.id.Diverse_Hours_Complex_1);
		English_Ability_Result = findViewById(R.id.English_Ability_Result);
		Physical_Fitness_Result = findViewById(R.id.Physical_Fitness_Result);
		Total_Credits_Required_Result = findViewById(R.id.Total_Credits_Required_Result);
		Credit_Course_Result = findViewById(R.id.Credit_Course_Result);
		Credit_Course_Result2 = findViewById(R.id.Credit_Course_Result2);
		ProgressOverlay = findViewById(R.id.progress_overlay);

		// 初始化設置
		Web_畢業門檻.getSettings().setJavaScriptEnabled(true);
		Web_畢業門檻.getSettings().setDomStorageEnabled(true);
		Web_畢業門檻.getSettings().setLoadWithOverviewMode(true);
		Web_畢業門檻.getSettings().setUseWideViewPort(true);
		// 禁用縮放控制，但啟用內部縮放功能
		Web_畢業門檻.getSettings().setSupportZoom(true);
		Web_畢業門檻.getSettings().setBuiltInZoomControls(true);
		Web_畢業門檻.getSettings().setDisplayZoomControls(false);
		// 讓 WebView 的寬度適應屏幕寬度
		Web_畢業門檻.setInitialScale(1);

		Web_畢業門檻.setWebViewClient(new WebViewClient() {
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
				if (url.equals("https://acade.niu.edu.tw/NIU/Application/ENR/ENRG0/ENRG010_01.aspx")) {
					GetValueOfAspx();
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
		Web_畢業門檻.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				//progress = newProgress / 100.0;
				Web_畢業門檻.setVisibility(View.GONE);
				Linear_Page.setVisibility(View.GONE);
				// 右上角按鈕鎖定
				button_appbar.setClickable(false);
				showProgressOverlay();
			}
		});


		// 網站Load
		Map<String, String> headers = new HashMap<>();
		headers.put("Referer", "https://acade.niu.edu.tw/NIU/Application/ENR/ENRG0/ENRG010_03.aspx");
		Web_畢業門檻.loadUrl("https://acade.niu.edu.tw/NIU/Application/ENR/ENRG0/ENRG010_01.aspx", headers);

		// 事件
		button_appbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				State = !State;
				if (!State) {
					// 詳細
					button_appbar.setText(getResources().getString(R.string.Check_Graduation_Threshold_0));
					Web_畢業門檻.setVisibility(View.VISIBLE);
					Linear_Page.setVisibility(View.GONE);
				} else {
					// 總綱
					button_appbar.setText(getResources().getString(R.string.Check_Graduation_Threshold));
					Web_畢業門檻.setVisibility(View.GONE);
					Linear_Page.setVisibility(View.VISIBLE);
				}
			}
		});

	}

	private void GetValueOfAspx() {
		String jsCode_HideElement =
				"document.querySelectorAll('input.btn').forEach(function(button) {" +
				"  button.style.display = 'none';" +
				"});";
		Web_畢業門檻.evaluateJavascript(jsCode_HideElement, null);

		// 多元時數
		String jsCode_Diverse_Hours =
				"var content = document.getElementById('div_B').innerText;" +
				"var regex = /\\d+/g;" +
				"var matches = content.match(regex);" +
				"matches;";
		Web_畢業門檻.evaluateJavascript(jsCode_Diverse_Hours, new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {

				// Log.d("JavaScriptResult", "Values: " + value);
				try {
					JSONArray jsonArray = new JSONArray(value);
					if (jsonArray.length() == 8) {
						int value1 = jsonArray.getInt(0);
						int value2 = jsonArray.getInt(1);
						int value3 = jsonArray.getInt(2);
						int value4 = jsonArray.getInt(3);
						int value5 = jsonArray.getInt(4);
						int value6 = jsonArray.getInt(5);
						int value7 = jsonArray.getInt(6);
						int value8 = jsonArray.getInt(7);
						// 更新 TextView 的文本
						Diverse_Hours_Services_0.setText(String.valueOf(value1));
						Diverse_Hours_Services_1.setText("/"+String.valueOf(value2));
						Diverse_Hours_Diverse_0.setText(String.valueOf(value3));
						Diverse_Hours_Diverse_1.setText("/"+String.valueOf(value4));
						Diverse_Hours_Major_0.setText(String.valueOf(value5));
						Diverse_Hours_Major_1.setText("/"+String.valueOf(value6));
						Diverse_Hours_Complex_0.setText(String.valueOf(value7));
						Diverse_Hours_Complex_1.setText("/"+String.valueOf(value8));
						// 更新 ProgressBar
						float Service_Percentage = (float) value1/value2;
						if (Service_Percentage >= 1)
							Service_Percentage = 1;
						float Diverse_Percentage = (float) value3/value4;
						if (Diverse_Percentage >= 1)
							Diverse_Percentage = 1;
						float Major_Percentage = (float) value5/value6;
						if (Major_Percentage >= 1)
							Major_Percentage = 1;
						float Complex_Percentage = (float) value7/value8;
						if (Complex_Percentage >= 1)
							Complex_Percentage = 1;
						updateProgress(Service_Percentage, Diverse_Percentage, Major_Percentage, Complex_Percentage);
					} else {
						// Log.e("JavaScriptResult", "Unexpected number of values");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// 英文門檻
		String jsCode_English_Ability =
				"var spanElement = document.querySelector('span[ml=\"PL_外語能力\"]');" +
				"var value = spanElement.closest('tr').querySelector('div').innerText;" +
				"value;";

		Web_畢業門檻.evaluateJavascript(jsCode_English_Ability, new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				value = value.replace("\"", "");
				// Log.d("JavaScriptResult", "Value: " + value);
				// 更新 TextView 的文本
				English_Ability_Result.setText(value);
				if (value.contains("未")) {
					English_Ability_Result.setTextColor(Color.RED);
				} else {
					English_Ability_Result.setTextColor(Color.GREEN);
				}
			}
		});

		// 體適能
		String jsCode_Physical_Fitness =
				"var spanElement = document.querySelector('span[ml=\"PL_體適能\"]');" +
				"var value = spanElement.closest('tr').querySelector('div').innerText;" +
				"value;";

		Web_畢業門檻.evaluateJavascript(jsCode_Physical_Fitness, new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				value = value.replace("\"", "");
				// Log.d("JavaScriptResult", "Value: " + value);
				// 更新 TextView 的文本
				Physical_Fitness_Result.setText(value);
				if (value.contains("未")) {
					Physical_Fitness_Result.setTextColor(Color.RED);
				} else {
					Physical_Fitness_Result.setTextColor(Color.GREEN);
				}
			}
		});

		// 應修學分
		String jsCode_Total_Credits_Required =
				"var rows = document.querySelectorAll('tr.tdWhite');" +
				"var result = [];" +
				"rows.forEach(function(row) {" +
				"  if (row.cells[0] && row.cells[0].innerText.trim() === '畢業最低學分數') {" +
				"    result.push(row.cells[1].innerText.trim());" +
				"    result.push(row.cells[2].innerText.trim());" +
				"  }" +
				"});" +
				"JSON.stringify(result);";

		Web_畢業門檻.evaluateJavascript(jsCode_Total_Credits_Required, new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				value = value.replace("\\\"", "\"");
				value = value.replace("\"[", "[");
				value = value.replace("]\"", "]");
				// Log.d("JavaScriptResult", "Value: " + value);
				try {
					JSONArray jsonArray = new JSONArray(value);
					if (jsonArray.length() == 2) {
						// 更新 TextView 的文本
						Total_Credits_Required_Result.setText(jsonArray.getString(1) + "/" + jsonArray.getString(0));
					} else {
						// Log.e("JavaScriptResult", "Unexpected number of values");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Linear_Page.setVisibility(View.VISIBLE);
				// 右上角按鈕可點擊
				button_appbar.setClickable(true);
				hideProgressOverlay();
			}
		});

		// 學分學程
		String jsCode_Credit_Course =
				"var element = document.getElementById('CRS_PROG');" +
				"var value = element.innerText;" +
				"value;";

		Web_畢業門檻.evaluateJavascript(jsCode_Credit_Course, new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				value = value.replace("\"", "");
				// Log.d("JavaScriptResult", "Value: " + value);
				if (value.length() < 5) {
					Credit_Course_Result.setText(getResources().getString(R.string.Credit_Course_None));
				} else {
					if (value.contains("、")) {
						value = value.replace("、", "\n");
					}
					Credit_Course_Result.setText("");
					Credit_Course_Result2.setTextSize(15);
					Credit_Course_Result2.setText(value);
				}
			}
		});
	}


	// 設標題
	private void setActionBarTitle(String title) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(title);
		}
	}


	private void updateProgress(float progress0, float progress1,float progress2,float progress3) {
		progressView_Services.setProgress(progress0);
		progressView_Diverse.setProgress(progress1);
		progressView_Major.setProgress(progress2);
		progressView_Complex.setProgress(progress3);
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
