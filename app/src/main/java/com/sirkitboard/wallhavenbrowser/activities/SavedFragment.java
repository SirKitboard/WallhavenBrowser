package com.sirkitboard.wallhavenbrowser.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Switch;

import com.sirkitboard.wallhavenbrowser.R;
import com.sirkitboard.wallhavenbrowser.app.WallhavenBrowser;
import com.sirkitboard.wallhavenbrowser.util.BitmapLoader;
import com.sirkitboard.wallhavenbrowser.util.ImageRecyclerAdapter;
import com.sirkitboard.wallhavenbrowser.util.WallhavenImageCache;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Aditya on 7/9/2015.
 */
public class SavedFragment extends Fragment {
	private RecyclerView mSavedRecyclerView;
	private RecyclerView.Adapter mSavedAdapter;
	private StaggeredGridLayoutManager mSavedLayoutManager;
	ArrayList<Integer> savedWallpaperIDs;
	BitmapLoader bitmapLoader;

	public static final String ARG_PAGE = "ARG_PAGE";

	private int mPage;

	public static SavedFragment newInstance(int page) {
		Bundle args = new Bundle();
		args.putInt(ARG_PAGE, page);
		SavedFragment fragment = new SavedFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPage = getArguments().getInt(ARG_PAGE);
		setHasOptionsMenu(true);
		bitmapLoader = WallhavenBrowser.getBitmapLoader();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		//noinspection SimplifiableIfStatement
		if (id == R.id.action_reload) {
			refresh();
			return true;
		}
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_saved, container, false);
		savedWallpaperIDs = new ArrayList<>();

		mSavedRecyclerView = (RecyclerView) view.findViewById(R.id.savedRecycler);
		mSavedRecyclerView.setHasFixedSize(true);
		mSavedLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
		mSavedRecyclerView.setLayoutManager(mSavedLayoutManager);
		mSavedAdapter = new ImageRecyclerAdapter(savedWallpaperIDs);
		mSavedRecyclerView.setAdapter(mSavedAdapter);
		refresh();
		ViewTreeObserver vto = view.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				Log.d("TEST", "Height = " + view.getHeight() + " Width = " + view.getWidth());
				ViewTreeObserver obs = view.getViewTreeObserver();
				obs.removeOnGlobalLayoutListener(this);
				float width = mSavedRecyclerView.getWidth();
					Log.e("Width", mSavedRecyclerView.getWidth() + "");
					int numColumns = (int) (width / 300);
					mSavedLayoutManager.setSpanCount(Math.max(numColumns, 2));
			}
		});
		return view;
	}

	public void refresh() {
		savedWallpaperIDs.clear();
		for(String wallID : bitmapLoader.getWallList()) {
			try {
				savedWallpaperIDs.add(Integer.parseInt(wallID));
			} catch (NumberFormatException e){}
		}
		mSavedAdapter.notifyDataSetChanged();
	}

//	@Override
//	public void onWindowFocusChanged(boolean hasFocus) {
//
//	}


}
