package com.sirkitboard.wallhavenbrowser.util;

import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.sirkitboard.wallhavenbrowser.R;
import com.sirkitboard.wallhavenbrowser.app.WallhavenBrowser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by Aditya on 7/9/2015.
 */
public class BitmapLoader {
	final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
	final int cacheSize = maxMemory / 8;
	WallhavenImageCache imageCache;
	ArrayList<String> wallList;
	ArrayList<String> fileNames;
	Bitmap mPlaceHolderBitmap;
	Bitmap loadingBitmap;
	String extension;
	Bitmap tempref;
	public BitmapLoader() {
		imageCache = new WallhavenImageCache(cacheSize);
		wallList = new ArrayList<>();
		makeDirectories();
		String path = Environment.getExternalStorageDirectory().toString()+"/Wallhaven/";
		File f = new File(path);
		File file[] = f.listFiles();
		fileNames = new ArrayList<>();
		for (int i=0; i < file.length; i++)
		{
			if(file[i].getName().indexOf(".jpg") > 0 || file[i].getName().indexOf(".png") > 0) {
				fileNames.add(file[i].getName());
				wallList.add(file[i].getName().substring(0,file[i].getName().indexOf(".")));
				Log.d("FileNameTest", file[i].getName());
			}
		}
		mPlaceHolderBitmap = BitmapFactory.decodeResource(WallhavenBrowser.getContext().getResources(), R.drawable.loading);
		loadingBitmap = BitmapFactory.decodeResource(WallhavenBrowser.getContext().getResources(), R.drawable.loadmore);
	}

	public void makeDirectories() {
		try {
			String path = Environment.getExternalStorageDirectory().toString()+"/Wallhaven/.temp/";
			File file = new File(path);
			if(!file.exists()){
				file.mkdirs();
				File file2 = new File(path+".nomedia");
				file.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public ArrayList<String> getWallList() {
		return wallList;
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

	public static Bitmap decodeSampledBitmapFromResource(int wallID, int reqWidth, int reqHeight) throws IOException {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap wallpaperThumb = null;
		String url = URLBuilder.getURLForThumb(wallID);
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
			String url = URLBuilder.getURLForThumb(data);
			try {
				URL imageURL = new URL(url);
				URLConnection conn = imageURL.openConnection();
				conn.connect();

				InputStream is = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				wallThumb = BitmapFactory.decodeStream(bis);
				//wallThumb = decodeSampledBitmapFromResource(data, 100, 100);
			} catch (IOException e) {
				Log.e("com.sirkitboard.wallhav", "Something went wrong");
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

	class BitmapWorkerFileTask extends AsyncTask<Integer, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private int data = 0;

		public BitmapWorkerFileTask(ImageView imageView) {
			// Use a WeakReference to ensure the ImageView can be garbage collected
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(Integer... params) {
			data = params[0];
			Bitmap wallThumb = null;
			String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/.temp/sm-"+data+".jpg";
			wallThumb = BitmapFactory.decodeFile(file_path);
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
					final BitmapWorkerFileTask bitmapWorkerTask =
							getBitmapWorkerFileTask(imageView);
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

	static class AsyncFileDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerFileTask> bitmapWorkerTaskReference;

		public AsyncFileDrawable(Resources res, Bitmap bitmap,
		                     BitmapWorkerFileTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference =
					new WeakReference<BitmapWorkerFileTask>(bitmapWorkerTask);
		}

		public BitmapWorkerFileTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	public void loadBitmapThumbnail(int wallID, ImageView imageView) {
		if(wallID == -1 || wallID == -2) {
			imageView.setImageBitmap(loadingBitmap);
			return;
		}

		final String imageKey = String.valueOf(wallID);
		Bitmap bitmap = imageCache.getBitmapFromMemCache(imageKey);
		if(bitmap!=null) {
			imageView.setImageBitmap(bitmap);
			return;
		}
		else if (cancelPotentialWork(wallID, imageView)) {
			if(wallList.contains(wallID) && smallThumbExists(wallID)) {
				final BitmapWorkerFileTask task = new BitmapWorkerFileTask(imageView);
				final AsyncFileDrawable asyncDrawable = new AsyncFileDrawable(WallhavenBrowser.getContext().getResources(), mPlaceHolderBitmap, task);
				imageView.setImageDrawable(asyncDrawable);
				task.execute(wallID);
			}
			else {
				final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
				final AsyncDrawable asyncDrawable = new AsyncDrawable(WallhavenBrowser.getContext().getResources(), mPlaceHolderBitmap, task);
				imageView.setImageDrawable(asyncDrawable);
				task.execute(wallID);
			}
		}
	}

	public boolean smallThumbExists(int wallID) {
		String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/.temp/sm-"+wallID+".jpg";
		File file = new File(file_path);
		return file.exists();
	}

	public Bitmap getWallpaperThumbnail(int wallID) {
		final String imageKey = String.valueOf(wallID);
		Bitmap bitmap = imageCache.getBitmapFromMemCache(imageKey);
		return bitmap;
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

	private static BitmapWorkerFileTask getBitmapWorkerFileTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncFileDrawable asyncDrawable = (AsyncFileDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	public boolean loadWallpaper(int wallID, ImageView imageView) {
		imageView.setImageBitmap(mPlaceHolderBitmap);
		if(wallList.contains(wallID + "")) {
			Log.d("FileTest","Found in List");
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/.temp/" + "th-"+wallID+".jpg";
			File file = new File(path);
			if(file.exists()) {
				Log.d("FileTest","File exists");
				LoadFromFile lff = new LoadFromFile(imageView,imageView.getWidth(),imageView.getHeight());
				lff.execute(path);
				return true;
			}
		}
		Log.d("FileTest","Not Found in List");
		SetImagePreview sip = new SetImagePreview(imageView,imageView.getWidth(),imageView.getHeight());
		sip.execute(wallID);
		return false;
	}

	private class LoadFromFile extends AsyncTask<String, String,Bitmap> {
		ImageView imageView;float height, width;
		LoadFromFile(ImageView imageView, float width, float height) {
			this.imageView = imageView;
			this.width = width;
			this.height = height;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = BitmapFactory.decodeFile(params[0]);
			return scaleDown(bitmap, width, height, true);
		}

		protected void onPostExecute(Bitmap bitmap) {
			tempref = bitmap;
			imageView.setImageBitmap(bitmap);
		}
	}

	private class SetImagePreview extends AsyncTask<Integer, String, String> {
		ImageView imageView;float height, width;

		SetImagePreview(ImageView imageView, float width, float height) {
			this.imageView = imageView;
			this.width = width;
			this.height = height;
			extension = ".jpg";
		}
		@Override
		protected String doInBackground(Integer... params) {
			try {
				String url = URLBuilder.getURLForWall(params[0],".jpg");
				String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/.temp/wallpaper.jpg";
				URL imageURL = new URL(url);
				HttpURLConnection conn = (HttpURLConnection)imageURL.openConnection();
				conn.connect();
				if(conn.getResponseCode() > 400) {
					url = URLBuilder.getURLForWall(params[0],".png");
					path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/.temp/wallpaper.png";
					extension = ".png";
					imageURL = new URL(url);
					conn = (HttpURLConnection)imageURL.openConnection();
					conn.connect();
				}
				InputStream is = conn.getInputStream();
				OutputStream output = new FileOutputStream(path);
				try {
					byte[] buffer = new byte[1024];
					int bytesRead = 0;
					while ((bytesRead = is.read(buffer, 0, buffer.length)) >= 0) {
						output.write(buffer, 0, bytesRead);
					}
				}
				finally {
					output.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				Log.e("SetWallpaper", "Malformed URL");
			} catch (IOException e) {
				Log.e("SetWallpaper", "IO");
			}
			return "fail";
		}

		@Override
		protected void onPostExecute(String result) {
			LoadFromFile lff = new LoadFromFile(imageView,width,height);
			lff.execute(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/.temp/wallpaper" +extension);
		}
	}


	public boolean saveWallpaper(int wallID) {
		File wallpaper = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/.temp/wallpaper"+extension);
		File dst = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/"+wallID+extension);
		if(wallpaper.exists()) {
			FileInputStream inStream;
			FileOutputStream outStream;
			try {
				inStream = new FileInputStream(wallpaper);
				outStream = new FileOutputStream(dst);
				FileChannel inChannel = inStream.getChannel();
				FileChannel outChannel = outStream.getChannel();
				inChannel.transferTo(0, inChannel.size(), outChannel);
				inStream.close();
				outStream.close();
				Intent scanFileIntent = new Intent(
						Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dst));
				WallhavenBrowser.getContext().sendBroadcast(scanFileIntent);
				String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/.temp/th-"+wallID+".jpg";

				File file = new File(file_path);
				FileOutputStream fOut = new FileOutputStream(file);
				tempref.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
				fOut.flush();
				fOut.close();
				wallList.add(wallID+"");
				fileNames.add(wallID+extension);
				saveThumb(wallID);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public void saveThumb(int wallID) {
		Bitmap bitmap = getWallpaperThumbnail(wallID);
		if(bitmap != null) {
			saveThumbToFile(bitmap,wallID);
		}
		else {
			SaveThumbnailFromInternet stft = new SaveThumbnailFromInternet();
			stft.execute(wallID);
		}
	}

	public void saveThumbToFile(Bitmap thumb, int wallID) {
		try {
			String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/.temp/sm-"+wallID+".jpg";
			File file = new File(file_path);
			FileOutputStream fOut = new FileOutputStream(file);
			thumb.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class SaveThumbnailFromInternet extends AsyncTask<Integer, Void, Bitmap> {
		@Override
		protected Bitmap doInBackground(Integer... params) {
			int data = params[0];
			File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Wallhaven/.temp/sm-"+data+".jpg");
			Bitmap wallThumb = null;
			String url = URLBuilder.getURLForThumb(data);
			try {
				URL imageURL = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) imageURL.openConnection();

				InputStream is = conn.getInputStream();
				OutputStream output = new FileOutputStream(path);
				try {
					byte[] buffer = new byte[1024];
					int bytesRead = 0;
					while ((bytesRead = is.read(buffer, 0, buffer.length)) >= 0) {
						output.write(buffer, 0, bytesRead);
					}
				} finally {
					output.close();
					is.close();
				}
			} catch (MalformedURLException e) {

			} catch (IOException e){

			}
			return wallThumb;
		}
	}

	public static Bitmap scaleDown(Bitmap realImage, float maxImageWidth, float maxImageheight,
	                               boolean filter) {
		float ratio = (float) maxImageWidth / realImage.getWidth();
		if(realImage.getHeight() * ratio > maxImageheight) {
			ratio = (float) maxImageheight / realImage.getHeight();
		}
		int width = Math.round((float) ratio * realImage.getWidth());
		int height = Math.round((float) ratio * realImage.getHeight());

		Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
				height, filter);
		return newBitmap;
	}

}
