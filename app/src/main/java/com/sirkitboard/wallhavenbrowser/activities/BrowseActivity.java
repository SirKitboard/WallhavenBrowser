package com.sirkitboard.wallhavenbrowser.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;

import com.sirkitboard.wallhavenbrowser.R;
import com.sirkitboard.wallhavenbrowser.util.BitmapLoader;
import com.sirkitboard.wallhavenbrowser.util.GridSpacingItemDecoration;
import com.sirkitboard.wallhavenbrowser.util.ImageRecyclerAdapter;
import com.sirkitboard.wallhavenbrowser.util.URLBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class BrowseActivity extends AppCompatActivity {
	private RecyclerView mBrowseRecyclerView;
	private RecyclerView.Adapter mBrowseAdapter;
	private GridLayoutManager mBrowseLayoutManager;
	ArrayList<Integer> browseWallpaperIDs;
	BitmapLoader bitmapLoader;
	int pageNo = 0;
	String urlQuery;
	boolean loading;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browser);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		urlQuery = getIntent().getStringExtra("url");
		browseWallpaperIDs = new ArrayList<>();
		mBrowseRecyclerView = (RecyclerView) findViewById(R.id.browseRecycler);
		mBrowseRecyclerView.setHasFixedSize(true);
		mBrowseLayoutManager = new GridLayoutManager(getApplicationContext(),3, GridLayoutManager.VERTICAL, false);
		loading = true;
		mBrowseRecyclerView.setLayoutManager(mBrowseLayoutManager);
		mBrowseAdapter = new ImageRecyclerAdapter(browseWallpaperIDs);
		mBrowseLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				switch (mBrowseAdapter.getItemViewType(position)) {
					case 0:case -1:case -2:
						return 1;
					case -3: case -4:
						return mBrowseLayoutManager.getSpanCount(); //number of columns of the grid
					default:
						return -1;
				}
			}
		});
		mBrowseRecyclerView.setAdapter(mBrowseAdapter);
		mBrowseRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

				int totalItemCount = mBrowseLayoutManager.getItemCount();
				int lastVisibleItem = mBrowseLayoutManager.findLastVisibleItemPosition();
				int visibleThreshold = mBrowseLayoutManager.getSpanCount();

				if (!loading) {
					if ((lastVisibleItem + visibleThreshold) >= totalItemCount) {
						getMoreWallpapers();
						loading = true;
					}
				}
			}
		});
		getMoreWallpapers();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.menu_browser, menu);
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

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		//Here you can get the size!
		//Log.d("HeightWidth",imageView.getWidth()+" "+imageView.getHeight());
		float width = mBrowseRecyclerView.getWidth();
		int numColumns = (int) (width / 320);
		mBrowseLayoutManager.setSpanCount(Math.max(numColumns, 2));
	}

	public void getMoreWallpapers() {
		pageNo++;
		GetSearchWallpaperAsyncTask gswat = new GetSearchWallpaperAsyncTask();
		gswat.execute("");
	}

	public void addWallpapersToDataset(ArrayList<Integer> tba) {
		if(browseWallpaperIDs.size() > 0 && browseWallpaperIDs.get(browseWallpaperIDs.size()-1) == -3) {
			browseWallpaperIDs.remove(browseWallpaperIDs.size()-1);
		}
		for(Integer i : tba) {
			browseWallpaperIDs.add(i);
		}
	}

	private class GetSearchWallpaperAsyncTask extends AsyncTask<String,String, String> {
		ArrayList<Integer> wallpaperIDs;
		@Override
		protected String doInBackground(String... uri) {
			try {
				String url =urlQuery+"&page="+pageNo;
				Log.e("Weird",url);
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
			if(wallpaperIDs.size() >= 24) {
				wallpaperIDs.add(-3);
			}
			else {
				wallpaperIDs.add(-4);
			}
			addWallpapersToDataset(wallpaperIDs);
			mBrowseAdapter.notifyDataSetChanged();
			if(wallpaperIDs.size() >= 24) {
				loading = false;
			}
			else {
				loading = true;
			}

		}
	}
}
