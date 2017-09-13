package com.fise.xw.ui.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.jinlin.zxing.example.activity.CodeCreator; 
import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.ui.menu.QrMenu;
import com.fise.xw.ui.widget.IMBaseImageView;
import com.fise.xw.utils.ImageLoaderUtil;
import com.fise.xw.utils.ImageUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import de.greenrobot.event.EventBus;


/** 
 *  个人的二维码界面
 * @author weileiguan
 *
 */
public class InfoQRActivity extends TTBaseActivity {
	private static IMService imService;
	private UserEntity Info;
	public QrMenu menu;
	public Bitmap bitmap = null;
	public final int QR_CODR = 1000;
	public String infoQr;
	

	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case QR_CODR: {
				Bitmap bitmap1 = (Bitmap) msg.obj;
				int peeid = Info.getPeerId();
				ImageView icon_qr = (ImageView) findViewById(R.id.icon_qr);
				try {
					

					String infoUrl = imService.getContactManager().getSystemConfig().getWebsite();
						
					String peerIdString = "wuid="+ peeid;
					String content = new String(com.fise.xw.Security
								.getInstance().EncryptMsg(peerIdString));
					
					if (bitmap1 != null) {

						 Bitmap temp = ImageUtil.GetRoundedCornerBitmap(bitmap1);
//						bitmap = CodeCreator.createQRCode("" + peeid);// MakeQRCodeUtil.makeQRImage(temp,
//																		// "" +
//																		// peeid,
//																		// 650,
//																		// 650); 
						bitmap = CodeCreator.createCode(infoUrl + content, temp);  
						menu.setBitmap(bitmap);
							
					} else {
						bitmap = CodeCreator.createQRCode(infoUrl + content);
						menu.setBitmap(bitmap);
					} 

				} catch (WriterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				icon_qr.setVisibility(View.VISIBLE);
				icon_qr.setImageBitmap(bitmap);
			}
				break;
			}
			super.handleMessage(msg);
		}
	};

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			logger.d("config#onIMServiceConnected");
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			Info = imService.getLoginManager().getLoginInfo();

			menu = new QrMenu(InfoQRActivity.this, bitmap, Info.getPeerId(),
					Info.getMainName());
			menu.addItems(new String[] { "保存到手机" });

			initDetailProfile();

		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.info_qr_activity);
		imServiceConnector.connect(this);
		EventBus.getDefault().register(this);

		TextView black = (TextView) findViewById(R.id.black);
		black.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				InfoQRActivity.this.finish();
			}
		});
		//
		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				InfoQRActivity.this.finish();
			}
		});

		Button icon_user_info = (Button) findViewById(R.id.icon_user_info);
		icon_user_info.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (menu != null) {
					menu.showAsDropDown(v);
				}
			}
		});

	}

	public void initDetailProfile() {

		ProgressBar progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
		progress_bar.setVisibility(View.GONE);

		final IMBaseImageView infoImage = (IMBaseImageView) this
				.findViewById(R.id.contact_portrait);
		infoImage
				.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
		infoImage.setCorner(0);
		infoImage.setImageUrl(Info.getAvatar());

		TextView info_name = (TextView) this.findViewById(R.id.info_name);

		String name = Info.getMainName();
		info_name.setText(Info.getMainName());
		info_name.setVisibility(View.VISIBLE);
  
		TextView add_name = (TextView) this.findViewById(R.id.add_name);
		if (Info.getProvince().equals(Info.getCity())) {
			add_name.setText("" + Info.getCity());
		} else {
			add_name.setText("" + Info.getProvince() + " " + Info.getCity());
		}
		add_name.setVisibility(View.VISIBLE);

		ImageView sex = (ImageView) findViewById(R.id.sex);
		if (Info.getGender() == DBConstant.SEX_MAILE) {
			sex.setBackgroundResource(R.drawable.sex_head_man);
		} else {
			sex.setBackgroundResource(R.drawable.sex_head_woman);
		}

		int peeid = Info.getPeerId();

		//infoQr = 
		 new Thread(new Runnable() {
		 @Override
		 public void run() {
		 // 这里写入子线程需要做的工作
	 
			 Bitmap bitmap = ImageUtil.returnBitmap(Info.getAvatar());
			 Message message = new Message();
			 message.what = QR_CODR;
			 message.obj = bitmap;
			 InfoQRActivity.this.myHandler.sendMessage(message);
		 }
		 }).start();

		 //暂时屏蔽
//		try {
//			bitmap = CodeCreator.createQRCode("" + peeid);
//		} catch (WriterException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		// menu.setBitmap(bitmap);
//		ImageView icon_qr = (ImageView) findViewById(R.id.icon_qr);
//		icon_qr.setVisibility(View.VISIBLE);
//		icon_qr.setImageBitmap(bitmap);
	}

	public void onEventMainThread(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			Info = imService.getLoginManager().getLoginInfo();
			initDetailProfile();
			break;

		case USER_QR_CODE_SAVE:
			Toast.makeText(InfoQRActivity.this, "二维码保存成功", Toast.LENGTH_SHORT)
					.show();
			break;

		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
		super.onDestroy();
	}

	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
}
