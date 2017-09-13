package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ChangeDataType;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *  小位号修改设置界面
 */
public class XiaoWeiNameActivity extends TTBaseActivity {
	private static IMService imService;
	private EditText xiao_wei_name;
	//问题最大字数
	private int num = 32;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
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
		setContentView(R.layout.tt_activity_xiaowei_name);
		imServiceConnector.connect(this);

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				XiaoWeiNameActivity.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				XiaoWeiNameActivity.this.finish();
			}
		});

		xiao_wei_name = (EditText) findViewById(R.id.new_xiaowei_name);
		final UserEntity user = IMLoginManager.instance().getLoginInfo();
// 		if (user.getRealName() != null) {
//			xiao_wei_name.setText("" + user.getRealName());
//		}

		xiao_wei_name.addTextChangedListener(new TextWatcher() {
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
				selectionStart = xiao_wei_name.getSelectionStart();
				selectionEnd = xiao_wei_name.getSelectionEnd();
				//删除多余输入的字（不会显示出来）
				if (temp.length() > num) {
					s.delete(selectionStart - 1, selectionEnd);
					xiao_wei_name.setText(s);
				}
				//设置光标在最后
				// signed_name.setSelection(s.length());
			}
		});




		TextView right_text = (TextView) findViewById(R.id.right_text);
		right_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// 保存
				String data = xiao_wei_name.getText().toString();
				if(!xiao_wei_name.getText().toString().equals(""))
				{

					if(!isXiaoWei(data))
					{
						Utils.showToast(XiaoWeiNameActivity.this, "使用英文字母、数字和'_'（下划线）且不以数字开头");
					}else{
						IMContactManager.instance().ChangeUserInfo(
								user.getPeerId(),
								ChangeDataType.CHANGE_USERINFO_DOMAIN, data);
					}

				}else
				{
					Utils.showToast(XiaoWeiNameActivity.this, "请输入小位号");
				}


			}
		});

	}

	public  boolean isXiaoWei(String str) {

		//Pattern pattern = Pattern.compile("^[A-Za-z_$]+[A-Za-z_$\\d]+$");
		Pattern pattern = Pattern.compile("^[_a-zA-Z]\\w*$");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			return true;
		}
		return false;
	}

	public  boolean isXiaoWeiN(String str) {

		Pattern pattern = Pattern.compile("^[0-9a-zA-Z_]+$");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			return true;
		}
		return false;
	}





	public  boolean isContainChinese(String str) {

		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			XiaoWeiNameActivity.this.finish();
			break;

			case USER_INFO_DATA_FAIL:
			{
				if(imService!=null)
				{
					if(imService.getContactManager().xiaoweiCode == IMBaseDefine.ResultType.REFUSE_REASON_DATA_EXIST.ordinal()){
						Utils.showToast(XiaoWeiNameActivity.this, "小雨号已存在");
					}else{
						Utils.showToast(XiaoWeiNameActivity.this, "小雨号更新失败");
					}

				}

			}

				break;

		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
				imServiceConnector.disconnect(this);
	}

}
