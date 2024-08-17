package com.niu.csie.edu.app;

import android.content.*;
import android.os.*;
import android.view.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.niu.csie.edu.app.ViewPagerAdapter.ViewPagerAdapter;


public class Activity6_Contact_Us extends AppCompatActivity {

	/**Element*/
	private Toolbar _toolbar;
	private TabLayout tabLayout;
	private ViewPager viewPager;

	/**Components*/
	private Intent page = new Intent();

	/**Variable*/


	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.activity6_contact_us);
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
		setActionBarTitle(getResources().getString(R.string.Contact_Us));
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
		tabLayout = findViewById(R.id.TabLayout);
		viewPager = findViewById(R.id.ViewPager);

		// 初始化設置
		getTabs();

	}

	// 設標題
	private void setActionBarTitle(String title) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(title);
		}
	}

	private void getTabs() {
		ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				viewPagerAdapter.addFragment(Activity6_Contact_Us_Fragment1.getInstance(), getResources().getString(R.string.Feedback));
				viewPagerAdapter.addFragment(Activity6_Contact_Us_Fragment2.getInstance(), getResources().getString(R.string.Bug_Report));
				viewPager.setAdapter(viewPagerAdapter);
				tabLayout.setupWithViewPager(viewPager);
			}
		});

	}

}
