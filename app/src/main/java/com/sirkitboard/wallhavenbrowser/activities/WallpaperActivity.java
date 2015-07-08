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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.sirkitboard.wallhavenbrowser.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class WallpaperActivity extends Activity {
	int wallID;
	Bitmap full;
	Bitmap scaled;
	ImageView imageView;
	ProgressDialog progress;
	static final int SET_WALLPAPER = 1;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wallpaper);
		try {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		progress = new ProgressDialog(this);
		progress.setTitle("Loading");
		progress.setMessage("Wait while loading...");
		progress.show();
		imageView = (ImageView)findViewById(R.id.wallpaperPreview);
		wallID = getIntent().getIntExtra("wallID",0);
		SetImagePreview sip = new SetImagePreview();
		sip.execute(wallID);

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == SET_WALLPAPER) {
			// Make sure the request was successful
				String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/temp";
				File dir = new File(file_path);
				File file = new File(dir, "wallpaper.jpg");
				file.delete();
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
		try {
			String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/temp";
			File dir = new File(file_path);
			if (!dir.exists())
				dir.mkdirs();
			File file = new File(dir, wallID + ".jpg");
			FileOutputStream fOut = new FileOutputStream(file);
			full.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private class SetImagePreview extends AsyncTask<Integer, String, String> {
		@Override
		protected String doInBackground(Integer... params) {
			try {
				String url = "http://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-" + params[0] + ".jpg";
				URL imageURL = new URL(url);
				HttpURLConnection conn = (HttpURLConnection)imageURL.openConnection();
				conn.connect();
				if(conn.getResponseCode() > 400) {
					url = "http://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-" + params[0] + ".png";
					imageURL = new URL(url);
					conn = (HttpURLConnection)imageURL.openConnection();
					conn.connect();
				}
				InputStream is = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				full = BitmapFactory.decodeStream(bis);
				bis.close();
				is.close();
				return "success";
			} catch (MalformedURLException e) {
				Log.e("SetWallpaper", "Malformed URL");
			} catch (IOException e) {
				Log.e("SetWallpaper", "IO");
			} catch (Exception e) {
				Log.e("SetWallpaper", "Out of memory");
			}
			return "fail";
		}

		@Override
		protected void onPostExecute(String result) {
			scaled = scaleDown(full,imageView.getWidth(),true);
			imageView.setImageBitmap(scaled);
			progress.dismiss();
		}
	}

	public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
	                               boolean filter) {
		float ratio = (float) maxImageSize / realImage.getWidth();
		int width = Math.round((float) ratio * realImage.getWidth());
		int height = Math.round((float) ratio * realImage.getHeight());

		Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
				height, filter);
		return newBitmap;
	}
}
