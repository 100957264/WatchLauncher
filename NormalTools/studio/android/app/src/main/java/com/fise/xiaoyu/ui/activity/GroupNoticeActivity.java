package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.GroupEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ChangeDataType;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.FilletDialog;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;


/** 
 *  发群公告界面
 */
@SuppressLint("UseValueOf")
public class GroupNoticeActivity extends TTBaseActivity {
	private static IMService imService;
	private GroupEntity groupInfo;
	private PeerEntity peerEntity;
	private String curSessionKey;
	private TextView userName;
	private TextView TimeName;
	private IMBaseImageView user_portrait;
	private UserEntity currentUser;
	private EditText notice_text;
	private TextView show_notice_text;
	

	private RelativeLayout layout_title;
	private View layout_Right;
	private View layout_left;
	private TextView text_num;


	//问题最大字数
	private int num = 256;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			curSessionKey = GroupNoticeActivity.this.getIntent()
					.getStringExtra(IntentConstant.KEY_SESSION_KEY);
			peerEntity = imService.getSessionManager().findPeerEntity(
					curSessionKey);

			if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
				groupInfo = (GroupEntity) peerEntity;
				initDetailProfile();
			}

		}

		@Override
		public void onServiceDisconnected() {

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.group_notice_activity);
		imServiceConnector.connect(this);

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				GroupNoticeActivity.this.finish();
			}
		});
		//
		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				GroupNoticeActivity.this.finish();
			}
		});

	
		Button icon_user_info = (Button) findViewById(R.id.icon_user_info);
		icon_user_info.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				final String notice_string = notice_text.getText()
						.toString();
				
				if(notice_string.equals("")){

					Utils.showToast(GroupNoticeActivity.this, "请输入公告内容");
				}else{

					final FilletDialog myDialog = new FilletDialog(GroupNoticeActivity.this ,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITH_MESSAGE);
					myDialog.setTitle(getString(R.string.device_prompt));
					myDialog.setMessage("该公告会通知全部成员,是否发布?");//设置内容
					myDialog.dialog.show();//显示
					myDialog.setRight("发布");

					//确认按键回调，按下确认后在此做处理
					myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
						@Override
						public void ok() {
							imService
									.getGroupManager()
									.modifyChangeGroupMember(
											groupInfo.getPeerId(),
											ChangeDataType.CHANGE_GROUP_NOTICE_BOARD,
											notice_string, groupInfo);

							notice_text.setCursorVisible(false);
							myDialog.dialog.dismiss();
						}
					});

					
				}
				
			

			}
		});

		userName = (TextView) findViewById(R.id.userName);
		TimeName = (TextView) findViewById(R.id.TimeName);

		notice_text = (EditText) findViewById(R.id.notice_text);
		show_notice_text = (TextView) findViewById(R.id.show_notice_text);
		
		

		user_portrait = (IMBaseImageView) findViewById(R.id.user_portrait);

		layout_title = (RelativeLayout) findViewById(R.id.layout_title);
		layout_left = (View) findViewById(R.id.layout_left);
		layout_Right = (View) findViewById(R.id.layout_Right);


		
		notice_text.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 将编辑框的内容重置为空
				notice_text.setCursorVisible(true);

			} 
		});

		text_num = (TextView) findViewById(R.id.text_num);
		int lenth = notice_text.length();
		text_num.setText((num - notice_text.getText().length()) +"");

		notice_text.addTextChangedListener(new TextWatcher() {
			private CharSequence temp;
			private int selectionStart;
			private int selectionEnd;

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				temp = s;
			}

			@Override
			public void afterTextChanged(Editable s) {

				int number = num - s.length();
			//	text_num.setText(number+"/" +num);
				text_num.setText(number +"");
				selectionStart = notice_text.getSelectionStart();
				selectionEnd = notice_text.getSelectionEnd();
				//删除多余输入的字（不会显示出来）
				if (temp.length() > num) {
					s.delete(selectionStart - 1, selectionEnd);
					notice_text.setText(s);
					//设置光标在最后
					notice_text.setSelection(s.length());
				}

			}
		});
	}

	public void initDetailProfile() {

		ProgressBar progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
		progress_bar.setVisibility(View.GONE);

		if (groupInfo.getCreatorId() != imService.getLoginManager()
				.getLoginId()) {
			Button icon_user_info = (Button) findViewById(R.id.icon_user_info);
			icon_user_info.setVisibility(View.GONE);

			layout_title.setVisibility(View.VISIBLE);
			layout_left.setVisibility(View.VISIBLE);
			layout_Right.setVisibility(View.VISIBLE);

			// notice_text.setVisibility(View.GONE);
			notice_text.setEnabled(false);
			notice_text.setInputType(InputType.TYPE_NULL);
			
			
			

		} else {
			layout_title.setVisibility(View.GONE);
			layout_left.setVisibility(View.GONE);
			layout_Right.setVisibility(View.GONE);

			// notice_text.setVisibility(View.VISIBLE);

		}

		currentUser = imService.getContactManager().findContact(
				groupInfo.getCreatorId());

		if (currentUser != null) {
			user_portrait.setImageUrl(currentUser.getAvatar());

			if (currentUser.getComment().equals("")) {
				userName.setText("" + currentUser.getMainName());
			} else {
				userName.setText("" + currentUser.getComment());
			}

			if (groupInfo.getBoardTime().equals("")) {
				TimeName.setVisibility(View.GONE);
			} else {

				TimeName.setVisibility(View.VISIBLE);
				// SimpleDateFormat sdf = new
				// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				// String time = sdf.format(new
				// Date(Long.parseLong(groupInfo.getBoardTime())));
				// TimeName.setText("" + time);
				long timeLong = Long.parseLong(groupInfo.getBoardTime());
				String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date(timeLong * 1000));
				TimeName.setText("" + date);
			}
			notice_text.setText("" + groupInfo.getBoard());
			notice_text.setSelection(notice_text.getText().toString().length());// 将光标移至文字末尾

			
			if (groupInfo.getCreatorId() != imService.getLoginManager()
					.getLoginId()) {
				notice_text.setVisibility(View.GONE);
				show_notice_text.setVisibility(View.VISIBLE);
				show_notice_text.setText("" + groupInfo.getBoard());
			}else{
				notice_text.setVisibility(View.VISIBLE);
				show_notice_text.setVisibility(View.GONE);
			}
		}

	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			initDetailProfile();
			break;

		case USER_QR_CODE_SAVE:
			break;
	 
			
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(GroupEvent event) {
		switch (event.getEvent()) {

		case CHANGE_GROUP_NOTICE_SUCCESS:
			initDetailProfile();
			GroupNoticeActivity.this.finish();
			break;
		case CHANGE_GROUP_EXIT_SUCCESS:
			groupInfo = imService.getGroupManager().findGroup(groupInfo.getPeerId());
			initDetailProfile(); 
			break; 
			
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
				imServiceConnector.disconnect(this);
		super.onDestroy();
	}

	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
}
