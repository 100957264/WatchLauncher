package com.fise.xiaoyu.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.HandlerConstant;
import com.fise.xiaoyu.imservice.event.GroupEvent;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.adapter.ContactAdapter;
import com.fise.xiaoyu.ui.widget.SortSideBar;
import com.fise.xiaoyu.ui.widget.SortSideBar.OnTouchingLetterChangedListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 通讯录 （全部、部门）
 */
public class ContactFragment extends MainFragment implements OnTouchingLetterChangedListener {
    private View curView = null;
    private static Handler uiHandler = null;
    private ListView allContactListView;
  //  private ListView departmentContactListView;
    private SortSideBar sortSideBar;
    private TextView dialog;

    private ContactAdapter contactAdapter; 

    private IMService imService;
    private IMContactManager contactMgr;
    private int curTabIndex = 0; 
    

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("contactUI#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            if (imService == null) {
                logger.e("ContactFragment#onIMServiceConnected# imservice is null!!");
                return;
            }
            contactMgr = imService.getContactManager();

            // 初始化视图
            initAdapter();
            renderEntityList();
            if (!EventBus.getDefault().isRegistered(ContactFragment.this)) {
                EventBus.getDefault().register(ContactFragment.this);
            }
        }

        @Override
        public void onServiceDisconnected() {
            if (EventBus.getDefault().isRegistered(ContactFragment.this)) {
                EventBus.getDefault().unregister(ContactFragment.this);
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        closeAutoEventbus();
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(getActivity());
	
        initHandler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(getActivity());
    }


    @SuppressLint("HandlerLeak")
    @Override
    protected void initHandler() {
        uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HandlerConstant.HANDLER_CHANGE_CONTACT_TAB:
                        if (null != msg.obj) {
                            curTabIndex = (Integer) msg.obj;
                            if (0 == curTabIndex) {
                                allContactListView.setVisibility(View.VISIBLE);
                            } else {
                                allContactListView.setVisibility(View.GONE);
                            }
                        }
                        break;
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_contact, topContentView);
        initRes();
        return curView;
    }

    /**
     * @Description 初始化界面资源
     */
    private void initRes() {
        // 设置顶部标题栏
        showContactTopBar(); 
        hideTopBar(); 
        hideTopLeftTitle();
        setTopNameTitle(getActivity().getString(R.string.contact_title_name));
        super.init(curView);
        showProgressBar();

        sortSideBar = (SortSideBar) curView.findViewById(R.id.sidrbar);
        dialog = (TextView) curView.findViewById(R.id.dialog);
        sortSideBar.setTextView(dialog);
        sortSideBar.setOnTouchingLetterChangedListener(this);

        allContactListView = (ListView) curView.findViewById(R.id.all_contact_list);

        allContactListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

    }

    private void initAdapter(){
        contactAdapter = new ContactAdapter(getActivity(),imService);
        allContactListView.setAdapter(contactAdapter);

        // 单击视图事件
        allContactListView.setOnItemClickListener(contactAdapter);
        allContactListView.setOnItemLongClickListener(contactAdapter);

        renderUserList();
        searchDataReady();
    }

    public void locateDepartment(int departmentId) {
        logger.d("department#locateDepartment id:%s", departmentId);
        locateDepartmentImpl(departmentId);
    }

    private void locateDepartmentImpl(int departmentId) {
        if (imService == null) {
            return;
        }

    }


    /**
     * 刷新单个entity
     * 很消耗性能
     */
    private void renderEntityList() {
        hideProgressBar();
        logger.d("contact#renderEntityList");

        if (contactMgr.isUserDataReady() ) {
            renderUserList();
        }
        showSearchFrameLayout();
    }


    private void UpdateUserList(){ 
    	 int unreadNum = imService.getUnReadMsgManager().getTotalReqMessageCount();
        if(unreadNum>0)
    	 { 
    		 contactAdapter.putUser(true);
    	 }else{
    		 contactAdapter.putUser(false);
    	 }
    }

    private void renderUserList(){

    	contactMgr.updateFriends();  // guanweile 可能影响功能
    	contactMgr.updateAllFriends();
        List<UserEntity> contactList = contactMgr.getContactFriendsList();
        // 没有任何的联系人数据
        contactAdapter.putUserList(contactList);
    }



    private ListView getCurListView() {
    	 return allContactListView;
    }

    @Override
    public void onTouchingLetterChanged(String s) {

        int position = -1;
        position =  contactAdapter.getPositionForSection(s.charAt(0));
        if (position != -1) {
            getCurListView().setSelection(position +2);
        }
    }


    public static Handler getHandler() {
        return uiHandler;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMessageEvent(GroupEvent event) {
        switch (event.getEvent()) {
        	case CHANGE_GROUP_DELETE_SUCCESS:
            case USER_GROUP_DELETE_SUCCESS:
            case GROUP_INFO_UPDATED:
            case GROUP_INFO_OK:
//                renderGroupList();
                searchDataReady();
                break;
        }
    }
 
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMessageEvent(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_UPDATE:
            case USER_INFO_OK: 
                renderUserList();
                searchDataReady();
                break; 
            case USER_INFO_REQ_ALL: 
                renderUserList();
                searchDataReady();
                break;

            case USER_INFO_REQ_YU:
                renderUserList();
                searchDataReady();
                break;
            case USER_INFO_REQ_UPDATE: 
            	UpdateUserList(); 
                break;

            case USER_INFO_DELETE_SUCCESS:
                renderUserList();
                searchDataReady();
                break;

                
        }
    }

    public void searchDataReady() {
        if (imService.getContactManager().isUserDataReady() &&
                imService.getGroupManager().isGroupReady()) {
            showSearchFrameLayout();
        }
    }
}
