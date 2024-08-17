package com.niu.csie.edu.app;

import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.niu.csie.edu.app.ViewPagerAdapter.ViewPagerAdapter;


public class Activity3_Score_Inquiry extends AppCompatActivity {

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
		setContentView(R.layout.activity3_score_inquiry);
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
		setActionBarTitle(getResources().getString(R.string.Score_Inquiry));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
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
	
	private void initializeLogic() {

	}

	private void getTabs() {
		ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				viewPagerAdapter.addFragment(Activity3_Score_Inquiry_Fragment1.getInstance(), getResources().getString(R.string.Mid_term_results));
				viewPagerAdapter.addFragment(Activity3_Score_Inquiry_Fragment2.getInstance(), getResources().getString(R.string.Final_term_results));
				// 期中預警先暫時不做，看實用性再決定
				//viewPagerAdapter.addFragment(Activity3_Score_Inquiry_Fragment3.getInstance(), getResources().getString(R.string.Mid_term_warning));
				viewPager.setAdapter(viewPagerAdapter);
				tabLayout.setupWithViewPager(viewPager);
			}
		});

	}


	private void showMessage(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

}
