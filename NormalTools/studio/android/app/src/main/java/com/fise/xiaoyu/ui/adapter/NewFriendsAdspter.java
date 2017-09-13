package com.fise.xiaoyu.ui.adapter;

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

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.entity.WeiEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.protobuf.IMUserAction.ActionResult;
import com.fise.xiaoyu.ui.widget.FilletDialog;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class NewFriendsAdspter extends BaseAdapter implements
		AdapterView.OnItemLongClickListener {

	private LayoutInflater layoutInflater;
	private Context context;
	private IMService imService;
	public List<WeiEntity> userList = new ArrayList<>();

	public NewFriendsAdspter(Context context, List<WeiEntity> userList, IMService imService) {
		this.context = context;
		this.userList = userList;
		this.layoutInflater = LayoutInflater.from(context);
		this.imService = imService;
	}

	/**
	 * 组件集合，对应list.xml中的控件
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

		return userSize ;
	}

	@Override
	public int getItemViewType(int position) {

		int userSize = userList == null ? 0 : userList.size();
		//
		return ContactType.FRIENDS.ordinal();
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

		case FRIENDS: {
			if (position < userSize) {
				return userList.get(position);
			}
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

	public void putReqFriendsList(List<WeiEntity> pUserList) {
		// this.userList.clear();
		this.userList = pUserList;
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

		 
	
		if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_NO
				||userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_VERIFY) {
			
			if(entity!=null){
				//如果这条请求未处理
				if(entity.getStatus() == DBConstant.FRIENDS_PENDING_REVIEW){
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

				if(Utils.isClientType(imService.getLoginManager().getLoginInfo()))
				{


					final FilletDialog myDialog = new FilletDialog(context ,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITHOUT_MESSAGE);

					myDialog.setTitle("同意好友需要家长同意");//
					myDialog.dialog.show();//显示

					//确认按键回调，按下确认后在此做处理
					myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
						@Override
						public void ok() {

							myDialog.dialog.dismiss();

							String content = "你们是朋友 ,现在可以相互聊天了";
							imService.getUserActionManager().confirmFriends(
									userEntityTemp.getPeerId(), entity.getActId(),
									entity.getActType(), ActionResult.ACTION_RESULT_YES,
									entity, content, userEntityTemp);

						}
					});

				}else{
					String content = "你们是朋友 ,现在可以相互聊天了";
					imService.getUserActionManager().confirmFriends(
							userEntityTemp.getPeerId(), entity.getActId(),
							entity.getActType(), ActionResult.ACTION_RESULT_YES,
							entity, content, userEntityTemp);
				}

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
		//int realIndex = position - userList.size();

		object = userList.get(position);
		type = DBConstant.DETLE_REQ_TYPE;


		 int tempType = type;
		if (object instanceof WeiEntity) {

			final WeiEntity Entity = (WeiEntity) object;
			AlertDialog.Builder builder = new AlertDialog.Builder(
					new ContextThemeWrapper(context,
							android.R.style.Theme_Holo_Light_Dialog));


			if(Entity.getFromId() == IMLoginManager.instance().getLoginId()){
				tempType = DBConstant.DETLE_PARENT_REFUSE;
			}

			String temp = "";
			if (tempType == DBConstant.DETLE_REQ_TYPE_YU) {
				temp = "删除雨友请求";
			} else if (tempType == DBConstant.DETLE_REQ_TYPE) {
				temp = "删除好友请求";
			}else if (tempType == DBConstant.DETLE_PARENT_REFUSE) {
				temp = "删除请求";
			}

			String[] items = new String[] { temp };

			// String[] items = new String[] { NewFriendActivity.this
			// .getString(R.string.delete_device) };

			final int finalTempType = tempType;
			builder.setItems(items, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:

						if (imService != null)
						{
							imService.getUserActionManager().deleteReqFriends(
									finalTempType, Entity);

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
		FRIENDS// ,
		// GROUP
	}
}
