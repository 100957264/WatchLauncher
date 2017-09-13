package com.fise.xiaoyu.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.sp.ConfigurationSp;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.Security;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.FilletDialog;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.BitmapFillet;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.ImageUtil;
import com.fise.xiaoyu.utils.Utils;
import com.google.zxing.WriterException;
import com.jinlin.zxing.example.activity.CodeCreator;
import com.jinlin.zxing.example.utils.LogoConfig;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *  我的界面的个人信息界面
 */
public class ActivityLoginInfo extends TTBaseActivity {
	
	private static IMService imService;
	private UserEntity loginInfo;
	private IMLoginManager imLoginManager = IMLoginManager.instance();
	public final int QR_CODR = 1000;
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

			loginInfo = imService.getLoginManager().getLoginInfo();

			initDetailProfile();
            String avatarUrl = ConfigurationSp.instance(getApplicationContext() ,loginInfo.getPeerId()).getStrCfg("avatar_url");
            if(!avatarUrl.equals(loginInfo.getAvatar())){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        saveQrCodePng();
                    }
                }).start();
            }

			if(Utils.isClientType(loginInfo)){
				Button logout_button = (Button) findViewById(R.id.logout_button);
				logout_button.setVisibility(View.GONE);
			}
		}

		@Override
		public void onServiceDisconnected() {
		}
	};

    private void saveQrCodePng() {
        // 这里写入子线程需要做的工作
        Bitmap bitmap = ImageUtil.returnBitmap(loginInfo.getAvatar());
        //将生成的logo url保存起来
        ConfigurationSp.instance(getApplicationContext() ,loginInfo.getPeerId()).setStrCfg("avatar_url" ,loginInfo.getAvatar());
        int peeid = loginInfo.getPeerId();
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
            File file = new  File(Environment.getExternalStorageDirectory() +"/" + "fise" +"/" + loginInfo.getPeerId());
            if(!file.exists()) {
                file.mkdirs();
            }
            //把图片保存到项目的根目录
            File imageFile = new File(file.getAbsolutePath() +"/" + "qr_code" + loginInfo.getMainName() +".jpg");

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tt_activity_my);
		imServiceConnector.connect(this);

		TextView left_text = (TextView) findViewById(R.id.left_text);
		left_text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				ActivityLoginInfo.this.finish();
			}
		});

		Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
		icon_arrow.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				ActivityLoginInfo.this.finish();
			}
		});

		RelativeLayout qr_code = (RelativeLayout) findViewById(R.id.qr_code);
		qr_code.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(ActivityLoginInfo.this,
						InfoQRActivity.class);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if(QrBitmap != null){
                    QrBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] bytes=baos.toByteArray();
                    Bundle b = new Bundle();
                    b.putByteArray("bitmap", bytes);
                    intent.putExtras(b);
                }
                ActivityLoginInfo.this.startActivity(intent);
			}
		});

		Button logout_button = (Button) findViewById(R.id.logout_button);
		logout_button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				 
				final FilletDialog myDialog = new FilletDialog(ActivityLoginInfo.this,FilletDialog.FILLET_DIALOG_TYPE.FILLET_DIALOG_WITHOUT_MESSAGE);

		        myDialog.setTitle(getString(R.string.exit_teamtalk_tip));//设置标题
		        myDialog.dialog.show();//显示
		        
		        //确认按键回调，按下确认后在此做处理
		        myDialog.setMyDialogOnClick(new FilletDialog.MyDialogOnClick() {
		            @Override
		            public void ok() {
		            	IMLoginManager.instance().setKickout(false);
						IMLoginManager.instance().logOut();
						ActivityLoginInfo.this.finish();
						myDialog.dialog.dismiss();
		            }
		        });
		         
			}
		});

	}

	public void initDetailProfile() {

		TextView locality_text = (TextView) findViewById(R.id.locality_text);
		if (loginInfo.getProvince().equals(loginInfo.getCity())) {
			locality_text.setText("" + loginInfo.getCity());
		} else {
			locality_text.setText("" + loginInfo.getProvince() + " "
					+ loginInfo.getCity());
		}

		TextView sex_text = (TextView) findViewById(R.id.sex_text);
		if (loginInfo.getGender() == DBConstant.SEX_MAILE) {
			sex_text.setText("男");
		} else {
			sex_text.setText("女");
		}

		RelativeLayout sex = (RelativeLayout) findViewById(R.id.sex);
		sex.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openLoginInfoSexActivity(ActivityLoginInfo.this,DBConstant.SEX_INFO_USER,loginInfo.getPeerId());
			}
		});

		RelativeLayout signature = (RelativeLayout) findViewById(R.id.signature);
		signature.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openLoginInfoSignedActivity(ActivityLoginInfo.this);
			}
		});

		RelativeLayout locality = (RelativeLayout) findViewById(R.id.locality_relat);
		locality.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openCityListActivity(ActivityLoginInfo.this);
			}
		});

		RelativeLayout login_name = (RelativeLayout) findViewById(R.id.login_name);
		login_name.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// IMUIHelper.openAvatarActivity(ActivityLoginInfo.this);
				Intent intent = new Intent(ActivityLoginInfo.this,
						HeadPortraitActivity.class);
				intent.putExtra(IntentConstant.KEY_AVATAR_URL,
						loginInfo.getAvatar());
				intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR,
						true);

				startActivity(intent);

			}
		});

		RelativeLayout nickname = (RelativeLayout) findViewById(R.id.nickname);
		nickname.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				IMUIHelper.openSetNickNameActivity(ActivityLoginInfo.this,
						DBConstant.OWN_NICK, loginInfo.getPeerId());
			}
		});

		TextView wei_text = (TextView) findViewById(R.id.wei_text);
		wei_text.setText(loginInfo.getRealName());

		TextView nickname_text = (TextView) findViewById(R.id.nickname_text);
		nickname_text.setText(loginInfo.getMainName());

		IMBaseImageView user_portrait = (IMBaseImageView) findViewById(R.id.user_portrait);
		// 头像设置
		user_portrait
				.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
		user_portrait.setCorner(8);
		user_portrait
				.setImageResource(R.drawable.tt_default_user_portrait_corner);
		user_portrait.setImageUrl(loginInfo.getAvatar());

		TextView signature_text = (TextView) findViewById(R.id.signature_text);
		signature_text.setText(loginInfo.getSign_info() + "");
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {

		case USER_INFO_DATA_UPDATE:
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
		loginInfo = imLoginManager.getLoginInfo();
        String avatarUrl = ConfigurationSp.instance(getApplicationContext() ,loginInfo.getPeerId()).getStrCfg("avatar_url");
        if(!avatarUrl.equals(loginInfo.getAvatar())){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    saveQrCodePng();
                }
            }).start();
        }
	}
}
