package com.sirkitboard.wallhavenbrowser.util;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

/**
 * Created by Aditya on 7/7/2015.
 */
public class RetainFragment extends Fragment {
	private static final String TAG = "RetainFragment";
	public WallhavenImageCache mRetainedCache;

	public RetainFragment() {
	}

	public static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
		RetainFragment fragment = (RetainFragment) fm.findFragmentByTag(TAG);
		if (fragment == null) {
			fragment = new RetainFragment();
			fm.beginTransaction().add(fragment, TAG).commit();
		}
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
}