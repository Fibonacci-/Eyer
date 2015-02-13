package com.helwigdev.eyer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.Display;
import android.widget.ImageView;

/**
 * Created by Tyler on 2/6/2015.
 * Copyright 2015 by Tyler Helwig
 */
public class PictureUtils {
	/**
	 * Get BitmapDrawable scaled to current Window size.
	 */
	@SuppressWarnings("deprecation")
	public static BitmapDrawable getScaledDrawable(Activity a, String path) {
		Display display = a.getWindowManager().getDefaultDisplay();
		float destWidth = display.getWidth();
		float destHeight = display.getHeight();

		//read in dimensions of image on disk
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

			BitmapFactory.decodeFile(path, options);

		float srcWidth = options.outWidth;
		float srcHeight = options.outHeight;

		int inSampleSize = 1;
		if (srcHeight > destHeight || srcWidth > destWidth) {
			if (srcWidth > srcHeight) {
				inSampleSize = Math.round(srcHeight / destHeight);
			} else {
				inSampleSize = Math.round((srcWidth / destWidth));
			}
		}

		options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;

		Bitmap bitmap = BitmapFactory.decodeFile(path, options);

		return new BitmapDrawable(a.getResources(), bitmap);

	}

	public static void cleanImageView(ImageView imageView){
		if(!(imageView.getDrawable() instanceof BitmapDrawable)){
			return;
		}

		BitmapDrawable b = (BitmapDrawable) imageView.getDrawable();
		if(b.getBitmap() != null) {
			b.getBitmap().recycle();
		}
		imageView.setImageDrawable(null);
	}


}
