package com.fise.xiaoyu.ui.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup; 
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.ui.activity.ActivityReqVerification;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.xiaowei.phone.PhoneMemberBean;

public class SortPhoneMemberAdapter extends BaseAdapter implements SectionIndexer   {
	private List<PhoneMemberBean> list = null;
	private Context mContext;

	public SortPhoneMemberAdapter(Context mContext, List<PhoneMemberBean> list) {
		this.mContext = mContext;
		this.list = list;
	}
 
	public void updateListView(List<PhoneMemberBean> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {
		return this.list.size();
	}

	  public void putPhoneList(List<PhoneMemberBean> list){ 
		  
	        this.list = list;
	        notifyDataSetChanged();
	    }
	  
	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	

	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		final PhoneMemberBean mContent = list.get(position);
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.tt_activity_phone_member_item, null);
			viewHolder.phoneName = (TextView) view.findViewById(R.id.phone_name);
			viewHolder.xiaoWei = (TextView) view.findViewById(R.id.xiao_name);
			viewHolder.img = (IMBaseImageView) view.findViewById(R.id.img);
			
			viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
			
			viewHolder.agree=(Button)view.findViewById(R.id.agree);  
			viewHolder.received_text=(TextView)view.findViewById(R.id.received_text);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		//   ֵ
		int section = getSectionForPosition(position);

		// 
		if (position == getPositionForSection(section)) {
			viewHolder.tvLetter.setVisibility(View.VISIBLE);
			String name = mContent.getSortLetters();
			viewHolder.tvLetter.setText(mContent.getSortLetters());
		} else {
			viewHolder.tvLetter.setVisibility(View.GONE);
		}

		viewHolder.phoneName.setText(this.list.get(position).getName());
		viewHolder.xiaoWei.setText("小雨:" + this.list.get(position).getUserEntity().getMainName());
		//viewHolder.img(this.list.get(position).getName());
		 //绑定数据  
		viewHolder.img.setImageUrl(this.list.get(position).getUserEntity().getAvatar());  
		
		viewHolder.agree.setBackgroundResource(R.drawable.button_tianjia);  
		viewHolder.received_text.setText("已添加");  
		viewHolder.received_text.setVisibility(View.GONE); 

		//不是好友
        if(this.list.get(position).getUserEntity().getIsFriend() == DBConstant.FRIENDS_TYPE_NO)
        {
        	viewHolder.agree.setVisibility(View.VISIBLE);
        	viewHolder.received_text.setVisibility(View.GONE);

			//待验证
        }else  if(this.list.get(position).getUserEntity().getIsFriend() == DBConstant.FRIENDS_TYPE_VERIFY)
        {
        	viewHolder.agree.setVisibility(View.GONE);
        	viewHolder.received_text.setVisibility(View.VISIBLE);
			viewHolder.received_text.setText("待验证");
        }  else // if(toInfo.getIsFriend() == 1)
		{
			viewHolder.agree.setVisibility(View.GONE);
			viewHolder.received_text.setVisibility(View.VISIBLE);
		}


		viewHolder.agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
            	 
            	UserEntity loginUser = IMLoginManager.instance().getLoginInfo(); 
            	String content = "你和" + loginUser.getMainName() + "是朋友,现在可以聊天了";  
            	UserEntity User = list.get(position).getUserEntity();
            	
            	Intent intent = new Intent(mContext,
						ActivityReqVerification.class);
				intent.putExtra(IntentConstant.KEY_PEERID,
						User.getPeerId());
				mContext.startActivity(intent);
				
						
            	//IMUserActionManager.instance().addReqFriends(User,content);
            }
        });
        
         
            
		return view;

	}
	 
	final static class ViewHolder {
		 
		TextView tvLetter;
		public IMBaseImageView img;
		public TextView phoneName;
		public TextView xiaoWei;
		

        public Button agree;  
        public TextView received_text;   
	}
 
	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}
 
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * ��ȡӢ�ĵ�����ĸ����Ӣ����ĸ��#���档
	 * 
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		String sortStr = str.trim().substring(0, 1).toUpperCase();
		// ������ʽ���ж�����ĸ�Ƿ���Ӣ����ĸ
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}
}