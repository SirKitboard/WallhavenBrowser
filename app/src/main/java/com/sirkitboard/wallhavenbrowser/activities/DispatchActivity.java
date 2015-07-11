package com.sirkitboard.wallhavenbrowser.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.sirkitboard.wallhavenbrowser.R;
import com.sirkitboard.wallhavenbrowser.util.DispatchFragmentPagerAdapter;
import com.sirkitboard.wallhavenbrowser.util.URLBuilder;


public class DispatchActivity extends AppCompatActivity {
	TabLayout tabLayout;
	SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dispatch);
		getSupportActionBar().setElevation(0);
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		DispatchFragmentPagerAdapter pagerAdapter =
				new DispatchFragmentPagerAdapter(getSupportFragmentManager(), DispatchActivity.this);
		viewPager.setAdapter(pagerAdapter);

		mSharedPreferences = getSharedPreferences("com.sirkitboard.wallhaven", MODE_PRIVATE);

		// Give the TabLayout the ViewPager
		tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
		tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
		tabLayout.setupWithViewPager(viewPager);

		for (int i = 0; i < tabLayout.getTabCount(); i++) {
			TabLayout.Tab tab = tabLayout.getTabAt(i);
			tab.setCustomView(pagerAdapter.getTabView(i));
		}
	}

	public void setPurity(View v) {
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		Switch sfwSwitch = (Switch) findViewById(R.id.sfwSwitch);
		Switch sketchyToggle = (Switch) findViewById(R.id.sketchySwitch);
		String purity = "";
		purity+=sfwSwitch.isChecked() ? "1" : "0";
		purity+=sketchyToggle.isChecked() ? "1" : "0";
		editor.putString("purity", purity + "0");
		editor.apply();
	}

	public void search(View v) {
		TextView textView = (TextView)(findViewById(R.id.searchBox));
		String searchText = textView.getText().toString();
		Intent intent = new Intent(getApplicationContext(),BrowseActivity.class);
		intent.putExtra("url", URLBuilder.getURLForSearch(searchText));
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_dispatch, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
}
