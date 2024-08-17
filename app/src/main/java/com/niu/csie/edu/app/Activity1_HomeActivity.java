package com.niu.csie.edu.app;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.*;
import android.text.Html;
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.*;
import android.widget.*;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.github.barteksc.pdfviewer.PDFView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import com.niu.csie.edu.app.AnnouncementItem.AnnouncementItem;
import com.niu.csie.edu.app.AnnouncementItemAdapter.AnnouncementItemAdapter;
import com.niu.csie.edu.app.AchievementItem.AchievementItem;
import com.niu.csie.edu.app.AchievementItemAdapter.AchievementItemAdapter;
import com.niu.csie.edu.app.HuhItem.HuhItem;
import com.niu.csie.edu.app.HuhItemAdapter.HuhItemAdapter;
import com.niu.csie.edu.app.LoginManager.LoginManager;
import com.niu.csie.edu.app.LoginManager.LoginManager_LightMode;
import com.niu.csie.edu.app.Services.NotificationPost;
import com.niu.csie.edu.app.Utils.FileUtil;


/** EUNI 系列變數設為 public static 是為了讓 Activity2_EUNI 能重新整理課程 */


public class Activity1_HomeActivity extends AppCompatActivity implements LoginManager.LoginListener, LoginManager_LightMode.LightModeListener {

	/**Element*/
	private Toolbar _toolbar;
	private DrawerLayout _drawer;
	private TextView Name;
	private LinearLayout Linear_HomePage, Linear_Announcement, Linear_Calender, Linear_Achievements, Linear_huh, Linear_About, Linear_Debug; // Debug 搭載3個網頁頁面，調式用
	private Button BTN_Calender_AppBar;
	private PDFView PDF_Calender_View;

	private ImageView M園區, 成績查詢, 我的課表, 活動報名, 聯絡我們, 畢業門檻, 選課系統, 公車動態, ZUVIO;
	public static WebView Web_M園區;
	private WebView Web_Acade, Web_Zuvio, Web_CCSYS, Web_Announcement;
	private ListView List_Announcement, List_Achievements, List_Huh;
	private View ProgressOverlay;

	/**Components*/
	private SharedPreferences sharedPreferences; // 儲存登入資訊
	public static SharedPreferences SP_EUNI_Course; // EUNI課程資訊
	private FusedLocationProviderClient fusedLocationClient;
	private AlertDialog TimeOutWebViewDevDialog;
	private Handler handler;
	private Runnable refreshRunnable;
	private FirebaseDatabase database = FirebaseDatabase.getInstance();
	private DatabaseReference 行事曆 = database.getReference("行事曆");
	private DatabaseReference usersRef = database.getReference("users");
	private Intent page = new Intent();

	/**Variable*/
	private String worldTimeApiUrl = "http://worldtimeapi.org/api/timezone/Asia/Taipei"; // 避免透過修改 日期/時間 獲得成就
	private String formattedDateNow; // 儲存現在登入日期
	private String formattedTimeNow; // 儲存現在登入時間
	private String DayOfWeekNow; // 儲存現在登入星期 (用於 夜市星人 成就)
	private String Achievement11;
	private String Achievement11_DialogContent;
	private int 行事曆_cnt = 0;
	private boolean quit = false;
	public static boolean EUNI_Login = true; // 登入true，登出false
	private boolean ACADE_Login = true; // 登入true，登出false
	private boolean Zuvio_Login = true; // 登入true，登出false
	private boolean CCSYS_Login = true; // 登入true，登出false
	public static String Semester = ""; // 從 sp 取得學年度，用於M園區篩選課程，及行事曆學年判斷
	private boolean Semester_BTN_State = true; // 僅 Database 有 2 key 時用得到，Semester = Key1 時 true, Semester = Key2 時 false
	private String Calender_URL1, Calender_URL2, Key1, Key2 = ""; // 若只有 1 個，使用 Calender_URL1
	public static String Get_EUNI_Courses = ""; // 防止未取得課程就進入功能
	public static boolean RefreshEUNI = false; // Activity2_EUNI 重新整理完成之判斷 false 預設，true重新整理ing -> 完成返回 false
	private static final int REFRESH_INTERVAL = 1800000; // 30 分鐘，防止 Euni 閒置自動登出
	private AnnouncementItemAdapter announcementItemAdapter; // 公告擷取 Adapter
	private List<AnnouncementItem> announcementItemList = new ArrayList<>(); // 公告擷取 List
	private AchievementItemAdapter achievementItemAdapter; // 成就 Adapter
	private List<AchievementItem> achievementItemList = new ArrayList<>(); // 成就 List
	private HuhItemAdapter huhItemAdapter; // 說明 Adapter
	private List<HuhItem> huhItemList = new ArrayList<>(); // 說明 List


	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.activity1_home);
		initialize(_savedInstanceState);
		initializeLogic();
		/** 檢查 Acade 是否 登入成功，或是可能已經被登出 */
		CheckAcadeLoginState();
	}

	@SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
	private void initialize(Bundle _savedInstanceState) {
		_toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		setActionBarTitle(getResources().getString(R.string.HomePage));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});
		_drawer = findViewById(R.id._drawer);
		ActionBarDrawerToggle _toggle = new ActionBarDrawerToggle(Activity1_HomeActivity.this, _drawer, _toolbar, R.string.app_name, R.string.app_name);
		_drawer.addDrawerListener(_toggle);
		_toggle.syncState();

		LinearLayout _nav_view = findViewById(R.id._nav_view);

		// 主畫面 UI
		Name = findViewById(R.id.name);

		// 側邊功能展開 Linear
		Linear_HomePage = findViewById(R.id.Linear_HomePage);

		Linear_Announcement = findViewById(R.id.Linear_Announcement);

		Linear_Calender = findViewById(R.id.Linear_Calender);
		BTN_Calender_AppBar = findViewById(R.id.button_appbar);
		PDF_Calender_View = findViewById(R.id.PDF_Calender_View);

		Linear_Achievements = findViewById(R.id.Linear_Achievements);

		Linear_huh = findViewById(R.id.Linear_huh);

		Linear_About = findViewById(R.id.Linear_About);

		Linear_Debug = findViewById(R.id.Linear_Debug);

		ProgressOverlay = findViewById(R.id.progress_overlay); // 登出中

		// 主畫面 9 大功能
		M園區 = findViewById(R.id.image_EUNI);
		成績查詢 = findViewById(R.id.image_Score_Inquiry);
		我的課表 = findViewById(R.id.image_MyClassSchedule);
		活動報名 = findViewById(R.id.image_Event_Registration);
		聯絡我們 = findViewById(R.id.image_Contact_Us);
		畢業門檻 = findViewById(R.id.image_Graduation_Threshold);
		選課系統 = findViewById(R.id.image_Subject_System);
		公車動態 = findViewById(R.id.image_Bus);
		ZUVIO = findViewById(R.id.image_Zuvio);

		Web_M園區 = findViewById(R.id.web_EUNI);
		Web_Zuvio = findViewById(R.id.web_Zuvio);
		Web_Acade = findViewById(R.id.web_Acade);
		Web_CCSYS = findViewById(R.id.web_CCSYS);

		Web_Announcement = findViewById(R.id.web_Announcement);
		List_Announcement = findViewById(R.id.List_Announcement);
		List_Achievements = findViewById(R.id.List_Achievements);
		List_Huh = findViewById(R.id.List_huh);

		// 初始化設置
		sharedPreferences = getSharedPreferences("sp", Activity.MODE_PRIVATE);
		SP_EUNI_Course = getSharedPreferences("EUNI_course_data", Activity.MODE_PRIVATE);
		Web_M園區.getSettings().setJavaScriptEnabled(true);
		Web_Acade.getSettings().setJavaScriptEnabled(true);
		Web_Zuvio.getSettings().setJavaScriptEnabled(true);
		Web_CCSYS.getSettings().setJavaScriptEnabled(true);
		Web_CCSYS.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		Web_Announcement.getSettings().setJavaScriptEnabled(true);

		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

		// 公告 Adapter 建立
		announcementItemAdapter = new AnnouncementItemAdapter(this, R.layout.announcement_customlistview, announcementItemList);
		List_Announcement.setAdapter(announcementItemAdapter);

		// 添加項目至 AchievementsAdapter
		achievementItemList.add(new AchievementItem(R.drawable.welcome, getString(R.string.Achievements_01_Title), getString(R.string.Achievements_01_Description), R.drawable.Image_Finished));
		achievementItemList.add(new AchievementItem(R.drawable.thx_for_ur_advice, getString(R.string.Achievements_02_Title), getString(R.string.Achievements_02_Description), R.drawable.Image_UnFinished));
		achievementItemList.add(new AchievementItem(R.drawable.am3, getString(R.string.Achievements_03_Title), getString(R.string.Achievements_03_Description), R.drawable.Image_UnFinished));
		achievementItemList.add(new AchievementItem(R.drawable.date_0401, getString(R.string.Achievements_04_Title), getString(R.string.Achievements_04_Description), R.drawable.Image_UnFinished));
		achievementItemList.add(new AchievementItem(R.drawable.date_0826, getString(R.string.Achievements_05_Title), getString(R.string.Achievements_05_Description), R.drawable.Image_UnFinished));
		achievementItemList.add(new AchievementItem(R.drawable.date_1225, getString(R.string.Achievements_06_Title), getString(R.string.Achievements_06_Description), R.drawable.Image_UnFinished));
		achievementItemList.add(new AchievementItem(R.drawable.day100, getString(R.string.Achievements_07_Title), getString(R.string.Achievements_07_Description), R.drawable.Image_UnFinished));
		achievementItemList.add(new AchievementItem(R.drawable.day365, getString(R.string.Achievements_08_Title), getString(R.string.Achievements_08_Description), R.drawable.Image_UnFinished));
		achievementItemList.add(new AchievementItem(R.drawable.never_give_up, getString(R.string.Achievements_09_Title), getString(R.string.Achievements_09_Description), R.drawable.Image_UnFinished));
		achievementItemList.add(new AchievementItem(R.drawable.dark_repulser, getString(R.string.Achievements_10_Title), getString(R.string.Achievements_10_Description), R.drawable.Image_UnFinished));
		achievementItemList.add(new AchievementItem(R.drawable.night_market, getString(R.string.Achievements_11_Title), getString(R.string.Achievements_11_Description), R.drawable.Image_UnFinished));
		// 成就 Adapter 建立
		achievementItemAdapter = new AchievementItemAdapter(this, achievementItemList);
		List_Achievements.setAdapter(achievementItemAdapter);

		// 添加項目至 HuhAdapter
		huhItemList.add(new HuhItem(R.drawable.euni, getString(R.string.EUNI), getString(R.string.EUNI_)));
		huhItemList.add(new HuhItem(R.drawable.score_inquiry, getString(R.string.Score_Inquiry), getString(R.string.Score_Inquiry_)));
		huhItemList.add(new HuhItem(R.drawable.my_class_schedule, getString(R.string.Class_Schedule), getString(R.string.Class_Schedule_)));
		huhItemList.add(new HuhItem(R.drawable.event_registration, getString(R.string.Event_Registration), getString(R.string.Event_Registration_)));
		huhItemList.add(new HuhItem(R.drawable.contact_us, getString(R.string.Contact_Us), getString(R.string.Contact_Us_)));
		huhItemList.add(new HuhItem(R.drawable.graduation_threshold, getString(R.string.Graduation_Threshold), getString(R.string.Graduation_Threshold_)));
		huhItemList.add(new HuhItem(R.drawable.subject_system, getString(R.string.Subject_System), getString(R.string.Subject_System_)));
		huhItemList.add(new HuhItem(R.drawable.bus, getString(R.string.Bus), getString(R.string.Bus_)));
		huhItemList.add(new HuhItem(R.drawable.zuvio, getString(R.string.Zuvio), getString(R.string.Zuvio_)));
		// 說明 Adapter 建立
		huhItemAdapter = new HuhItemAdapter(this, huhItemList);
		List_Huh.setAdapter(huhItemAdapter);


		String M園區_UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"; // 電腦版網站才沒 bug
		Web_M園區.getSettings().setUserAgentString(M園區_UserAgent);

		String Euni_url = "https://euni.niu.edu.tw/my/";
		String Zuvio_url = "https://irs.zuvio.com.tw/student5/setting/index";
		String CCSYS_url = "https://ccsys.niu.edu.tw/MvcTeam/Act";


		// M園區 網站Load
		Web_M園區.loadUrl(Euni_url);
		// 重新整理
		Web_M園區.reload();
		Web_M園區.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (Get_EUNI_Courses.length() <= 10 || !SP_EUNI_Course.contains("課程_1_名稱")) {
					checkEUNIPageLoadComplete(); // 先確認是否動態加載完成
				}
			}
		});

		// Zuvio 網站Load
		Web_Zuvio.loadUrl(Zuvio_url);
		Web_Zuvio.setWebViewClient(new WebViewClient());

		// CCSYS 網站Load
		Web_CCSYS.loadUrl(CCSYS_url);
		Web_CCSYS.setWebViewClient(new WebViewClient());

		// Acade WebViewClient
		Web_Acade.setWebViewClient(new WebViewClient());

		// 公告擷取
		Web_Announcement.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				GetAnnouncementData();
			}
		});
		Web_Announcement.loadUrl("https://www.niu.edu.tw/p/422-1000-1019.php"); // 沒調整預設 UserAgent 為手機版網頁

		// 使用一個 HTTP 客戶端來取得時間
		OkHttpClient client = new OkHttpClient();

		// 事件
		BTN_Calender_AppBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// 切換狀態，並 Load 另一個 url
				Semester_BTN_State = !Semester_BTN_State;
				if (Semester_BTN_State) {
					BTN_Calender_AppBar.setText(Key2);
					LoadCalenderPDF(Calender_URL1);
				} else {
					BTN_Calender_AppBar.setText(Key1);
					LoadCalenderPDF(Calender_URL2);
				}
			}
		});


		M園區.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Get_EUNI_Courses.length() > 10 || SP_EUNI_Course.contains("課程_1_名稱")) {
					page.setClass(getApplicationContext(), Activity2_EUNI.class);
					startActivity(page);
					overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				} else {
					showMessage(getResources().getString(R.string.EUNI_Cources_INIT));
				}

			}
		});
		成績查詢.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				page.setClass(getApplicationContext(), Activity3_Score_Inquiry.class);
				startActivity(page);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		我的課表.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				page.setClass(getApplicationContext(), Activity4_My_Class_Schedule.class);
				startActivity(page);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		活動報名.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				page.setClass(getApplicationContext(), Activity5_Event_Registration.class);
				startActivity(page);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		聯絡我們.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				page.setClass(getApplicationContext(), Activity6_Contact_Us.class);
				startActivity(page);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		畢業門檻.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				page.setClass(getApplicationContext(), Activity7_Graduation_Threshold.class);
				startActivity(page);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		選課系統.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				page.setClass(getApplicationContext(), Activity8_Subject_System.class);
				startActivity(page);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		公車動態.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				page.setClass(getApplicationContext(), Activity9_Bus.class);
				startActivity(page);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		ZUVIO.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				page.setClass(getApplicationContext(), Activity10_Zuvio.class);
				startActivity(page);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});

		/** Firebase DB 區段 */
		// 取得 行事曆 url
		行事曆.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				// 檢查子節點的數量
				long childCount = dataSnapshot.getChildrenCount();
				行事曆_cnt = (int) childCount;

				if (childCount == 1) {
					// 只有一個子節點時的處理
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						String key = snapshot.getKey();
						String value = snapshot.getValue(String.class);
						// 執行需要的操作
						Calender_URL1 = value;
					}
				} else if (childCount == 2) {
					// 有兩個子節點時的處理
					Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
					DataSnapshot firstSnapshot = iterator.next();
					DataSnapshot secondSnapshot = iterator.next();

					String key1 = firstSnapshot.getKey();
					String value1 = firstSnapshot.getValue(String.class);
					String key2 = secondSnapshot.getKey();
					String value2 = secondSnapshot.getValue(String.class);

					// 執行需要的操作
					Key1 = key1;
					Key2 = key2;
					if (Semester.equals(Key1)) { // 學期尚未完全交接，大約 6,7,8 月
						// Semester 等於 Key1
						Semester_BTN_State = true;
					} else if (Semester.equals(Key2)) { // 學期完成交接，但 Database Calender 仍有 2 個 key
						// Semester 等於 Key2
						Semester_BTN_State = false;
					}
					Calender_URL1 = value1;
					Calender_URL2 = value2;

				} else {
					showMessage("ʕ\u2060´\u2060•\u2060ᴥ\u2060•\u2060`\u2060ʔ");
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		});

		// 若不存在個人檔案 登錄個人檔案
		DatabaseReference userRef = usersRef.child(Objects.requireNonNull(sharedPreferences.getString("account", null)));
		DatabaseReference achievementsRef = userRef.child("Achievements");

		// 取得當前日期 ( 使用 worldTimeApi 並加上 GMT+8 時區，用戶修改裝置日期時間不影響 )
		Request request = new Request.Builder()
				.url(worldTimeApiUrl)
				.build();

		userRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				// 紀錄登入天數
				LoginManager loginManager = new LoginManager(getApplicationContext(), Activity1_HomeActivity.this);
				loginManager.userLoggedIn(); // 紀錄連續 30 天登入
				// 紀錄亮色模式 74 天連續登入
				LoginManager_LightMode lightModeManager = new LoginManager_LightMode(getApplicationContext(), Activity1_HomeActivity.this);
				lightModeManager.userLoggedIn();

				if (!dataSnapshot.exists()) {
					// ID 不存在，創建新用戶
					String NAME = sharedPreferences.getString("Name", null);
					NAME = processName(NAME);
					userRef.child("Name").setValue(NAME);

					// 添加 10 個成就
					for (int i = 1; i <= 10; i++) {
						@SuppressLint("DefaultLocale")
						String achievementKey = String.format("%02d", i);
						achievementsRef.child(achievementKey).setValue(false);
					}
					// 添加 第11 成就 (夜市)
					achievementsRef.child("11").setValue((String) "00000000000000");
					// 解鎖 01 成就 首次登入
					AchievementsGet("01");

					// 返回時間日期
					client.newCall(request).enqueue(new Callback() {
						@Override
						public void onFailure(@NonNull Call call, @NonNull IOException e) {
							e.printStackTrace();
						}

						@Override
						public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
							if (response.isSuccessful()) {
								assert response.body() != null;
								String responseStr = response.body().string();
								// 解析 JSON 來取得時間
								try {
									JSONObject json = new JSONObject(responseStr);
									formattedDateNow = json.getString("datetime").split("T")[0];
									formattedTimeNow = json.getString("datetime").split("T")[1].split("\\.")[0];
									DayOfWeekNow = json.getString("day_of_week");

									// 添加首次登入日期
									achievementsRef.child("首次登入日期").setValue(formattedDateNow);

									// 日期成就
									if (formattedDateNow.contains("-04-01")) {
										AchievementsGet("04");
									} else if (formattedDateNow.contains("-08-26")) {
										AchievementsGet("05");
									} else if (formattedDateNow.contains("-12-25")) {
										AchievementsGet("06");
									}
									// 好棒，3點了
									if (formattedTimeNow.contains("03:00:")) {
										AchievementsGet("03");
									}

									AchievementsCheck(); // 刷新成就檢查

								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}
					});

				} else {
					/** ID 已存在，檢查成就列表，以初始化成就" */

					// 返回時間日期
					client.newCall(request).enqueue(new Callback() {
						@Override
						public void onFailure(@NonNull Call call, @NonNull IOException e) {
							e.printStackTrace();
						}

						@Override
						public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
							if (response.isSuccessful()) {
								assert response.body() != null;
								String responseStr = response.body().string();
								// 解析 JSON 來取得時間
								try {
									JSONObject json = new JSONObject(responseStr);
									formattedDateNow = json.getString("datetime").split("T")[0];
									formattedTimeNow = json.getString("datetime").split("T")[1].split("\\.")[0];
									DayOfWeekNow = json.getString("day_of_week");

									// 取得當前時間，檢查登入天數成就
									String firstLoginDateStr = dataSnapshot.child("Achievements").child("首次登入日期").getValue(String.class);
									DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
									assert firstLoginDateStr != null;
									LocalDate firstLoginDate = LocalDate.parse(firstLoginDateStr, formatter);
									LocalDate nowLoginDate = LocalDate.parse(formattedDateNow, formatter);
									long daysBetween = ChronoUnit.DAYS.between(firstLoginDate, nowLoginDate);
									// 檢查是否大於 1年 或 百日
									if (daysBetween >= 365) {
										AchievementsGet("08");
									} else if (daysBetween >= 100) {
										AchievementsGet("07");
									}

									// 日期成就
									if (formattedDateNow.contains("-04-01")) {
										AchievementsGet("04");
									} else if (formattedDateNow.contains("-08-26")) {
										AchievementsGet("05");
									} else if (formattedDateNow.contains("-12-25")) {
										AchievementsGet("06");
									}
									// 好棒，3點了
									if (formattedTimeNow.contains("03:00:")) {
										AchievementsGet("03");
									}

									AchievementsCheck(); // 刷新成就檢查

								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}
					});

				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		});

		/** Firebase DB 區段結束 */
	}


	// LoginManager 連續登入 30 天回調函數
	@Override
	public void onThirtyDaysLogin() {
		AchievementsGet("09");
	}

	@Override
	public void on74DaysLogin_LightMode() {
		AchievementsGet("10");
	}

	@Override
	public void onBackPressed() {
		if (_drawer.isDrawerOpen(GravityCompat.START)) {
			// 開啟的 Drawer 關閉
			_drawer.closeDrawer(GravityCompat.START);
		} else {
			if (!quit) { // 詢問退出程序
				showMessage(getResources().getString(R.string.back_again));
				new Timer(true).schedule(new TimerTask() { // 啟動定時任務
					@Override
					public void run() {
						quit = false; // 重置退出標示
					}
				}, 2000); // 2秒後執行run()方法
				quit = true;
			} else {//確認退出應用
				super.onBackPressed();
				finishAffinity();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(refreshRunnable);
	}


	// 處理姓名，Firebase Database users 添加使用，王○明
	private String processName(String name) {
		int length = name.length();

		if (length == 2) {
			return name.charAt(0) + "○";
		} else if (length > 2) {
			StringBuilder processedName = new StringBuilder();
			processedName.append(name.charAt(0));
			for (int i = 1; i < length - 1; i++) {
				processedName.append("○");
			}
			processedName.append(name.charAt(length - 1));
			return processedName.toString();
		} else {
			return name;
		}
	}


	// 設標題
	private void setActionBarTitle(String title) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(title);
		}
	}


	// 公告解碼，由於有編碼問題所以需要這個，否則導致爬失敗
	private String decodeUnicode(String unicodeString) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < unicodeString.length()) {
			char c = unicodeString.charAt(i);
			if (c == '\\') {
				if (i + 1 < unicodeString.length() && unicodeString.charAt(i + 1) == 'u') {
					// 提取 unicode 字符
					String unicode = unicodeString.substring(i + 2, i + 6);
					sb.append((char) Integer.parseInt(unicode, 16));
					i += 6;
				} else {
					sb.append(c);
					i++;
				}
			} else {
				sb.append(c);
				i++;
			}
		}
		return sb.toString();
	}

	// 整理 HTML 的方法，去除造成爬失敗的其他字符
	private String cleanHtml(String html) {
		// Replace tabs, newlines, and escaped quotes
		html = html.replaceAll("\\\\t", ""); // Remove tabs
		html = html.replaceAll("\\\\n", ""); // Remove newlines
		html = html.replaceAll("\\\\\"", "\""); // Convert escaped quotes to normal quotes
		return html;
	}

	// 取得公告內容
	private void GetAnnouncementData() {
		Web_Announcement.evaluateJavascript("(function() { return document.querySelector('.listTB.table').outerHTML; })();",
				html -> {

					if (html != null && !html.isEmpty()) {
						// 刪除 HTML 字串周圍的引號
						html = html.replaceAll("^\"|\"$", "");
						// 解碼 Unicode 字符
						html = decodeUnicode(html);
						// 整理 HTML
						html = cleanHtml(html);
						// 使用 Jsoup 解析 HTML
						Document doc = Jsoup.parse(html);
						// 選擇表體內的行
						Elements rows = doc.select("tbody tr");

						for (Element row : rows) {
							// 提取日期
							String date = row.select("td[data-th=日期] .d-txt").text().trim();
							// 提取標題和href
							Element titleElement = row.select("td[data-th=標題] .d-txt .mtitle a").first();

							if (titleElement != null) {
								String title = titleElement.text().trim();
								String href = titleElement.attr("href").trim();

								// 確保日期、標題和 href 不為空
								if (!date.isEmpty() && !title.isEmpty() && !href.isEmpty()) {
									// 建 AnnouncementItem 並添加到列表
									AnnouncementItem item = new AnnouncementItem(title, date, href);
									announcementItemList.add(item);
								}
							}
						}
						// 通知適配器資料更改
						announcementItemAdapter.notifyDataSetChanged();
					}
				});
	}

	// 處理公告 Item 點擊事件
	public void onAnnouncementItemClick(int position) {
		AnnouncementItem item = announcementItemList.get(position);
		AnnouncementClickItemHintDialog(item.getHref());
	}

	// 公告點擊提示跳轉外部瀏覽器 Dialog
	private void AnnouncementClickItemHintDialog(String url) {
		AlertDialog.Builder builder = new AlertDialog.Builder(Activity1_HomeActivity.this, R.style.AlertDialogTheme);
		View view = LayoutInflater.from(Activity1_HomeActivity.this).inflate(
				R.layout.custom_alert_dialog2,
				findViewById(R.id.layoutDialogContainer)
		);
		builder.setView(view);
		((TextView) view.findViewById(R.id.textTitle)).setText(getResources().getString(R.string.Dialog_JumpOutsideWebHint_Title));
		((TextView) view.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.Dialog_JumpOutsideWebHint_Message));
		((Button) view.findViewById(R.id.buttonAction_Cancel)).setText(getResources().getString(R.string.Dialog_Cancel));
		((Button) view.findViewById(R.id.buttonAction)).setText(getResources().getString(R.string.Dialog_OK));
		((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_website_white);

		builder.setCancelable(false);
		AlertDialog WebsiteAlertDialog = builder.create();

		view.findViewById(R.id.buttonAction_Cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				WebsiteAlertDialog.dismiss();
			}
		});
		view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				WebsiteAlertDialog.dismiss();
				String url_final = url;
				if (!url.startsWith("https")) {
					// var/file 通常是 pdf 文件
					url_final = "https://www.niu.edu.tw/" + url;
				}
				Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url_final));
				startActivity(browser);
			}
		});

		if (WebsiteAlertDialog.getWindow() != null) {
			WebsiteAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		}
		WebsiteAlertDialog.show();
	}


	// 載入行事曆 PDF_URL
	private void LoadCalenderPDF(String url) {
		loadPdf(url);
	}

	private void initializeLogic() {
		// 取得 sp 的姓名
		Name.setText(sharedPreferences.getString("Name", null));
		// 取得 sp 的學年度
		Semester = sharedPreferences.getString("學年度", null);

		// 防止 Euni, Acade 閒置登出
		handler = new Handler();
		refreshRunnable = new Runnable() {
			@Override
			public void run() {
				// 重新整理
				Web_M園區.reload();
				handler.postDelayed(this, REFRESH_INTERVAL);
			}
		};
		handler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
	}


	// 處理超時，建議更換 WebView Dev Dialog
	private void HandleTimeOutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Activity1_HomeActivity.this, R.style.AlertDialogTheme);
		View view = LayoutInflater.from(Activity1_HomeActivity.this).inflate(
				R.layout.custom_alert_dialog2,
				findViewById(R.id.layoutDialogContainer)
		);
		builder.setView(view);
		((TextView) view.findViewById(R.id.textTitle)).setText(getResources().getString(R.string.WebViewDev_Title2));
		((TextView) view.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.WebViewDev_Message2));
		((Button) view.findViewById(R.id.buttonAction_Cancel)).setText(getResources().getString(R.string.WebViewDev_Btn1));
		((Button) view.findViewById(R.id.buttonAction)).setText(getResources().getString(R.string.WebViewDev_Btn2));

		builder.setCancelable(false);
		TimeOutWebViewDevDialog = builder.create();

		view.findViewById(R.id.buttonAction_Cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				TimeOutWebViewDevDialog.dismiss();
				Intent playstore = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.webview.beta"));
				startActivity(playstore);
				finishAffinity();
			}
		});
		view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				TimeOutWebViewDevDialog.dismiss();
				Intent playstore = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.webview"));
				startActivity(playstore);
				finishAffinity();
			}
		});

		if (TimeOutWebViewDevDialog.getWindow() != null){
			TimeOutWebViewDevDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		}
		TimeOutWebViewDevDialog.show();
	}

	private void CheckAcadeLoginState() {
		Map<String, String> headers = new HashMap<>();
		headers.put("Referer", "https://acade.niu.edu.tw/NIU/");
		Web_Acade.loadUrl("https://acade.niu.edu.tw/NIU/MainFrame.aspx", headers);
		Web_Acade.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				view.loadUrl(request.getUrl().toString());
				return true; // 表示我們已經處理這個 URL，不需要再外部處理，eg: 跳轉外部瀏覽器
			}
		});
		Web_Acade.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				if (message.contains("自動登出")) {
					showMessage(getResources().getString(R.string.Login_TimeOut));
					HandleTimeOutDialog();
					return true;
				}
				return super.onJsAlert(view, url, message, result);
			}
		});
	}

	public static void checkEUNIPageLoadComplete() {
		RefreshEUNI = true;
		Web_M園區.evaluateJavascript(
				"(function() { return document.readyState; })()",
				new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						if ("\"complete\"".equals(value) || "\"interactive\"".equals(value)) {
							GetCourseJavaScript();
						} else {
							Web_M園區.postDelayed(new Runnable() {
								@Override
								public void run() {
									checkEUNIPageLoadComplete();
								}
							}, 500);  // 每隔 500 ms 檢查一次
						}
					}
				}
		);
	}

	public static void GetCourseJavaScript() {
		/** 預先加載M園區課程 */
		if (EUNI_Login) {
			Web_M園區.evaluateJavascript(
					"(function() { " +
							"var elements = document.querySelectorAll('i.fa.fa-graduation-cap');" +
							"var result = [];" +
							"elements.forEach(function(element) {" +
							"var parent = element.closest('a');" +
							"const Semester = '" + Semester + "';" +
							"if (parent && parent.textContent.trim().match(new RegExp(`.{0,7}?${Semester}\\\\d`))) {" +
							//"if (parent && parent.textContent.trim().substring(0, 3) === '" + Semester + "') {" +
							"result.push({" +
							"title: parent.textContent.trim()," +
							"href: parent.href" +
							"});" +
							"}" +
							"});" +
							"return JSON.stringify(result);" +
							"})()",
					new ValueCallback<String>() {
						@Override
						public void onReceiveValue(String value) {
							// 這裡的 value 是 JSON 格式的字串
							// 解析並儲存到 SharedPreferences
							// 如果沒抓到，再執行一次

							if (value.length() < 10) {
								GetCourseJavaScript();
							} else {
								Save_EUNI_Course_Data(value);
								Get_EUNI_Courses = value;
							}

						}
					}
			);
		}
	}

	public static void Save_EUNI_Course_Data(String jsonData) {
		jsonData = jsonData.replace("\\", "");
		jsonData = jsonData.replace("\"[", "[");
		jsonData = jsonData.replace("]\"", "]");
		SP_EUNI_Course.edit().clear().commit();

		try {
			JSONArray jsonArray = new JSONArray(jsonData);
			SharedPreferences.Editor editor = SP_EUNI_Course.edit();

			String First_Course = "", First_Course_ID = ""; // Get_EUNI_Courses 有 Bug 會重複抓

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String title = jsonObject.getString("title");
				String href = jsonObject.getString("href").split("id=")[1];
				if (i == 0) {
					First_Course = title;
					First_Course_ID = href;
				} else {
					if (title.equals(First_Course) || href.equals(First_Course_ID)) {
						break;
					}
				}
				editor.putString("課程_" + i + "_名稱", title);
				editor.putString("課程_" + i + "_ID", href);


			}
			editor.apply();
			RefreshEUNI = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	// 處理成就 夜市星人 點擊事件
	public void onAchievementItemClick(int position) {
		if (position == 10) {
			// 僅夜市成就點擊事件有效
			try {
				if (!Achievement11.equals("11111111111111")) {
					AchievementClickItemDialog(Achievement11_DialogContent);
				} else {
					showMessage(getResources().getString(R.string.Achievements_11_showMessage1));
				}
			} catch (Exception e) {
				// 點太快，成就尚未抓取成功 
				showMessage(getResources().getString(R.string.Achievements_11_showMessage0));
			}



		}
	}

	// 處理 html 樣式文字
	private String buildHtmlContent(String input) {
		StringBuilder contentBuilder = new StringBuilder();
		String[] strArray = getResources().getStringArray(R.array.night_market_array); // 從 string.xml 提取字串陣列

		for (int i = 0; i < input.length(); i++) {
			char currentChar = input.charAt(i);
			String currentStr = strArray[i]; // 提取對應的字串

			if (currentChar == '1') {
				contentBuilder.append("<font color=#00AF00>").append(currentStr).append("</font><br>");
			} else if (currentChar == '0') {
				contentBuilder.append("<font color=#FF0000>").append(currentStr).append("</font><br>");
			}
		}

		return contentBuilder.toString();
	}

	// 夜市星人 Dialog
	private void AchievementClickItemDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(Activity1_HomeActivity.this, R.style.AlertDialogTheme);
		View view = LayoutInflater.from(Activity1_HomeActivity.this).inflate(
				R.layout.custom_alert_dialog,
				findViewById(R.id.layoutDialogContainer)
		);
		builder.setView(view);
		((TextView) view.findViewById(R.id.textTitle)).setText(getResources().getString(R.string.Achievements_11_Dialog_Title));
		((TextView) view.findViewById(R.id.textMessage)).setText(Html.fromHtml(message));
		((Button) view.findViewById(R.id.buttonAction)).setText(getResources().getString(R.string.Dialog_OK));

		builder.setCancelable(false);
		AlertDialog AchievementAlertDialog = builder.create();


		view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AchievementAlertDialog.dismiss();
			}
		});
		if (AchievementAlertDialog.getWindow() != null) {
			AchievementAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		}
		AchievementAlertDialog.show();
	}


	// 成就檢查，於 Firebase DB 取得 users 後檢查
	// 目的：更新成就介面資訊
	private void AchievementsCheck() {
		DatabaseReference achievementRef = usersRef.child(Objects.requireNonNull(sharedPreferences.getString("account", null))).child("Achievements");
		achievementRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {

					boolean[] achievementResults = new boolean[10];

					for (int i = 1; i <= 10; i++) {
						String key = String.format("%02d", i);
						if (dataSnapshot.child(key).exists()) {
							achievementResults[i - 1] = dataSnapshot.child(key).getValue(Boolean.class);
						}
					}
					if (dataSnapshot.child("11").exists()) {
						Achievement11 = dataSnapshot.child("11").getValue(String.class);
					}

					// 更新成就牆
					for (int i = 0; i < achievementResults.length; i++) {
						if (achievementResults[i]) { // 獲得成就的話
							achievementItemAdapter.updateImageResult(i, R.drawable.Image_Finished);
						} else {
							achievementItemAdapter.updateImageResult(i, R.drawable.Image_UnFinished);
						}
					}
					// 紀錄夜市紀錄
					if (!Achievement11.equals("11111111111111")) {
						CheckIfInNightMarket();
					} else {
						achievementItemAdapter.updateImageResult(10, R.drawable.Image_Finished);
					}

				}

			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		});
	}

	// 獲得成就，自帶檢查是否已獲得
	private void AchievementsGet(String index) {

		int Index = Integer.parseInt(index);
		DatabaseReference achievementRef = usersRef.child(Objects.requireNonNull(sharedPreferences.getString("account", null))).child("Achievements").child(index);
		achievementRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					Boolean achievementValue = dataSnapshot.getValue(Boolean.class);
					if (achievementValue != null) {
						if (achievementValue) {
							// 已經獲得成就 true, 無需再獲得
						} else {
							// 獲得成就
							achievementRef.setValue(true);
							NotificationPost notificationPost = new NotificationPost(Activity1_HomeActivity.this);
							switch (Index) {
								case 1:
									notificationPost.sendNotification(getResources().getString(R.string.Achievements_Get), getResources().getString(R.string.Achievements_01_Title));
									break;
								case 2:
									notificationPost.sendNotification(getResources().getString(R.string.Achievements_Get), getResources().getString(R.string.Achievements_02_Title));
									break;
								case 3:
									notificationPost.sendNotification(getResources().getString(R.string.Achievements_Get), getResources().getString(R.string.Achievements_03_Title));
									break;
								case 4:
									notificationPost.sendNotification(getResources().getString(R.string.Achievements_Get), getResources().getString(R.string.Achievements_04_Title));
									break;
								case 5:
									notificationPost.sendNotification(getResources().getString(R.string.Achievements_Get), getResources().getString(R.string.Achievements_05_Title));
									break;
								case 6:
									notificationPost.sendNotification(getResources().getString(R.string.Achievements_Get), getResources().getString(R.string.Achievements_06_Title));
									break;
								case 7:
									notificationPost.sendNotification(getResources().getString(R.string.Achievements_Get), getResources().getString(R.string.Achievements_07_Title));
									break;
								case 8:
									notificationPost.sendNotification(getResources().getString(R.string.Achievements_Get), getResources().getString(R.string.Achievements_08_Title));
									break;
								case 9:
									notificationPost.sendNotification(getResources().getString(R.string.Achievements_Get), getResources().getString(R.string.Achievements_09_Title));
									break;
								case 10:
									notificationPost.sendNotification(getResources().getString(R.string.Achievements_Get), getResources().getString(R.string.Achievements_10_Title));
									break;
								default:
									System.out.println("處理其他情況的邏輯");
									break;
							}

						}
						AchievementsCheck(); // 刷新檢查成就列表
					} else {
						// Achievement + index + value is null.
					}
				} else {
					// Achievement + index + does not exist.
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
			}
		});

	}

	@SuppressLint("VisibleForTests")
	private void CheckIfInNightMarket() {
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			showMessage(getResources().getString(R.string.Achievements_11_showMessage2));
		} else {
			// LocationRequest 以設定位置請求參數
			LocationRequest locationRequest = LocationRequest.create();
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			locationRequest.setNumUpdates(1);

			fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
				@Override
				public void onLocationResult(LocationResult locationResult) {
					if (locationResult == null) {
						return;
					}
					for (Location location : locationResult.getLocations()) {
						if (location != null) {
							// 處理獲取到的位置數據
							double latitude = location.getLatitude(); // 緯度
							double longitude = location.getLongitude(); // 經度

							if (location.isFromMockProvider()) {
								// 拒絕 FakeGPS，但無法阻止安卓模擬器的自定義位置
								showMessage(getResources().getString(R.string.Using_FakeGPS));
								latitude = 7.435923;
								longitude = 151.853135;
							}

							// 多個景點的經緯度和範圍(公尺)
							double[][] spots = {
									{24.758140, 121.758050, 160}, // 東門
									{24.676150, 121.769659, 250}, // 羅東
									{24.745119, 121.724427, 190}, // 員山
									{24.634899, 121.792794, 200}, // 冬山
									{24.830381, 121.786570, 310}, // 礁溪
									{24.788901, 121.756742, 60}, // 礁溪澤蘭宮
									{24.669425, 121.653343, 100}, // 三星
									{24.855386, 121.821405, 230}, // 頭城 (座標可能有誤)
									{24.661063, 121.760875, 600}, // 清溝
									{24.644374, 121.832876, 100}, // 頂寮路 (座標可能有誤)
									{24.593593, 121.841677, 230}, // 蘇澳 (座標可能有誤)
									{24.617340, 121.838315, 100}, // 馬賽
									{24.584273, 121.865151, 100}, // 南方澳
									{24.463239, 121.802785, 100}, // 南澳
							};

							// 是否在時間區段內 17:30:00~22:30:00
							String startTimeString = "17:30:00";
							String endTimeString = "22:30:00";
							boolean IsInTimeRange = false;

							SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
							try {
								Date now = sdf.parse(formattedTimeNow);
								Date startTime = sdf.parse(startTimeString);
								Date endTime = sdf.parse(endTimeString);

								if (now.after(startTime) && now.before(endTime)) {
									IsInTimeRange = true;
								}

							} catch (Exception e) {
								e.printStackTrace();
							}

							for (int i = 0; i < spots.length; i++) {
								boolean isWithinRange = isWithinRange(latitude, longitude, spots[i][0], spots[i][1], (float) spots[i][2]);
								boolean isDayMatched = checkDayOfWeek(i, DayOfWeekNow);

								if (isWithinRange && isDayMatched && IsInTimeRange) {
									updateAchievementString(i);
								}

							}

							// 更新結果
							DatabaseReference achievementRef = usersRef.child(Objects.requireNonNull(sharedPreferences.getString("account", null))).child("Achievements").child("11");
							achievementRef.addListenerForSingleValueEvent(new ValueEventListener() {
								@Override
								public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
									achievementRef.setValue(Achievement11);
									if (Achievement11.equals("11111111111111")) {
										NotificationPost notificationPost = new NotificationPost(Activity1_HomeActivity.this);
										notificationPost.sendNotification(getResources().getString(R.string.Achievements_Get), getResources().getString(R.string.Achievements_11_Title));
									}

									Achievement11_DialogContent = buildHtmlContent(Achievement11);

									if (Objects.equals(Achievement11, "11111111111111")) {
										achievementItemAdapter.updateImageResult(10, R.drawable.Image_Finished);
									}

								}
								@Override
								public void onCancelled(@NonNull DatabaseError databaseError) {
								}
							});
						}
					}
				}
			}, Looper.getMainLooper());
		}

	}

	private boolean checkDayOfWeek(int index, String currentDayOfWeek) {
		switch (index) {
			case 0: // 員山
				return true;
			case 1: // 員山
				return true;
			case 2: // 員山
				return currentDayOfWeek.equals("4");
			case 3: // 冬山
				return currentDayOfWeek.equals("6");
			case 4: // 礁溪
				return currentDayOfWeek.equals("7");
			case 5: // 礁溪澤蘭宮
				return currentDayOfWeek.equals("1") || currentDayOfWeek.equals("2");
			case 6: // 三星
				return currentDayOfWeek.equals("5");
			case 7: // 頭城
				return currentDayOfWeek.equals("5");
			case 8: // 清溝
				return currentDayOfWeek.equals("3");
			case 9: // 蘇澳頂寮
				return currentDayOfWeek.equals("4");
			case 10: // 蘇澳
				return currentDayOfWeek.equals("7");
			case 11: // 馬賽
				return currentDayOfWeek.equals("6");
			case 12: // 南方澳
				return currentDayOfWeek.equals("1");
			case 13: // 南澳
				return currentDayOfWeek.equals("2");
			default:
				return true;
		}
	}

	private boolean isWithinRange(double userLat, double userLng, double spotLat, double spotLng, float radius) {
		float[] results = new float[1];
		Location.distanceBetween(userLat, userLng, spotLat, spotLng, results);
		return results[0] <= radius;
	}

	private void updateAchievementString(int index) {
		if (Achievement11.charAt(index) == '0') {
			StringBuilder sb = new StringBuilder(Achievement11);
			sb.setCharAt(index, '1');
			Achievement11 = sb.toString();
		}
	}





	/***** Drawer 方法 *****/
	private void ClickEvent(View view) { //總綱
		String title = (String) view.getTag();
		if (title != null) {
			setActionBarTitle(title);
			if (_drawer.isDrawerOpen(GravityCompat.START)) {
				//開啟的 Drawer 關閉
				_drawer.closeDrawer(GravityCompat.START);
			}
		}
	}

	public void HomeClick(View view) {
		ClickEvent(view);
		BTN_Calender_AppBar.setVisibility(View.GONE);
		Linear_HomePage.setVisibility(View.VISIBLE);
		Linear_Announcement.setVisibility(View.GONE);
		Linear_Calender.setVisibility(View.GONE);
		Linear_Achievements.setVisibility(View.GONE);
		Linear_huh.setVisibility(View.GONE);
		Linear_About.setVisibility(View.GONE);
		/**調式用*/
		Linear_Debug.setVisibility(View.GONE);
	}
	public void AnnouncementClick(View view) {
		ClickEvent(view);
		BTN_Calender_AppBar.setVisibility(View.GONE);
		Linear_HomePage.setVisibility(View.GONE);
		Linear_Announcement.setVisibility(View.VISIBLE);
		Linear_Calender.setVisibility(View.GONE);
		Linear_Achievements.setVisibility(View.GONE);
		Linear_huh.setVisibility(View.GONE);
		Linear_About.setVisibility(View.GONE);
		/**調式用*/
		Linear_Debug.setVisibility(View.GONE);
	}
	public void CalendarClick(View view) {
		ClickEvent(view);
		Linear_HomePage.setVisibility(View.GONE);
		Linear_Announcement.setVisibility(View.GONE);
		Linear_Calender.setVisibility(View.VISIBLE);
		if (行事曆_cnt == 2) { // 學年交接尷尬期，右上角按鈕顯示
			if (Semester_BTN_State) {
				BTN_Calender_AppBar.setText(Key2);
				LoadCalenderPDF(Calender_URL1);
			} else { // 完全交接完成，Btn 顯示 Key1
				BTN_Calender_AppBar.setText(Key1);
				LoadCalenderPDF(Calender_URL2);
			}
			BTN_Calender_AppBar.setVisibility(View.VISIBLE);
		} else if (行事曆_cnt == 1){
			BTN_Calender_AppBar.setVisibility(View.GONE);
			LoadCalenderPDF(Calender_URL1);
		}
		Linear_Achievements.setVisibility(View.GONE);
		Linear_huh.setVisibility(View.GONE);
		Linear_About.setVisibility(View.GONE);
		/**調式用*/
		Linear_Debug.setVisibility(View.GONE);
	}
	public void QuizClick(View view) {
		page.setClass(getApplicationContext(), Activity1_QuestionnaireActivity.class);
		startActivity(page);
		overridePendingTransition(0, 0);
	}
	public void AchievementsClick(View view) {
		ClickEvent(view);
		BTN_Calender_AppBar.setVisibility(View.GONE);
		Linear_HomePage.setVisibility(View.GONE);
		Linear_Announcement.setVisibility(View.GONE);
		Linear_Calender.setVisibility(View.GONE);
		Linear_Achievements.setVisibility(View.VISIBLE);
		//Linear_Achievements.setVisibility(View.GONE);
		Linear_huh.setVisibility(View.GONE);
		Linear_About.setVisibility(View.GONE);
		/**調式用*/
		Linear_Debug.setVisibility(View.GONE);
	}
	public void HuhClick(View view) {
		ClickEvent(view);
		BTN_Calender_AppBar.setVisibility(View.GONE);
		Linear_HomePage.setVisibility(View.GONE);
		Linear_Announcement.setVisibility(View.GONE);
		Linear_Calender.setVisibility(View.GONE);
		Linear_Achievements.setVisibility(View.GONE);
		Linear_huh.setVisibility(View.VISIBLE);
		Linear_About.setVisibility(View.GONE);
		/**調式用*/
		Linear_Debug.setVisibility(View.GONE);
	}
	// 處理說明 Item 點擊事件
	public void onDirectionsItemClick(int position) {
		// 取得點擊項目之標題
		HuhItem item = huhItemList.get(position);
		Intent page = new Intent();
		page.setClass(getApplicationContext(), Activity1_HuhViewPagerActivity.class);
		page.putExtra("position", position); // 傳遞 position
		page.putExtra("title", item.getMainText());
		startActivity(page);

	}

	public void AboutClick(View view) {
		ClickEvent(view);
		BTN_Calender_AppBar.setVisibility(View.GONE);
		Linear_HomePage.setVisibility(View.GONE);
		Linear_Announcement.setVisibility(View.GONE);
		Linear_Calender.setVisibility(View.GONE);
		Linear_Achievements.setVisibility(View.GONE);
		Linear_huh.setVisibility(View.GONE);
		Linear_About.setVisibility(View.VISIBLE);
		/**調式用*/
		Linear_Debug.setVisibility(View.GONE);
	}
	public void LogoutClick(View view) {
		// 刪除SP
		sharedPreferences.edit().remove("account").commit();
		sharedPreferences.edit().remove("pwd").commit();
		SP_EUNI_Course.edit().clear().commit();
		FileUtil.deleteFile("/data/user/0/com.niu.csie.edu.app/shared_prefs");
		// 顯示請稍等
		showProgressOverlay();
		// 關閉抽屜
		if (_drawer.isDrawerOpen(GravityCompat.START)) {
			// 開啟的 Drawer 關閉
			_drawer.closeDrawer(GravityCompat.START);
		}
		// 登出所有網頁
		Logout_All();
		// 跳轉至登入
		final Handler handler = new Handler();
		final Runnable checkRunnable = new Runnable() {
			@Override
			public void run() {
				if (!EUNI_Login && !Zuvio_Login && !ACADE_Login && !CCSYS_Login) {
					// 刪除緩存
					WebStorage.getInstance().deleteAllData();
					CookieManager cookieManager = CookieManager.getInstance();
					cookieManager.removeAllCookies(null); // 移除所有 Cookies
					cookieManager.flush();
					Web_Zuvio.clearCache(true);
					Web_CCSYS.clearCache(true);
					Web_Acade.clearCache(true);
					Web_M園區.clearCache(true);
					Web_Zuvio.clearHistory();
					Web_CCSYS.clearHistory();
					Web_Acade.clearHistory();
					Web_M園區.clearHistory();
					deleteDatabase("webview.db");
					deleteDatabase("webviewCache.db");
					// 隱藏等待中
					hideProgressOverlay();
					showMessage(getResources().getString(R.string.Logout_Successful));
					// 跳轉畫面
					Intent page = new Intent(Activity1_HomeActivity.this, Activity0_LoginActivity.class);
					startActivity(page);
					restartApp();
					overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
					finish();
				} else {
					handler.postDelayed(this, 100); // 每 100ms 檢查一次
				}
			}
		};
		handler.post(checkRunnable);
	}

	public void Logout_All() {
		/** M園區 */
		String EUNI_Logout = "document.querySelector('a[title=\"登出\"]').click();";
		Web_M園區.evaluateJavascript(EUNI_Logout, value -> {
			Web_M園區.setWebChromeClient(new WebChromeClient() {
				@Override
				public void onProgressChanged(WebView view, int newProgress) {
					super.onProgressChanged(view, newProgress);
					if (newProgress == 100) {
						EUNI_Login = false;
					}
				}
			});
		});

		/** Zuvio */
		String Zuvio_Logout = "var elements = document.getElementsByClassName('s-i-b-b-row');" +
				"for (var i = 0; i < elements.length; i++) {" +
				"    if (elements[i].classList.length === 1) {" +
				"        elements[i].click();" +
				"        break;" +
				"    }" +
				"}";
		Web_Zuvio.evaluateJavascript(Zuvio_Logout, value -> {
			Web_Zuvio.setWebChromeClient(new WebChromeClient() {
				@Override
				public void onProgressChanged(WebView view, int newProgress) {
					super.onProgressChanged(view, newProgress);
					if (newProgress == 100) {
						Zuvio_Login = false;
					}
				}
			});
		});

		/** Acade */
		Web_Acade.loadUrl("https://acade.niu.edu.tw/NIU/Logout.aspx");
		ACADE_Login = false;

		/** CCSYS */
		Web_CCSYS.evaluateJavascript("javascript:document.getElementById('logoutForm').submit()", token -> {
			CCSYS_Login = false;
		});

	}

	// 在需要重啟的地方調用，不做的話宜大活動有登出BUG
	private void restartApp() {
		Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 190, pendingIntent);
		// 結束應用
		finishAffinity();
		System.exit(0);
	}



	//顯示 登出中
	private void showProgressOverlay() {
		if (ProgressOverlay != null) {
			ProgressOverlay.setVisibility(View.VISIBLE);
			// 旋轉動畫
			ImageView loadingImage = ProgressOverlay.findViewById(R.id.loading_image);
			Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.prog_rotate_icon);
			loadingImage.startAnimation(rotateAnimation);
		}
	}

	//隱藏 登出中
	private void hideProgressOverlay() {
		if (ProgressOverlay != null && ProgressOverlay.getVisibility() == View.VISIBLE) {
			ProgressOverlay.setVisibility(View.GONE);
		}
	}

	private void showMessage(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}


	private void loadPdf(String pdfUrl) {
		new RetrivePDFfromUrl().execute(pdfUrl);
	}

	// 行事曆 class
	class RetrivePDFfromUrl extends AsyncTask<String, Void, InputStream> {
		@Override
		protected InputStream doInBackground(String... strings) {
			InputStream inputStream = null;
			try {
				URL url = new URL(strings[0]);
				// below is the step where we are creating our connection.
				HttpURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
				if (urlConnection.getResponseCode() == 200) {
					//response is success.
					//we are getting input stream from url and storing it in our variable.
					inputStream = new BufferedInputStream(urlConnection.getInputStream());
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			return inputStream;
		}

		@Override
		protected void onPostExecute(InputStream inputStream) {
			// after the execution of our async task we are loading our pdf in our pdf view.
			PDF_Calender_View.fromStream(inputStream).load();

		}
	}

}
