package com.fise.xiaoyu.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 密码界面 包含 找回密码和 注册之后的密码
 */
@SuppressLint("NewApi")
public class PassActivityVerify extends TTBaseActivity {

	private Logger logger = Logger.getLogger(PassActivityVerify.class);
	public Button submit;
	private EditText mPassword;
	private EditText mPassZaiword;

	private int inputNum;
	private TextView name;
	public String phoneName;
	private static IMService imService;
	private int currentUserId;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			currentUserId = PassActivityVerify.this.getIntent().getIntExtra(
					IntentConstant.KEY_PEERID, 0);

			TextView name = (TextView) findViewById(R.id.name);
			name.setText(""
					+ imService.getLoginManager().getLoginInfo().getPhone());

		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	public boolean vd(String str) {

		char[] chars = str.toCharArray();
		boolean isGB2312 = false;
		for (int i = 0; i < chars.length; i++) {
			byte[] bytes = ("" + chars[i]).getBytes();
			if (bytes.length == 2) {
				int[] ints = new int[2];
				ints[0] = bytes[0] & 0xff;
				ints[1] = bytes[1] & 0xff;

				if (ints[0] >= 0x81 && ints[0] <= 0xFE && ints[1] >= 0x40
						&& ints[1] <= 0xFE) {
					isGB2312 = true;
					break;
				}
			}
		}
		return isGB2312;
	}

	/**
	 * * 纯字母
	 * 
	 * @param data
	 * @return
	 */
	public boolean isChar(String data) {
		for (int i = data.length(); --i >= 0;) {
			char c = data.charAt(i);
			if (((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	public static boolean ispsd(String psd) {
		Pattern p = Pattern.compile("^[a-zA-Z].*[0-9]|.*[0-9].*[a-zA-Z]");
		Matcher m = p.matcher(psd);

		return m.matches();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_pass_verify);
		mPassword = (EditText) findViewById(R.id.password);
		mPassZaiword = (EditText) findViewById(R.id.password_zai);

		imServiceConnector.connect(this);

		Button confirm = (Button) findViewById(R.id.confirm);
		confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if ((!mPassword.getText().toString().equals(""))
						&& (!mPassZaiword.getText().toString().equals(""))) {

					if (!(mPassword.getText().toString().equals(mPassZaiword
							.getText().toString()))) {
						Utils.showToast(PassActivityVerify.this, "密码不一样 请重新输入");
						return;
					}

					Pattern pattern = Pattern.compile("[0-9]{1,}");
					Matcher matcher = pattern.matcher((CharSequence) (mPassword
							.getText().toString()));
					boolean result = matcher.matches();
					if (result) {
						Utils.showToast(PassActivityVerify.this, "密码不能是纯数字");
						return;
					}

					boolean resultHan = vd(mPassZaiword.getText().toString());
					if (resultHan) {
						Utils.showToast(PassActivityVerify.this, "密码不能是汉字");
						return;
					}


					if (mPassword.getText().toString().indexOf(" ") >= 0) {
						// 含有空格
						Utils.showToast(PassActivityVerify.this, "密码不能有空格");
						return;
					}

					if (mPassword.getText().toString().length() < DBConstant.PASS_LENGTH) {
						// 含有空格
						Utils.showToast(PassActivityVerify.this, "密码不能少于6位");
						return;
					}

					boolean resultZi = ispsd(mPassword.getText().toString());// isChar(mPasswordView.getText().toString());
					if (resultZi == false) {
						Utils.showToast(PassActivityVerify.this, "密码不能是纯字母");
						return;
					}

					imService.getUserActionManager().changePassword(
							currentUserId, mPassword.getText().toString());

				} else {
					Utils.showToast(PassActivityVerify.this, "请输入正确的密码");
				}
			}
		});

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				PassActivityVerify.this.finish();
			}
		});

		mPassword.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {

		case USER_MODIFY_PASS_FAIL:

			Utils.showToast(PassActivityVerify.this,
					"" + imService.getUserActionManager().getModifyPass());
			break;
		case USER_MODIFY_PASS_SUCCESS:
			Utils.showToast(PassActivityVerify.this, "修改密码成功");
			PassActivityVerify.this.finish();
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
				imServiceConnector.disconnect(this);

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

}
