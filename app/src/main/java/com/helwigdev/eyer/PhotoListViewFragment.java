package com.helwigdev.eyer;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.PictureDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
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
	public static final String ENDPOINT = "http://54.68.77.148/api/";
	public static final String GET_PHOTO_JSON = "getPhotosJson.php";
	public static final String GET_PHOTO = "/uploads/";

	public static final String DIALOG_IMAGE = "image";

	ImageDownloader<ImageView> mImageThread;
	ArrayList<Post> mPosts = new ArrayList<>();


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
		jsonArrayDownloader.execute(ENDPOINT + GET_PHOTO_JSON + "?latitude=40.8047555&longitude=-76.8779612&pagenum=1");
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			//if view not passed, inflate one
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater()
						.inflate(R.layout.fragment_photo_list_view, parent, false);
			}

			final TextView votes = (TextView) convertView.findViewById(R.id.tv_num_votes);
			int vote = mPosts.get(position).vote;
			votes.setText(vote + "");
			final ImageView imageView = (ImageView)convertView.findViewById(R.id.iv_list_view_photo);
			imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					FragmentManager fm = getActivity().getSupportFragmentManager();
					if(imageView.getDrawable() != null) {
						ImageFragment.newInstance(mPosts.get(position).getFilename()).show(fm, DIALOG_IMAGE);
					}
				}
			});


			TextView title = (TextView) convertView.findViewById(R.id.tv_image_title);
			title.setText(mPosts.get(position).getTitle());

			String url = ENDPOINT + GET_PHOTO + mPosts.get(position).getFilename();
			Log.i(TAG, url);

			final ImageView upvote = (ImageView) convertView.findViewById(R.id.iv_button_up);
			final ImageView downvote = (ImageView) convertView.findViewById(R.id.iv_button_down);



			if(vote == 0) {
				upvote.setImageResource(R.drawable.upvote_unselected);
				downvote.setImageResource(R.drawable.downvote_unselected);
			} else if(vote == 1){
				upvote.setImageResource(R.drawable.upvote);
				downvote.setImageResource(R.drawable.downvote_unselected);
			} else {
				upvote.setImageResource(R.drawable.upvote_unselected);
				downvote.setImageResource(R.drawable.downvote);
			}

			upvote.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//TODO flush this out to post.vote(1 OR -1);
					if(mPosts.get(position).vote == 0) {
						upvote.setImageResource(R.drawable.upvote);
						mPosts.get(position).vote = 1;
						votes.setText("1");
					}
				}
			});
			downvote.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//TODO this as well
					if(mPosts.get(position).vote == 0) {
						downvote.setImageResource(R.drawable.downvote);
						mPosts.get(position).vote = -1;
						votes.setText("-1");
					}
				}
			});

			imageView.setImageDrawable(getResources().getDrawable(android.R.color.transparent));//reset imageview
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
