package com.niu.csie.edu.app;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.CalendarContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.*;
import android.widget.*;
import android.Manifest;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.niu.csie.edu.app.Utils.FileUtil;



public class Activity4_My_Class_Schedule extends AppCompatActivity {

	/**Element*/
	private Toolbar _toolbar;
	private Button appbar_btn;
	private WebView Web_Class_Schedule;
	private TableLayout tableLayout;
	private View ProgressOverlay;
	// Dialog
	private EditText EditTextDateTime, EditTextTitle, EditTextDescription;

	/**Components*/
	private Intent page = new Intent();
	private AlertDialog CalenderDialog;
	private final Handler handler = new Handler(Looper.getMainLooper());

	/**Variable*/
	private boolean ViewMode = true; // false 為 網頁模式
	private static final int PERMISSIONS_REQUEST_CODE = 100;
	private boolean State = false; // 由於要先載入，再post，載入後post前->改為ture，避免post後重複進入onPageFinished
	private String[] startTimes = {
			"06:10", "07:10", "08:10", "09:10", "10:10", "11:10", "13:10", "14:10",
			"15:10", "16:10", "17:10", "18:20", "19:15", "20:10", "21:05"
	};
	private String[] endTimes = {
			"07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "14:00", "15:00",
			"16:00", "17:00", "18:00", "19:10", "20:05", "21:00", "21:55"
	};

	
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
		appbar_btn = findViewById(R.id.button_appbar);
		Web_Class_Schedule = findViewById(R.id.web_class_schedule);
		ProgressOverlay = findViewById(R.id.progress_overlay);
		tableLayout = findViewById(R.id.tableLayout);

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
		// 檢查是否已經擁有日曆權限
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
			// 如果沒有權限，請求權限
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR},
					PERMISSIONS_REQUEST_CODE);
		}



		Web_Class_Schedule.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onConsoleMessage(ConsoleMessage consoleMessage) { // 可以從 logcat 中看到 webview 的 console.log
				Log.d("WebViewwwww", consoleMessage.message() + " -- From line " +
						consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
				return true;
			}
			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				showMessage(message);
				page.setClass(getApplicationContext(), Activity1_HomeActivity.class);
				startActivity(page);
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				Web_Class_Schedule.destroy();
				finish();
				return true;
			}
		});

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
										// 儲存課表 html 於 PackageDataDir
										FileUtil.writeFile(FileUtil.getPackageDataDir(getApplicationContext())+"/ClassSchedule.html", table2Html);
										// 用 js 化為 json 課表形式
										String jsCode = "javascript:(function(htmlContent) {" +
												"    var parser = new DOMParser();" +
												"    var doc = parser.parseFromString(htmlContent, 'text/html');" +
												"    var courseIntervals = {};" +
												"    var rows = doc.querySelectorAll('#table2 tbody tr');" +
												"    var startTimes = [" +
												"        '07:10', '08:10', '09:10', '10:10', '11:10', '13:10', '14:10', '15:10', '16:10', '17:10', '18:20', '19:15', '20:10', '21:05'" +
												"    ];" +
												"    var endTimes = [" +
												"        '08:00', '09:00', '10:00', '11:00', '12:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:10', '20:05', '21:00', '21:55'" +
												"    ];" +
												"    for (let i=1; i < rows.length; i++) {" +  // 從1開始跳過標題行
												"        const cells = rows[i].getElementsByTagName('td');" +
												"        for (let j=1; j < cells.length; j++) {" + // 遍歷每一行中的每個單元格
												"            const cell = cells[j];" +
												//"            const course = cell.textContent.trim();" +
												"            const courseTidy = cell.innerHTML.replace(/<a[^>]*>/g, '').replace(/<\\/a>/g, '').trim();" + // 使用 cell.textContent.trim(); 會把 <br> 變不見，導致無法分割
												"            if (courseTidy) {" +
												"                const day = rows[0].getElementsByTagName('td')[j].textContent.trim();" +
												"                if (!courseIntervals[day]) {" +
												"                    courseIntervals[day] = [];" +
												"                }" +

												"                const lastCourse = courseIntervals[day][courseIntervals[day].length - 1];" +
												"                const TeacherName = courseTidy.split('<br>')[0];" +
												"                const CourseName = courseTidy.split('<br>')[1];" +
												"                const Location = courseTidy.split('<br>')[2];" +

												"                if (lastCourse && lastCourse.name === CourseName) {" +
												"                    lastCourse.end = i;" +
												"                } else {" +
												"                    courseIntervals[day].push({" +
												"                        name: CourseName," +
												"                        teacher: TeacherName," +
												"                        location: Location," +
												"                        start: i," +
												"                        end: i" +
												"                    });" +
												//"                    courseIntervals[day].push({ name: course, start: i, end: i });" +
												"                }" +

												"            }" +
												"        }" +
												"    }" +
												//"    for (const day in courseIntervals) {" +
												//"        console.log(`Day: ${day}`);" +
												//"        courseIntervals[day].forEach(course => {" +
												//"            console.log(`  Course: ${course.name}, Start: ${course.start}, End: ${course.end}`);" +
												//"        });" +
												//"    }" +
												"   return JSON.stringify(courseIntervals);" +
												"})(`" + table2Html + "`);"; // 使用模板字串傳遞 HTML 內容

										// 執行 JavaScript 程式碼並取得 JSON 結果
										Web_Class_Schedule.evaluateJavascript(jsCode, new ValueCallback<String>() {
											@Override
											public void onReceiveValue(String jsonValue) {
												jsonValue = jsonValue.replace("\\\"", "\"").replaceAll("^\"|\"$", "");
												// Log.d("WebViewwwww", jsonValue);
												FileUtil.writeFile(FileUtil.getPackageDataDir(getApplicationContext())+"/ClassSchedule.json", jsonValue);

												// tableLayout.setVisibility(View.VISIBLE);
												Web_Class_Schedule.setVisibility(View.VISIBLE);
												hideProgressOverlay();
/*
												try {
													JSONObject jsonObject = new JSONObject(jsonValue);
													// 動態添加課程信息
													for (int i = 0; i < jsonObject.names().length(); i++) {
														String day = jsonObject.names().getString(i);
														JSONArray classes = jsonObject.getJSONArray(day);

														for (int j = 0; j < classes.length(); j++) {
															JSONObject classInfo = classes.getJSONObject(j);
															String name = classInfo.getString("name");
															String teacher = classInfo.getString("teacher");
															String location = classInfo.getString("location");
															int start = classInfo.getInt("start");
															int end = classInfo.getInt("end");

															// 計算 FrameLayout 的位置
															int dayIndex = getDayIndex(day);

															for (int k = start; k <= end; k++) {
																TableRow tableRow = (TableRow) tableLayout.getChildAt(k);
																FrameLayout frameLayout = new FrameLayout(Activity4_My_Class_Schedule.this);
																frameLayout.setId(View.generateViewId());
																frameLayout.setBackgroundResource(R.drawable.class_schedule);
																// 設置 padding
																int padding = 16; // 您可以根據需要調整 padding 的數值
																frameLayout.setPadding(padding, padding, padding, padding);

																// 添加課程信息
																TextView classTextView = new TextView(Activity4_My_Class_Schedule.this);
																classTextView.setText(name + "\n" + teacher + "\n" + location);
																classTextView.setGravity(Gravity.CENTER);
																frameLayout.addView(classTextView);

																// 設置點擊事件
																frameLayout.setOnClickListener(new View.OnClickListener() {
																	@Override
																	public void onClick(View v) {
																		// 呼叫方法查詢最近的一天
																		Date nextDate = findNextDate(dayIndex, endTimes[start-1]);
																		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
																		ShowCalenderDialog(sdf.format(nextDate), name);
																	}
																});

																// 添加 FrameLayout 到 TableRow
																tableRow.addView(frameLayout, dayIndex);
															}
														}
													}

												} catch (Exception e) {
													e.printStackTrace();
												}*/
											}
										});


										table2Html = "<html><head><style>" +
												"table { border-collapse: collapse; width: 100%; }" +
												"td, th { border: 1px solid black; padding: 8px; text-align: center; }" +
												"</style></head><body>" +
												table2Html +
												"</body></html>";
										LoadWeb(table2Html);

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
		appbar_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewMode = !ViewMode;
				if (!ViewMode) {
					// 若是網頁模式，則顯示網頁
					Web_Class_Schedule.setVisibility(View.VISIBLE);
					appbar_btn.setText(getResources().getString(R.string.AppBar_BTN_Form));
				} else {
					Web_Class_Schedule.setVisibility(View.GONE);
					appbar_btn.setText(getResources().getString(R.string.AppBar_BTN_Web));
				}
			}
		});
	}


	private void LoadWeb(String content) {
		Web_Class_Schedule.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);
		Web_Class_Schedule.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				if (isDarkMode()) {
					// 暗黑模式更改 UI
					BlackMode();
				}
			}
		});
	}

	private boolean isDarkMode() {
		int nightMode = AppCompatDelegate.getDefaultNightMode();
		int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
		boolean isSystemDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
		boolean isAppDarkMode = nightMode == AppCompatDelegate.MODE_NIGHT_YES;
		boolean isFollowSystem = nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
		return isAppDarkMode || (isFollowSystem && isSystemDarkMode);
	}

	private void BlackMode() {
		String js =
				"document.lastElementChild.appendChild(document.createElement('style')).textContent = '" +
						"html, body { background-color: #121212; color: #e0e0e0; }" + // 黑灰色背景，亮灰色文字
						"table { border-collapse: collapse; width: 100%; border: 1px solid #ffffff; }" + // 白色框線
						"td, th { border: 1px solid #ffffff; padding: 8px; text-align: center; color: #e0e0e0; }" + // 白色框線，亮灰色文字
						"a { color: #DFA909; }" + // 連結文字顏色變成暗黃色
						"a:hover { color: #FFC107; }'"; // 連結懸停狀態顏色變為暗黃色
		Web_Class_Schedule.evaluateJavascript(js, null);
	}


	private int getDayIndex(String day) {
		switch (day) {
			case "星期一":
				return 1;
			case "星期二":
				return 2;
			case "星期三":
				return 3;
			case "星期四":
				return 4;
			case "星期五":
				return 5;
			case "星期六":
				return 6;
			case "星期日":
				return 7;
			default:
				return 0;
		}
	}

	// 顯示課程點擊 Dialog
	private void ShowCalenderDialog(String DateTime, String Title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(Activity4_My_Class_Schedule.this, R.style.AlertDialogTheme);
		View view = LayoutInflater.from(Activity4_My_Class_Schedule.this).inflate(
				R.layout.custom_alert_dialog_class_schedule_calender,
				findViewById(R.id.layoutDialogContainer)
		);
		builder.setView(view);

		EditTextDateTime = view.findViewById(R.id.editTextDateTime);
		EditTextTitle = view.findViewById(R.id.editTextTitle);
		EditTextDescription = view.findViewById(R.id.editTextDescription);

		EditTextDateTime.setText(DateTime);
		EditTextTitle.setText(Title);

		builder.setCancelable(true);
		CalenderDialog = builder.create();

		boolean[] DateFormatCorrect = new boolean[]{true};

		// 隨時監聽，若格式錯誤，紅字
		EditTextDateTime.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					String DateTimeString = EditTextDateTime.getText().toString();
					SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
					Date parsedTime = null;
					parsedTime = timeFormat.parse(DateTimeString);
					DateFormatCorrect[0] = true;
				} catch (Exception e) {
					// Log.d("ERRRR", "日期格式錯誤");
					DateFormatCorrect[0] = false;
				}
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		view.findViewById(R.id.buttonAction_Cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				CalenderDialog.dismiss();
			}
		});
		view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!DateFormatCorrect[0]) {
					showMessage(getResources().getString(R.string.Parse_DateTime_Err));
				} else {
					if (EditTextTitle.getText().toString().equals("")) {
						showMessage(getResources().getString(R.string.Empty_Title));
					} else {
						CalenderDialog.dismiss();
						// 設置google日曆事件
						try {
							// 設置事件
							String DateTimeString = EditTextDateTime.getText().toString();
							SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
							Date parsedTime = timeFormat.parse(DateTimeString);
							long startMillis = parsedTime.getTime();
							long endMillis = startMillis + 10 * 60 * 1000; // 10 分鐘後

							long calID = 1; // 這裡的 calID 需要替換為你要插入事件的日曆 ID

							ContentResolver cr = getContentResolver();
							ContentValues values = new ContentValues();
							values.put(CalendarContract.Events.DTSTART, startMillis);
							values.put(CalendarContract.Events.DTEND, endMillis);
							values.put(CalendarContract.Events.TITLE, Title);
							values.put(CalendarContract.Events.DESCRIPTION, EditTextDescription.getText().toString());
							values.put(CalendarContract.Events.CALENDAR_ID, calID);
							values.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Taipei");

							Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
							// 獲取事件ID
							long eventID = Long.parseLong(uri.getLastPathSegment());
							// 設置提醒
							ContentValues reminderValues = new ContentValues();
							reminderValues.put(CalendarContract.Reminders.MINUTES, 0); // 活動時間點提醒
							reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventID);
							reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);

							Uri reminderUri = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);

							showMessage(getResources().getString(R.string.Set_Calender_Success));
						} catch (Exception e) {
							// 如果沒有 Google 日曆應用，顯示錯誤訊息
							if (e.getMessage().contains("Permission Denial")) {
								showMessage(getResources().getString(R.string.Set_Calender_Failed));
								ActivityCompat.requestPermissions(Activity4_My_Class_Schedule.this,
										new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR},
										PERMISSIONS_REQUEST_CODE);
							} else {
								showMessage(getResources().getString(R.string.None_Google_Calender));
							}
						}
					}
				}

			}
		});

		if (CalenderDialog.getWindow() != null){
			CalenderDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		}
		CalenderDialog.show();
	}



	// 找尋離今天最近的日期，時間，向後尋找條件，不得回朔
	private static Date findNextDate(int dayOfWeek, String timeString) {
		// 取得當前時間
		Calendar now = Calendar.getInstance();

		// 設定目標時間
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		try {
			Date targetTime = sdf.parse(timeString);
			Calendar target = Calendar.getInstance();
			target.setTime(targetTime);
			// 設定目標日期
			Calendar result = (Calendar) now.clone();
			result.set(Calendar.HOUR_OF_DAY, target.get(Calendar.HOUR_OF_DAY));
			result.set(Calendar.MINUTE, target.get(Calendar.MINUTE));
			result.set(Calendar.SECOND, 0);
			result.set(Calendar.MILLISECOND, 0);

			// 計算距離今天最近的目標日期
			while (result.get(Calendar.DAY_OF_WEEK) != dayOfWeek+1 || result.before(now)) {
				result.add(Calendar.DAY_OF_MONTH, 1);
			}
			return result.getTime();
		} catch (Exception e) {
			return null;
		}
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
