package com.niu.csie.edu.app;

import android.content.*;
import android.os.*;
import android.view.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.niu.csie.edu.app.ViewPagerAdapter.ViewPagerAdapter;


public class Activity5_Event_Registration extends AppCompatActivity {

	/**Element*/
	private Toolbar _toolbar;
	private TabLayout tabLayout;
	private ViewPager viewPager;

	/**Components*/
	private Intent page = new Intent();
	private ViewPagerAdapter viewPagerAdapter;


	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.activity5_event_registration);
		initialize(_savedInstanceState);
	}

	
	private void initialize(Bundle _savedInstanceState) {
		_toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		setActionBarTitle(getResources().getString(R.string.Event_Registration));
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
		viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				viewPagerAdapter.addFragment(Activity5_Event_Registration_Fragment1.getInstance(), getResources().getString(R.string.EventRegistration));
				viewPagerAdapter.addFragment(Activity5_Event_Registration_Fragment2.getInstance(), getResources().getString(R.string.EventRegistrationSuccess));
				viewPager.setAdapter(viewPagerAdapter);
				tabLayout.setupWithViewPager(viewPager);
			}
		});
	}


	// 重新整理
	public void refreshFragments() {
		if (viewPagerAdapter != null) {
			for (Fragment fragment : viewPagerAdapter.getFragments()) {
				if (fragment instanceof RefreshableFragment) {
					((RefreshableFragment) fragment).refresh();
				}
			}
			viewPagerAdapter.notifyDataSetChanged();
		}
	}

	public interface RefreshableFragment {
		void refresh();
	}

}
