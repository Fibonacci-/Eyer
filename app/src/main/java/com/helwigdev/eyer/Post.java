package com.helwigdev.eyer;

/**
 * Created by Tyler on 2/13/2015.
 * Copyright 2015 by Tyler Helwig
 */
public class Post {
	//{"id":"38","title":"butt","time_posted":"1414207271","latitude":"40.8006536","longitude":"-76.8747837","filename":"6c5da111-34cd-41ad-8545-b25d0ed829f71414207249151.jpg"}
	int mId;
	int mPoints = 0;
	int vote = 0;
	String title;
	String time_posted;
	String latitude;
	String longitude;
	String filename;

	public Post(int id, String title, String time_posted, String latitude, String longitude,
				String filename) {
		mId = id;
		this.title = title;
		this.time_posted = time_posted;
		this.latitude = latitude;
		this.longitude = longitude;
		this.filename = filename;
	}

	public int getId() {
		return mId;
	}

	public String getTitle() {
		return title;
	}

	public String getTime_posted() {
		return time_posted;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public String getFilename() {
		return filename;
	}
}
