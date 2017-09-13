package com.fise.xw.ui.menu;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.fise.xw.R;

/**
 * 修改于：2013-2-28 17:03:35
 * 	修正 ListView item 点击响应失败！
 * 
 * @author Yichou
 *
 */
public class HeadMenu implements OnItemClickListener {
	public interface OnItemClickListener {
		public void onItemClick(int index);
	}
	 
	private Context context;
	private PopupWindow popupWindow;
	private ListView listView;
	private OnItemClickListener listener;
	private LayoutInflater inflater;
	private static final int REQUEST_CODE_SCAN = 0x0000;
	private static final int PHOTO_REQUEST_CAMERA = 1;// 拍照
	private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
	
	/* 头像名称 */
	private static final String PHOTO_FILE_NAME = "temp_photo.jpg";
	
	public HeadMenu(Context context) {
		this.context = context; 

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.head_pop_menu, null);
 
		listView = (ListView) view.findViewById(R.id.listView);
		List<Map<String, Object>> list=getData();
		listView.setAdapter(new PopAdapter(context, list));
		listView.setOnItemClickListener(this);

		popupWindow = new PopupWindow(view, 
				context.getResources().getDimensionPixelSize(R.dimen.popmenu_width),  //这里宽度需要自己指定，使用 WRAP_CONTENT 会很大
				LayoutParams.WRAP_CONTENT);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景（很神奇的）
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
	}

	  public List<Map<String, Object>> getData(){  
	        List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();  
	         
	        String itemString[] = {"手机相册","拍照"};//,"保存到手机"
	        
	        for (int i = 0; i < 2; i++) {  
	            Map<String, Object> map=new HashMap<String, Object>();   
	            map.put("title", itemString[i]);   
	            list.add(map);  
	        }  
	        return list;  
	    } 
	
	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (listener != null) {
			listener.onItemClick(position);
		}
		if(position == 0)
		{  
			// 激活系统图库，选择一张图片
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			((Activity)context).startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
			 
		}else if(position == 1)
		{
			//IMUIHelper.openAddFriendActivity(this.context);
			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			// 判断存储卡是否可以用，可用进行存储
			if (hasSdcard()) {
				intent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(new File(Environment
								.getExternalStorageDirectory(), PHOTO_FILE_NAME)));
			}
			
			//Activity activity = (Activity) context;
			((Activity)context).startActivityForResult(intent, PHOTO_REQUEST_CAMERA);
			
		}else if(position == 2)
		{			
			
			
			//
			
		} 
		 
		dismiss();
		
	}
	
	/*
	 * 从相机获取
	 */
	public void camera(View view) {

	}
	
	private boolean hasSdcard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
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
		popupWindow.showAsDropDown(parent, 10,
		// 保证尺寸是根据屏幕像素密度来的
				context.getResources().getDimensionPixelSize(R.dimen.popmenu_yoff));

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
		    
		 public PopAdapter(Context context,List<Map<String, Object>> data){  
		        this.context=context;  
		        this.data=data;  
		        this.layoutInflater =LayoutInflater.from(context);  
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
				convertView = inflater.inflate(R.layout.head_menu_item, null);
				holder = new ViewHolder();
				convertView.setTag(holder); 
				holder.ItemText = (TextView) convertView.findViewById(R.id.textView);
				
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			//holder.groupItem.setText(itemList.get(position));
			//holder.groupItem.setText(itemList.get(position));  
			holder.ItemText.setText((String)data.get(position).get("title")); 

			return convertView;
		}

		private final class ViewHolder { 
			TextView ItemText;
			
		}
	}
}
