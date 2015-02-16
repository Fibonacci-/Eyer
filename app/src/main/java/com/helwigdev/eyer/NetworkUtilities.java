package com.helwigdev.eyer;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Tyler on 2/13/2015.
 * Copyright 2015 by Tyler Helwig
 */
public class NetworkUtilities {

	byte[] getUrlBytes(String urlSpec) throws IOException {
		URL url = new URL(urlSpec);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		Log.i("getUrlBytes",urlSpec + ": HTTP response is " + connection.getResponseCode());
		try{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = connection.getInputStream();
			if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
				Log.e("Fetchr", connection.getResponseMessage() + " : " + connection.getResponseCode());
				return null;
			}

			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while((bytesRead = in.read(buffer)) > 0){
				out.write(buffer, 0, bytesRead);
			}
			out.close();
			return out.toByteArray();
		} finally {
			connection.disconnect();
		}
	}

	public String getUrl(String urlSpec) throws IOException{
		return new String(getUrlBytes(urlSpec));
	}
}
