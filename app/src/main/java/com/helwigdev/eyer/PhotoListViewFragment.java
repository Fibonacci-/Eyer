package com.helwigdev.eyer;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Tyler on 2/13/2015.
 * Copyright 2015 by Tyler Helwig
 */
public class PhotoListViewFragment extends ListFragment{
	public static final String TAG = "PhotoListViewFragment";

	ImageDownloader<ImageView> mImageThread;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("Fragment","I'm alive!");
		ArrayList<Photo> photos = new ArrayList<>();

		for (int i = 0; i < 50; i++) {
			Photo p = new Photo();
			photos.add(p);
		}
		PhotoAdapter adapter = new PhotoAdapter(photos);
		setListAdapter(adapter);

		mImageThread = new ImageDownloader<>(new Handler());
		mImageThread.setListener(new ImageDownloader.Listener<ImageView>() {
			@Override
			public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
				if(isVisible()){
					imageView.setImageBitmap(thumbnail);
				}
			}
		});
		mImageThread.start();
		mImageThread.getLooper();
		Log.i(TAG, "Background thread started OK");
	}

	public class PhotoAdapter extends ArrayAdapter<Photo> {
		public PhotoAdapter(ArrayList<Photo> photos) {
			super(getActivity(), 0, photos);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//if view not passed, inflate one
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater()
						.inflate(R.layout.fragment_photo_list_view, null);
			}

			TextView votes = (TextView) convertView.findViewById(R.id.tv_num_votes);
			votes.setText(position + "");
			ImageView imageView = (ImageView)convertView.findViewById(R.id.iv_list_view_photo);

			mImageThread.queueThumbnail(imageView, "https://i.imgur.com/Yq1mc9H.jpg");

			return convertView;
		}
	}

}
