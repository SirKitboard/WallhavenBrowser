package com.sirkitboard.wallhavenbrowser.activities;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.sirkitboard.wallhavenbrowser.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class MainActivity extends Activity {

	GetWallpaperAsyncTask gwat;
//	SharedPreferences settings;
//	SharedPreferences.Editor settingEditor;
	ImageView imageView;
	int pageNo;
	int wallpaperCounter;
	ArrayList<Integer> wallpaperID;
	Bitmap wallpaper;
	int sketchy;
	Toast toast;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//settings = getApplicationContext().getSharedPreferences("com.sirkitboard", 0);
		imageView = (ImageView) findViewById(R.id.wallpaperPreview);
		pageNo = 1;
		sketchy = 0;
		Button button = (Button) findViewById(R.id.button2);
		button.setEnabled(false);
		toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
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
			sketchy = (sketchy+1)%3;
			pageNo = 1;
			wallpaperCounter = 0;
			if(sketchy == 0){
				toast.setText("SFW only");toast.show();
			}
			else if(sketchy == 1){
				toast.setText("SFW + Sketchy");toast.show();
			}
			else if(sketchy == 2){
				toast.setText("Sketchy Only only");toast.show();
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void runTask(View view) {
		getImageIds(1);
	}

	public void getImageIds(int pageNo) {
		this.pageNo = pageNo;
		String purity = "100";
		if(sketchy == 1) {
			purity = "110";
		}
		else if(sketchy == 2) {
			purity = "010";
		}
		String url = "http://alpha.wallhaven.cc/search?categories=111&purity="+purity+"&sorting=date_added&order=desc";
		url+="&page="+pageNo;
		gwat = new GetWallpaperAsyncTask();
		gwat.execute(url);
		Button button = (Button) findViewById(R.id.button2);
		button.setEnabled(true);
	}

	public void setImage(View view) {
		setImage();
	}

	public void setWallpaper(View view) {
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if(currentapiVersion > Build.VERSION_CODES.KITKAT) {
			try {
				String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/WHAW";
				File dir = new File(file_path);
				if (!dir.exists())
					dir.mkdirs();
				File file = new File(dir, "wallpaper.jpg");
				FileOutputStream fOut = new FileOutputStream(file);
				wallpaper.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
				fOut.flush();
				fOut.close();
				WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
				Uri uri = Uri.fromFile(file);
				Intent intent = wallpaperManager.getCropAndSetWallpaperIntent(getImageContentUri(getApplicationContext(), file.getAbsolutePath()));
				startActivity(intent);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
				wallpaperManager.setBitmap(wallpaper);
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	public void setImage() {
		if(wallpaperCounter >= wallpaperID.size()){
			pageNo++;
			wallpaperCounter = 0;
			getImageIds(pageNo);
		}
		else if(pageNo != 3) {
			SetImageAsWallpaperAsyncTask swat = new SetImageAsWallpaperAsyncTask();
			swat.execute(wallpaperID.get(wallpaperCounter++));
			toast.setText("" + wallpaperCounter);toast.show();
		}
		else {
			toast.setText("Error");toast.show();
		}
	}

	private class GetWallpaperAsyncTask extends AsyncTask<String,String, String> {
		ArrayList<Integer> wallpaperIDs;
		@Override
		protected String doInBackground(String... uri) {
			try {
				Document document = Jsoup.connect(uri[0]).get();
				Elements results = document.select(".thumb-listing-page ul li figure");
				wallpaperIDs = new ArrayList<Integer>();
				for (Element result : results) {
					wallpaperIDs.add(Integer.parseInt(result.attr("data-wallpaper-id")));
				}
				//Log.d("ID : ", results.get(0).attr("data-wallpaper-id"));
			} catch (IOException e) {
				return "fail";
			}
			// Do updating and stopping logical here.
			return "success";
		}

		@Override
		protected void onPostExecute(String result) {
			if(result.equalsIgnoreCase("success")) {
				wallpaperID = wallpaperIDs;
				setImage();
			}
			else {
				toast.setText("Error");toast.show();
			}
		}
	}


	private class SetImageAsWallpaperAsyncTask extends AsyncTask<Integer, String, String> {
		@Override
		protected String doInBackground(Integer... params) {
			try {
				String url = "http://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-" + params[0] + ".jpg";
				URL imageURL = new URL(url);
				URLConnection conn = imageURL.openConnection();
				conn.connect();

				InputStream is = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				wallpaper = BitmapFactory.decodeStream(bis);
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
			if(result.equalsIgnoreCase("success")) {
				if(wallpaper.getHeight() > 4096 || wallpaper.getWidth() > 4096) {
					setImage();
					return;
				}
				imageView.setImageBitmap(wallpaper);
			}
			else {
				setImage();
			}
		}
	}
}
