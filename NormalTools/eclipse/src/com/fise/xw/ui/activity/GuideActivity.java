package com.fise.xw.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.fise.xw.R;
import com.fise.xw.ui.base.TTBaseActivity;

/**
 * @author dwtedx 功能描述：引导界面
 */
public class GuideActivity extends TTBaseActivity {

	private View view1, view2;
	private ViewPager viewPager;
	private List<View> viewList;

	private View pointRed; // 移动的红点
	private int mPointWidth;// 圆点间的距离
	private LinearLayout llPointView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guide_main);
		initData();
		setData();
	}

	/**
	 * 初始化点点
	 */
	private void setData() {
		// 初始化引导页的三个圆点
		for (int i = 0; i < viewList.size(); i++) {
			View point = new View(this);
			point.setBackgroundResource(R.drawable.point_normal);
			int view_width = dip2px(this, 10);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					view_width, view_width);
			if (i > 0) {
				params.leftMargin = 10;
			}
			point.setLayoutParams(params);  
			llPointView.addView(point);

			// 获取视图树, 对layout结束事件进行监听
			llPointView.getViewTreeObserver().addOnGlobalLayoutListener(
					new OnGlobalLayoutListener() {

						// 当layout执行结束后回调此方法
						@Override
						public void onGlobalLayout() {
							System.out.println("layout 结束");
							llPointView.getViewTreeObserver()
									.removeGlobalOnLayoutListener(this);
							mPointWidth = llPointView.getChildAt(1).getLeft()
									- llPointView.getChildAt(0).getLeft();
							System.out.println("圆点距离:" + llPointView);
						}
					});
		}
	}

	/**
	 * 初始化各个控件
	 */
	private void initData() {
		viewPager = (ViewPager) findViewById(R.id.vp_test);
		view1 = View.inflate(this, R.layout.guidelayout1, null);
		view2 = View.inflate(this, R.layout.guidelayout2, null); 

		pointRed = findViewById(R.id.point_red);
		llPointView = (LinearLayout) findViewById(R.id.ll_point_view);

		viewList = new ArrayList<View>();
		viewList.add(view1);
		viewList.add(view2); 
		
		Button btn_start = (Button) view2.findViewById(R.id.btn_start); 
		btn_start.setOnClickListener(new OnClickListener() {
			 
		      @Override
		      public void onClick(View v) {
		        // TODO Auto-generated method stub 
		  			Intent intent = new Intent(GuideActivity.this,
		  					LoginActivity.class);
        			startActivity(intent);
        			GuideActivity.this.finish(); 
		      }
		    });
		viewPager.setAdapter(new MyPagerAdapter());
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());

	}

	class MyPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return viewList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(viewList.get(position));
			return viewList.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(viewList.get(position));
		}

	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			int len = (int) (mPointWidth * positionOffset) + position
					* mPointWidth;
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) pointRed
					.getLayoutParams();// 获取当前红点的布局参数
			params.leftMargin = len;// 设置左边距

			pointRed.setLayoutParams(params);// 重新给小红点设置布局参数
		}

		@Override
		public void onPageSelected(int arg0) {

		}

	}
}