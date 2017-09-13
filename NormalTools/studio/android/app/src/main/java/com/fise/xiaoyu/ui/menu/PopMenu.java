package com.fise.xiaoyu.ui.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.jinlin.zxing.example.activity.CaptureActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PopMenu
 */
public class PopMenu implements OnItemClickListener , View.OnClickListener{


	public interface OnItemClickListener {
		public void onItemClick(int index);
	}

	private Context context;
	private PopupWindow popupWindow;
	private ListView listView;
	private OnItemClickListener listener;
	private LayoutInflater inflater;
	private static final int REQUEST_CODE_SCAN = 0x0000;

	public PopMenu(Context context) {
		this.context = context;

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//		View view = inflater.inflate(R.layout.popmenu_list, null);
		View view = inflater.inflate(R.layout.popmenu_layout, null);
        initPopMenuView(view);
//		listView = (ListView) view.findViewById(R.id.listView);
//		List<Map<String, Object>> list = getData();
//		listView.setAdapter(new PopAdapter(context, list));
//		listView.setOnItemClickListener(this);



		popupWindow = new PopupWindow(view, context.getResources()
				.getDimensionPixelSize(R.dimen.popmenu_width), // 这里宽度需要自己指定，使用
																// WRAP_CONTENT
																// 会很大
				LayoutParams.WRAP_CONTENT);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景（很神奇的）
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
	}




	private void initPopMenuView(View view) {
		view.findViewById(R.id.rl_pop_menu_group).setOnClickListener(this);
		view.findViewById(R.id.rl_pop_menu_scan).setOnClickListener(this);
		view.findViewById(R.id.rl_pop_menu_add_friend).setOnClickListener(this);


	}

	@Override
	public void onClick(View v) {

		switch (v.getId()){
			case R.id.rl_pop_menu_group:
				if (listener != null) {
					listener.onItemClick(0);
				}
				IMUIHelper.openGroupMemberSelectActivity(context, false);
				break;
			case R.id.rl_pop_menu_scan:
				if (listener != null) {
					listener.onItemClick(2);
				}
				Activity intentCont = (Activity) this.context;
				Intent intent = new Intent(context, CaptureActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(IntentConstant.KEY_QR_ACTIVITY_TYPE, DBConstant.SEX_INFO_USER);

				intentCont.startActivityForResult(intent,
						CaptureActivity.SCANNIN_GREQUEST_CODE);
				break;
			case R.id.rl_pop_menu_add_friend:
				if (listener != null) {
					listener.onItemClick(1);
				}
				IMUIHelper.openAddFriendActivity(this.context);
				break;

		}
		dismiss();
	}

	public List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		int item[] = { R.drawable.icon_item_group, R.drawable.icon_friend_add,
				R.drawable.icon_scan };
		String itemString[] = { "发起群聊", "添加朋友", "扫一扫" };

		for (int i = 0; i < 3; i++) {
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
			IMUIHelper.openGroupMemberSelectActivity(context, false);

		} else if (position == 1) {
			IMUIHelper.openAddFriendActivity(this.context);

		} else if (position == 2) {

			Activity intentCont = (Activity) this.context;
			Intent intent = new Intent(context, CaptureActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(IntentConstant.KEY_QR_ACTIVITY_TYPE, DBConstant.SEX_INFO_USER);
			
			intentCont.startActivityForResult(intent,
					CaptureActivity.SCANNIN_GREQUEST_CODE);

		}

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

		private List<Map<String, Object>> data;
		private LayoutInflater layoutInflater;
		private Context context;

		public PopAdapter(Context context, List<Map<String, Object>> data) {
			this.context = context;
			this.data = data;
			this.layoutInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
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
				lp.setMargins(0, 30, 0, 0);
				rlLayout.setLayoutParams(lp);
			}

			// holder.groupItem.setText(itemList.get(position));
			// holder.groupItem.setText(itemList.get(position));
			holder.itemImage.setBackgroundResource((Integer) data.get(position)
					.get("img"));
			holder.ItemText.setText((String) data.get(position).get("title"));

			return convertView;
		}

		private final class ViewHolder {
			ImageView itemImage;
			TextView ItemText;

		}
	}
}
