package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.fise.xiaoyu.DB.entity.WeiEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMUserActionManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMUserAction;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * 拒绝请求/应用下载界面
 */
@SuppressLint("NewApi")
public class RefuseReqActivity extends TTBaseActivity {

	private Logger logger = Logger.getLogger(RefuseReqActivity.class);
	private Handler uiHandler = new Handler();
	private IMService imService;
	private int reqType;
	private int yuEntityId;
	private int yuReqId;
	private EditText refuse_reason;
	private WeiEntity weiEntity;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onServiceDisconnected() {
		}

		@Override
		public void onIMServiceConnected() {
			logger.d("login#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			try {
				do {
					if (imService == null) {
						// 后台服务启动链接失败
						break;
					}

					return;
				} while (false);

				// 异常分支都会执行这个
				// handleNoLoginIdentity();
			} catch (Exception e) {
				// 任何未知的异常
				logger.w("loadIdentity failed");
				// handleNoLoginIdentity();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		imServiceConnector.connect(RefuseReqActivity.this);

		setContentView(R.layout.tt_activity_refuse_req);
		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				RefuseReqActivity.this.finish();
			}
		});
//
		reqType = getIntent().getIntExtra(IntentConstant.YU_REQ_TYPE, 0);
		refuse_reason = (EditText) findViewById(R.id.refuse_reason);


		if(reqType == DBConstant.FRIENDS_REQ)
		{
			yuEntityId =   getIntent().getIntExtra(IntentConstant.YU_ENTITY_ID, 0);
			yuReqId  =   getIntent().getIntExtra(IntentConstant.YU_REQ_ID, 0);
			refuse_reason.setHint("" + getResources().getString(R.string.refuse_reason_text));

			weiEntity = IMUserActionManager.instance().findYuEntity(yuEntityId);
		}



		Button send_req_btn = (Button) findViewById(R.id.send_req_btn);
		send_req_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				if(reqType == DBConstant.FRIENDS_REQ)
				{
					String content = refuse_reason.getText().toString() + "";
					if(weiEntity!=null){
						imService.getUserActionManager().confirmYuFriends(
								yuReqId, weiEntity.getActId(),
								weiEntity.getActType(), IMUserAction.ActionResult.ACTION_RESULT_NO,
								weiEntity, content, weiEntity.getDevice_id());
					}
				}
			}
		});



//		TextView left_text = (TextView) findViewById(R.id.left_text);
//		left_text.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				RefuseReqActivity.this.finish();
//			}
//		});

	}


	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(RefuseReqActivity.this);
			}

	// 为什么会有两个这个
	// 可能是 兼容性的问题 导致两种方法onBackPressed
	@Override
	public void onBackPressed() {
		logger.d("login#onBackPressed");
		// imLoginMgr.cancel();
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	/**
	 * ----------------------------event 事件驱动----------------------------
	 */

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(LoginEvent event) {
		switch (event) {
		case LOCAL_LOGIN_SUCCESS:
		case LOGIN_OK:
			break;

		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {
			case USER_INFO_REQ_ALL:
				RefuseReqActivity.this.finish();
				break;

		}
	}
}
