package com.sirkitboard.wallhavenbrowser.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.sirkitboard.wallhavenbrowser.R;
import com.sirkitboard.wallhavenbrowser.util.BitmapLoader;
import com.sirkitboard.wallhavenbrowser.util.WallhavenImage;
import com.sirkitboard.wallhavenbrowser.util.WallhavenImageCache;

/**
 * Created by Aditya on 7/7/2015.
 */
public class WallhavenBrowser extends Application {

	static WallhavenBrowser self;
	static BitmapLoader bitmapLoader;

	public void onCreate() {
		super.onCreate();
//		Parse.initialize(this, "FqKgzUWXQ6csiIQdZhJJFXWFqsOYBtGZ2cCWuMIM", "TWLqqbq95AA7IWqHWA0WQHDD8WIxtWITeCWdUJV1");
//		ParseInstallation.getCurrentInstallation().saveInBackground();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		self = this;
	}

	public static BitmapLoader getBitmapLoader() {
		if(bitmapLoader == null) {
			bitmapLoader = new BitmapLoader();
		}
		return bitmapLoader;
	}

	public static Context getContext()
	{
		return self.getApplicationContext();
	}
}
