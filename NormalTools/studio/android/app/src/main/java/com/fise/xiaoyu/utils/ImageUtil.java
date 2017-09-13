package com.fise.xiaoyu.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;

import com.fise.xiaoyu.ui.helper.PhotoHelper;

/**
 * @Description 图片处理
 * 
 */
@SuppressLint("NewApi")
public class ImageUtil {
	private static Logger logger = Logger.getLogger(ImageUtil.class);

	public static Bitmap getBigBitmapForDisplay(String imagePath,
			Context context) {
		if (null == imagePath || !new File(imagePath).exists())
			return null;
		try {
			int degeree = PhotoHelper.readPictureDegree(imagePath);
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
			if (bitmap == null)
				return null;
			DisplayMetrics dm = new DisplayMetrics();
			((Activity) context).getWindowManager().getDefaultDisplay()
					.getMetrics(dm);
			float scale = bitmap.getWidth() / (float) dm.widthPixels;
			Bitmap newBitMap = null;
			if (scale > 1) {
				newBitMap = zoomBitmap(bitmap,
						(int) (bitmap.getWidth() / scale),
						(int) (bitmap.getHeight() / scale));
				bitmap.recycle();
				Bitmap resultBitmap = PhotoHelper.rotaingImageView(degeree,
						newBitMap);
				return resultBitmap;
			}
			Bitmap resultBitmap = PhotoHelper.rotaingImageView(degeree, bitmap);
			return resultBitmap;
		} catch (Exception e) {
			logger.e(e.getMessage());
			return null;
		}
	}

	private static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
		if (null == bitmap) {
			return null;
		}
		try {
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			Matrix matrix = new Matrix();
			float scaleWidth = ((float) width / w);
			float scaleHeight = ((float) height / h);
			matrix.postScale(scaleWidth, scaleHeight);
			Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix,
					true);
			return newbmp;
		} catch (Exception e) {
			logger.e(e.getMessage());
			return null;
		}
	}

	/**
	 * 根据图片的url路径获得Bitmap对象
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap returnBitmap(String url) {
		URL fileUrl = null;
		Bitmap bitmap = null;

		try {
			fileUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		try {
			HttpURLConnection conn = (HttpURLConnection) fileUrl
					.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;

	}

	// 生成圆角图片
	public static Bitmap GetRoundedCornerBitmap(Bitmap bitmap) {
		try {
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
					bitmap.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(),
					bitmap.getHeight());
			final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),
					bitmap.getHeight()));
			final float roundPx = 14;
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(Color.BLACK);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			final Rect src = new Rect(0, 0, bitmap.getWidth(),
					bitmap.getHeight());
			canvas.drawBitmap(bitmap, src, rect, paint);
			return output;
		} catch (Exception e) {
			return bitmap;
		}
	}

}
