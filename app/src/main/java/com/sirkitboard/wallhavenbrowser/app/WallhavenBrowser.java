package com.sirkitboard.wallhavenbrowser.app;

import android.app.Application;
import android.content.Context;

import com.sirkitboard.wallhavenbrowser.util.WallhavenImage;
import com.sirkitboard.wallhavenbrowser.util.WallhavenImageCache;

/**
 * Created by Aditya on 7/7/2015.
 */
public class WallhavenBrowser extends Application {

	static WallhavenBrowser self;

	public void onCreate() {
		super.onCreate();
		self = this;
	}

	public static Context getContext()
	{
		return self.getApplicationContext();
	}
}
