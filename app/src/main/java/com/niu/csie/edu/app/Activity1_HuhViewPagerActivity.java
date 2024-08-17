package com.niu.csie.edu.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.niu.csie.edu.app.HuhItemViewPagerAdapter.HuhItemViewPagerAdapter;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;


public class Activity1_HuhViewPagerActivity extends AppCompatActivity {

	/**Element*/
	private Toolbar _toolbar;
	private ViewPager viewPager;
	private DotsIndicator dotsIndicator;

	/**Components*/


	/**Variable*/


	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.activity1_huhviewpager);
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
				onBackPressed();
			}
		});

		// 主畫面 UI
		viewPager = findViewById(R.id.ViewPager);
		dotsIndicator = findViewById(R.id.dots_indicator);

		// 初始化設置
		Intent Info = getIntent();
		String Title = Info.getStringExtra("title");
		int position = Info.getIntExtra("position", 0);
		// 設標題
		setActionBarTitle(Title);
		// 設置 PagerAdapter
		HuhItemViewPagerAdapter pagerAdapter = new HuhItemViewPagerAdapter(this, position);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(0); // 默認顯示第一頁
		// 將 DotsIndicator 與 ViewPager 連接
		dotsIndicator.setViewPager(viewPager);

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(0, 0);
	}


	// 設標題
	private void setActionBarTitle(String title) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(title);
		}
	}

}
