package com.sirkitboard.wallhavenbrowser.util;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sirkitboard.wallhavenbrowser.R;
import com.sirkitboard.wallhavenbrowser.activities.BrowseActivity;
import com.sirkitboard.wallhavenbrowser.activities.DispatchActivity;
import com.sirkitboard.wallhavenbrowser.activities.MainActivity;
import com.sirkitboard.wallhavenbrowser.activities.WallpaperActivity;
import com.sirkitboard.wallhavenbrowser.app.WallhavenBrowser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by Aditya on 7/7/2015.
 */
public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.ViewHolder> {
	private ArrayList<Integer> mDataset;
	BitmapLoader bitmapLoader;
	Bitmap mPlaceHolderBitmap;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, andon
	// you provide access to all the views for a data item in a view holder
	public class ViewHolder extends RecyclerView.ViewHolder {
		// each data item is just a string in this case
		public ImageView mImageView;
		public ViewHolder(View itemview) {
			super(itemview);
			mImageView = (ImageView)itemview.findViewById(R.id.wallpaperPreview);
			itemview.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = getAdapterPosition();
					int wallID = mDataset.get(position);
					Intent intent = new Intent(WallhavenBrowser.getContext(), WallpaperActivity.class);
					intent.putExtra("wallID", wallID);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					WallhavenBrowser.getContext().startActivity(intent);
				}
			});
		}

		public ViewHolder(View itemview, int state) {
			super(itemview);
			if(state == -1) {
				mImageView = (ImageView) itemview.findViewById(R.id.wallpaperPreview);
				itemview.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(WallhavenBrowser.getContext(), BrowseActivity.class);
						intent.putExtra("url", URLBuilder.getURLforLatest());
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						WallhavenBrowser.getContext().startActivity(intent);
					}
				});
			}
			if(state == -2) {
				mImageView = (ImageView) itemview.findViewById(R.id.wallpaperPreview);
				itemview.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(WallhavenBrowser.getContext(), BrowseActivity.class);
						intent.putExtra("url", URLBuilder.getURLforRandom());
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						WallhavenBrowser.getContext().startActivity(intent);
					}
				});
			};
		}
	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public ImageRecyclerAdapter(ArrayList myDataset) {
		mDataset = myDataset;
		bitmapLoader = WallhavenBrowser.getBitmapLoader();
	}

	// Create new views (invoked by the layout manager)
	@Override
	public ImageRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
	                                               int viewType) {
		View v;
		ViewHolder vh;
		// create a new view
		if(viewType == -1) {
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnailview, parent, false);
			vh = new ViewHolder(v,-1);
		}
		else if(viewType == -2) {
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnailview, parent, false);
			vh = new ViewHolder(v,-2);
		}
		else if(viewType == -3) {
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
			vh = new ViewHolder(v,-3);
		}
		else if(viewType == -4) {
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.endofresults, parent, false);
			vh = new ViewHolder(v,-4);
		}
		else {
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnailview, parent, false);
			vh = new ViewHolder(v);
		}

		// set the view's size, margins, paddings and layout parameters

		return vh;
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		// - get element from your dataset at this position
		// - replace the contents of the view with that element
		int viewType = getItemViewType(position);
		if(viewType >-3)
			bitmapLoader.loadBitmapThumbnail(mDataset.get(position),holder.mImageView);

	}

	@Override
	public int getItemViewType(int position) {
		if(mDataset.get(position) >= 0) {
			return 0;
		}
		else return mDataset.get(position);
	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return mDataset.size();
	}

}
