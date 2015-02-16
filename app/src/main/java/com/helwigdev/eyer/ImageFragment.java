package com.helwigdev.eyer;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Tyler on 2/16/2015.
 * Copyright 2015 by Tyler Helwig
 */
public class ImageFragment extends DialogFragment {

	public static final String EXTRA_IMAGE_PATH = "com.helwigdev.eyer.image_path";
	private ImageView mImageView;

	ProgressBar pb;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		String path = (String) getArguments().getSerializable(EXTRA_IMAGE_PATH);
		String url = PhotoListViewFragment.ENDPOINT + PhotoListViewFragment.GET_PHOTO + path;
		View v = inflater.inflate(R.layout.image_view_dialog, container, false);

		mImageView = (ImageView) v.findViewById(R.id.iv_dialog_image);
		mImageView.setImageResource(android.R.color.transparent);

		mImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		pb = (ProgressBar) v.findViewById(R.id.pb_image_dialog);
		new getImageAsync().execute(url);

		return v;
	}

	public static ImageFragment newInstance(String fileName) {
		Bundle args = new Bundle();
		args.putSerializable(EXTRA_IMAGE_PATH, fileName);

		ImageFragment fragment = new ImageFragment();
		fragment.setArguments(args);
		fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		return fragment;
	}

	private class getImageAsync extends AsyncTask<String, Integer, BitmapDrawable> {

		@Override
		protected BitmapDrawable doInBackground(String... params) {
			byte[] byteArray = null;
			try {
				URL url = new URL(params[0]);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				Log.i("ImFrag", params[0] + ": HTTP response is " + connection.getResponseCode());
				try {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					InputStream in = connection.getInputStream();
					if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
						Log.e("Fetchr", connection.getResponseMessage() + " : " + connection.getResponseCode());
						return null;
					}


					int bytesRead = 0;
					int counter = 0;
					int maxLength = connection.getContentLength();
					byte[] buffer = new byte[1024];
					while ((bytesRead = in.read(buffer)) > 0) {
						counter += bytesRead;
						out.write(buffer, 0, bytesRead);
						onProgressUpdate(counter, maxLength);
					}
					out.close();
					byteArray = out.toByteArray();
				} finally {
					connection.disconnect();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if(byteArray == null){
				return null;
			} else if(isAdded()){
				Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
				return new BitmapDrawable(getResources(), bitmap);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			pb.setIndeterminate(false);
			pb.setMax(values[1]);
			pb.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(BitmapDrawable bitmapDrawable) {
			super.onPostExecute(bitmapDrawable);
			mImageView.setImageDrawable(bitmapDrawable);
		}
	}
}
