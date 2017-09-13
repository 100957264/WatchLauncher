package com.fise.xw.ui.activity;
//javaapk.com�ṩ���� 

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.fise.xw.R;
import com.fise.xw.ui.base.TTBaseActivity;


/**
 * 图片
 * @author weileiguan
 *
 */
public class PreviewHeadActivity extends TTBaseActivity
{
	 
	ImageView preview;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.preview);

		Bitmap bitmap = null;
		Intent in = getIntent();
		if (in != null)
		{
			byte[] bis = in.getByteArrayExtra("bitmap");
			bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length);
		}

		preview = (ImageView) this.findViewById(R.id.preview);
		if(bitmap == null)
		{
			Log.e("11","bitmap is NULL !");
		}
		else
		{
			preview.setImageBitmap(bitmap);
		}
	}

}
