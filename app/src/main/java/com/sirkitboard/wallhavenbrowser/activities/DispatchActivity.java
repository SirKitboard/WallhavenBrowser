package com.sirkitboard.wallhavenbrowser.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.sirkitboard.wallhavenbrowser.R;
import com.sirkitboard.wallhavenbrowser.util.ImageRecyclerAdapter;
import com.sirkitboard.wallhavenbrowser.util.WallhavenImageCache;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class DispatchActivity extends Activity {

	private RecyclerView mLatestRecyclerView;
	private RecyclerView.Adapter mLatestAdapter;
	private RecyclerView.LayoutManager mLatestLayoutManager;
	private RecyclerView mRandomRecyclerView;
	private RecyclerView.Adapter mRandomAdapter;
	private RecyclerView.LayoutManager mRandomLayoutManager;
	ArrayList<Integer> latestWallpaperIDs;
	ArrayList<Integer> randomWallpapers;
	public static ImageThumbOnClickListener myOnlClickListener;
	Toast toast;
	final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
	final int cacheSize = maxMemory / 8;
	Switch mSfwSwitch;
	Switch mSketchySwitch;
	WallhavenImageCache imageCache;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dispatch);
		imageCache = new WallhavenImageCache(cacheSize);
		showToast(cacheSize + "");
		latestWallpaperIDs = new ArrayList<>();
		randomWallpapers = new ArrayList<>();
		myOnlClickListener  = new ImageThumbOnClickListener();
		mSfwSwitch = (Switch) findViewById(R.id.sfwSwitch);
		mSketchySwitch = (Switch) findViewById(R.id.sketchySwitch);
		mSfwSwitch.setChecked(true);
		//Latest
		mLatestRecyclerView = (RecyclerView) findViewById(R.id.latestRecycler);
		mLatestRecyclerView.setHasFixedSize(true);
		mLatestLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		mLatestRecyclerView.setLayoutManager(mLatestLayoutManager);
		mLatestAdapter = new ImageRecyclerAdapter(latestWallpaperIDs,imageCache);
		mLatestRecyclerView.setAdapter(mLatestAdapter);

		//Random
		mRandomRecyclerView = (RecyclerView) findViewById(R.id.randomRecycler);
		mRandomRecyclerView.setHasFixedSize(true);
		mRandomLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		mRandomRecyclerView.setLayoutManager(mRandomLayoutManager);
		mRandomAdapter = new ImageRecyclerAdapter(randomWallpapers,imageCache);
		mRandomRecyclerView.setAdapter(mRandomAdapter);

		refresh();
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
			refresh();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void showImage(View v) {
		int position = mLatestRecyclerView.getChildAdapterPosition(v);
		int wallID = 0;
		if(position == -1) {
			showToast("hi");
			position = mRandomRecyclerView.getChildAdapterPosition(v);
			wallID = randomWallpapers.get(position);
		}
		else {
			wallID = latestWallpaperIDs.get(position);
		}

		Intent intent = new Intent(getApplicationContext(), WallpaperActivity.class);
		intent.putExtra("wallID", wallID);
		startActivity(intent);

	}

	public void addWallpapersToDataset(ArrayList<Integer> ta, ArrayList<Integer> tba) {
		ta.clear();
		for(Integer i : tba) {
			ta.add(i);
		}
	}

	public class ImageThumbOnClickListener implements View.OnClickListener{
		@Override
		public void onClick(View v) {
			showImage(v);
		}
	}

	public String getPurity() {
		String purity = "";
		purity+=mSfwSwitch.isChecked() ? "1" : "0";
		purity+=mSketchySwitch.isChecked() ? "1" : "0";
		return purity+"0";
	}

	public void refresh() {
		GetLatestWallpaperAsyncTask gwat = new GetLatestWallpaperAsyncTask();
		gwat.execute();
		GetRandomWallpaperAsyncTask rwat = new GetRandomWallpaperAsyncTask();
		rwat.execute();
	}

	private class GetLatestWallpaperAsyncTask extends AsyncTask<String,String, String> {
		ArrayList<Integer> wallpaperIDs;
		@Override
		protected String doInBackground(String... uri) {
			try {
				String url = "http://alpha.wallhaven.cc/search?categories=111&purity="+getPurity()+"&sorting=date_added&order=desc";
				Document document = Jsoup.connect(url).get();
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
			addWallpapersToDataset(latestWallpaperIDs, wallpaperIDs);
			mLatestAdapter.notifyDataSetChanged();
		}
	}

	private class GetRandomWallpaperAsyncTask extends AsyncTask<String,String, String> {
		ArrayList<Integer> wallpaperIDs;
		@Override
		protected String doInBackground(String... uri) {
			try {
				String url = "http://alpha.wallhaven.cc/search?categories=111&purity="+getPurity()+"&sorting=random&order=desc";
				Document document = Jsoup.connect(url).get();
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
			addWallpapersToDataset(randomWallpapers, wallpaperIDs);
			mRandomAdapter.notifyDataSetChanged();
		}
	}

	public void showToast(String text) {
		if(toast!=null) {
			toast.cancel();
		}
		toast = Toast.makeText(getApplicationContext(),text,Toast.LENGTH_SHORT);
		toast.show();
	}
}
