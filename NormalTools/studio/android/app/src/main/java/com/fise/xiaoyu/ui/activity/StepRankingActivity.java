package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.fise.xiaoyu.DB.entity.StepRanking;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMDevice;
import com.fise.xiaoyu.ui.adapter.StepRankingAdspter;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


/**
 * 计步名次列表界面
 */
@SuppressLint("NewApi")
public class StepRankingActivity extends TTBaseActivity {

	private Logger logger = Logger.getLogger(StepRankingActivity.class);
	private Handler uiHandler = new Handler();
	private IMService imService;
	private PullToRefreshListView pullToRefreshListView;
	private StepRankingAdspter adspter;
	public List<StepRanking> rankingList = new ArrayList<>();

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

					//refreshTodayStep();

					//获取本地名次
					rankingList = imService.getUserActionManager().getStepRankingList();
					adspter =  new StepRankingAdspter(StepRankingActivity.this,rankingList,imService);
					pullToRefreshListView.setAdapter(adspter);
					pullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
												long arg3) {

							Intent intent = new Intent(StepRankingActivity.this, RankingListActivity.class);
							intent.putExtra(IntentConstant.STEP_ID,rankingList.get(arg2 -1).getId());
							StepRankingActivity.this.startActivity(intent);
						}
					});
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

		imServiceConnector.connect(StepRankingActivity.this);

		setContentView(R.layout.tt_activity_step_ranking);
		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				StepRankingActivity.this.finish();
			}
		});

//		TextView left_text = (TextView) findViewById(R.id.left_text);
//		left_text.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				StepRankingActivity.this.finish();
//			}
//		});


		pullToRefreshListView = (PullToRefreshListView) findViewById(R.id.ranking_refresh_list);
		pullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);//上拉刷新 PULL_FROM_START
		pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				//refreshType = REFRESH_TYPE_DOWN;
				pullToRefreshListView.onRefreshComplete();
			//	new GetDataTask().execute();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
			//	refreshType = REFRESH_TYPE_UP;
//				refreshView.getLoadingLayoutProxy().setRefreshingLabel("正在加载");
//				refreshView.getLoadingLayoutProxy().setPullLabel("上拉加载更多");
//				refreshView.getLoadingLayoutProxy().setReleaseLabel("释放开始加载");
//                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("最后加载时间:");
				// Do work to refresh the list here.
				//refreshTodayStep();

				refreshView.getLoadingLayoutProxy().setRefreshingLabel("正在加载");
				refreshView.getLoadingLayoutProxy().setPullLabel("下拉加载更多");
				refreshView.getLoadingLayoutProxy().setReleaseLabel("释放开始加载");
//                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("最后加载时间:");

				// Do work to refresh the list here.

				pullToRefreshListView.onRefreshComplete();
				//new GetDataTask().execute();
			}
		});


		RelativeLayout step_ranking = (RelativeLayout) findViewById(R.id.step_ranking);
		step_ranking.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(StepRankingActivity.this, RankingListActivity.class);
				StepRankingActivity.this.startActivity(intent);
			}
		});
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(StepRankingActivity.this);
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
	/**
	 * ----------------------------event 事件驱动----------------------------
	 */
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(DeviceEvent event) {
		switch (event){

			case DEVICE_STEP_RECORD_LIST_SUCCESS: {
				rankingList = imService.getUserActionManager().getStepRankingList();
				adspter.putRankingList(rankingList);
			}
			break;
			case DEVICE_STEP_RECORD_LIST_FAILED: {
				Utils.showToast(StepRankingActivity.this,"获取失败");
				rankingList = imService.getUserActionManager().getStepRankingList();
				adspter.putRankingList(rankingList);
			}

			break;

		}
	}

	private class GetDataTask extends AsyncTask<Void, Void, String[]> {
		@Override
		protected String[] doInBackground(Void... params) {
//			try {
//				Thread.sleep(3000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			return new String[0];
		}

		@Override
		protected void onPostExecute(String[] result) {
			// Call onRefreshComplete when the list has been refreshed.
			pullToRefreshListView.onRefreshComplete();

//			if(REFRESH_TYPE_DOWN == refreshType){
//				Article article = new Article();
//				article.setTitle("下拉刷新添加title");
//				adapter.insert(article,0);//adapter是一个LinkedList
//			}else if(REFRESH_TYPE_UP == refreshType){
//				Article article = new Article();
//				article.setTitle("上拉刷新添加title");
//				adapter.add(article);
//			}
//
			super.onPostExecute(result);
		}
	}
}
