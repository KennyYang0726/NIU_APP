package com.niu.csie.edu.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.Manifest;
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import com.niu.csie.edu.app.Utils.FileUtil;



public class Activity0_LoginActivity extends AppCompatActivity {

	/**Element*/
	private EditText account, pwd;
	private ImageView SeePWD;
	private TextView account_warn, pwd_warn;
	private LinearLayout Anim_Linear, account_linear, pwd_linear;
	private WebView webView, webView_EUNI, webView_ACADE, webView_ccsys; // webView 嘗試登入Zuvio
	private Button button_login;
	private View ProgressOverlay;
	private TextView ProgressText;

	/**Components*/
	private SharedPreferences sharedPreferences; // 儲存登入資訊
	private AlertDialog locationAlertDialog;
	private AlertDialog notificationAlertDialog;
	private AlertDialog TimeOutWebViewDevDialog;
	private final Handler handler = new Handler(Looper.getMainLooper());
	private Runnable timeoutRunnable; // 登入超時檢測

	/**Variable*/
	private long startTime; // 登入超時檢測
	private boolean isTimerCancelled = false; // 登入超時檢測
	private boolean Password_State = false;
	private boolean PageFinished = false; // 避免重複載入
	private boolean EUNI_Login = false; // 登入true
	private boolean ACADE_Login = false; // 登入true
	private boolean GetACADEViewState = false; // 防止重複取得 重複 post
	private boolean GetACADERecaptcha = false; // 防止重複取得 重複 post
	private boolean Zuvio_Login = false; // 登入true
	private boolean CCSYS_Login = false; // 登入true
	private boolean quit = false;
	private String Login_Method = "";
	private String app_lastest_ver = "";
	private String app_ver = "";
	private String app_lastest_DLlink = "";
	private String DeviceABI = ""; // 裝置類型
	private boolean IsDirectDownload = false;
	private boolean IsFromPlayStore = false;
	private static final int LOCATION_PERMISSION_REQUEST_CODE = 19890604;
	private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 8964;


	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.activity0_login);
		initialize(_savedInstanceState);
		initializeLogic();
	}

	@Override
	public void onBackPressed() {
		if (!quit) { // 詢問退出程序
			showMessage(getResources().getString(R.string.back_again));
			new Timer(true).schedule(new TimerTask() {//啟動定時任務
				@Override
				public void run() {
					quit = false; // 重置退出標示
				}
			}, 2000); // 2秒後執行run()方法
			quit = true;
		} else { // 確認退出應用
			super.onBackPressed();
			finishAffinity();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 避免 memory leak，當 Activity 被銷毀時，移除所有的回調
		handler.removeCallbacks(timeoutRunnable);
	}


	@SuppressLint("SetJavaScriptEnabled")
	private void initialize(Bundle _savedInstanceState) {
		account = findViewById(R.id.account);
		pwd = findViewById(R.id.pwd);
		SeePWD = findViewById(R.id.show_pwd);
		account_warn = findViewById(R.id.empty0);
		pwd_warn = findViewById(R.id.empty1);

		webView = findViewById(R.id.web);
		webView_EUNI = findViewById(R.id.web_EUNI);
		webView_ACADE = findViewById(R.id.web_Acade);
		webView_ccsys = findViewById(R.id.web_ccsys);

		Anim_Linear = findViewById(R.id.Anim_linear);
		account_linear = findViewById(R.id.account_linear);
		pwd_linear = findViewById(R.id.pwd_linear);
		button_login = findViewById(R.id.button);
		ProgressOverlay = findViewById(R.id.progress_overlay);
		ProgressText = findViewById(R.id.loading_text);

		// 初始化設置
		account_warn.setVisibility(View.GONE);
		pwd_warn.setVisibility(View.GONE);
		account_linear.setBackgroundResource(R.drawable.input_field);
		pwd_linear.setBackgroundResource(R.drawable.input_field);
		sharedPreferences = getSharedPreferences("sp", Activity.MODE_PRIVATE);

		// Firebase 初始化
		FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
		firebaseAppCheck.installAppCheckProviderFactory(
				PlayIntegrityAppCheckProviderFactory.getInstance());

		FirebaseAuth mAuth = FirebaseAuth.getInstance();
		mAuth.signInAnonymously()
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							// 登入成功
							Log.d("TAG", "signInAnonymously:success");
							FirebaseUser user = mAuth.getCurrentUser();
							// 在這裡可以使用 user 物件
						} else {
							// 登入失敗
							Log.w("TAG", "signInAnonymously:failure", task.getException());
						}
					}
				});

		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference mDatabase = database.getReference();

		// WebView 初始化
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBlockNetworkImage(true);
		webView.getSettings().setDomStorageEnabled(true);
		String EUNI_UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"; // 電腦版網站才沒 bug
		webView_EUNI.getSettings().setUserAgentString(EUNI_UserAgent);
		webView_EUNI.getSettings().setJavaScriptEnabled(true);
		webView_EUNI.getSettings().setBlockNetworkImage(true);
		webView_EUNI.getSettings().setDomStorageEnabled(true);
		webView_ACADE.getSettings().setJavaScriptEnabled(true);
		webView_ACADE.getSettings().setBlockNetworkImage(true);
		webView_ACADE.getSettings().setDomStorageEnabled(true);
		webView_ccsys.getSettings().setJavaScriptEnabled(true);
		webView_ccsys.getSettings().setBlockNetworkImage(true);
		webView_ccsys.getSettings().setDomStorageEnabled(true);


		// 取得app資訊 (版本, 版本號)
		PackageInfo pinfo = null;
		try {
			pinfo = getPackageManager().getPackageInfo("com.niu.csie.edu.app", PackageManager.GET_ACTIVITIES);
			app_ver = pinfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}

		// 事件
		SeePWD.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Password_State = !Password_State;
				if (Password_State) {
					SeePWD.setImageResource(R.drawable.ic_visibility_grey);
					pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				} else {
					SeePWD.setImageResource(R.drawable.ic_visibility_off_grey);
					pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				}
				pwd.setSelection(pwd.getText().length());
			}
		});

		button_login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!account.getText().toString().equals("") && !pwd.getText().toString().equals("")) {
					// 樣式恢復原狀
					account_warn.setVisibility(View.GONE);
					account_linear.setBackgroundResource(R.drawable.input_field);
					pwd_warn.setVisibility(View.GONE);
					pwd_linear.setBackgroundResource(R.drawable.input_field);
					expand2(Anim_Linear);

					// 嘗試登入
					String Account = account.getText().toString();
					String PWD = pwd.getText().toString();
					if (Account.contains("@")) // 轉換，去除 @ 之後部分
						Account = Account.split("@")[0];
					Account = Account.toUpperCase(); // 強制轉大寫
					sharedPreferences.edit().putString("account", Account).apply();
					sharedPreferences.edit().putString("pwd", PWD).apply();

					// 登入
					Login_Method = "BTN";
					Login_Zuvio(Account + "@ms.niu.edu.tw", PWD);
					Login_ccsys(Account, PWD);

				} else {
					if (account.getText().toString().equals("")) {
						account_warn.setText(getResources().getString(R.string.school_num_hint));
						account_warn.setVisibility(View.VISIBLE);
						account_linear.setBackgroundResource(R.drawable.input_field_warn);
					} else {
						account_warn.setVisibility(View.GONE);
						account_linear.setBackgroundResource(R.drawable.input_field);
					}

					if (pwd.getText().toString().equals("")) {
						pwd_warn.setText(getResources().getString(R.string.pwd_hint));
						pwd_warn.setVisibility(View.VISIBLE);
						pwd_linear.setBackgroundResource(R.drawable.input_field_warn);
					} else {
						pwd_warn.setVisibility(View.GONE);
						pwd_linear.setBackgroundResource(R.drawable.input_field);
					}
					expand2(Anim_Linear);
				}

			}
		});


		// Firebase DB 取得學年度 存入 sp
		mDatabase.child("學年度").addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				Long academicYearLong = dataSnapshot.getValue(Long.class);
				if (academicYearLong != null) {
					String academicYearString = String.valueOf(academicYearLong);
					sharedPreferences.edit().putString("學年度", academicYearString).apply();
				}
			}
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		});

		// Firebase DB 取得學 app 版本資訊
		mDatabase.child("app").addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				app_lastest_ver = dataSnapshot.child("ver").getValue(String.class);
				app_lastest_DLlink = dataSnapshot.child("apk下載地址").getValue(String.class);
				IsDirectDownload = Boolean.TRUE.equals(dataSnapshot.child("apk直接下載").getValue(boolean.class));
				IsFromPlayStore = Boolean.TRUE.equals(dataSnapshot.child("Play商店").getValue(boolean.class));

			}
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		});


	}


	private void initializeLogic() {
		// 取得裝置 ABI
		checkDeviceArchitecture();
		// 刪除緩存
		WebStorage.getInstance().deleteAllData();
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookies(null); // 移除所有 Cookies
		cookieManager.flush();
		webView.clearCache(true);
		webView_ccsys.clearCache(true);
		webView_ACADE.clearCache(true);
		webView_EUNI.clearCache(true);
		webView.clearHistory();
		webView_ccsys.clearHistory();
		webView_ACADE.clearHistory();
		webView_EUNI.clearHistory();
		deleteDatabase("webview.db");
		deleteDatabase("webviewCache.db");

		// EUNI 網址先載入，節省時間
		webView_EUNI.loadUrl("https://euni.niu.edu.tw/");

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				// 使用 JavaScript 檢查網頁內容
				if (url.equals("https://irs.zuvio.com.tw/student5/setting/index")) {
					// 取得名字
					webView.evaluateJavascript("(function() { return document.querySelector('.s-i-b-b-r-user-name').innerText; })();", Name -> {
						sharedPreferences.edit().putString("Name", Name.replaceAll("\"", "")).apply(); //取出字為 "Name"
						// 姓名存於 sp 後 ， 跳轉至 Home ， 通常動畫也結束了
						if (Zuvio_Login && EUNI_Login && ACADE_Login && CCSYS_Login) {
							if (app_ver.equals(app_lastest_ver)) {

								// 中斷計時
								isTimerCancelled = true;
								handler.removeCallbacks(timeoutRunnable);

								Intent page = new Intent(Activity0_LoginActivity.this, Activity1_HomeActivity.class);
								startActivity(page);
								overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
							} else {
								Update_App();
							}

						}
					});
				}
				// 檢查登入
				webView.evaluateJavascript(
						"(function() { return document.body.innerText.includes('若您使用學校帳號登入'); })();",
						new ValueCallback<String>() {
							@Override
							public void onReceiveValue(String value) {
								if (Boolean.parseBoolean(value)) {
									// 帳密錯誤

									// 中斷計時
									isTimerCancelled = true;
									handler.removeCallbacks(timeoutRunnable);

									account_warn.setText(getResources().getString(R.string.wrong_hint));
									account_warn.setVisibility(View.VISIBLE);
									account_linear.setBackgroundResource(R.drawable.input_field_warn);
									pwd_warn.setText(getResources().getString(R.string.wrong_hint));
									pwd_warn.setVisibility(View.VISIBLE);
									pwd_linear.setBackgroundResource(R.drawable.input_field_warn);
									showMessage(getResources().getString(R.string.wrong_hint));
									hideProgressOverlay();
									expand2(Anim_Linear);
									Zuvio_Login = false;
									ACADE_Login = false;
									EUNI_Login = false;

								} else if (url.contains("irs.zuvio.com.tw/student5")) {
									// 學生
									if (!PageFinished) { // 避免重複載入
										PageFinished = true;
										Zuvio_Login = true;

										// 若是按鈕登入，需先確認 Zuvio 已成功登入才登 M園區 和 校務行政，雖然速度便慢，但可確認不被封鎖
										if (Objects.equals(Login_Method, "BTN")) {
											String account = sharedPreferences.getString("account", null);
											String pwd = sharedPreferences.getString("pwd", null);
											Login_EUNI(account, pwd);
											Login_Acade(account, pwd);
										}

										handler.postDelayed(new Runnable() {
											@Override
											public void run() {
												Log.d("登入狀態", "Zuvio："+Zuvio_Login+"\nEUNI："+EUNI_Login+"\nACADE："+ACADE_Login+"\nCCSYS："+CCSYS_Login);
												if (Zuvio_Login && EUNI_Login && ACADE_Login && CCSYS_Login) {
													webView.loadUrl("https://irs.zuvio.com.tw/student5/setting/index");
													collapse(Anim_Linear);
													hideProgressOverlay();
													showMessage(getResources().getString(R.string.login_success));
												} else {
													// 狀態為 false，繼續每 100 毫秒檢查一次
													handler.postDelayed(this, 100);
												}
											}
										}, 100);

									}
								} else {
									Zuvio_Login = false;
									ACADE_Login = false;
									EUNI_Login = false;
									showMessage(getResources().getString(R.string.login_failed));
								}
							}


						});

			}
			/*
			@Override
			public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
				super.onReceivedError(view, request, error);
				// 處理網路錯誤
				if (!PageFinished)
					showMessage(getResources().getString(R.string.Net_ERR));
			}*/
		});

		expand(Anim_Linear); // 展開Linear

		// 若有 sp，自動登入
		if (sharedPreferences.contains("account") && sharedPreferences.contains("pwd")) {
			String account = sharedPreferences.getString("account", null);
			String pwd = sharedPreferences.getString("pwd", null);
			// 登入
			Login_Method = "SP";
			Login_Zuvio(account + "@ms.niu.edu.tw", pwd);
			Login_EUNI(account, pwd);
			Login_Acade(account, pwd);
			Login_ccsys(account, pwd);
		}


		// 要求定位權限
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
			if (!FileUtil.isExistFile("/data/user/0/com.niu.csie.edu.app/Don't Ask Again Location")) {
				RequestLocationPermissionDialog();
			}

		}

		// 安卓13以上要求通知權限
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
				if (!FileUtil.isExistFile("/data/user/0/com.niu.csie.edu.app/Don't Ask Again Notification")) {
					ShowNotificationDialog();
				} // 若不再詢問，便不再要求
			}
		}


		FirebaseMessaging.getInstance().getToken()
				.addOnCompleteListener(new OnCompleteListener<String>() {
					@Override
					public void onComplete(@NonNull Task<String> task) {
						if (!task.isSuccessful()) {
							return;
						}
					}
				});


	}

	// 權限檢查
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
			// 先關閉 Dialog
			if (locationAlertDialog != null)
				locationAlertDialog.dismiss();
			if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
					// 使用者拒絕了權限但沒有選擇 "不再提醒"，重新請求權限
					RequestLocationPermissionDialog();
				} else {
					// 使用者選擇了 "不再提醒"，顯示說明對話框，引導到設置頁面，並寫入檔案
					FileUtil.writeFile("/data/user/0/com.niu.csie.edu.app/Don't Ask Again Location", "");
				}
			}
		} else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
			// 安卓13以上要求通知權限
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				if (notificationAlertDialog != null)
					notificationAlertDialog.dismiss();
				if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
					if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
						// 使用者拒絕了權限但沒有選擇 "不再提醒"，重新請求權限
						ShowNotificationDialog();
					} else {
						// 使用者選擇了 "不再提醒"，顯示說明對話框，引導到設置頁面，並寫入檔案
						// 這邊直接省略，不強制要求通知權限
						FileUtil.writeFile("/data/user/0/com.niu.csie.edu.app/Don't Ask Again Notification", "");
					}
				}
			}
		}
	}


	// 須要定位權限
	private void RequestLocationPermissionDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Activity0_LoginActivity.this, R.style.AlertDialogTheme);
		View view = LayoutInflater.from(Activity0_LoginActivity.this).inflate(
				R.layout.custom_alert_dialog2,
				findViewById(R.id.layoutDialogContainer)
		);
		builder.setView(view);
		((TextView) view.findViewById(R.id.textTitle)).setText(getResources().getString(R.string.Location_Title));
		((TextView) view.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.Location_Content));
		((Button) view.findViewById(R.id.buttonAction_Cancel)).setText(getResources().getString(R.string.Dialog_Cancel2));
		((Button) view.findViewById(R.id.buttonAction)).setText(getResources().getString(R.string.Dialog_OK));
		((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_location_white);

		builder.setCancelable(false);
		locationAlertDialog = builder.create();

		view.findViewById(R.id.buttonAction_Cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showMessage(getResources().getString(R.string.QAQ));
				locationAlertDialog.dismiss();
			}
		});
		view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ActivityCompat.requestPermissions(Activity0_LoginActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
			}
		});

		if (locationAlertDialog.getWindow() != null){
			locationAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		}
		locationAlertDialog.show();
	}

	// 顯示需要通知權限
	private void ShowNotificationDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Activity0_LoginActivity.this, R.style.AlertDialogTheme);
		View view = LayoutInflater.from(Activity0_LoginActivity.this).inflate(
				R.layout.custom_alert_dialog2,
				findViewById(R.id.layoutDialogContainer)
		);
		builder.setView(view);
		((TextView) view.findViewById(R.id.textTitle)).setText(getResources().getString(R.string.Notification_Title));
		((TextView) view.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.Notification_Content));
		((Button) view.findViewById(R.id.buttonAction_Cancel)).setText(getResources().getString(R.string.Dialog_Cancel2));
		((Button) view.findViewById(R.id.buttonAction)).setText(getResources().getString(R.string.Dialog_OK));
		((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_notifications_none_white);

		builder.setCancelable(false);
		notificationAlertDialog = builder.create();

		view.findViewById(R.id.buttonAction_Cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showMessage(getResources().getString(R.string.QAQ));
				notificationAlertDialog.dismiss();
			}
		});
		view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
			@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
			@Override
			public void onClick(View view) {
				ActivityCompat.requestPermissions(Activity0_LoginActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
			}
		});

		if (notificationAlertDialog.getWindow() != null){
			notificationAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		}
		notificationAlertDialog.show();
	}


	// 展開動畫
	private void expand(final View v) {
		v.setVisibility(View.VISIBLE);
		final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		v.measure(widthSpec, heightSpec);
		ValueAnimator mAnimator = slideAnimator(0, v.getMeasuredHeight(), v);
		mAnimator.setDuration(700); //持續時間
		mAnimator.start();
	}

	// Visible展開動畫
	@SuppressLint("Range")
	private void expand2(final View v) {
		v.measure(View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, View.MeasureSpec.EXACTLY),
				View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		final int initialHeight = v.getHeight();
		final int targetHeight = v.getMeasuredHeight();
		ValueAnimator animator = ValueAnimator.ofInt(initialHeight, targetHeight);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(@NonNull ValueAnimator animation) {
				int value = (Integer) animation.getAnimatedValue();
				ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
				layoutParams.height = value;
				v.setLayoutParams(layoutParams);
			}
		});
		animator.setDuration(487);
		animator.start();
	}

	// 收合動畫
	private void collapse(final View v) {
		int finalHeight = v.getHeight();
		ValueAnimator mAnimator = slideAnimator(finalHeight, 0, v);
		mAnimator.setDuration(700); //持續時間
		mAnimator.addListener(new android.animation.Animator.AnimatorListener() {
			@Override
			public void onAnimationEnd(@NonNull android.animation.Animator animator) {
				v.setVisibility(View.GONE);
			}
			@Override
			public void onAnimationStart(@NonNull android.animation.Animator animator) {
			}
			@Override
			public void onAnimationCancel(@NonNull android.animation.Animator animator) {
			}
			@Override
			public void onAnimationRepeat(@NonNull android.animation.Animator animator) {
			}
		});
		mAnimator.start();
	}

	private ValueAnimator slideAnimator(int start, int end, final View v) {
		ValueAnimator animator = ValueAnimator.ofInt(start, end);
		animator.setDuration(300);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
				int value = (Integer) valueAnimator.getAnimatedValue();
				ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
				layoutParams.height = value;
				v.setLayoutParams(layoutParams);
			}
		});
		return animator;
	}

	// 顯示等待中
	private void showProgressOverlay() {
		if (ProgressOverlay != null) {
			ProgressOverlay.setVisibility(View.VISIBLE);
			// 旋轉動畫
			ImageView loadingImage = ProgressOverlay.findViewById(R.id.loading_image);
			Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.prog_rotate_icon);
			loadingImage.startAnimation(rotateAnimation);
		}
	}

	// 隱藏等待中
	private void hideProgressOverlay() {
		if (ProgressOverlay != null && ProgressOverlay.getVisibility() == View.VISIBLE) {
			ProgressOverlay.setVisibility(View.GONE);
		}
	}

	// 處理超時，建議更換 WebView Dev Dialog
	private void HandleTimeOutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Activity0_LoginActivity.this, R.style.AlertDialogTheme);
		View view = LayoutInflater.from(Activity0_LoginActivity.this).inflate(
				R.layout.custom_alert_dialog2,
				findViewById(R.id.layoutDialogContainer)
		);
		builder.setView(view);
		((TextView) view.findViewById(R.id.textTitle)).setText(getResources().getString(R.string.WebViewDev_Title));
		((TextView) view.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.WebViewDev_Message));
		((Button) view.findViewById(R.id.buttonAction_Cancel)).setText(getResources().getString(R.string.WebViewDev_Btn1));
		((Button) view.findViewById(R.id.buttonAction)).setText(getResources().getString(R.string.WebViewDev_Btn2));

		builder.setCancelable(true);
		TimeOutWebViewDevDialog = builder.create();

		view.findViewById(R.id.buttonAction_Cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				TimeOutWebViewDevDialog.dismiss();
				Intent playstore = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.webview.beta"));
				startActivity(playstore);
			}
		});
		view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				TimeOutWebViewDevDialog.dismiss();
				Intent playstore = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.webview"));
				startActivity(playstore);
			}
		});

		if (TimeOutWebViewDevDialog.getWindow() != null){
			TimeOutWebViewDevDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		}
		TimeOutWebViewDevDialog.show();
	}


	// 登入 Zuvio 方法
	private void Login_Zuvio(String account, String pwd) {
		// 改變請稍等字樣
		ProgressText.setText(getResources().getString(R.string.loginING));
		// 顯示請稍等
		showProgressOverlay();

		// 記錄開始時間
		startTime = System.currentTimeMillis();
		isTimerCancelled = false;  // Reset flag when starting timer
		// 設定計時 60 秒後 未登入完成 算超時
		timeoutRunnable = new Runnable() {
			@Override
			public void run() {
				if (!isTimerCancelled) {
					HandleTimeOutDialog();
				}
			}
		};
		handler.postDelayed(timeoutRunnable, 60000);

		String formData;
		try {
			formData = "email=" + URLEncoder.encode(account, "UTF-8") +
					"&password=" + URLEncoder.encode(pwd, "UTF-8") +
					"&current_language=zh-TW";
			formData = formData.replace("%22", ""); // 編碼處理產生了%22("導致，可事前replace，或這裡再replace(若沒有可能導致Post異常的內文雙引號))

			byte[] postData = formData.getBytes(StandardCharsets.UTF_8);
			// 發送POST
			webView.postUrl("https://irs.zuvio.com.tw/irs/submitLogin", postData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	// 登入 M園區 方法，先確保取得到 token
	private void Login_EUNI(String account, String pwd) {
		webView_EUNI.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				view.loadUrl(request.getUrl().toString());
				return true; // 表示我們已經處理這個 URL，不需要再外部處理，eg: 跳轉外部瀏覽器
			}
		});
		if (!Login_Method.equals("BTN")) {
			webView_EUNI.setWebChromeClient(new WebChromeClient() {
				@Override
				public void onProgressChanged(WebView view, int newProgress) {
					super.onProgressChanged(view, newProgress);
					if (newProgress == 100) {
						// 頁面加載完成
						webView_EUNI.evaluateJavascript("document.querySelector('[name=\"logintoken\"]').value", token -> {
							token = token.replaceAll("\"", "");
							handleTokenResponse_EUNI(token, account, pwd);
						});
					}
				}
			});
		} else {
			// 若是 BTN ，頁面早就加載完，加入 PageFinish 反而無法觸發
			// 頁面加載完成
			webView_EUNI.evaluateJavascript("document.querySelector('[name=\"logintoken\"]').value", token -> {
				token = token.replaceAll("\"", "");
				handleTokenResponse_EUNI(token, account, pwd);
			});
		}

	}

	private void handleTokenResponse_EUNI(String token, String account, String pwd) {
		String js = "javascript:document.querySelector('[name=\"username\"]').value = '" + account + "';" +
				"document.querySelector('[name=\"password\"]').value = '" + pwd + "';" +
				"document.querySelector('.btn-login').click();";
		webView_EUNI.evaluateJavascript(js, state -> {
			handleLogin_EUNI();
		});
	}

	private void handleLogin_EUNI() {
		webView_EUNI.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				view.loadUrl(request.getUrl().toString());
				return true; // 表示我們已經處理這個 URL，不需要再外部處理，eg: 跳轉外部瀏覽器
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (!url.equals("https://euni.niu.edu.tw/my/")) { // 正常會跳至 https://euni.niu.edu.tw/my/
					// 處理 M園區 您已經以 XXX 身分登入，如果您要用不同身分登入前，需要先登出
					webView_EUNI.evaluateJavascript("document.querySelector('.btn.btn-secondary').click();", state -> {
						EUNI_Login = true;
					});
				} else {
					EUNI_Login = true;
				}
			}
		});
	}


	// 登入 校務行政系統 方法
	private void Login_Acade(String account, String pwd) {
		webView_ACADE.loadUrl("https://acade.niu.edu.tw/NIU/Default.aspx");
		webView_ACADE.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				view.loadUrl(request.getUrl().toString());
				return true; // 表示我們已經處理這個 URL，不需要再外部處理，eg: 跳轉外部瀏覽器
			}
		});
		webView_ACADE.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				// Log.d("WebViewwwww", "Loading progress: " + newProgress + "%");
				if (newProgress == 100) {
					// 頁面加載完成
					webView_ACADE.evaluateJavascript("document.querySelector('#__VIEWSTATE').value", viewState -> {
						webView_ACADE.evaluateJavascript("document.querySelector('#__VIEWSTATEGENERATOR').value", viewStateGenerator -> {
							webView_ACADE.evaluateJavascript("document.querySelector('#__EVENTVALIDATION').value", eventValidation -> {
								if (!viewState.equals("null") && !GetACADEViewState) {
									GetACADEViewState = true;
									waitForRecaptchaResponse(viewState, viewStateGenerator, eventValidation, account, pwd);
								}
							});
						});
					});
				}
			}
		});
	}
	private void waitForRecaptchaResponse(String viewState, String viewStateGenerator, String eventValidation, String account, String pwd) {
		// 等待 Recaptcha，每 50ms 檢查一次，直到有值後，才發送post
		Handler handler = new Handler(Looper.getMainLooper());
		final String[] RecaptchaResponse = {""};
		new Thread(() -> {
			AtomicBoolean responseReceived = new AtomicBoolean(false); // 防止取得 Recaptcha 後繼續在 loop 裡面
			try {
				for (int timer = 0; timer <= 30000 && !responseReceived.get(); timer += 50) {
					Thread.sleep(50);
					int finalTimer = timer;
					if (responseReceived.get()) {
						break; // Ensure we break out of the loop immediately if the response is received
					}
					handler.post(() -> {
						if (responseReceived.get()) {
							return; // Ensure we don't execute the JS if the response is already received
						}
						webView_ACADE.evaluateJavascript("document.querySelector('#recaptchaResponse').value", recaptchaResponse -> {
							if (!(recaptchaResponse.length() < 10) && !GetACADERecaptcha) {
								// 不能判斷 recaptchaResponse.isEmpty()，因為有長度為 2 的非可見字串
								responseReceived.set(true);
								GetACADERecaptcha = true;
								RecaptchaResponse[0] = recaptchaResponse;
								handleRecaptchaResponse(viewState, viewStateGenerator, eventValidation, account, pwd, RecaptchaResponse[0], handler);
							} else if (finalTimer == 30000 && !ACADE_Login) {
								showMessage(getResources().getString(R.string.TimeOut));
							}
						});
					});
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private void handleRecaptchaResponse(String viewState, String viewStateGenerator, String eventValidation, String account, String pwd, String recaptchaResponse, Handler handler) {
		String formData;

		try {
			formData = "ScriptManager1=AjaxPanel%7CLGOIN_BTN" +
					"&__EVENTTARGET=" +
					"&__EVENTARGUMENT=" +
					"&__VIEWSTATE=" + URLEncoder.encode(viewState, "UTF-8") +
					"&__VIEWSTATEGENERATOR=" + URLEncoder.encode(viewStateGenerator, "UTF-8") +
					"&__VIEWSTATEENCRYPTED=" +
					"&__EVENTVALIDATION=" + URLEncoder.encode(eventValidation, "UTF-8") +
					"&M_PORTAL_LOGIN_ACNT=" + URLEncoder.encode(account, "UTF-8") +
					"&M_PW=" + URLEncoder.encode(pwd, "UTF-8") +
					"&recaptchaResponse=" + URLEncoder.encode(recaptchaResponse, "UTF-8") +
					"&__ASYNCPOST=true" +
					"&LGOIN_BTN.x=91" +
					"&LGOIN_BTN.y=13";
			formData = formData.replace("%22", ""); // 編碼處理產生了%22("導致，可事前replace，或這裡再replace(若沒有可能導致Post異常的內文雙引號))

			// FileUtil.makeDir(FileUtil.getPackageDataDir(this));
			// FileUtil.writeFile(FileUtil.getPackageDataDir(this) + "/LoginInfo.ini", formData);

			byte[] postData = formData.getBytes(StandardCharsets.UTF_8);
			handler.post(() -> {
				webView_ACADE.postUrl("https://acade.niu.edu.tw/NIU/Default.aspx", postData);
				ACADE_Login = true;
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 登入 宜大活動 方法
	private void Login_ccsys(String account, String pwd) {
		webView_ccsys.loadUrl("https://ccsys.niu.edu.tw/MvcTeam/Account/Login?ReturnUrl=~%2FAct");
		webView_ccsys.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				view.loadUrl(request.getUrl().toString());
				return true; // 表示我們已經處理這個 URL，不需要再外部處理，eg: 跳轉外部瀏覽器
			}
		});
		webView_ccsys.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				if (newProgress == 100) {
					// 頁面加載完成
					webView_ccsys.evaluateJavascript("document.querySelector('[name=\"__RequestVerificationToken\"]').value", token -> {
						token = token.replaceAll("\"", "");
						handleTokenResponse_CCSYS(token, account, pwd);
					});
				}
			}
		});
	}

	private void handleTokenResponse_CCSYS(String token, String account, String pwd) {
		String formData;

		try {
			formData = "__RequestVerificationToken=" + URLEncoder.encode(token, "UTF-8") +
					"&Account=" + URLEncoder.encode(account, "UTF-8") +
					"&Password=" + URLEncoder.encode(pwd, "UTF-8");
			formData = formData.replace("%22", ""); // 編碼處理產生了%22(不應該出現)

			byte[] postData = formData.getBytes(StandardCharsets.UTF_8);
			handler.post(() -> {
				webView_ccsys.postUrl("https://ccsys.niu.edu.tw/MvcTeam/Account/Login?ReturnUrl=~%2FAct", postData);
				CCSYS_Login = true;
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 取得裝置類型
	private void checkDeviceArchitecture() {
		String[] supportedAbis = Build.SUPPORTED_ABIS;
		if (supportedAbis.length > 0) {
			String primaryAbi = supportedAbis[0];

			switch (primaryAbi) {
				case "armeabi-v7a":
					Log.d("DeviceArchitecture", "Device architecture is ARM (32-bit).");
					DeviceABI = primaryAbi;
					break;
				case "arm64-v8a":
					Log.d("DeviceArchitecture", "Device architecture is ARM (64-bit).");
					DeviceABI = primaryAbi;
					break;
				case "x86":
					Log.d("DeviceArchitecture", "Device architecture is x86.");
					DeviceABI = primaryAbi;
					break;
				case "x86_64":
					Log.d("DeviceArchitecture", "Device architecture is x86_64.");
					DeviceABI = primaryAbi;
					break;
				default:
					Log.d("DeviceArchitecture", "other device architecture.");
					DeviceABI = "all";
					break;
			}
		} else {
			Log.d("DeviceArchitecture", "No supported ABIs found.");
			DeviceABI = "all";
		}
	}


	// 更新 app
	private void Update_App() {
		final AlertDialog db = new AlertDialog.Builder(Activity0_LoginActivity.this).create();
		LayoutInflater inflater = getLayoutInflater();
		View convertView = inflater.inflate(R.layout.app_update, null);
		db.setView(convertView);
		db.requestWindowFeature(Window.FEATURE_NO_TITLE);
		db.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
		LinearLayout over_ly01 = convertView.findViewById(R.id.over_ly01);
		LinearLayout main_bgd1 = convertView.findViewById(R.id.main_bgd1);
		TextView ttext01 = convertView.findViewById(R.id.ttext01);
		TextView ttext03 = convertView.findViewById(R.id.ttext03);
		ttext03.setText(getResources().getString(R.string.App_Update_Ver) + app_lastest_ver);
		Button bt01 = convertView.findViewById(R.id.bt01);
		Button bt02 = convertView.findViewById(R.id.bt02);

		if (!IsFromPlayStore) {
			if (FileUtil.isExistFile("/storage/emulated/0/Android/data/com.niu.csie.edu.app/files/" + app_lastest_ver + ".apk")){
				bt02.setText(getResources().getString(R.string.App_Update_Btn1_Install));
			} else {
				bt02.setText(getResources().getString(R.string.App_Update_Btn1_Download));
			}
		}

		db.setCancelable(false);
		ObjectAnimator anim = ObjectAnimator.ofFloat(ttext01, "Alpha", 0, 1);
		anim.setDuration(8000);
		anim.start();
		ObjectAnimator anim2 = ObjectAnimator.ofFloat(bt01, "Alpha", 0, 1);
		anim2.setDuration(2500);
		anim2.start();
		ObjectAnimator anim3 = ObjectAnimator.ofFloat(bt02, "Alpha", 0, 1);
		anim3.setDuration(2500);
		anim3.start();
		ObjectAnimator anim4 = ObjectAnimator.ofFloat(over_ly01, "ScaleY", 0, 1);
		anim.setInterpolator(new BounceInterpolator());
		anim4.setDuration(500);
		anim4.start();
		ObjectAnimator anim5 = ObjectAnimator.ofFloat(ttext03, "Alpha", 0, 1);
		anim5.setDuration(7000);
		anim5.start();
		bt01.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				// finishAffinity();
				Intent page = new Intent(Activity0_LoginActivity.this, Activity1_HomeActivity.class);
				startActivity(page);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		bt02.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				// 下載更新或安裝
				if (IsFromPlayStore) {
					// 下載 play商店 更新 app
					Intent playstore = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.niu.csie.edu.app"));
					startActivity(playstore);
				} else {
					if (IsDirectDownload) {
						// 直接下載 ( 須要敏感權限 REQUEST_INSTALL_PACKAGES )
						if(FileUtil.isExistFile("/storage/emulated/0/Android/data/com.niu.csie.edu.app/files/" + app_lastest_ver + ".apk")){
							// 安裝更新
							// install_package(app_lastest_ver + ".apk");
						} else {
							// 下載更新
							// DownloadAndInstall(app_lastest_DLlink+app_lastest_ver+"-"+DeviceABI+".apk", "/storage/emulated/0/Android/data/com.niu.csie.edu.app/files/", app_lastest_ver + ".apk");
						}
					} else {
						// 跳轉 apk release github 頁面
						Intent GithubPage = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/KennyYang0726/NIU-app/releases"));
						startActivity(GithubPage);
					}

				}

			}
		});
		Shape(20, 20, 20, 20, R.color.app_update_btn, 5, R.color.black, 5, bt01, getApplicationContext());
		Shape(20, 20, 20, 20, R.color.app_update_btn, 5, R.color.black, 5, bt02, getApplicationContext());
		db.show();
	}

	private void Shape(final double _t1, final double _t2, final double _b1, final double _b2, final int _BackgroundRes, final double _Stroke, final int _strokeRes, final double _Elevation, final View _view, Context context) {
		GradientDrawable gs = new GradientDrawable();
		int backgroundColor = ContextCompat.getColor(context, _BackgroundRes);
		int strokeColor = ContextCompat.getColor(context, _strokeRes);
		gs.setColor(backgroundColor);
		gs.setStroke((int)_Stroke, strokeColor);
		gs.setCornerRadii(new float[]{(int)_t1, (int)_t1, (int)_t2, (int)_t2, (int)_b1, (int)_b1, (int)_b2, (int)_b2});
		_view.setBackground(gs);
		_view.setElevation((int)_Elevation);
	}


	/*
	// 安裝更新
	public void install_package(String apk_name) {
		try {
			Uri uri = androidx.core.content.FileProvider.getUriForFile(getApplicationContext(), Activity0_LoginActivity.this.getPackageName() + ".provider", new java.io.File("/storage/emulated/0/Android/data/com.niu.csie.edu.app/files/" + apk_name));
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(uri, "application/vnd.android.package-archive");
			startActivity(intent);
		} catch (Exception rr) {
			showMessage (rr.toString());
		}
	}

	// 下載安裝更新
	private void DownloadAndInstall(String Url, String Path, String apk_name){
		FileUtil.makeDir(FileUtil.getPackageDataDir(getApplicationContext()));
		final ProgressDialog prog = new ProgressDialog(this);
		prog.setIcon(R.drawable.ic_launcher_round);
		prog.setMax(100);
		prog.setIndeterminate(true);
		prog.setCancelable(false);
		prog.setCanceledOnTouchOutside(false);
		prog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		prog.getWindow().setBackgroundDrawableResource(R.drawable.dialog);
		Runnable updatethread = new Runnable() {
			public void run() {
				try {
					android.net.ConnectivityManager connMgr = (android.net.ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
					android.net.NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
					Looper.prepare();
					if (networkInfo != null && networkInfo.isConnected()) {
						URL url = new URL(Url);
						HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
						long completeFileSize = httpConnection.getContentLength();
						String filename;
						filename = apk_name;
						java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());
						java.io.FileOutputStream fos = new java.io.FileOutputStream(Path + filename);
						java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
						byte[] data = new byte[1024];
						long downloadedFileSize = 0;
						int x = 0;
						while ((x = in.read(data, 0, 1024)) >= 0) {
							downloadedFileSize += x;
							// calculate progress
							final int currentProgress = (int) ((((double)downloadedFileSize) / ((double)completeFileSize)) * 100000d);
							// update progress bar
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									prog.setTitle(getResources().getString(R.string.Download_Title2));
									prog.setMessage(getResources().getString(R.string.Download_Prog) + (currentProgress/1000) + "%");
									prog.show();
								}
							});
							bout.write(data, 0, x);
						}
						bout.close();
						in.close();
						prog.dismiss();
						install_package(apk_name);
					} else {
						showMessage(getResources().getString(R.string.Net_ERR));
					}
					Looper.loop();
				} catch (FileNotFoundException e) {
					showMessage(e.getMessage());
				} catch (IOException e) {
					showMessage(e.getMessage());
				}
			}
		};
		new Thread(updatethread).start();
	}
	*/

	private void showMessage(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

}
