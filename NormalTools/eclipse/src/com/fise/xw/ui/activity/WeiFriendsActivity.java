package com.fise.xw.ui.activity;
 
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.adapter.WeiContactAdapter;
import com.fise.xw.ui.base.TTBaseFragmentActivity;
import com.fise.xw.utils.IMUIHelper;

import de.greenrobot.event.EventBus;


/**
 *  位友列表界面
 * @author weileiguan
 *
 */
public class WeiFriendsActivity extends  TTBaseFragmentActivity{
	
	private  ListView listView=null; 
	private  WeiFriendsActivity activity;
	private  WeiContactAdapter adapter;
	private  static IMService imService;
	private IMContactManager contactMgr;
	private List<UserEntity> weiList;
	
    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("config#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return ;
			}
			 
			weiList =  imService.getContactManager().getContactWeiFriendsList(); 
			 
            adapter = new WeiContactAdapter(WeiFriendsActivity.this,imService);
            listView.setAdapter(adapter);  
            // 单击视图事件
            listView.setOnItemClickListener(adapter);
            listView.setOnItemLongClickListener(adapter);
            
            adapter.putWeiList(weiList);
            
			// 查看用户状态 是否在线
			ArrayList<Integer> userIdStats = new ArrayList<>(1);
			// just single type
			
			for(int i=0;i<weiList.size();i++){
				userIdStats.add(weiList.get(i).getPeerId());
			}
			
			imService.getContactManager().reqGetDetaillUsersStat(
					userIdStats);
         
        }
        @Override
        public void onServiceDisconnected() {
        }
    };
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        setContentView(R.layout.tt_activity_star_list); 
        imServiceConnector.connect(this);
        
        listView=(ListView)findViewById(R.id.list_start);  

   
        activity = this;
        EventBus.getDefault().register(this);
        

        TextView weiwang =(TextView)findViewById(R.id.weiwang);  
        weiwang.setOnClickListener(new View.OnClickListener() {

	         public void onClick(View v) { 
	        	 WeiFriendsActivity.this.finish();
	         } 
         }); 
        
        Button icon_arrow =(Button)findViewById(R.id.icon_arrow);  
        icon_arrow.setOnClickListener(new View.OnClickListener() {

	         public void onClick(View v) { 
	        	 WeiFriendsActivity.this.finish();
	         } 
         });
        
        
        EditText  text = (EditText)findViewById(R.id.search_phone);  
        text.setOnClickListener(new View.OnClickListener() {

	         public void onClick(View v) { 
	        	 IMUIHelper.openSearchFriendActivity(WeiFriendsActivity.this,DBConstant.SEACHFRIENDS);
	         } 
        }); 
        
    }
    
    
    private void renderWeiList(){
    	 
    	List<UserEntity> weiList = imService.getContactManager().getContactWeiFriendsList();  
        // 没有任何的联系人数据 
         adapter.putWeiList(weiList);
    }
     
    public void onEventMainThread(UserInfoEvent event) {
        switch (event) { 
	        case WEI_FRIENDS_INFO_REQ_ALL: { 
	        	renderWeiList(); 
	        }break; 
	        case USER_INFO_UPDATE: { 
	        	renderWeiList();
	        }break;
	        
	        case USER_INFO_UPDATE_STAT: { 
	        	renderWeiList();
	        }break;
	          
	         
	        
        }
    }
    
    @Override
    public void onDestroy() { 
        EventBus.getDefault().unregister(this);
        imServiceConnector.disconnect(this);
        super.onDestroy();
    }
}
