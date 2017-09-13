package com.fise.xw.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.DB.entity.WeiEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.protobuf.IMUserAction.ActionResult;
import com.fise.xw.ui.widget.FilletDialog;
import com.fise.xw.ui.widget.IMBaseImageView;

public class NewFriendsAdspter extends BaseAdapter implements
		AdapterView.OnItemLongClickListener {

	private LayoutInflater layoutInflater;
	private Context context;
	private IMService imService;
	public List<WeiEntity> userList = new ArrayList<>();
	public List<WeiEntity> userWeiList = new ArrayList<>();

	public NewFriendsAdspter(Context context, List<WeiEntity> userList,
			List<WeiEntity> userWeiList, IMService imService) {
		this.context = context;
		this.userList = userList;
		this.userWeiList = userWeiList;
		this.layoutInflater = LayoutInflater.from(context);
		this.imService = imService;
	}

	/**
	 * 组件集合，对应list.xml中的控件
	 * 
	 * @author Administrator
	 */
	public final class Zujian {
		public IMBaseImageView image;
		public TextView title;
		public TextView info;
		public Button agree;
		public TextView received_text;
		public TextView req_title;
		public RelativeLayout layout_title;
		public ImageView line_title;

	}

	@Override
	public int getCount() {

		int userSize = userList == null ? 0 : userList.size();
		int userWeiSize = userWeiList == null ? 0 : userWeiList.size();

		return userSize + userWeiSize;
	}

	@Override
	public int getItemViewType(int position) {

		int userSize = userList == null ? 0 : userList.size();
		if (position < userSize)
			return ContactType.FRIENDS.ordinal();

		//
		return ContactType.WEIFRIENDS.ordinal();
	}

	/**
	 * 获得某一位置的数据
	 */
	@Override
	public Object getItem(int position) {
		int typeIndex = getItemViewType(position);
		ContactType renderType = ContactType.values()[typeIndex];
		int userSize = userList == null ? 0 : userList.size();
		switch (renderType) {

		case WEIFRIENDS: {
			if (position < userSize) {
				return userList.get(position);
			}
		}

		case FRIENDS: {
			int realIndex = position - userSize;
			if (realIndex < 0) {
				throw new IllegalArgumentException(
						"ContactAdapter#getItem#user类型判断错误!");
			}
			return userWeiList.get(realIndex);
		}

		default:
			throw new IllegalArgumentException("ContactAdapter#getItem#不存在的类型"
					+ renderType.name());
		}

	}

	/**
	 * 获得唯一标识
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	public void putReqFriendsList(List<WeiEntity> pUserList,
			List<WeiEntity> pUserWeiList) {
		// this.userList.clear();
		this.userList = pUserList;
		this.userWeiList = pUserWeiList;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		int typeIndex = getItemViewType(position);
		ContactType renderType = ContactType.values()[typeIndex];
		View view = null;
		int userSize = userList == null ? 0 : userList.size();

		switch (renderType) {

		case FRIENDS: {
			view = renderFunc(position, convertView, parent);
		}
			break;

		case WEIFRIENDS: {
			int realIndex = position - userSize;
			if (realIndex < 0) {
				throw new IllegalArgumentException(
						"ContactAdapter#getItem#user类型判断错误!");
			}
			view = renderUser(realIndex, convertView, parent);
		}
			break;
		}

		return view;
	}

	public View renderFunc(int position, View convertView, ViewGroup parent) {

		Zujian zujian = null;
		if (convertView == null) {
			zujian = new Zujian();
			// 获得组件，实例化组件
			convertView = layoutInflater.inflate(R.layout.friend_list_item,
					null);

			convertView.setTag(zujian);
		} else {
			zujian = (Zujian) convertView.getTag();
		}

		zujian.image = (IMBaseImageView) convertView.findViewById(R.id.img);
		zujian.title = (TextView) convertView.findViewById(R.id.tv);
		zujian.info = (TextView) convertView.findViewById(R.id.info);
		zujian.agree = (Button) convertView.findViewById(R.id.agree);
		zujian.agree = (Button) convertView.findViewById(R.id.agree);
		zujian.req_title = (TextView) convertView.findViewById(R.id.req_title);
		zujian.received_text = (TextView) convertView
				.findViewById(R.id.received_text);

		zujian.layout_title = (RelativeLayout) convertView
				.findViewById(R.id.layout_title);
		zujian.line_title = (ImageView) convertView
				.findViewById(R.id.line_title);

		zujian.req_title.setText("新的好友");

		if (position == 0) {
			zujian.layout_title.setVisibility(View.VISIBLE);
			zujian.line_title.setVisibility(View.VISIBLE);
		} else {
			zujian.layout_title.setVisibility(View.GONE);
			zujian.line_title.setVisibility(View.GONE);
		}

		UserEntity userEntity = imService.getContactManager().findContact(
				userList.get(position).getFromId());

		if (userEntity == null) {
			userEntity = imService.getContactManager().findReq(
					userList.get(position).getFromId());
		}

		if (userEntity == null) {
			return convertView;
		}

		// 绑定数据
		zujian.image.setImageUrl(userEntity.getAvatar());

		if (userEntity.getComment().equals("")) {
			zujian.title.setText(userEntity.getMainName());
		} else {
			zujian.title.setText(userEntity.getComment());
		}

		final WeiEntity entity = userList.get(position);
		if (entity != null) {
			if (entity.getMasgData().equals("")) {
				zujian.info.setText("我是:" + userEntity.getMainName());
			} else {
				zujian.info.setText("" + entity.getMasgData());
			}

		} else {
			zujian.info.setText("我是:" + userEntity.getMainName());
		}

		 
		zujian.agree.setBackgroundResource(R.drawable.button_accept);

		zujian.received_text.setText("已添加");
		zujian.received_text.setVisibility(View.GONE);

		 
	
		if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_NO) {
			
			if(entity!=null){
				//如果这条请求未处理
				if(entity.getStatus() == DBConstant.FRIENDS_REQ_NO){
					zujian.agree.setVisibility(View.VISIBLE);
					zujian.received_text.setVisibility(View.GONE);
				}else { 
					zujian.agree.setVisibility(View.GONE);
					zujian.received_text.setVisibility(View.VISIBLE);
				}
			}else{ 
				zujian.agree.setVisibility(View.VISIBLE);
				zujian.received_text.setVisibility(View.GONE);
			}
		 
		} else {
			zujian.agree.setVisibility(View.GONE);
			zujian.received_text.setVisibility(View.VISIBLE);
		}
		
		 

		final UserEntity userEntityTemp = userEntity;
		zujian.agree.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String content = "你们是朋友 ,现在可以相互聊天了";
				imService.getUserActionManager().confirmFriends(
						userEntityTemp.getPeerId(), entity.getActId(),
						entity.getActType(), ActionResult.ACTION_RESULT_YES,
						entity, content, userEntityTemp);

			}
		});

		return convertView;
	}

	public View renderUser(int position, View convertView, ViewGroup parent) {

		Zujian zujian = null;
		if (convertView == null) {
			zujian = new Zujian();
			// 获得组件，实例化组件
			convertView = layoutInflater.inflate(R.layout.friend_list_item,
					null);
			convertView.setTag(zujian);

		} else {
			zujian = (Zujian) convertView.getTag();
		}

		zujian.image = (IMBaseImageView) convertView.findViewById(R.id.img);
		zujian.title = (TextView) convertView.findViewById(R.id.tv);
		zujian.info = (TextView) convertView.findViewById(R.id.info);
		zujian.agree = (Button) convertView.findViewById(R.id.agree);
		zujian.req_title = (TextView) convertView.findViewById(R.id.req_title);
		zujian.received_text = (TextView) convertView
				.findViewById(R.id.received_text);

		zujian.layout_title = (RelativeLayout) convertView
				.findViewById(R.id.layout_title);
		zujian.line_title = (ImageView) convertView
				.findViewById(R.id.line_title);

		UserEntity userEntity = imService.getContactManager()
				.findFriendsContact(userWeiList.get(position).getFromId());
		if (userEntity == null) {
			userEntity = imService.getContactManager().findContact(
					userWeiList.get(position).getFromId());
		}

		if (userEntity == null) {
			return convertView;
		}

		if (position == 0) {
			zujian.layout_title.setVisibility(View.VISIBLE);
			zujian.line_title.setVisibility(View.VISIBLE);
		} else {
			zujian.layout_title.setVisibility(View.GONE);
			zujian.line_title.setVisibility(View.GONE);
		}

		// 绑定数据
		zujian.image.setImageUrl(userEntity.getAvatar());
		if (userEntity.getComment().equals("")) {
			zujian.title.setText(userEntity.getMainName());
		} else {
			zujian.title.setText(userEntity.getComment());
		}

		final WeiEntity entity = userWeiList.get(position);

		if (userEntity.getComment().equals("")) {
			zujian.info.setText(userEntity.getMainName() + "请求你为位友");
		} else {
			zujian.info.setText(userEntity.getComment() + "请求你为位友");
		}

		zujian.req_title.setText("新的位友");

		zujian.agree.setBackgroundResource(R.drawable.button_accept);
		zujian.received_text.setText("已同意");
		zujian.received_text.setVisibility(View.GONE);

		/*
		if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YES) {

			zujian.agree.setVisibility(View.VISIBLE);
			zujian.received_text.setVisibility(View.GONE);
		} else {
			zujian.agree.setVisibility(View.GONE);
			zujian.received_text.setVisibility(View.VISIBLE);
		}
*/
		if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_WEI) {
	
			zujian.agree.setVisibility(View.GONE);
			zujian.received_text.setVisibility(View.VISIBLE);
			
		}else{
			
			if(entity!=null){
				
				//如果这条请求未处理
				if(entity.getStatus() == DBConstant.FRIENDS_REQ_NO){
					zujian.agree.setVisibility(View.VISIBLE);
					zujian.received_text.setVisibility(View.GONE);
				}else{
					zujian.agree.setVisibility(View.GONE);
					zujian.received_text.setVisibility(View.VISIBLE);
				}
			}else{
				zujian.agree.setVisibility(View.VISIBLE);
				zujian.received_text.setVisibility(View.GONE);
			}
		}	
		 			
		zujian.agree.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				final FilletDialog myDialog = new FilletDialog(context);
				// myDialog.setTitle(title);//设置标题
				myDialog.setMessage("确定通过对方位友的申请，对方将看到你的位置等详细信息");// 设置内容 
				myDialog.dialog.show();// 显示 
				// 确认按键回调，按下确认后在此做处理
				myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
					@Override
					public void ok() {

						int toActId = 0;
						int actId = 0;
						int actType = 0;

						toActId = entity.getFromId();
						actId = entity.getActId();
						actType = entity.getActType();
						imService.getUserActionManager().confirmWeiFriends(
								toActId, actId, actType,
								ActionResult.ACTION_RESULT_YES, entity, "");

						myDialog.dialog.dismiss();
					}
				});

			}
		});

		return convertView;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			int position, long arg3) {

		// TODO Auto-generated method stub
		// When clicked, show a toast with the TextView text

		int type = 0;
		Object object;
		int realIndex = position - userList.size();

		if (realIndex >= 0) {
			object = userWeiList.get(realIndex);
			type = 1;
		} else {
			object = userList.get(position);
			type = 2;
		}

		final int tempType = type;
		if (object instanceof WeiEntity) {

			final WeiEntity Entity = (WeiEntity) object;
			AlertDialog.Builder builder = new AlertDialog.Builder(
					new ContextThemeWrapper(context,
							android.R.style.Theme_Holo_Light_Dialog));

			String temp = "";
			if (tempType == 1) {
				temp = "删除位友请求";
			} else if (tempType == 2) {
				temp = "删除好友请求";
			}
			String[] items = new String[] { temp };

			// String[] items = new String[] { NewFriendActivity.this
			// .getString(R.string.delete_device) };

			builder.setItems(items, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:

						if (imService != null) {
							imService.getUserActionManager().deleteReqFriends(
									tempType, Entity);

							int toActId = 0;
							int actId = 0;
							int actType = 0;

							toActId = Entity.getFromId();
							actId = Entity.getActId();
							actType = Entity.getActType();

							imService.getUserActionManager().confirmWeiFriends(
									toActId, actId, actType,
									ActionResult.ACTION_RESULT_DELETE, Entity,
									"");

						}
						break;
					}
				}
			});
			AlertDialog alertDialog = builder.create();
			alertDialog.setCanceledOnTouchOutside(true);
			alertDialog.show();

		}
		return true;

	}

	private enum ContactType {
		FRIENDS, WEIFRIENDS// ,
		// GROUP
	}
}
