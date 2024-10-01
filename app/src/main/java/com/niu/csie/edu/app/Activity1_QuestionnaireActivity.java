package com.niu.csie.edu.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.niu.csie.edu.app.Services.NotificationPost;

import java.util.Objects;


public class Activity1_QuestionnaireActivity extends AppCompatActivity {

	/**Element*/
	private Toolbar _toolbar;
	private Button btn;
	private WebView web;

	/**Components*/
	private SharedPreferences sharedPreferences; // 儲存登入資訊
	private FirebaseDatabase database = FirebaseDatabase.getInstance();
	private DatabaseReference usersRef = database.getReference("users");
	private Intent page = new Intent();


	/**Variable*/
	private boolean start = false;
	private boolean Finished = false;


	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.activity1_questionnaire);
		initialize(_savedInstanceState);
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void initialize(Bundle _savedInstanceState) {
		_toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		setActionBarTitle(getResources().getString(R.string.Satisfaction_Survey));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _v) {
				if (Finished) { // 若完成表單，這樣才能觸發刷新成就牆
					page.setClass(getApplicationContext(), Activity1_HomeActivity.class);
					startActivity(page);
					finish();
					overridePendingTransition(0, 0);
				} else {
					finish();
					overridePendingTransition(0, 0);
				}
			}
		});

		// 主畫面 UI
		btn = findViewById(R.id.button);
		web = findViewById(R.id.web);

		// 初始化設置
		sharedPreferences = getSharedPreferences("sp", Activity.MODE_PRIVATE);
		web.getSettings().setJavaScriptEnabled(true);
		web.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				String javascript = "javascript:(function() { " +
									"  return document.body.innerText.includes('我們已經收到您回覆的表單'); " +
									"})()";
				web.evaluateJavascript(javascript, new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						// value 是 JavaScript 返回的結果，它會是 "true" 或 "false"
						if ("true".equals(value)) {
							// 字串存在於頁面中，完成表單
							if (!Finished) {
								Finished = true;
								start = false;
								AchievementsGet();
							}

						}
					}
				});
			}
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				view.loadUrl(request.getUrl().toString());
				return true; // 表示我們已經處理這個 URL，不需要再外部處理，eg: 跳轉外部瀏覽器
			}
		});



		// 網站 load
		web.loadUrl("https://forms.gle/oCdW3GfZddjrDNDq8");


		// 事件
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				web.setVisibility(View.VISIBLE);
				start = true;
			}
		});

		if (!start) {
			web.setVisibility(View.GONE);
		}

	}

	@Override
	public void onBackPressed() {
		if (start) {
			start = false;
			web.setVisibility(View.GONE);
		} else {
			super.onBackPressed();
			if (Finished) { // 若完成表單，這樣才能觸發刷新成就牆
				page.setClass(getApplicationContext(), Activity1_HomeActivity.class);
				startActivity(page);
				finish();
				overridePendingTransition(0, 0);
			} else {
				finish();
				overridePendingTransition(0, 0);
			}
		}
	}

	// 設標題
	private void setActionBarTitle(String title) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(title);
		}
	}


	private void AchievementsGet() {

		DatabaseReference achievementRef = usersRef.child(Objects.requireNonNull(sharedPreferences.getString("account", null))).child("Achievements").child("02");
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
							NotificationPost notificationPost = new NotificationPost(Activity1_QuestionnaireActivity.this);
							notificationPost.sendNotification(getResources().getString(R.string.Achievements_Get), getResources().getString(R.string.Achievements_02_Title));
						}
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

	public void WebView_Help(View view) {
		Intent page = new Intent(getApplicationContext(), Activity_WebviewProvider.class);
		startActivity(page);
	}


}
