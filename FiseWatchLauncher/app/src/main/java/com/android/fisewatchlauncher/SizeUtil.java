package com.android.fisewatchlauncher;

import android.content.Context;

public class SizeUtil {

	public static float Dp2Px(Context context, float f) {
		// TODO Auto-generated method stub
		final float scale = context.getResources().getDisplayMetrics().density;
		return f*scale+0.5f;
	}

	public static float Dp2Px(Context context, int f) {
		// TODO Auto-generated method stub
		final float scale = context.getResources().getDisplayMetrics().density;
		return f*scale+0.5f;
	}

	public static float Sp2Px(Context context, int value) {
		// TODO Auto-generated method stub
		final float fontScale = context.getResources().getDisplayMetrics().density;
		return value*fontScale + 0.5f;
	}

}
