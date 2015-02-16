package com.helwigdev.eyer;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.PictureDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Tyler on 2/13/2015.
 * Copyright 2015 by Tyler Helwig
 */
public class PhotoListViewFragment extends ListFragment{
	public static final String TAG = "PhotoListViewFragment";

	ImageDownloader<ImageView> mImageThread;
	ArrayList<Post> mPosts = new ArrayList<>();
	int buttonImageSize = 100;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//mPosts.add(new Post(38, "butt", "1414207271", "40.8006536", "-76.8747837", "6c5da111-34cd-41ad-8545-b25d0ed829f71414207249151.jpg"));

		PhotoAdapter adapter = new PhotoAdapter(mPosts);
		setListAdapter(adapter);

		mImageThread = new ImageDownloader<>(new Handler());
		mImageThread.setListener(new ImageDownloader.Listener<ImageView>() {
			@Override
			public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
				if(isVisible()){
					imageView.setImageBitmap(thumbnail);
				}
			}

			@Override
			public void onThumbnailDownloadStart(ImageView imageView, Bitmap thumbnail) {
				imageView.setImageDrawable(null);//reset drawable so we don't re-use old ones
				Log.i(TAG, "Resetting image drawable");
			}
		});
		mImageThread.start();
		mImageThread.getLooper();


		Log.i(TAG, "Background threads started OK");

		JsonArrayDownloader jsonArrayDownloader = new JsonArrayDownloader(new JsonArrayDownloader.JsonListener() {


			@Override
			public void onJsonDownloaded(JSONArray array) {
				Log.i(TAG, "Got json download response! " + array.length());
				ArrayList<Post> posts = new ArrayList<>();
				for(int i = 0; i < array.length(); i++){
					try {
						JSONObject o = array.getJSONObject(i);
						Post p = new Post(o.getInt("id"), o.getString("title") ,o.getString("time_posted"), o.getString("latitude"), o.getString("longitude"), o.getString("filename"));
						posts.add(p);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				updateItems(posts, false);
			}
		});
		jsonArrayDownloader.execute("http://54.68.77.148/api/getPhotosJson.php?latitude=40.8047555&longitude=-76.8779612&pagenum=1");
	}

	void updateItems(ArrayList<Post> posts, boolean replaceAll){
		if(mPosts == null){
			mPosts = posts;
		} else if (replaceAll){
			mPosts = posts;
		} else {
			for(Post p : posts){
				mPosts.add(p);
			}
		}

		ArrayAdapter a = ((ArrayAdapter)getListAdapter());
		a.notifyDataSetChanged();
		Log.i(TAG, "Updated list!");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.blank_layout, container, false);
	}

	public class PhotoAdapter extends ArrayAdapter<Post> {
		public PhotoAdapter(ArrayList<Post> posts) {
			super(getActivity(), 0, posts);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//if view not passed, inflate one
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater()
						.inflate(R.layout.fragment_photo_list_view, parent, false);
			}

			TextView votes = (TextView) convertView.findViewById(R.id.tv_num_votes);
			votes.setText(mPosts.get(position).mPoints + "");
			ImageView imageView = (ImageView)convertView.findViewById(R.id.iv_list_view_photo);

			TextView title = (TextView) convertView.findViewById(R.id.tv_image_title);
			title.setText(mPosts.get(position).getTitle());

			String url = "http://54.68.77.148/api/uploads/" + mPosts.get(position).getFilename();
			Log.i(TAG, url);

			ImageView upvote = (ImageView) convertView.findViewById(R.id.iv_button_up);
			ImageView downvote = (ImageView) convertView.findViewById(R.id.iv_button_down);

			upvote.setImageDrawable(new BitmapDrawable(getResources(),PictureUtils.decodeSampledBitmapFromResource(getResources(), R.drawable.upvote, buttonImageSize,buttonImageSize)));
			downvote.setImageDrawable(new BitmapDrawable(getResources(),PictureUtils.decodeSampledBitmapFromResource(getResources(), R.drawable.downvote, buttonImageSize,buttonImageSize)));

			mImageThread.queueThumbnail(imageView, url);

			return convertView;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mImageThread.quit();
		Log.i(TAG, "Background threads destroyed");
	}

	@Override
	public void onDestroyView() {
		super.onDestroy();
		mImageThread.clearQueue();
		Log.i(TAG, "Background threads cleaned");
	}

	public static class JsonArrayDownloader extends AsyncTask<String, Void, JSONArray>{

		JsonListener mListener;

		public interface JsonListener{
			void onJsonDownloaded(JSONArray array);
		}

		public JsonArrayDownloader(JsonListener listener){
			mListener = listener;
		}

		@Override
		protected JSONArray doInBackground(String... params) {
			try {
				byte[] byteArray = new NetworkUtilities().getUrlBytes(params[0]);
				JSONArray array = new JSONArray(new String(byteArray));
				return array;
			} catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONArray jsonArray) {
			if(jsonArray != null) {
				mListener.onJsonDownloaded(jsonArray);
			}
		}
	}

}
