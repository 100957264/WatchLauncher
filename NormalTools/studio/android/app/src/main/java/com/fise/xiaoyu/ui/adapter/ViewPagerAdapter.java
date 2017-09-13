package com.fise.xiaoyu.ui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Map;


public class ViewPagerAdapter extends PagerAdapter {
    Context context;
    List<Map<String, Object>> viewLists;

    public ViewPagerAdapter(List<Map<String, Object>> lists, Context context) {
        this.viewLists = lists;
        this.context = context;
    }


    public void setPagerAdapter(List<Map<String, Object>> lists) {
        this.viewLists = lists;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {  //获得size
        return viewLists.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup view, int position, Object object) { //销毁Item
        ImageView x = (ImageView) viewLists.get(position).get("view");
        x.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.removeView(x);
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) { //实例化Item
        ImageView imageView = (ImageView) viewLists.get(position).get("view");
        // imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // imageView.getLayoutParams().height = 200;
        // imageView.getLayoutParams().width = 80;
        // // I also tried this ////
        // LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
        // imageView.setLayoutParams(layoutParams);
        RequestOptions requestOptions = new RequestOptions()
                // .placeholder(R.mipmap.new_default)
                // .error(R.mipmap.ic_launcher)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
        Glide.with(context)
                .load(viewLists.get(position).get("url").toString())
                .apply(requestOptions)
                .into(imageView);
        view.addView(imageView, 0);

        return viewLists.get(position).get("view");
    }
}