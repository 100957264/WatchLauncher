package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.RankingListEntity;
import com.fise.xiaoyu.DB.entity.StepRanking;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMDevice;
import com.fise.xiaoyu.ui.adapter.RankingListAdspter;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;



/**
 * 排行榜的列表界面
 */
@SuppressLint("NewApi")
public class RankingListActivity extends TTBaseActivity {

	private Logger logger = Logger.getLogger(RankingListActivity.class);
	private Handler uiHandler = new Handler();
	private IMService imService;
	private Long id ;
	private List<RankingListEntity> list = new ArrayList<>();
	private ListView listView = null;
	private RankingListAdspter adapter;


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



					IMBaseDefine.ClientType client_type = IMBaseDefine.ClientType.CLIENT_TYPE_ANDROID;
					if(Utils.isClientType(imService.getLoginManager().getLoginInfo()))
					{
						client_type  =  IMBaseDefine.ClientType.CLIENT_TYPE_MOBILE_DEVICE;
					}

					id = RankingListActivity.this.getIntent().getLongExtra(
							IntentConstant.STEP_ID, -1);

					int timeNow = (int) (System.currentTimeMillis() / 1000);
					if(id == -1)
					{
						imService.getUserActionManager().getStepRecordListRequest(id,client_type, IMDevice.StepRecordType.GET_STEP_RANKING_LIST,timeNow,null);
					}else{

						//如果不是今天的数据　已经请求过的不要再请求  Latest_data 是否请求的判断
						StepRanking stepRanking = imService.getUserActionManager().getSetpRanking(id);
						if(stepRanking!=null && (stepRanking.getLatest_data() !=0))
						{

							list = imService.getUserActionManager().getRankingList(id);
							Collections.sort(list, new Comparator<RankingListEntity>() {

								@Override
								public int compare(RankingListEntity o1, RankingListEntity o2) {
									int i = o2.getStep_num() - o1.getStep_num();
									return i;
								}
							});

						}else{
							imService.getUserActionManager().getStepRecordListRequest(id,client_type, IMDevice.StepRecordType.GET_STEP_RANKING_LIST,stepRanking.getUpdate_time(),stepRanking);
						}
					}


					adapter = new RankingListAdspter(RankingListActivity.this, list,
							imService);

					listView.setAdapter(adapter);
					updateSetp();
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

	private void updateSetp() {

		IMBaseImageView my_image = (IMBaseImageView) findViewById(R.id.my_image);
		my_image.setImageUrl(imService.getLoginManager().getLoginInfo().getAvatar());


		TextView xiaoyu_name = (TextView) findViewById(R.id.xiaoyu_name);
		xiaoyu_name.setText("" + imService.getLoginManager().getLoginInfo().getMainName());


		int loginRanking= list.size() + 1;
		int setpNum=0;
		for(int i=0;i<list.size();i++){
			if(list.get(i).getChampion_id() == imService.getLoginManager().getLoginId())
			{

				setpNum = list.get(i).getStep_num();
				loginRanking = i+ 1;

				break;
			}
		}
		TextView rangking = (TextView) findViewById(R.id.rangking);
		rangking.setText("第" + loginRanking + "名");

		TextView step_num = (TextView) findViewById(R.id.step_num);
		step_num.setText("" + setpNum);


		View bttom_line = (View) findViewById(R.id.bttom_line);
		if(list.size()>0)
		{
			bttom_line.setVisibility(View.VISIBLE);
		}else{
			bttom_line.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		imServiceConnector.connect(RankingListActivity.this);

		setContentView(R.layout.tt_activity_ranking_list);
		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				RankingListActivity.this.finish();
			}
		});

		listView = (ListView) findViewById(R.id.list);

//		TextView left_text = (TextView) findViewById(R.id.left_text);
//		left_text.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				RankingListActivity.this.finish();
//			}
//		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(RankingListActivity.this);
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
	public void onMessageEvent(DeviceEvent event) {
		switch (event){

			case DEVICE_RANKING_LIST_SUCCESS:
			{
				list = imService.getUserActionManager().getRankingList(id);
				Collections.sort(list, new Comparator<RankingListEntity>() {

					@Override
					public int compare(RankingListEntity o1, RankingListEntity o2) {
						//int i = o1.getStep_num() - o2.getStep_num();
						int i = o2.getStep_num() - o1.getStep_num();
						return i;
					}
				});
				adapter.putRankingList(list);
				updateSetp();
			}
				break;
			case DEVICE_RANKING_LIST_FAILED:
			{
				Utils.showToast(RankingListActivity.this,"获取失败");
				list = imService.getUserActionManager().getRankingList(id);
				Collections.sort(list, new Comparator<RankingListEntity>() {

					@Override
					public int compare(RankingListEntity o1, RankingListEntity o2) {
						//int i = o1.getStep_num() - o2.getStep_num();
						int i = o2.getStep_num() - o1.getStep_num();
						return i;
					}
				});
				adapter.putRankingList(list);
				updateSetp();
			}

				break;

		}
	}


}
