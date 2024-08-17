package com.niu.csie.edu.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.security.keystore.StrongBoxUnavailableException;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.niu.csie.edu.app.EUNICourseAdapter.EUNICourseAdapter;
import com.niu.csie.edu.app.EUNICourseAdapter.OnSubItemClickListener;


public class Activity2_EUNI extends AppCompatActivity implements OnSubItemClickListener {

	/**Element*/
	private Toolbar _toolbar;
	private ListView listView;
	private WebView Web_M園區;
	private ImageView Reload;
	private View ProgressOverlay;

	/**Components*/
	private SharedPreferences SP_EUNI_Course;
	private Handler handler;
	private Runnable refreshRunnable;
	private Intent page = new Intent();

	/**Variable*/
	private static final int REFRESH_INTERVAL = 1800000; // 30 分鐘，防止 Euni 閒置自動登出
	private String Position = ""; // 從 CustomAdapter 傳回的課程 position
	private String CourseName = ""; // 從 CustomAdapter 傳回的課程名稱
	private boolean CanPressReload = true;


	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.activity2_euni);
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
		setActionBarTitle(getResources().getString(R.string.EUNI));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});

		// 主畫面UI
		listView = findViewById(R.id.listView);
		Web_M園區 = findViewById(R.id.web_EUNI);
		Reload = findViewById(R.id.reload);
		Animation spinAnimation = AnimationUtils.loadAnimation(this, R.anim.spin);
		ProgressOverlay = findViewById(R.id.progress_overlay);

		// 事件
		Reload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (CanPressReload) {
					CanPressReload = false;
					Reload.startAnimation(spinAnimation);
					Activity1_HomeActivity.checkEUNIPageLoadComplete();
					showProgressOverlay();
					// 開始 500ms 檢查一次，直到 RefreshEUNI 變回 false
					new Thread(() -> {
						try {
							for (int timer = 0; timer <= 30000 && Activity1_HomeActivity.RefreshEUNI; timer += 50) {
								Thread.sleep(50);
								// Check the condition and post updates to the main thread
								int finalTimer = timer;
								handler.post(() -> {
									if (finalTimer == 30000 && Activity1_HomeActivity.RefreshEUNI) {
										showMessage(getResources().getString(R.string.TimeOut));
										showMessage(getResources().getString(R.string.try_it_later));
										hideProgressOverlay();
										CanPressReload = true;
									}
								});
							}
						} catch (Exception e) {
						}
						// Ensure the progress overlay is hidden when the loop ends
						handler.post(() -> {
							hideProgressOverlay();
							if (!Activity1_HomeActivity.RefreshEUNI) {
								RefreshCourse();
								showMessage(getResources().getString(R.string.EUNI_Course_Reload_Successful));
								CanPressReload = true;
							}
						});
					}).start();
				}

			}
		});

		// 初始化設置
		SP_EUNI_Course = getSharedPreferences("EUNI_course_data", Activity.MODE_PRIVATE);
		Web_M園區.getSettings().setJavaScriptEnabled(true);
		Web_M園區.getSettings().setBlockNetworkImage(true);
		Web_M園區.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				// 頁面加載完成
				if (url.contains("view.php?id=")) {
					// 僅在課程首頁爬取公告 ID
					String jsCode =
							"var links = document.querySelectorAll('.activityinstance a');" +
							"var pattern = /https:\\/\\/euni\\.niu\\.edu\\.tw\\/mod\\/forum\\/view\\.php\\?id=\\d+/;" +
							"for (var i = 0; i < links.length; i++) {" +
							"    if (pattern.test(links[i].href)) {" +
							"        links[i].href;" + // 返回第一個符合條件的 href
							"        break;" + // 停止遍歷，找到第一個符合條件的即可
							"    }" +
							"}";

					Web_M園區.evaluateJavascript(jsCode, new ValueCallback<String>() {
						@Override
						public void onReceiveValue(String value) {
							if (!value.isEmpty()) {
								// 移除開頭和結尾的雙引號，如果有的話
								value = value.replaceAll("^\"|\"$", "");
								// 處理完畢的 value 即為第一個符合條件的 href
								String AnnouncementID = value.split("id=")[1];
								SP_EUNI_Course.edit().putString("課程_" + Position + "_公告ID", AnnouncementID).apply();
								hideProgressOverlay();
								page.setClass(getApplicationContext(), Activity2_EUNI2.class);
								page.putExtra("Url_Final", "https://euni.niu.edu.tw/mod/forum/view.php?id="+AnnouncementID);
								page.putExtra("Title", CourseName + "-" + getResources().getString(R.string.EUNI_Sub_Item1));
								startActivity(page);
								overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
							} else {
								showMessage(getResources().getString(R.string.EUNI_GetAnnouncement_Failed));
							}
						}
					});

				}

			}
		});


		// M園區 網站Load
		String Euni_url = "https://euni.niu.edu.tw/my/";
		Web_M園區.loadUrl(Euni_url);

		// 事件
		RefreshCourse();
		
	}

	// 設標題
	private void setActionBarTitle(String title) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(title);
		}
	}
	
	private void initializeLogic() {
		// 防止 Euni 閒置登出
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



	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(refreshRunnable);
	}

	@Override
	public void onSubItemClick(String url, String position, String courseName) {
		Position = position;
		CourseName = courseName;
		showProgressOverlay();
		Web_M園區.loadUrl(url);
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

	// 重新整理課程
	private void RefreshCourse() {
		List<String> mainItems = new ArrayList<>(); // 用於添加 items
		Map<String, ?> allEntries = SP_EUNI_Course.getAll();

		int maxId = -1;
		Pattern pattern = Pattern.compile("課程_(\\d+)_ID");

		for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
			String key = entry.getKey();
			Matcher matcher = pattern.matcher(key);
			if (matcher.matches()) {
				int id = Integer.parseInt(matcher.group(1));
				if (id > maxId) {
					maxId = id;
				}
			}
		}

		for (int i = 0; i < maxId+1; i++) {
			mainItems.add("Main Item " + (i + 1));
		}

		EUNICourseAdapter adapter = new EUNICourseAdapter(this, mainItems, this);
		listView.setAdapter(adapter);
	}

	private void showMessage(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

}
