package com.sirkitboard.wallhavenbrowser.util;

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

import com.sirkitboard.wallhavenbrowser.R;
import com.sirkitboard.wallhavenbrowser.activities.DispatchActivity;
import com.sirkitboard.wallhavenbrowser.activities.MainActivity;
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

	Bitmap mPlaceHolderBitmap;

	WallhavenImageCache imageCache;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder {
		// each data item is just a string in this case
		public ImageView mImageView;
		public ViewHolder(View itemview) {
			super(itemview);
			mImageView = (ImageView)itemview.findViewById(R.id.wallpaperPreview);
			itemview.setOnClickListener(DispatchActivity.myOnlClickListener);
		}
	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public ImageRecyclerAdapter(ArrayList myDataset, WallhavenImageCache imageCache) {
		mDataset = myDataset;
		this.imageCache = imageCache;
		mPlaceHolderBitmap = BitmapFactory.decodeResource(WallhavenBrowser.getContext().getResources(), R.drawable.loading);
	}

	// Create new views (invoked by the layout manager)
	@Override
	public ImageRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
	                                               int viewType) {
		// create a new view
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnailview, parent, false);
		// set the view's size, margins, paddings and layout parameters
		ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		// - get element from your dataset at this position
		// - replace the contents of the view with that element
		loadBitmap(mDataset.get(position),holder.mImageView);

	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(int wallID, int reqWidth, int reqHeight) throws IOException{

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap wallpaperThumb = null;
		String url = "http://alpha.wallhaven.cc/wallpapers/thumb/small/th-"+wallID+".jpg";
		URL imageURL = new URL(url);
		URLConnection conn = imageURL.openConnection();
		conn.connect();

		InputStream is = conn.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is);
		wallpaperThumb = BitmapFactory.decodeStream(bis,null,options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		bis.reset();
		wallpaperThumb = BitmapFactory.decodeStream(bis, null, options);
		bis.close();
		is.close();
		return wallpaperThumb;
	}

	class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private int data = 0;

		public BitmapWorkerTask(ImageView imageView) {
			// Use a WeakReference to ensure the ImageView can be garbage collected
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(Integer... params) {
			data = params[0];
			Bitmap wallThumb = null;
			String url = "http://alpha.wallhaven.cc/wallpapers/thumb/small/th-"+data+".jpg";
			try {
				URL imageURL = new URL(url);
				URLConnection conn = imageURL.openConnection();
				conn.connect();

				InputStream is = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				wallThumb = BitmapFactory.decodeStream(bis);
				//wallThumb = decodeSampledBitmapFromResource(data, 100, 100);
			} catch (IOException e) {
				Log.e("com.sirkitboard.wallhav","Something went wrong");
			}
			return wallThumb;
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageViewReference != null && bitmap != null) {
				if (isCancelled()) {
					bitmap = null;
				}

				if (imageViewReference != null && bitmap != null) {
					final ImageView imageView = imageViewReference.get();
					final BitmapWorkerTask bitmapWorkerTask =
							getBitmapWorkerTask(imageView);
					if (this == bitmapWorkerTask && imageView != null) {
						imageCache.addBitmapToMemoryCache(String.valueOf(data), bitmap);
						imageView.setImageBitmap(bitmap);
					}
				}
			}
		}
	}

	static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap,
		                     BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference =
					new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
		}

		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	public void loadBitmap(int wallID, ImageView imageView) {
		final String imageKey = String.valueOf(wallID);
		Bitmap bitmap = imageCache.getBitmapFromMemCache(imageKey);
		if(bitmap!=null) {
			imageView.setImageBitmap(bitmap);
			return;
		}
		else if (cancelPotentialWork(wallID, imageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			final AsyncDrawable asyncDrawable = 	new AsyncDrawable(WallhavenBrowser.getContext().getResources(), mPlaceHolderBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute(wallID);
		}
	}

	public static boolean cancelPotentialWork(int data, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final int bitmapData = bitmapWorkerTask.data;
			// If bitmapData is not yet set or it differs from the new data
			if (bitmapData == 0 || bitmapData != data) {
				// Cancel previous task
				bitmapWorkerTask.cancel(true);
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was cancelled
		return true;
	}

	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

}
