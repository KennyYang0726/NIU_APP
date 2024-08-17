package com.niu.csie.edu.app;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import android.content.*;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.niu.csie.edu.app.Utils.FileUtil;



public class Activity4_My_Class_Schedule extends AppCompatActivity {

	/**Element*/
	private Toolbar _toolbar;
	private WebView Web_Class_Schedule;
	private View ProgressOverlay;

	/**Components*/
	private Intent page = new Intent();
	private final Handler handler = new Handler(Looper.getMainLooper());

	/**Variable*/
	private boolean State = false; // 由於要先載入，再post，載入後post前->改為ture，避免post後重複進入onPageFinished

	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.activity4_my_class_schedule);
		initialize(_savedInstanceState);
		initializeLogic();
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
		setActionBarTitle(getResources().getString(R.string.Class_Schedule));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			}
		});
		
		// 主畫面UI
		Web_Class_Schedule = findViewById(R.id.web_class_schedule);
		ProgressOverlay = findViewById(R.id.progress_overlay);

		// 初始化設置
		Web_Class_Schedule.getSettings().setJavaScriptEnabled(true);
		Web_Class_Schedule.getSettings().setDomStorageEnabled(true);
		Web_Class_Schedule.getSettings().setLoadWithOverviewMode(true);
		Web_Class_Schedule.getSettings().setUseWideViewPort(true);
		// 禁用縮放控制，但啟用內部縮放功能
		Web_Class_Schedule.getSettings().setSupportZoom(true);
		Web_Class_Schedule.getSettings().setBuiltInZoomControls(true);
		Web_Class_Schedule.getSettings().setDisplayZoomControls(false);
		// 讓 WebView 的寬度適應屏幕寬度
		Web_Class_Schedule.setInitialScale(1);

		Web_Class_Schedule.setWebViewClient(new WebViewClient() {
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
				if (url.equals("https://acade.niu.edu.tw/NIU/Application/TKE/TKE22/TKE2240_01.aspx")) {
					if (!State) {
						PostWeb();
					} else {
						Web_Class_Schedule.evaluateJavascript(
								"(function() { return document.documentElement.outerHTML; })();",
								value -> {
									// 這裡的value是頁面的HTML內容
									// 處理你的HTML內容
									// value 是一個包含HTML字符串的JSON字符串，進一步處理
									value = value.substring(1, value.length()-1); // 去除頭尾雙引號
									String htmlContent = value.replace("\\u003C", "<")
											.replace("\\\"", "\"")
											.replace("\\n", "\n")
											.replace("\\t", "\t")
											.replace("\\/", "/")
											.replace("\\\\", "\\")
											.replace("&amp;nbsp;", " ")
											.replace("&lt;", "<")
											.replace("&gt;", ">");
											//.substring(1, value.length() - 1);
									// 使用 Jsoup 解析 HTML 内容，僅擷取課表部分
									Document document = Jsoup.parse(htmlContent);
									Element table2Element = document.getElementById("table2");
									if (table2Element != null) {
										String table2Html = table2Element.outerHtml();
										table2Html = "<html><head><style>" +
												"table { border-collapse: collapse; width: 100%; }" +
												"td, th { border: 1px solid black; padding: 8px; text-align: center; }" +
												"</style></head><body>" +
												table2Html +
												"</body></html>";

										LoadWeb(table2Html);

										FileUtil.writeFile(FileUtil.getPackageDataDir(getApplicationContext()) + "/WeeklyClassSchedule.html", table2Html);
									} else {
										System.out.println("Element with id 'table2' not found.");
									}
								});

					}

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

		// 網站 Load，先載入畫面，再使用 Post 方法取得課表
		Map<String, String> headers = new HashMap<>();
		headers.put("Referer", "https://acade.niu.edu.tw/NIU/Application/TKE/TKE22/TKE2240_01.aspx");
		Web_Class_Schedule.loadUrl("https://acade.niu.edu.tw/NIU/Application/TKE/TKE22/TKE2240_01.aspx", headers);

		// 事件
		
	}

	// 設標題
	private void setActionBarTitle(String title) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(title);
		}
	}
	
	private void initializeLogic() {
		Web_Class_Schedule.setVisibility(View.GONE);
		showProgressOverlay();
	}

	private void PostWeb() {
		// 頁面加載完成
		Web_Class_Schedule.evaluateJavascript("document.querySelector('#__CRYSTALSTATECrystalReportViewer').value", Crystal -> {
			Web_Class_Schedule.evaluateJavascript("document.querySelector('#SAYEAR').value", SaYear -> {
				Web_Class_Schedule.evaluateJavascript("document.querySelector('#printType').value", PrintType -> {
					Web_Class_Schedule.evaluateJavascript("document.querySelector('#M_ASYS_CODE').value", M_ASYS_CODE -> {
						Web_Class_Schedule.evaluateJavascript("document.querySelector('#M_DEGREE_CODE').value", M_DEGREE_CODE -> {
							Web_Class_Schedule.evaluateJavascript("document.querySelector('#M_COLLEGE_CODE').value", M_COLLEGE_CODE -> {
								Web_Class_Schedule.evaluateJavascript("document.querySelector('#M_TEACH_GROUP_CODE').value", M_TEACH_GROUP_CODE -> {
									Web_Class_Schedule.evaluateJavascript("document.querySelector('#M_TEACH_GRP').value", M_TEACH_GRP -> {
										Web_Class_Schedule.evaluateJavascript("document.querySelector('#M_TEACH_GRP_NAME').value", M_TEACH_GRP_NAME -> {
											Web_Class_Schedule.evaluateJavascript("document.querySelector('#M_GRADE').value", M_GRADE -> {
												Web_Class_Schedule.evaluateJavascript("document.querySelector('#M_CLASSNO').value", M_CLASSNO -> {
													Web_Class_Schedule.evaluateJavascript("document.querySelector('#M_STNO').value", M_STNO -> {
														Web_Class_Schedule.evaluateJavascript("document.getElementById('Q_SMS').querySelector('option[selected]').value", Q_SMS -> {
															Web_Class_Schedule.evaluateJavascript("document.querySelector('#__VIEWSTATE').value", viewState -> {
																Web_Class_Schedule.evaluateJavascript("document.querySelector('#__VIEWSTATEGENERATOR').value", viewStateGenerator -> {
																	Web_Class_Schedule.evaluateJavascript("document.querySelector('#__EVENTVALIDATION').value", eventValidation -> {
																		handlePostInfo(Crystal, SaYear, PrintType, M_ASYS_CODE, M_DEGREE_CODE, M_COLLEGE_CODE, M_TEACH_GROUP_CODE, M_TEACH_GRP, M_TEACH_GRP_NAME, M_GRADE, M_CLASSNO, M_STNO, Q_SMS, viewState, viewStateGenerator, eventValidation, handler);
																	});
																});
															});
														});
													});
												});
											});
										});
									});
								});
							});
						});
					});
				});
			});
		});
	}

	private void handlePostInfo(String Crystal, String SaYear, String PrintType, String M_ASYS_CODE, String M_DEGREE_CODE, String M_COLLEGE_CODE, String M_TEACH_GROUP_CODE, String M_TEACH_GRP, String M_TEACH_GRP_NAME, String M_GRADE, String M_CLASSNO, String M_STNO, String Q_SMS, String viewState, String viewStateGenerator, String eventValidation, Handler handler) {
		//FileUtil.writeFile(FileUtil.getPackageDataDir(getApplicationContext()) + "/PostInfo.txt", Crystal+"\n\n\n"+SaYear+"\n\n\n"+PrintType+"\n\n\n"+M_ASYS_CODE+"\n\n\n"+M_DEGREE_CODE+"\n\n\n"+M_COLLEGE_CODE+"\n\n\n"+M_TEACH_GROUP_CODE+"\n\n\n"+M_TEACH_GRP+"\n\n\n"+M_TEACH_GRP_NAME+"\n\n\n"+M_GRADE+"\n\n\n"+M_CLASSNO+"\n\n\n"+M_STNO+"\n\n\n"+Q_SMS+"\n\n\n"+viewState+"\n\n\n"+viewStateGenerator+"\n\n\n"+eventValidation+"\n\n\n");

		String formData;
		try {
			formData = "ScriptManager1=AjaxPanel%7CQUERY_BTN3" +
					"&__CRYSTALSTATECrystalReportViewer=" + URLEncoder.encode(Crystal, "UTF-8") +
					"&ActivePageControl=" +
					"&ColumnFilter=" +
					"&SAYEAR=" + URLEncoder.encode(SaYear, "UTF-8") +
					"&printType=" + URLEncoder.encode(PrintType, "UTF-8") +
					"&TYPE=" +
					"&M_ASYS_CODE=" + URLEncoder.encode(M_ASYS_CODE, "UTF-8") +
					"&M_DEGREE_CODE=" + URLEncoder.encode(M_DEGREE_CODE, "UTF-8") +
					"&M_COLLEGE_CODE=" + URLEncoder.encode(M_COLLEGE_CODE, "UTF-8") +
					"&M_TEACH_GROUP_CODE=" + URLEncoder.encode(M_TEACH_GROUP_CODE, "UTF-8") +
					"&M_TEACH_GRP=" + URLEncoder.encode(M_TEACH_GRP, "UTF-8") +
					"&M_TEACH_GRP_NAME=" + URLEncoder.encode(M_TEACH_GRP_NAME, "UTF-8") +
					"&M_GRADE=" + URLEncoder.encode(M_GRADE, "UTF-8") +
					"&M_CLASSNO=" + URLEncoder.encode(M_CLASSNO, "UTF-8") +
					"&M_STNO=" + URLEncoder.encode(M_STNO, "UTF-8") +
					"&Q_AYEAR=" + URLEncoder.encode(SaYear, "UTF-8") +
					"&Q_SMS=" + URLEncoder.encode(Q_SMS, "UTF-8") +
					"&__EVENTTARGET=" +
					"&__EVENTARGUMENT=" +
					"&__VIEWSTATE=" + URLEncoder.encode(viewState, "UTF-8") +
					"&__VIEWSTATEGENERATOR=" + URLEncoder.encode(viewStateGenerator, "UTF-8") +
					"&__EVENTVALIDATION=" + URLEncoder.encode(eventValidation, "UTF-8") +
					"&__VIEWSTATEENCRYPTED=" +
					"&__ASYNCPOST=true" +
					"&QUERY_BTN3=%E9%81%B8%E8%AA%B2%E8%AA%B2%E8%A1%A8";
			formData = formData.replace("%22", ""); // 編碼處理產生了%22("導致，可事前replace，或這裡再replace(若沒有可能導致Post異常的內文雙引號))

			byte[] postData = formData.getBytes(StandardCharsets.UTF_8);
			handler.post(() -> {
				State = true;
				Web_Class_Schedule.postUrl("https://acade.niu.edu.tw/NIU/Application/TKE/TKE22/TKE2240_01.aspx", postData);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void LoadWeb(String content) {
		Web_Class_Schedule.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);
		Web_Class_Schedule.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				if (isDarkMode()) {
					// 暗黑模式更改 UI
					BlackMode();
				} else {
					ShowWeb();
				}
			}
		});
	}

	private boolean isDarkMode() {
		int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
		return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
	}

	private void BlackMode() {

		String js =
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = '" +
				"html, body { background-color: #121212; color: #e0e0e0; }" + // 黑灰色背景，亮灰色文字
				"table { border-collapse: collapse; width: 100%; border: 1px solid #ffffff; }" + // 白色框線
				"td, th { border: 1px solid #ffffff; padding: 8px; text-align: center; color: #e0e0e0; }" + // 白色框線，亮灰色文字
				"a { color: #DFA909; }" + // 連結文字顏色變成暗黃色
				"a:hover { color: #FFC107; }'"; // 連結懸停狀態顏色變為暗黃色
		Web_Class_Schedule.evaluateJavascript(js, new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				// 確認是執行完 js 才顯示 web
				ShowWeb();
			}
		});
	}

	private void ShowWeb() {
		Web_Class_Schedule.setVisibility(View.VISIBLE);
		hideProgressOverlay();
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
