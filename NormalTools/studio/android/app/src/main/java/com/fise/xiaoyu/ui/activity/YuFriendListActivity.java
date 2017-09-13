package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.imservice.event.DeviceEvent;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.adapter.YuFriendsListAdspter;
import com.fise.xiaoyu.ui.adapter.YuGroupListAdspter;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.menu.QrDevMenu;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 雨友列表
 */
public class YuFriendListActivity extends TTBaseActivity {

    private static IMService imService;
    public QrDevMenu menu;
    private List<UserEntity> userList;
    private YuFriendsListAdspter adapter;
    private ListView listView = null;


    private List<GroupEntity>  groupList;
    private List<UserEntity>  manageYuList;


    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("config#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                // 后台服务启动链接失败
                return;
            }

            userList = imService.getContactManager().getContactDevicesList();
            groupList = imService.getGroupManager().getNormalFamilyGroupList();
           // manageYuList = imService.getContactManager().getContactParentList();
            if(Utils.isClientType(IMLoginManager.instance().getLoginInfo())){
                manageYuList = imService.getContactManager().getContactParentList();
            }else{
                manageYuList = new ArrayList<>();
            }

            //设备列表
            adapter = new YuFriendsListAdspter(YuFriendListActivity.this, userList,manageYuList,groupList, imService);
            listView.setAdapter(adapter);
            listView.setOnItemLongClickListener(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                                        int position, long arg3) {


                    PeerEntity peerEntity;
                    if(position <userList.size()){
                        peerEntity = userList.get(position);
                    }else  if((position >=userList.size())&&(position <(userList.size() + manageYuList.size()))){
                        peerEntity = manageYuList.get(position - userList.size());
                    }else{
                        peerEntity = groupList.get(position - (userList.size() + manageYuList.size()));
                    }

                    if (peerEntity.getType() ==  DBConstant.SESSION_TYPE_SINGLE) {

                        UserEntity userEntity = (UserEntity) peerEntity;
                        boolean isWeiFriends = false;
                        if (userEntity.getIsFriend() == DBConstant.FRIENDS_TYPE_YUYOU) {
                            isWeiFriends = true;
                        }
                        IMUIHelper.openUserProfileActivity(YuFriendListActivity.this, userEntity.getPeerId(),
                                isWeiFriends);

                    } else if (peerEntity.getType() ==  DBConstant.SESSION_TYPE_GROUP) {
                        GroupEntity groupEntity = (GroupEntity) peerEntity;
                        IMUIHelper.openChatActivity(YuFriendListActivity.this, groupEntity.getSessionKey());
                    }
                }
            });


        }

        @Override
        public void onServiceDisconnected() {

        }
    };


    public void renderDeviceList() {
        userList = imService.getContactManager().getContactDevicesList();
        if(Utils.isClientType(IMLoginManager.instance().getLoginInfo())){
            manageYuList = imService.getContactManager().getContactParentList();
        }else{
            manageYuList = new ArrayList<>();
        }

        groupList = imService.getGroupManager().getNormalFamilyGroupList();
        adapter.putYuFriendsList(userList,manageYuList,groupList);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_yu_friends);

        imServiceConnector.connect(this);
                menu = new QrDevMenu(this);
        menu.addItems(new String[] { "扫描添加", "手动添加","购买设备" });



        listView = (ListView) findViewById(R.id.list_yu_friends);

        Button icon_arrow = (Button) findViewById(R.id.icon_arrow);
        icon_arrow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                YuFriendListActivity.this.finish();
            }
        });

        TextView left_text = (TextView) findViewById(R.id.left_text);
        left_text.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                YuFriendListActivity.this.finish();
            }
        });


        Button addfriend_button = (Button) findViewById(R.id.addfriend_button);
        addfriend_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                menu.showAsDropDown(v);
            }
        });


    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginEvent event) {
        switch (event) {
            case LOCAL_LOGIN_MSG_SERVICE:
            case LOGIN_OK: {
                renderDeviceList();
            }
            break;
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeviceEvent event) {
        switch (event) {

            case USER_INFO_ADD_DEVICE_SUCCESS:
                break;
            case USER_INFO_UPDATE_INFO_SUCCESS:
                renderDeviceList();
                break;
            case USER_INFO_ADD_DEVICE_FAILED:
                // Utils.showToast(DeviceListActivity.this,
                // imService.getDeviceManager().getError());
                break;

            case USER_INFO_DELETE_DEVICE_SUCCESS:
              //  Utils.showToast(YuFriendListActivity.this, "删除设备成功");
                renderDeviceList();
                break;

            case USER_INFO_DELETE_AUTH_SUCCESS:
                renderDeviceList();
                break;


            case USER_INFO_SETTING_DEVICE_SUCCESS:
                renderDeviceList();
                break;


        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserInfoEvent event) {
        switch (event) {

            case USER_INFO_DELETE_FAIL:
                Utils.showToast(YuFriendListActivity.this, "删除设备失败");
                break;
//		case USER_P2PCOMMAND_OFFLINE_HINT:
//            RuntimeException here = new RuntimeException("here");
//            here.fillInStackTrace();
//            Log.w("aaa", "CallStackTrace: " + this, here);
//            Utils.showToast(this.getActivity(), "对方不在线");
//			break;
            case USER_INFO_UPDATE_STAT:
                renderDeviceList();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
                imServiceConnector.disconnect(this);
    }

}
