package com.fise.xiaoyu.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.DBInterface;
import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.GroupNickEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.sp.ConfigurationSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.entity.RecentInfo;
import com.fise.xiaoyu.imservice.event.GroupEvent;
import com.fise.xiaoyu.imservice.event.SessionEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMSessionManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ChangeDataType;
import com.fise.xiaoyu.ui.activity.MessageActivity;
import com.fise.xiaoyu.ui.activity.PostionTouchActivity;
import com.fise.xiaoyu.ui.activity.SettingMessageBgActivity;
import com.fise.xiaoyu.ui.adapter.GroupManagerAdapter;
import com.fise.xiaoyu.ui.helper.CheckboxConfigHelper;
import com.fise.xiaoyu.ui.widget.FilletDialog;
import com.fise.xiaoyu.ui.widget.PassDialog;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.photoselector.model.PhotoModel;
import com.photoselector.ui.PhotoSelectorActivity;
import com.photoselector.util.CommonUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @YM 个人与群组的聊天详情都会来到这个页面 single: 这有sessionId的头像，以及加号"+" ， 创建群成功之后，跳到聊天的页面
 *     group: 群成员，加减号 ， 修改成功之后，跳到群管理页面
 *     临时群任何人都可以加人，但是只有群主可以踢人”这个逻辑修改下，正式群暂时只给createId开放
 */
public class GroupManagerFragment extends MainFragment {
	private View curView = null;
	/** adapter配置 */
	private GridView gridView;
	private GroupManagerAdapter adapter;

	/** 详情的配置 勿扰以及指定聊天 */
	CheckboxConfigHelper checkBoxConfiger = new CheckboxConfigHelper();
	CheckBox noDisturbCheckbox;
	CheckBox topSessionCheckBox;
	CheckBox saveCheckBox;
	CheckBox group_nicknameCheckbox;



	View  go_xiao_wei_line;
	RelativeLayout go_xiao_wei;
	RelativeLayout save_wei;
	View  save_wei_line;

	View group_nickname_line;
	RelativeLayout messageBg;
	RelativeLayout group_nickname;
	RelativeLayout group_manager_nick;

	View group_manager_nick_line;

	RelativeLayout group_manager_notice;
	TextView group_manager_notice_content;
	TextView group_manager_right;

	View group_xiao_wei;
	View group_line1;
	View group_line2;

	View top_message_line;


	TextView group_manage_title;

	/** 需要的状态参数 */
	private IMService imService;
	private String curSessionKey;
	private PeerEntity peerEntity;
	private Button delete_group_btn;
	private GroupNickEntity entity;
	private RelativeLayout group_manager_name;
	private RelativeLayout group_manager_qr;
	private RelativeLayout group_manager_notice1;
    private int loginId;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onServiceDisconnected() {
		}

		@Override
		public void onIMServiceConnected() {
			logger.d("groupmgr#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				Utils.showToast(
						GroupManagerFragment.this.getActivity(),
						getResources().getString(
								R.string.im_service_disconnected));
				return;
			}

			checkBoxConfiger.init(imService.getConfigSp());
			initView();
			initAdapter();
            loginId = imService.getLoginManager().getLoginId();
			if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {

				final GroupEntity groupEntity = (GroupEntity) peerEntity;
				//如果是家庭群
				if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {

					delete_group_btn.setVisibility(View.GONE);
					group_xiao_wei.setVisibility(View.GONE);
					go_xiao_wei_line.setVisibility(View.GONE);
                    go_xiao_wei.setVisibility(View.GONE);
					group_manager_name.setVisibility(View.GONE);
					group_manager_notice.setVisibility(View.GONE);
                    find_message.setVisibility(View.GONE);
                    clean_file.setVisibility(View.GONE);

					View group_manager_qr_line = curView.findViewById(R.id.group_manager_qr_line);
					group_manager_qr_line.setVisibility(View.GONE);

					View group_manager_name_line = (View) curView.findViewById(R.id.group_manager_name_line);
					group_manager_name_line.setVisibility(View.GONE);

					View find_message_line = (View) curView.findViewById(R.id.find_message_line);
					find_message_line.setVisibility(View.GONE);

					View setting_message_bg_line = (View) curView.findViewById(R.id.setting_message_bg_line);
					setting_message_bg_line.setVisibility(View.GONE);

//					View clean_file_line = (View) curView.findViewById(R.id.clean_file_line);
//					clean_file_line.setVisibility(View.GONE);

					View group_managername_top_line = (View) curView.findViewById(R.id.group_managername_top_line);
					group_managername_top_line.setVisibility(View.GONE);


					View top_message_line = (View) curView.findViewById(R.id.top_message_line);
					top_message_line.setVisibility(View.VISIBLE);

				}else if (delete_group_btn != null) {

					go_xiao_wei_line.setVisibility(View.VISIBLE);
					delete_group_btn.setVisibility(View.VISIBLE);
					go_xiao_wei.setVisibility(View.VISIBLE);
					group_manager_name.setVisibility(View.VISIBLE);
					group_manager_notice.setVisibility(View.VISIBLE);
					find_message.setVisibility(View.VISIBLE);
					clean_file.setVisibility(View.VISIBLE);
				}

				RelativeLayout	check_group_user = (RelativeLayout) curView.findViewById(R.id.check_group_user);

				if(groupEntity.getlistGroupMemberIds().size()>DBConstant.GROUP_USER_NUM){
					check_group_user.setVisibility(View.VISIBLE);
				}else{
					check_group_user.setVisibility(View.GONE);
				}


				check_group_user.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						IMUIHelper.openGroupMemberActivity(GroupManagerFragment.this
								.getActivity(),groupEntity.getPeerId());

					}
				});


			} else {
				if (delete_group_btn != null) {
					delete_group_btn.setVisibility(View.GONE);
				}

				RelativeLayout	check_group_user = (RelativeLayout) curView.findViewById(R.id.check_group_user);
				check_group_user.setVisibility(View.GONE);


			}

			if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
				final GroupEntity groupEntity1 = (GroupEntity) peerEntity;

				UserEntity loginInfo = imService.getLoginManager()
						.getLoginInfo();
				entity = imService.getGroupManager().findGroupNick(
						groupEntity1.getPeerId(), loginInfo.getPeerId());

				if (entity == null) {
					ArrayList<Integer> userIds = new ArrayList<>();
					Set<Integer> set = groupEntity1.getlistGroupMemberIds();
					for (Integer s : set) {
						userIds.add(s);
					}
					imService.getContactManager().reqGetGroupNick(userIds,
							groupEntity1.getPeerId(), true);
				}

			}

		}
	};
	private RelativeLayout clean_file;
    private RelativeLayout find_message;
	private View go_xiao_wei_line_height;
	private View notificationNoDisturb_height_line;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imServiceConnector.connect(getActivity());
			}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_group_manage,
				topContentView);
		notificationNoDisturb_height_line = curView.findViewById(R.id.NotificationNoDisturb_height_line);
		noDisturbCheckbox = (CheckBox) curView
				.findViewById(R.id.NotificationNoDisturbCheckbox);
		topSessionCheckBox = (CheckBox) curView
				.findViewById(R.id.NotificationTopMessageCheckbox);
		save_wei = (RelativeLayout) curView.findViewById(R.id.save_wei);
		save_wei_line = (View) curView.findViewById(R.id.save_wei_line);

		messageBg = (RelativeLayout) curView.findViewById(R.id.message_bg);

		group_nickname = (RelativeLayout) curView
				.findViewById(R.id.group_nickname);
		group_nickname_line= (View) curView
				.findViewById(R.id.group_nickname_line);


		group_manager_nick = (RelativeLayout) curView
				.findViewById(R.id.group_manager_nick);

		group_manager_notice = (RelativeLayout) curView
				.findViewById(R.id.group_manager_notice);

		group_manager_notice_content = (TextView) curView
				.findViewById(R.id.group_manager_notice_content);

		group_manager_right = (TextView) curView
				.findViewById(R.id.group_manager_right);

		group_line1 = (View) curView.findViewById(R.id.group_line1);
		group_line2 = (View) curView.findViewById(R.id.group_line2);
		group_manager_nick_line = (View) curView
				.findViewById(R.id.group_manager_nick_line);

		group_manage_title = (TextView) curView
				.findViewById(R.id.group_manage_title);

		go_xiao_wei_line = (View) curView.findViewById(R.id.go_xiao_wei_line);
		group_xiao_wei = (View) curView.findViewById(R.id.group_xiao_wei);
		top_message_line = (View) curView.findViewById(R.id.top_message_line);

		go_xiao_wei = (RelativeLayout) curView.findViewById(R.id.go_xiao_wei);
		go_xiao_wei_line_height = curView.findViewById(R.id.go_xiao_wei_line_height);
		go_xiao_wei.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {

					final GroupEntity groupEntity = (GroupEntity) peerEntity;
					if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_TEMP) {

						ArrayList<Integer> infoList = new ArrayList<Integer>();
						Set<Integer> set = groupEntity.getlistGroupMemberIds();
						for (Integer s : set) {
							infoList.add(s);
						}

						Intent intent = new Intent(GroupManagerFragment.this
								.getActivity(), PostionTouchActivity.class);
//						intent.putExtra(IntentConstant.POSTION_TYPE,
//								DBConstant.POSTION_WEI);
						intent.putIntegerArrayListExtra("infoList", infoList);
						GroupManagerFragment.this.getActivity().startActivity(
								intent);
					}

				} else if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
					UserEntity userEntity = (UserEntity) peerEntity;
					if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
						Intent intent = new Intent(GroupManagerFragment.this
								.getActivity(), PostionTouchActivity.class);
//                        intent.putExtra(IntentConstant.POSTION_TYPE,
//                                DBConstant.POSTION_INFO_WEI);
                        intent.putExtra(IntentConstant.DEV_USER_ID,
                                peerEntity.getPeerId());
						intent.putExtra(IntentConstant.POSTION_LAT,
								userEntity.getLatitude());
						intent.putExtra(IntentConstant.POSTION_LNG,
								userEntity.getLongitude());
						GroupManagerFragment.this.getActivity().startActivity(
								intent);
					}

				}

			}
		});

		group_manager_nick.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub


				final PassDialog myDialog = new PassDialog(GroupManagerFragment.this
						.getActivity(),PassDialog.PASS_DIALOG_TYPE.PASS_DIALOG_WITHOUT_MESSAGE);
				myDialog.setTitle("群聊昵称");//设置内容
				myDialog.dialog.show();//显示


				//确认按键回调，按下确认后在此做处理
				myDialog.setMyDialogOnClick(new PassDialog.MyDialogOnClick() {
					@Override
					public void ok() {

						String input = myDialog.getEditText().getText().toString();
						if (input.equals("")) {

							Utils.showToast(GroupManagerFragment.this
									.getActivity(),"不能为空值");
						} else {
							final GroupEntity groupEntity = (GroupEntity) peerEntity;
							imService
									.getGroupManager()
									.modifyChangeGroupMember(
											groupEntity
													.getPeerId(),
											ChangeDataType.CHANGE_GROUP_USER_UPDATE_NICK,
											input, groupEntity);
							myDialog.dialog.dismiss();
						}


					}
				});

			}
		});

		messageBg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				IMUIHelper.openMessageBgActivity(GroupManagerFragment.this
						.getActivity());

			}
		});

		RelativeLayout setting_message_bg = (RelativeLayout) curView
				.findViewById(R.id.setting_message_bg);
		setting_message_bg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(GroupManagerFragment.this
						.getActivity(), SettingMessageBgActivity.class);
				intent.putExtra(IntentConstant.KEY_SESSION_KEY, curSessionKey);
				intent.putExtra(IntentConstant.KEY_ALL_MESSAGE_BG, false);
				GroupManagerFragment.this.getActivity().startActivity(intent);

			}
		});

		clean_file = (RelativeLayout) curView
				.findViewById(R.id.clean_file);
		clean_file.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CommonUtils.launchActivityForResult(
						GroupManagerFragment.this.getActivity(),
						PhotoSelectorActivity.class, 0, peerEntity.getPeerId());


			}
		});

		RelativeLayout clean_message = (RelativeLayout) curView.findViewById(R.id.clean_message);
		clean_message.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub


				final FilletDialog myDialog = new FilletDialog(GroupManagerFragment.this.getActivity() ,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITHOUT_MESSAGE);
				myDialog.setTitle(getString(R.string.delete_chatting_records_notice));//设置内容
				myDialog.dialog.show();//显示

				//确认按键回调，按下确认后在此做处理
				myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
					@Override
					public void ok() {
						UserEntity user = imService.getLoginManager().getLoginInfo();
						//IMSessionManager.instance().reqRemoveSessionAll(user, DBConstant.SESSION_MESSAGE_ALL);
						String session = peerEntity.getSessionKey();
								List<RecentInfo> recentSessionList = IMSessionManager
										.instance().getRecentListInfo();
								for (int i = 0; i < recentSessionList.size(); i++) {
									if (recentSessionList.get(i).getSessionKey().equals(session)) {
										IMSessionManager.instance().reqRemoveSession(recentSessionList.get(i),
														DBConstant.SESSION_MESSAGE);
									}
								}
						 myDialog.dialog.dismiss();
							}


				});



			}
		});

        find_message = (RelativeLayout) curView
                .findViewById(R.id.find_message);
		find_message.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				IMUIHelper.openSearchMessageActivity(
						GroupManagerFragment.this.getActivity(),
						peerEntity.getSessionKey());

			}
		});



		delete_group_btn = (Button) curView.findViewById(R.id.delete_group_btn);


		delete_group_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				final FilletDialog myDialog = new FilletDialog(GroupManagerFragment.this.getActivity() ,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITH_MESSAGE);
				myDialog.setTitle(GroupManagerFragment.this.getActivity().getString(R.string.delete_group));//设置内容
				myDialog.setMessage(GroupManagerFragment.this.getActivity().getString(R.string.delete_group_hint));
				myDialog.dialog.show();//显示

				//确认按键回调，按下确认后在此做处理
				myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
					@Override
					public void ok() {
						if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
							final GroupEntity groupEntity = (GroupEntity) peerEntity;

							imService.getGroupManager().deleteGroupMember(
									groupEntity.getPeerId(),
									ChangeDataType.CHANGE_GROUP_USER_EXIT,
									String.valueOf(1), groupEntity);

						}
					}
				});


			}
		});

		initRes();
		return curView;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			if (data != null && data.getExtras() != null) {
				@SuppressWarnings("unchecked")
				List<PhotoModel> photos = (List<PhotoModel>) data.getExtras()
						.getSerializable("photos");
				if (photos == null || photos.isEmpty())
					return;
				StringBuffer sb = new StringBuffer();
				for (PhotoModel photo : photos) {
					sb.append(photo.getOriginalPath() + "\r\n");
				}
			}

		}
	}

	private void initRes() {
		// 设置标题栏
		this.hideTopBar();
		setTopLeftButton(R.drawable.button_back);
		setTopLeftText(getActivity().getString(R.string.top_left_back));
		topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getActivity().finish();
			}
		});

		ImageView btn = (ImageView) curView.findViewById(R.id.left_btn_tt);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getActivity().finish();
			}
		});

		TextView left_txt = (TextView) curView.findViewById(R.id.left_txt_tt);
		left_txt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getActivity().finish();
			}
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	/**
	 * Called when the fragment is no longer in use. This is called after
	 * {@link #onStop()} and before {@link #onDetach()}.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
				imServiceConnector.disconnect(getActivity());
	}

	@Override
	protected void initHandler() {
	}



	private void initView() {
		setTopTitle(getString(R.string.chat_detail));
		if (null == imService || null == curView) {
			logger.e("groupmgr#init failed,cause by imService or curView is null");
			return;
		}

		curSessionKey = getActivity().getIntent().getStringExtra(
				IntentConstant.KEY_SESSION_KEY);
		if (TextUtils.isEmpty(curSessionKey)) {
			logger.e("groupmgr#getSessionInfoFromIntent failed");
			return;
		}
		peerEntity = imService.getSessionManager()
				.findPeerEntity(curSessionKey);
		if (peerEntity == null) {
			logger.e("groupmgr#findPeerEntity failed,sessionKey:%s",
					curSessionKey);
			return;
		}
		switch (peerEntity.getType()) {
		case DBConstant.SESSION_TYPE_GROUP: {
			final GroupEntity groupEntity = (GroupEntity) peerEntity;
			// 群组名称的展示
			TextView groupNameView = (TextView) curView
					.findViewById(R.id.group_manager_title);
			groupNameView.setText(groupEntity.getMainName());
			saveCheckBox = (CheckBox) curView
					.findViewById(R.id.NotificationSaveCheckbox);
			group_nicknameCheckbox = (CheckBox) curView
					.findViewById(R.id.group_nicknameCheckbox);


			if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY) {
				group_manager_notice.setVisibility(View.GONE);
				go_xiao_wei.setVisibility(View.GONE);
				go_xiao_wei_line_height.setVisibility(View.GONE);
				go_xiao_wei_line.setVisibility(View.GONE);
			}else{

				group_manager_notice.setVisibility(View.VISIBLE);
				go_xiao_wei.setVisibility(View.VISIBLE);
				go_xiao_wei_line.setVisibility(View.VISIBLE);

			}

			if (groupEntity.getBoard().equals("")) {
				group_manager_notice_content.setVisibility(View.GONE);
				group_manager_right.setText("未设置");
			} else {
				group_manager_notice_content.setVisibility(View.VISIBLE);
				group_manager_notice_content.setText(groupEntity.getBoard());
				group_manager_right.setVisibility(View.GONE);
			}

			UserEntity loginInfo = imService.getLoginManager().getLoginInfo();
			entity = imService.getGroupManager().findGroupNick(
					groupEntity.getPeerId(), loginInfo.getPeerId());

			boolean isGroupNick = false;
			if (entity != null) {

				if (entity.getStatus() == DBConstant.SHOW_GROUP_NICK_CLOSE) {
					isGroupNick = false;
				} else {
					isGroupNick = true;
				}

			}
			group_nicknameCheckbox.setChecked(isGroupNick);
			// 绑定监听器
			group_nicknameCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton arg0,
								boolean arg1) {
							// TODO Auto-generated method stub
							if (groupEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
								if (arg1) {
									if (entity != null) {
										entity.setStatus(DBConstant.SHOW_GROUP_NICK_OPEN);

										MessageActivity.isShowNick = true;
										int timeNow = (int) (System
												.currentTimeMillis() / 1000);
										entity.setUpdated(timeNow);
										DBInterface
												.instance()
												.insertOrUpdateGroupNick(entity);

										adapter.updateNiick(entity);

										triggerEvent(new GroupEvent(
												GroupEvent.Event.CHANGE_GROUP_NICK_SUCCESS));
									}

								} else {
									if (entity != null) {
										entity.setStatus(DBConstant.SHOW_GROUP_NICK_CLOSE);
										MessageActivity.isShowNick = false;

										int timeNow = (int) (System
												.currentTimeMillis() / 1000);
										entity.setUpdated(timeNow);
										DBInterface
												.instance()
												.insertOrUpdateGroupNick(entity);
										adapter.updateNiick(entity);

										triggerEvent(new GroupEvent(
												GroupEvent.Event.CHANGE_GROUP_NICK_SUCCESS));
									}
								}
							}

						}
					});

			// 修改群昵称
			group_manager_name = (RelativeLayout) curView
					.findViewById(R.id.group_manager_name);
			group_manager_name.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					IMUIHelper.openGroupNameActivity(
							GroupManagerFragment.this.getActivity(),
							curSessionKey);

				}
			});

			// 群的二维码
			group_manager_qr = (RelativeLayout) curView
					.findViewById(R.id.group_manager_QR);
			group_manager_qr.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					IMUIHelper.openGroupQRActivity(
							GroupManagerFragment.this.getActivity(),
							curSessionKey);

				}
			});

			// 群的公告　
			group_manager_notice.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					IMUIHelper.openGroupNoticeActivity(
							GroupManagerFragment.this.getActivity(),
							curSessionKey);

				}
			});

			 if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_FAMILY){

				group_xiao_wei.setVisibility(View.GONE);
				top_message_line.setVisibility(View.VISIBLE);
				go_xiao_wei.setVisibility(View.GONE);
				go_xiao_wei_line_height.setVisibility(View.GONE);
				go_xiao_wei_line.setVisibility(View.GONE);

				View group_manager_name_line = curView
						.findViewById(R.id.group_manager_name_line);
				group_manager_name_line.setVisibility(View.GONE);

				View group_name_qr = curView
						.findViewById(R.id.group_manager_QR);
				group_name_qr.setVisibility(View.GONE);

				curView.findViewById(R.id.group_xiao_wei_line1).setVisibility(View.GONE);
				curView.findViewById(R.id.no_disturb_show_line).setVisibility(View.VISIBLE);

			}else {
				group_xiao_wei.setVisibility(View.GONE);
				top_message_line.setVisibility(View.VISIBLE);
				go_xiao_wei.setVisibility(View.GONE);
				go_xiao_wei_line.setVisibility(View.GONE);

				curView.findViewById(R.id.group_xiao_wei_line1).setVisibility(View.GONE);
				curView.findViewById(R.id.no_disturb_show_line).setVisibility(View.VISIBLE);
			}

			if (groupEntity.getGroupType() == DBConstant.GROUP_TYPE_TEMP) {
				int save = groupEntity.getSave();
				boolean isCheck = false;
				if (save == DBConstant.GROUP_MEMBER_STATUS_SAVE) {

					isCheck = true;
				}
				saveCheckBox.setChecked(isCheck);
				save_wei.setVisibility(View.VISIBLE);

				save_wei_line.setVisibility(View.VISIBLE);

				group_nickname_line.setVisibility(View.VISIBLE);
				group_nickname.setVisibility(View.VISIBLE);

				group_line1.setVisibility(View.VISIBLE);
				group_line2.setVisibility(View.VISIBLE);

				group_manager_nick.setVisibility(View.VISIBLE);
				group_manager_nick_line.setVisibility(View.VISIBLE);
				// 绑定监听器
				saveCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

							@Override
							public void onCheckedChanged(CompoundButton arg0,
									boolean arg1) {
								// TODO Auto-generated method stub
								if (arg1) {
									imService
											.getGroupManager()
											.saveChangeGroupMember(
													groupEntity.getPeerId(),
													ChangeDataType.CHANGE_GROUP_SAVE_GROUP,
													String.valueOf(1),
													groupEntity);
								} else {

									imService
											.getGroupManager()
											.saveChangeGroupMember(
													groupEntity.getPeerId(),
													ChangeDataType.CHANGE_GROUP_SAVE_GROUP,
													String.valueOf(0),
													groupEntity);

								}
							}
						});

			} else {
				save_wei.setVisibility(View.GONE);

				save_wei_line.setVisibility(View.GONE);
				saveCheckBox.setVisibility(View.GONE);
				// group_nickname.setVisibility(View.GONE);
				// group_line1.setVisibility(View.GONE);
				// group_line2.setVisibility(View.GONE);
			}

		}
			break;

		case DBConstant.SESSION_TYPE_SINGLE: {
			// 个人不显示群聊名称
			View groupNameContainerView = curView
					.findViewById(R.id.group_manager_name);
			groupNameContainerView.setVisibility(View.GONE);

			View group_manager_name_line = curView
					.findViewById(R.id.group_manager_name_line);
			group_manager_name_line.setVisibility(View.GONE);

			View group_manager_notice_line = curView
					.findViewById(R.id.group_manager_notice_line);
			group_manager_notice_line.setVisibility(View.GONE);
			group_manager_notice.setVisibility(View.GONE);
			notificationNoDisturb_height_line.setVisibility(View.GONE);
			View group_manager_qr = curView.findViewById(R.id.group_manager_QR);
			group_manager_qr.setVisibility(View.GONE);

			View group_manager_qr_line = curView.findViewById(R.id.group_manager_qr_line);
			group_manager_qr_line.setVisibility(View.GONE);


			save_wei_line.setVisibility(View.GONE);
			save_wei.setVisibility(View.GONE);
			group_nickname.setVisibility(View.GONE);
			group_nickname_line.setVisibility(View.GONE);

			group_line1.setVisibility(View.GONE);
			group_line2.setVisibility(View.GONE);

			group_manager_nick.setVisibility(View.GONE);
			group_manager_nick_line.setVisibility(View.GONE);

			UserEntity userEntity = (UserEntity) peerEntity;
			if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {

				group_xiao_wei.setVisibility(View.GONE);
				top_message_line.setVisibility(View.VISIBLE);
				go_xiao_wei.setVisibility(View.VISIBLE);
				go_xiao_wei_line.setVisibility(View.VISIBLE);
				notificationNoDisturb_height_line.setVisibility(View.VISIBLE);
				curView.findViewById(R.id.group_xiao_wei_line1).setVisibility(View.GONE);
				curView.findViewById(R.id.group_xiao_wei_test).setVisibility(View.VISIBLE);
				curView.findViewById(R.id.no_disturb_show_line).setVisibility(View.VISIBLE);

			} else {
				group_xiao_wei.setVisibility(View.GONE);
				top_message_line.setVisibility(View.VISIBLE);
				go_xiao_wei.setVisibility(View.GONE);
				go_xiao_wei_line.setVisibility(View.VISIBLE);
				notificationNoDisturb_height_line.setVisibility(View.GONE);
				curView.findViewById(R.id.group_xiao_wei_line1).setVisibility(View.GONE);

			}
			}
			break;
		}
		// 初始化配置checkBox
		initCheckbox();

	}

	private void initAdapter() {
		logger.d("groupmgr#initAdapter");

		gridView = (GridView) curView.findViewById(R.id.group_manager_grid);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));// 去掉点击时的黄色背影
		gridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), true, true));

		adapter = new GroupManagerAdapter(getActivity(), imService, peerEntity,
				entity,false); //　　最后一个参数　是否显示全部成员

		if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
			UserEntity login = imService.getLoginManager().getLoginInfo();
			if (entity != null) {
				if (entity.getNick().equals("")) {
					group_manage_title.setText(login.getMainName());
				} else {
					group_manage_title.setText(entity.getNick());
				}
			} else {
				group_manage_title.setText(login.getMainName());
			}

		}

		gridView.setAdapter(adapter);
	}

	/** 事件驱动通知 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(SessionEvent event) {
		switch (event) {

		case SET_REMOVE_MESSAGE_SUCCESS: {
			imService.getMessageManager().deleteMessageAll(
					peerEntity.getSessionKey());// curSessionKey
		}
			break;
		case SET_REMOVE_MESSAGE_FAIL: {
			Utils.showToast(getActivity(),
					getString(R.string.remove_message_fail));
		}
			break;
		case SET_SESSION_MUTE_TOP: {
			initCheckbox();
		}
			break;

		}
	}

	/** 事件驱动通知 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(GroupEvent event) {
		switch (event.getEvent()) {

		case CHANGE_GROUP_MEMBER_FAIL:
		case CHANGE_GROUP_MEMBER_TIMEOUT: {
			Utils.showToast(getActivity(),
					getString(R.string.change_temp_group_failed));
			return;
		}
		case CHANGE_GROUP_MEMBER_SUCCESS: {
			onMemberChangeSuccess(event);
		}
			break;

		case CHANGE_GROUP_NOTICE_SUCCESS: {
			if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
				final GroupEntity groupEntity = (GroupEntity) peerEntity;
				if (groupEntity.getBoard().equals("")) {
					group_manager_notice_content.setVisibility(View.GONE);
					group_manager_right.setText("未设置");
				} else {
					group_manager_notice_content.setVisibility(View.VISIBLE);
					group_manager_notice_content
							.setText(groupEntity.getBoard());
					group_manager_right.setVisibility(View.GONE);
				}

				// group_manager_notice_content.setText(groupEntity.getBoard());
			}
		}

			break;

		case CHANGE_GROUP_EXIT_SUCCESS: {
			initView();
			initAdapter();
		}
			break;
		case CHANGE_GROUP_NICK_SUCCESS: {
			initView();
			initAdapter();
		}
			break;

		case CHANGE_GROUP_DELETE_SUCCESS: {
			getActivity().finish();
		}
			break;
			case USER_GROUP_DELETE_SUCCESS: {
				getActivity().finish();
			}
			break;
		case CHANGE_GROUP_DELETE_FAIL: {
			Utils.showToast(getActivity(), "退出群失败");

		}
			break;

		case CHANGE_GROUP_DELETE_TIMEOUT: {
			Utils.showToast(getActivity(), "退出群失败");

		}
			break;

		case CHANGE_GROUP_MODIFY_FAIL: {
			Utils.showToast(getActivity(), "退出群失败");
		}
			break;
		case CHANGE_GROUP_MODIFY_TIMEOUT: {
			Utils.showToast(getActivity(), "修改超时");
		}
			break;
		case CHANGE_GROUP_MODIFY_SUCCESS: {
			onModifyberChangeSuccess();

		}
			break;
		}
	}

	private void onModifyberChangeSuccess() {

		if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
			peerEntity = imService.getSessionManager().findPeerEntity(
					curSessionKey);
			final GroupEntity groupEntity = (GroupEntity) peerEntity;
			TextView groupNameView = (TextView) curView
					.findViewById(R.id.group_manager_title);
			groupNameView.setText(groupEntity.getMainName());
		}

	}

	private void onMemberChangeSuccess(GroupEvent event) {
		int groupId = event.getGroupEntity().getPeerId();
		if (groupId != peerEntity.getPeerId()) {
			return;
		} 
		List<Integer> changeList = event.getChangeList();
		if (changeList == null || changeList.size() <= 0) {
			return;
		}
		int changeType = event.getChangeType();

		switch (changeType) {
		case DBConstant.GROUP_MODIFY_TYPE_ADD:
			ArrayList<UserEntity> newList = new ArrayList<>();
			for (Integer userId : changeList) {
				UserEntity userEntity = imService.getContactManager()
						.findContact(userId);
				if (userEntity != null) {
					newList.add(userEntity);
				}
			}
			adapter.add(newList);
			break;
		case DBConstant.GROUP_MODIFY_TYPE_DEL:
			for (Integer userId : changeList) {
				adapter.removeById(userId);
			}
			break;
		}
	}

	private void initCheckbox() {

		// 消息免打扰 先从个人信息中读取
		// boolean globallyOnOff =
		// ConfigurationSp.instance(GroupManagerFragment.this.getActivity(),
		// imService.getLoginManager().getLoginId()).getCfg(
		// SysConstant.SETTING_GLOBAL,
		// ConfigurationSp.CfgDimension.NOTIFICATION);
		//
		// if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
		// final GroupEntity groupEntity = (GroupEntity) peerEntity;
		// if(groupEntity.getMuteNotification() == 1){
		// noDisturbCheckbox.setChecked(true);
		// }else{
		// noDisturbCheckbox.setChecked(globallyOnOff);
		// }
		// }else if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE){
		// UserEntity userEntity = (UserEntity) peerEntity;
		// if(userEntity.getMuteNotification() == 1){
		// noDisturbCheckbox.setChecked(true);
		// }else{
		// noDisturbCheckbox.setChecked(globallyOnOff);
		// }
		// }
		//
		//
		// noDisturbCheckbox.setOnCheckedChangeListener(new
		// CompoundButton.OnCheckedChangeListener(){
		// @Override
		// public void onCheckedChanged(CompoundButton buttonView,
		// boolean isChecked) {
		// // TODO Auto-generated method stub
		// int loginId = imService.getLoginManager().getLoginId();
		// if(isChecked){
		// IMContactManager.instance().ChangeSessionInfo(loginId,peerEntity.getPeerId(),1,CommentType.COMMENT_TYPE_MUTE_NOTIFICATION,"1",peerEntity);
		// }else{
		// IMContactManager.instance().ChangeSessionInfo(loginId,peerEntity.getPeerId(),0,CommentType.COMMENT_TYPE_MUTE_NOTIFICATION,"0",peerEntity);
		// }
		//
		//
		// }
		// });
		//
		//
		//
		// if (peerEntity.getType() == DBConstant.SESSION_TYPE_GROUP) {
		// final GroupEntity groupEntity = (GroupEntity) peerEntity;
		// if(groupEntity.getStickyOnTop() == 1){
		// topSessionCheckBox.setChecked(true);
		// }else{
		// topSessionCheckBox.setChecked(false);
		// }
		// }else if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE){
		//
		// UserEntity userEntity = (UserEntity) peerEntity;
		// if(userEntity.getStickyOnTop() == 1){
		// topSessionCheckBox.setChecked(true);
		// }else{
		// topSessionCheckBox.setChecked(false);
		// }
		// }
		//
		//
		// topSessionCheckBox.setOnCheckedChangeListener(new
		// CompoundButton.OnCheckedChangeListener(){
		// @Override
		// public void onCheckedChanged(CompoundButton buttonView,
		// boolean isChecked) {
		// // TODO Auto-generated method stub
		// int loginId = imService.getLoginManager().getLoginId();
		// if(isChecked){
		// IMContactManager.instance().ChangeSessionInfo(loginId,peerEntity.getPeerId(),1,CommentType.COMMENT_TYPE_STICKY_ON_TOP,"1",peerEntity);
		// }else{
		// IMContactManager.instance().ChangeSessionInfo(loginId,peerEntity.getPeerId(),0,CommentType.COMMENT_TYPE_STICKY_ON_TOP,"0",peerEntity);
		// }
		//
		//
		// }
		// });

		checkBoxConfiger.initCheckBox(noDisturbCheckbox, curSessionKey,
				ConfigurationSp.CfgDimension.NOTIFICATION);

		checkBoxConfiger.initTopCheckBox(topSessionCheckBox, curSessionKey);
	}

	/**
	 * @param event
	 */
	public void triggerEvent(GroupEvent event) {
		// 先更新自身的状态
		EventBus.getDefault().postSticky(event);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {
		case USER_INFO_UPDATE:
			checkBoxConfiger.init(imService.getConfigSp());
			initView();
			initAdapter();
			break;

		case USER_INFO_DELETE_DATA_SUCCESS:
			Utils.showToast(GroupManagerFragment.this.getActivity(), "清空数据成功");
			break;

		case USER_MUTE_NOTIFICATION:
			initCheckbox();
			break;

		default:
			break;
		}
	}
}
