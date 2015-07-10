package com.sirkitboard.wallhavenbrowser.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sirkitboard.wallhavenbrowser.R;
import com.sirkitboard.wallhavenbrowser.util.DispatchFragmentPagerAdapter;


public class DispatchActivity extends AppCompatActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dispatch);

		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		DispatchFragmentPagerAdapter pagerAdapter =
				new DispatchFragmentPagerAdapter(getSupportFragmentManager(), DispatchActivity.this);
		viewPager.setAdapter(pagerAdapter);

		// Give the TabLayout the ViewPager
		TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
		tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
		tabLayout.setupWithViewPager(viewPager);

		for (int i = 0; i < tabLayout.getTabCount(); i++) {
			TabLayout.Tab tab = tabLayout.getTabAt(i);
			tab.setCustomView(pagerAdapter.getTabView(i));
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_dispatch, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_reload) {
			//refresh();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
