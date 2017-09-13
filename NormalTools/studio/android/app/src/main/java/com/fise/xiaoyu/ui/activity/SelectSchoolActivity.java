package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.areawheel.ScrollerNumberPicker;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMUserAction;
import com.fise.xiaoyu.ui.adapter.SchoolAdapter;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.SearchEditText;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


/**
 * 选择学校
 */
@SuppressLint("NewApi")
public class SelectSchoolActivity extends TTBaseActivity {

	private Logger logger = Logger.getLogger(SelectSchoolActivity.class);
	private IMService imService;
	private String province;
	private String city;
	private TextView show_city;

	private SchoolAdapter adapter;
	private ListView listView;

	private List<IMUserAction.SchoolInfo> schoolList = new ArrayList<>();
	private SearchEditText searchEditText;

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
					adapter = new SchoolAdapter(SelectSchoolActivity.this,imService,schoolList);
					listView.setAdapter(adapter);
					listView.setOnItemClickListener(adapter);
					listView.setOnItemLongClickListener(adapter);

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

		setContentView(R.layout.tt_activity_select_school);
		imServiceConnector.connect(SelectSchoolActivity.this);


		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SelectSchoolActivity.this.finish();
			}
		});

//		TextView left_text = (TextView) findViewById(R.id.left_text);
//		left_text.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				SelectSchoolActivity.this.finish();
//			}
//		});

		show_city = (TextView) findViewById(R.id.show_city);
		listView = (ListView) findViewById(R.id.school_list);

		RelativeLayout select_city = (RelativeLayout) findViewById(R.id.select_city);
		select_city.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				AlertDialog.Builder builder=new AlertDialog.Builder(SelectSchoolActivity.this);
				View view = LayoutInflater.from(SelectSchoolActivity.this).inflate(R.layout.addressdialog, null);
				builder.setView(view);
				LinearLayout addressdialog_linearlayout = (LinearLayout)view.findViewById(R.id.addressdialog_linearlayout);
				final ScrollerNumberPicker provincePicker = (ScrollerNumberPicker)view.findViewById(R.id.province);
				final ScrollerNumberPicker cityPicker = (ScrollerNumberPicker)view.findViewById(R.id.city);
				final AlertDialog dialog = builder.show();
				addressdialog_linearlayout.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						province = provincePicker.getSelectedText();
						city = cityPicker.getSelectedText();
						if(city.equals("")){
							Utils.showToast(SelectSchoolActivity.this,"请选择城市");
						}else{

							if(province.equals(city)){
								show_city.setText(province);
							}else{
								show_city.setText(province + " "+ city);
							}

							imService.getUserActionManager().reqSchoolInfoReq(city);
							dialog.dismiss();
						}
					}
				});

			}
		});

		searchEditText = (SearchEditText) findViewById(R.id.filter_edit);
		//chat_title_search_tt
		searchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
				String key = s.toString();
				adapter.setSearchKey(key);
				if(!key.isEmpty())
				{
					searchEntityLists(key);
				}else{
					updateSchool();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {

			}
			@Override
			public void afterTextChanged(Editable s) {


			}
			});

	}

	// search 模块
	private void searchEntityLists(String key) {
		List<IMUserAction.SchoolInfo> schoolListTemp = new ArrayList<>();

		List<IMUserAction.SchoolInfo> schoolList1 =  imService.getUserActionManager().getSchoolList();
		for (int i=0;i<schoolList1.size();i++){
			if(schoolList1.get(i).getSchoolName().indexOf(key)!=-1){
				schoolListTemp.add(schoolList1.get(i));
			}
		}

		adapter.putSchoolList(schoolListTemp);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		imServiceConnector.disconnect(SelectSchoolActivity.this);
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

	public void updateSchool(){
		List<IMUserAction.SchoolInfo> schoolListTemp = imService.getUserActionManager().getSchoolList();
		adapter.putSchoolList(schoolListTemp);
	}

	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {
			case USER_INFO_DEVICE_SCHOOL_SUCCESS: {
				//schoolList.clear();
				updateSchool();
			}
			break;


			case USER_INFO_DEVICE_SCHOOL_FAIL: {
			 Utils.showToast(SelectSchoolActivity.this,"获取失败");

			}
			break;


		}
	}
	 
}
