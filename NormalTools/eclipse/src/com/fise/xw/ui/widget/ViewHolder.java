package com.fise.xw.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder {
	private SparseArray<View> mViews;
	private int mPosition;
	private View mRootView;

	private ViewHolder(Context context, ViewGroup parent, int layoutId,
			int position) {
		mPosition = position;
		mViews = new SparseArray<View>();
		mRootView = LayoutInflater.from(context).inflate(layoutId, parent, false);
		mRootView.setTag(this);
	}

	public static ViewHolder get(Context context, View convertView,
			ViewGroup parent, int layoutId, int position) {
		if (convertView == null) {
			return new ViewHolder(context, parent, layoutId, position);
		} else {
			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.mPosition = position;
			return viewHolder;
		}
	}

	public View getRootView() {
		return mRootView;
	}
	
	public int getPosition() {
		return mPosition;
	}

	/** 通过ViewId获取控件 */
	@SuppressWarnings("unchecked")
	public <T extends View> T getView(int viewId) {
		View view = mViews.get(viewId);
		if (view == null) {
			view = mRootView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		return (T) view;
	}
	
	/** ImageView及子类专用 */
	public <T extends ImageView> ViewHolder setImageBitmap(int viewId, Bitmap bm) {
		T imageView = getView(viewId);
		imageView.setImageBitmap(bm);
		return this;
	}

	/** ImageView及子类专用 */
	public <T extends ImageView> ViewHolder setImageDrawable(int viewId, Drawable drawable) {
		T imageView = getView(viewId);
		imageView.setImageDrawable(drawable);
		return this;
	}

	/** ImageView及子类专用 */
	public <T extends ImageView> ViewHolder setImageResource(int viewId, int resId) {
		T imageView = getView(viewId);
		imageView.setImageResource(resId);
		return this;
	}
	
	/** TextView及子类专用 */
	public <T extends TextView> ViewHolder setText(int viewId, CharSequence text) {
		T textView = getView(viewId);
		textView.setText(text);
		return this;
    }
	
	/** TextView及子类专用 */
	public <T extends TextView> ViewHolder setText(int viewId, int resId) {
		T textView = getView(viewId);
		textView.setText(resId);
		return this;
	}
	
	/** CheckBox及子类专用 */
	public <T extends CheckBox> ViewHolder setChecked(int viewId, boolean checked) {
		T checkBox = getView(viewId);
		checkBox.setChecked(checked);
		return this;
	}
	
	/** View及子类专用 */
	public ViewHolder setTag(int viewId, Object tag) {
		getView(viewId).setTag(tag);
		return this;
	}
}
