package com.jinlin.zxing.example.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.media.ThumbnailUtils;

/**
 *
 * 项目名称 : ZXingScanQRCode<br>
 * 创建人 : skycracks<br>
 * 创建时间 : 2016-4-19下午9:53:29<br>
 * 版本 :	[v1.0]<br>
 * 类描述 : LOGO图片加上白色背景图片<br>
 */
public class LogoConfig {
	/**
	 * @return 返回带有白色背景框logo
	 */
	public Bitmap modifyLogo(Bitmap bgBitmap, Bitmap logoBitmap) {

		//读取背景图片，并构建绘图对象
		int bgWidth = bgBitmap.getWidth();
		int bgHeigh = bgBitmap.getHeight();
		Bitmap newBitmap = bgBitmap.copy(Bitmap.Config.ARGB_8888, true);
		//通过ThumbnailUtils压缩原图片，并指定宽高为背景图的3/4
		logoBitmap = ThumbnailUtils.extractThumbnail(logoBitmap,bgWidth*9/10, bgHeigh*9/10, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
//		Bitmap cvBitmap = Bitmap.createBitmap(bgWidth, bgHeigh, Config.ARGB_8888);
		Canvas canvas = new Canvas(newBitmap);
		// 开始绘制图片
//		canvas.drawBitmap(bgBitmap, 0, 0, null);
		canvas.drawBitmap(logoBitmap,(bgWidth - logoBitmap.getWidth()) /2,(bgHeigh - logoBitmap.getHeight()) / 2, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);// 保存
		canvas.restore();
		if(newBitmap.isRecycled()){
			newBitmap.recycle();
		}
		return newBitmap;
	}
}
