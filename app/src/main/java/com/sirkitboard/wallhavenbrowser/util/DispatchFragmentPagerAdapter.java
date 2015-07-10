package com.sirkitboard.wallhavenbrowser.util;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sirkitboard.wallhavenbrowser.R;
import com.sirkitboard.wallhavenbrowser.activities.ExploreFragment;

/**
 * Created by Aditya on 7/9/2015.
 */
public class DispatchFragmentPagerAdapter extends FragmentPagerAdapter {
	final int PAGE_COUNT = 1;
	private String tabTitles[] = new String[] { "Tab1", "Tab2" };

	private Context context;

	public DispatchFragmentPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		this.context = context;
	}

	@Override
	public int getCount() {
		return PAGE_COUNT;
	}

	@Override
	public Fragment getItem(int position) {
		return ExploreFragment.newInstance(position + 1);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		// Generate title based on item position
		return tabTitles[position];
	}

	public View getTabView(int position) {
		// Given you have a custom layout in `res/layout/custom_tab.xml` with a TextView and ImageView
		View v = null;
		Log.e("TabNum",position + "");
		if(position == 1) {
			v =  LayoutInflater.from(context).inflate(R.layout.fragment_browse, null);
		}
		return v;
	}
}
