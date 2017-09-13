package com.fise.xiaoyu.ui.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.sp.ConfigurationSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.menu.QrMenu;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.BitmapFillet;
import com.fise.xiaoyu.utils.ImageUtil;
import com.fise.xiaoyu.utils.Utils;
import com.google.zxing.WriterException;
import com.jinlin.zxing.example.activity.CodeCreator;
import com.jinlin.zxing.example.utils.LogoConfig;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 *  个人的二维码界面
 */
public class InfoQRActivity extends TTBaseActivity {
	private static IMService imService;
	private UserEntity Info;
	public QrMenu menu;
	private Bitmap QrBitmap ;

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
            String filePath = Environment.getExternalStorageDirectory() +"/" + "fise" +"/" + Info.getPeerId() +"/" + "qr_code" + Info.getMainName() +".jpg";
           File file = new File(filePath);
			if(file.exists())
			{
				Bitmap bitmap= BitmapFactory.decodeFile(filePath);
				if(bitmap != null){
					icon_qr.setVisibility(View.VISIBLE);
					icon_qr.setImageBitmap(bitmap);
				}
			}else{
				saveQrCodePng();
			}


            menu.addItems(new String[] { "保存到手机","扫描二维码" });
            if(bitmap != null){
                menu.setBitmap(bitmap);
            }

			initDetailProfile();

		}

		@Override
		public void onServiceDisconnected() {
		}
	};
    private IMBaseImageView infoImage;
    private Bitmap bitmap = null;
    private ImageView icon_qr;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.info_qr_activity);
		imServiceConnector.connect(this);

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
        icon_qr = (ImageView) findViewById(R.id.icon_qr);
        Bundle b=getIntent().getExtras();


		icon_user_info.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				if (menu != null) {
					menu.showBottomDia();
				}
			}
		});

	}


	private void saveQrCodePng() {
		// 这里写入子线程需要做的工作
		Bitmap bitmap = ImageUtil.returnBitmap(Info.getAvatar());
		//将生成的logo url保存起来
		ConfigurationSp.instance(getApplicationContext() ,Info.getPeerId()).setStrCfg("avatar_url" ,Info.getAvatar());
		int peeid = Info.getPeerId();
		if(bitmap == null){
			bitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.tt_default_user_portrait_corner);
		}
		//得到圆角logo
		Bitmap filletLogoBitmap =  BitmapFillet.fillet(bitmap , 50 ,BitmapFillet.CORNER_ALL );
		//得到带白色边框背景的圆角logo
		LogoConfig logoConfig = new LogoConfig();
		Bitmap logoBitmap = logoConfig.modifyLogo(
				BitmapFactory.decodeResource(getResources(),
						R.drawable.yuanjiao_bg),filletLogoBitmap);

		try {

			String infoUrl = "";
			if(imService.getContactManager().getSystemConfig()!=null){
				infoUrl = imService.getContactManager().getSystemConfig().getWebsite();
			}

			String peerIdString = "wuid="+ peeid;
			String content = new String(Security
					.getInstance().EncryptMsg(peerIdString));

			if (logoBitmap != null) {

				QrBitmap = CodeCreator.createCode(infoUrl + content, logoBitmap);

			} else {
				QrBitmap = CodeCreator.createQRCode(infoUrl + content);
			}

			//保存到sd卡
			File file = new  File(Environment.getExternalStorageDirectory() +"/" + "fise" +"/" + Info.getPeerId());
			if(!file.exists()) {
				file.mkdirs();
			}
			//把图片保存到项目的根目录
			File imageFile = new File(file.getAbsolutePath() +"/" + "qr_code" + Info.getMainName() +".jpg");

			FileOutputStream out;
			try {
				out = new FileOutputStream(imageFile);
				QrBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
				out.flush();
				out.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void initDetailProfile() {

		ProgressBar progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
		progress_bar.setVisibility(View.GONE);

        infoImage = (IMBaseImageView) this
                .findViewById(R.id.contact_portrait);
		infoImage
				.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
		infoImage.setCorner(9);
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

	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
			Info = imService.getLoginManager().getLoginInfo();
			initDetailProfile();
			break;

		case USER_QR_CODE_SAVE:
			Utils.showToast(InfoQRActivity.this, "二维码保存成功");
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
