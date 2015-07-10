package com.sirkitboard.wallhavenbrowser.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.sirkitboard.wallhavenbrowser.R;
import com.sirkitboard.wallhavenbrowser.util.ImageRecyclerAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Aditya on 7/9/2015.
 */
public class ExploreFragment extends Fragment {
	private RecyclerView mLatestRecyclerView;
	private RecyclerView.Adapter mLatestAdapter;
	private RecyclerView.LayoutManager mLatestLayoutManager;
	private RecyclerView mRandomRecyclerView;
	private RecyclerView.Adapter mRandomAdapter;
	private RecyclerView.LayoutManager mRandomLayoutManager;
	ArrayList<Integer> latestWallpaperIDs;
	ArrayList<Integer> randomWallpapers;
	Switch mSfwSwitch;
	Switch mSketchySwitch;

	public static final String ARG_PAGE = "ARG_PAGE";

	private int mPage;

	public static ExploreFragment newInstance(int page) {
		Bundle args = new Bundle();
		args.putInt(ARG_PAGE, page);
		ExploreFragment fragment = new ExploreFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPage = getArguments().getInt(ARG_PAGE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_browse, container, false);
		latestWallpaperIDs = new ArrayList<>();
		randomWallpapers = new ArrayList<>();
		mSfwSwitch = (Switch) view.findViewById(R.id.sfwSwitch);
		mSketchySwitch = (Switch) view.findViewById(R.id.sketchySwitch);
		mSfwSwitch.setChecked(true);
		//Latest
		mLatestRecyclerView = (RecyclerView) view.findViewById(R.id.latestRecycler);
		mLatestRecyclerView.setHasFixedSize(true);
		mLatestLayoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);
		mLatestRecyclerView.setLayoutManager(mLatestLayoutManager);
		mLatestAdapter = new ImageRecyclerAdapter(latestWallpaperIDs);
		mLatestRecyclerView.setAdapter(mLatestAdapter);

		//Random
		mRandomRecyclerView = (RecyclerView) view.findViewById(R.id.randomRecycler);
		mRandomRecyclerView.setHasFixedSize(true);
		mRandomLayoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);
		mRandomRecyclerView.setLayoutManager(mRandomLayoutManager);
		mRandomAdapter = new ImageRecyclerAdapter(randomWallpapers);
		mRandomRecyclerView.setAdapter(mRandomAdapter);
		refresh();

		return view;
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

	public void addWallpapersToDataset(ArrayList<Integer> ta, ArrayList<Integer> tba) {
		ta.clear();
		for(Integer i : tba) {
			ta.add(i);
		}
	}
}
