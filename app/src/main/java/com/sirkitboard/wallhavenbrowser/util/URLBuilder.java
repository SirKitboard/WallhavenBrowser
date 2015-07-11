package com.sirkitboard.wallhavenbrowser.util;

import android.content.SharedPreferences;

import com.sirkitboard.wallhavenbrowser.app.WallhavenBrowser;

/**
 * Created by Aditya on 7/10/2015.
 */
public class URLBuilder {
	final static String BASE_URL_QUERY = "http://alpha.wallhaven.cc/search?";
	final static String BASE_URL_THUMB = "http://alpha.wallhaven.cc/wallpapers/thumb/small/th-";
	final static String BASE_URL_WALL = "http://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-";
	public static String getURLForSearch(String search) {
		String url = BASE_URL_QUERY + "q="+search+getProperties()+"&sorting=relevance&order=desc";
		return url;
	}

	public static String getURLforLatest() {
		String url = BASE_URL_QUERY + getProperties()+"&sorting=date_added&order=desc";
		return url;
	}
	public static String getURLforRandom() {
		String url = BASE_URL_QUERY + getProperties()+"&sorting=random&order=desc";
		return url;
	}

	public static String getURLForThumb(int wallid) {
		return BASE_URL_THUMB+wallid+".jpg";
	}

	public static String getURLForWall(int wallid, String extension) {
		return BASE_URL_WALL + wallid + extension;
	}

	public static String getProperties() {
		return (getPurity() + getCategories() + getResolutions() + getRatios());
	}

	public static String getPurity() {
		SharedPreferences sharedPreferences = WallhavenBrowser.getContext().getSharedPreferences("com.sirkitboard.wallhavenbrowser",WallhavenBrowser.getContext().MODE_PRIVATE);
		return "&purity=" + sharedPreferences.getString("purity","100");
	}

	public static String getCategories() {
		return "&categories=" + "111";
	}

	public static String getResolutions() {
		return "";
	}

	public static String getRatios() {
		return "";
	}
}
