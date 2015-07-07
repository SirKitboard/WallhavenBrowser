package com.sirkitboard.wallhavenbrowser.util;

import android.graphics.Bitmap;
import android.util.LruCache;

import java.util.BitSet;

/**
 * Created by Aditya on 7/7/2015.
 */
public class WallhavenImageCache extends LruCache<String, Bitmap> {

	public WallhavenImageCache(int maxSize) {
		super(maxSize);
	}

	protected int sizeOf(String key, Bitmap bitmap) {
		// The cache size will be measured in kilobytes rather than
		// number of items.
		return bitmap.getByteCount() / 1024;
	}

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			this.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return this.get(key);
	}
}
