package com.fise.xw.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.manager.IMLoginManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.protobuf.IMBaseDefine.ChangeDataType;
import com.fise.xw.ui.base.TTBaseActivity;

import de.greenrobot.event.EventBus;


/**
 *  查看 信息的昵称界面
 * @author weileiguan
 *
 */
public class NickNameActivity extends   TTBaseActivity {

	private EditText nick_name ;
	private  static IMService imService;
	
	 private IMServiceConnector imServiceConnector = new IMServiceConnector(){
	        @Override
	        public void onIMServiceConnected() {
	            logger.d("config#onIMServiceConnected");
	            imService = imServiceConnector.getIMService();
				if (imService == null) {
					// 后台服务启动链接失败
					return ;
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
		setContentView(R.layout.tt_activity_nickname);
	    imServiceConnector.connect(this);
	    EventBus.getDefault().register(this);
	    
		
		TextView left_text =(TextView)findViewById(R.id.left_text);  
		left_text.setOnClickListener(new View.OnClickListener() {

	         public void onClick(View v) { 
	        	 NickNameActivity.this.finish();
	         } 
         });
		
		Button icon_arrow =(Button)findViewById(R.id.icon_arrow);  
		icon_arrow.setOnClickListener(new View.OnClickListener() {

	         public void onClick(View v) { 
	        	 NickNameActivity.this.finish();
	         } 
         });
		
		
		final UserEntity user =  IMLoginManager.instance().getLoginInfo();
		nick_name =(EditText)findViewById(R.id.nick_name);  
		nick_name.setText("" + user.getMainName());
		
		TextView right_text =(TextView)findViewById(R.id.right_text);  
		right_text.setOnClickListener(new View.OnClickListener() {

	         public void onClick(View v) {  
	        	 //保存
	        	
	        	 String data = nick_name.getText().toString();
	        	 IMContactManager.instance().ChangeUserInfo(user.getPeerId(),ChangeDataType.CHANGE_USERINFO_NICK,data);
	         } 
         });
		
		
		
	}

	
	  public void onEventMainThread(UserInfoEvent event){
	        switch (event){ 
	        
	        case USER_INFO_DATA_UPDATE:  
	        	NickNameActivity.this.finish();
	        	//Toast.makeText(NickNameActivity.this, "修改昵称成功", Toast.LENGTH_SHORT).show();
	        	break; 
	                
	        }
	    }
	  
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		EventBus.getDefault().unregister(this);
	    imServiceConnector.disconnect(this);
	}

}
