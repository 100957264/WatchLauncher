package com.fise.xiaoyu.ui.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.jinlin.zxing.example.activity.CaptureActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QrDevMenu
 * 
 */
public class QrDevMenu implements OnItemClickListener , View.OnClickListener{

	private  PopAdapter mPopAdapter;

	private List<Map<String, Object>> mlist;


	public interface OnItemClickListener {
		public void onItemClick(int index);
	}

	private Context context;
	private PopupWindow popupWindow;
	private ListView listView;
	private OnItemClickListener listener;
	private LayoutInflater inflater;
	private static final int REQUEST_CODE_SCAN = 0x0000;
    private RelativeLayout buyDeviceRl;

	public QrDevMenu(Context context) {
		this.context = context;

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//		View view = inflater.inflate(R.layout.dev_pop_menu_list, null);
		View view = inflater.inflate(R.layout.dev_pop_menu_layout, null);
        initView( view);
//		listView = (ListView) view.findViewById(R.id.listView);
		popupWindow = new PopupWindow(view, context.getResources()
				.getDimensionPixelSize(R.dimen.popmenu_width), // 这里宽度需要自己指定，使用
																// WRAP_CONTENT
																// 会很大
				LayoutParams.WRAP_CONTENT);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景（很神奇的）
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
	}

	private void initView(View view) {

		view.findViewById(R.id.rl_dev_pop_menu_scan_add).setOnClickListener(this);
		view.findViewById(R.id.rl_dev_pop_menu_manual_add).setOnClickListener(this);
		buyDeviceRl = (RelativeLayout) view.findViewById(R.id.rl_dev_pop_menu_buy_add);
		buyDeviceRl.setOnClickListener(this);

	}


	@Override
	public void onClick(View v) {
          switch (v.getId()){

			  case R.id.rl_dev_pop_menu_scan_add:
				  if (listener != null) {
					  listener.onItemClick(0);
				  }
				  Activity intentCont = (Activity) this.context;
				  Intent intent = new Intent(context,
						  CaptureActivity.class);
				  intent.putExtra(IntentConstant.KEY_QR_ACTIVITY_TYPE, DBConstant.SEX_INFO_DEV);
				  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				  intentCont.startActivityForResult(intent, CaptureActivity.SCANNIN_GREQUEST_CODE);
			  	break;

			  case R.id.rl_dev_pop_menu_manual_add:
				  if (listener != null) {
					  listener.onItemClick(1);
				  }
				  IMUIHelper.openSelectSchoolActivity(context);
				  break;

			  case R.id.rl_dev_pop_menu_buy_add:
				  String phoneName = IMLoginManager.instance().getLoginInfo().getPhone(); 
				  String desPwd = new String(Security.getInstance()
						  .EncryptPass(IMLoginManager.instance().getLoginUserPwd()));
				  String url = IMContactManager.instance().getSystemConfig().getMallUrl() + "?" + "UserName=" + phoneName  + "&PassWord=" + desPwd ;


				 // UserName=15820915392&PassWord=e10adc3949ba59abbe56e057f20f883e
				  Intent intent1= new Intent();
                  intent1.setAction("android.intent.action.VIEW");
                  Uri content_url = Uri.parse(url);
                  intent1.setData(content_url);
                  context.startActivity(intent1);

				  break;



		  }
		dismiss();

	}





	public List<Map<String, Object>> getData(int count) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		int item[] = { R.drawable.icon_scan, R.drawable.icon_friend_add ,R.drawable.icon_goum };
		String itemString[] = { "扫描添加", "手动添加","购买设备"}; //,"购买设备"
		 
		for (int i = 0; i < count; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("img", item[i]);
			map.put("title", itemString[i]);
			list.add(map);
		} 
		return list;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (listener != null) {
			listener.onItemClick(position);
		}
		if (position == 0) {
			
			Activity intentCont = (Activity) this.context;
			Intent intent = new Intent(context,
					CaptureActivity.class); 
			intent.putExtra(IntentConstant.KEY_QR_ACTIVITY_TYPE, DBConstant.SEX_INFO_DEV);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intentCont.startActivityForResult(intent, CaptureActivity.SCANNIN_GREQUEST_CODE);

		} else if (position == 1) {
			IMUIHelper.openSelectSchoolActivity(context);
		}

//		else if(position == 2){
//			Intent intent= new Intent();
//			intent.setAction("android.intent.action.VIEW");
//			Uri content_url = Uri.parse("mall.fise-wi.com");
//			intent.setData(content_url);
//			context.startActivity(intent);
//		}

		dismiss();

	}

	// 设置菜单项点击监听器
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}

	// 批量添加菜单项      
	public void addItems(String[] items) {
	}

	// 单个添加菜单项
	public void addItem(String item) {
	}



	// 下拉式 弹出 pop菜单 parent 右下角
	public void showAsDropDown(View parent) {
//		mlist = getData(itemCount);
//		mPopAdapter = new PopAdapter(context);
//		listView.setAdapter(mPopAdapter);
//		listView.setOnItemClickListener(this);
//
//		mPopAdapter.notifyDataSetChanged();
//        if(itemCount < 3){
//			buyDeviceRl.setVisibility(View.INVISIBLE);
//		}

		popupWindow.showAsDropDown(parent,
				10,
				// 保证尺寸是根据屏幕像素密度来的
				context.getResources().getDimensionPixelSize(
						R.dimen.popmenu_yoff));

		// 使其聚集
		popupWindow.setFocusable(true);
		// 设置允许在外点击消失
		popupWindow.setOutsideTouchable(true);
		// 刷新状态
		popupWindow.update();
	}


	// 下拉式 弹出 pop菜单 parent 右下角
	public void showAsDropDown(View parent,int itemCount) {
//		mlist = getData(itemCount);
//		mPopAdapter = new PopAdapter(context);
//		listView.setAdapter(mPopAdapter);
//		listView.setOnItemClickListener(this);
//
//		mPopAdapter.notifyDataSetChanged();
//        if(itemCount < 3){
//			buyDeviceRl.setVisibility(View.INVISIBLE);
//		}

		popupWindow.showAsDropDown(parent,
				10,
				// 保证尺寸是根据屏幕像素密度来的
				context.getResources().getDimensionPixelSize(
						R.dimen.popmenu_yoff));

		// 使其聚集
		popupWindow.setFocusable(true);
		// 设置允许在外点击消失
		popupWindow.setOutsideTouchable(true);
		// 刷新状态
		popupWindow.update();
	}

	// 隐藏菜单
	public void dismiss() {
		popupWindow.dismiss();
	}

	// 适配器
	private final class PopAdapter extends BaseAdapter {


		private LayoutInflater layoutInflater;
		private Context context;

		public PopAdapter(Context context) {
			this.context = context;


			this.layoutInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mlist.size();
		}

		@Override
		public Object getItem(int position) {
			return mlist.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.pomenu_item, null);
				holder = new ViewHolder();
				convertView.setTag(holder);
				holder.itemImage = (ImageView) convertView
						.findViewById(R.id.imageView);
				holder.ItemText = (TextView) convertView
						.findViewById(R.id.textView);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}

            if(position == 0){
                RelativeLayout rlLayout = (RelativeLayout) convertView.findViewById(R.id.rl_item_layout);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(rlLayout.getLayoutParams());
                lp.setMargins(0, 35, 0, 0);
                rlLayout.setLayoutParams(lp);
            }

			holder.itemImage.setBackgroundResource((Integer) mlist.get(position)
					.get("img"));
			holder.ItemText.setText((String) mlist.get(position).get("title"));

			return convertView;
		}

		private final class ViewHolder {
			ImageView itemImage;
			TextView ItemText;

		}
	}
}
