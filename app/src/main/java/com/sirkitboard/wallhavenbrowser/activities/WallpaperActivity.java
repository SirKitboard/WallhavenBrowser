package com.sirkitboard.wallhavenbrowser.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.sirkitboard.wallhavenbrowser.R;
import com.sirkitboard.wallhavenbrowser.app.WallhavenBrowser;
import com.sirkitboard.wallhavenbrowser.util.BitmapLoader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class WallpaperActivity extends AppCompatActivity {
	int wallID;
	Bitmap full;
	Bitmap scaled;
	ImageView imageView;
	ProgressDialog progress;
	Toast toast;
	static final int SET_WALLPAPER = 1;
	BitmapLoader bitmapLoader;
	boolean wallpaperSaved;
	boolean loaded;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wallpaper);
		try {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		imageView = (ImageView)findViewById(R.id.wallpaperPreview);
		wallID = getIntent().getIntExtra("wallID",0);
		bitmapLoader = WallhavenBrowser.getBitmapLoader();
		loaded = false;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		//Here you can get the size!
		//Log.d("HeightWidth",imageView.getWidth()+" "+imageView.getHeight());
		if(!loaded) {
			wallpaperSaved = bitmapLoader.loadWallpaper(wallID, imageView);
			loaded = true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_wallpaper, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void setAsWallpaper(View view) {
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if(currentapiVersion > Build.VERSION_CODES.KITKAT) {
			try {
				String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/temp";
				File dir = new File(file_path);
				if (!dir.exists())
					dir.mkdirs();
				File file = new File(dir, "wallpaper.jpg");
				FileOutputStream fOut = new FileOutputStream(file);
				full.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
				fOut.flush();
				fOut.close();
				WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
				Uri uri = Uri.fromFile(file);
				Intent intent = wallpaperManager.getCropAndSetWallpaperIntent(getImageContentUri(getApplicationContext(), file.getAbsolutePath()));
				startActivityForResult(intent,SET_WALLPAPER);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
				wallpaperManager.setBitmap(full);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void openInGallery(View view) {
		String file_path;
		File file;
		if(!wallpaperSaved) {
			file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/.temp/wallpaper.jpg";
			file = new File(file_path);
			if (!file.exists()) {
				file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/.temp/wallpaper.png";
				file = new File(file_path);
			}
		}
		else {
			file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/"+wallID+".jpg";
			file = new File(file_path);
			if (!file.exists()) {
				file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/"+wallID+".png";
				file = new File(file_path);
			}
		}
		if(file.exists()) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			Uri uri = Uri.fromFile(file);
			intent.setDataAndType(uri, "image/*");
			startActivityForResult(intent, SET_WALLPAPER);
		}
		else {
			showToast("ERROR");
		}
	}

	public static Uri getImageContentUri(Context context, String absPath) {
		Log.v("com.sirkitboard.r", "getImageContentUri: " + absPath);

		Cursor cursor = context.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI
				, new String[] { MediaStore.Images.Media._ID }
				, MediaStore.Images.Media.DATA + "=? "
				, new String[] { absPath }, null);

		if (cursor != null && cursor.moveToFirst()) {
			int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
			return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , Integer.toString(id));

		} else if (!absPath.isEmpty()) {
			ContentValues values = new ContentValues();
			values.put(MediaStore.Images.Media.DATA, absPath);
			return context.getContentResolver().insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		} else {
			return null;
		}
	}

	public void saveWallpaper(View v) {
		if(!wallpaperSaved) {
			wallpaperSaved = bitmapLoader.saveWallpaper(wallID);
			if(wallpaperSaved) {
				showToast("Wallpaper Saved");
			}
			else {
				showToast("error");
			}
		}
		else {
			showToast("Already Saved");
		}
	}

	public void showToast(String text) {
		if(toast!=null) {
			toast.cancel();
		}
		toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
		toast.show();
	}
}
