package com.helwigdev.eyer;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tyler on 2/12/2015.
 * Copyright 2015 by Tyler Helwig
 */
@SuppressLint("HandlerLeak")
public class ImageDownloader<Token> extends HandlerThread {
	private static final String TAG = "ThumbnailDownloader";
	private static final int MESSAGE_DOWNLOAD = 0;
	private static final int MESSAGE_PRECACHE = 1;

	Handler mHandler;
	Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());

	Handler mResponseHandler;
	Listener<Token> mListener;
	LruCache<String, Bitmap> cache = new LruCache<>(500);//cache bitmap by URL key

	public interface Listener<Token> {
		void onThumbnailDownloaded(Token token, Bitmap thumbnail);
	}

	public void setListener(Listener<Token> listener) {
		mListener = listener;
	}

	public ImageDownloader(Handler responseHandler) {
		super(TAG);
		mResponseHandler = responseHandler;
	}


	@Override
	protected void onLooperPrepared() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				if (message.what == MESSAGE_DOWNLOAD) {
					@SuppressWarnings("unchecked")
					Token token = (Token) message.obj;
					//Log.i(TAG, "Got a request for a URL: " + requestMap.get(token));
					handleRequest(token);
				} else if (message.what == MESSAGE_PRECACHE){
					@SuppressWarnings("unchecked")
					Token token = (Token) message.obj;
					//too much output Log.i(TAG, "Got request to precache image: " + requestMap.get(token));
					precacheImage(token);
				}
			}
		};
	}

	private void precacheImage(final Token token){
		try{
			final String url = requestMap.get(token);
			if(url == null) return;
			final Bitmap bitmap;
			if (cache.get(url) == null) {
				byte[] bitmapBytes = new NetworkUtilities().getUrlBytes(url);
				bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
				Log.i(TAG, "PRECACHER: Bitmap created: no cache found");
				cache.put(url, bitmap);
			} else {
				//no action - already in cache
			}
		} catch (IOException e){
			Log.e(TAG, "Could not precache image: ", e);
		}
	}


	private void handleRequest(final Token token) {
		try {

			final String url = requestMap.get(token);
			if (url == null) return;
			final Bitmap bitmap;
			if (cache.get(url) == null) {
				byte[] bitmapBytes = new NetworkUtilities().getUrlBytes(url);
				bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
				Log.i(TAG, "Bitmap created: no cache found");
				cache.put(url, bitmap);
			} else {
				bitmap = cache.get(url);
				//Log.i(TAG, "Found bitmap in cache");
			}
			mResponseHandler.post(new Runnable() {
				@Override
				public void run() {
					if (requestMap.get(token) != url) return;

					requestMap.remove(token);

					mListener.onThumbnailDownloaded(token, bitmap);

				}
			});
		} catch (IOException e) {
			Log.e(TAG, "Error downloading image", e);
		}
	}

	public void queueThumbnail(Token token, String url) {
		//Log.i(TAG, "Got a URL: " + url);
		requestMap.put(token, url);

		mHandler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget();
	}

	public void queuePrecache(Token token, String url){
		requestMap.put(token, url);

		mHandler.obtainMessage(MESSAGE_PRECACHE, token).sendToTarget();
	}

	public void clearQueue() {
		mHandler.removeMessages(MESSAGE_DOWNLOAD);
		mHandler.removeMessages(MESSAGE_PRECACHE);
		requestMap.clear();
	}
}
