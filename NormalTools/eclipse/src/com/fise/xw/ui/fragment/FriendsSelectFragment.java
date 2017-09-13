package com.fise.xw.ui.fragment;
 
import java.util.List; 
import android.content.Context; 
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher; 
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener; 
import android.widget.ListView; 
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R; 
import com.fise.xw.DB.entity.PeerEntity;
import com.fise.xw.DB.entity.UserEntity; 
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.entity.CardMessage;
import com.fise.xw.imservice.event.GroupEvent; 
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.adapter.FriendsSelectAdapter; 
import com.fise.xw.ui.widget.SearchEditText;
import com.fise.xw.ui.widget.SortSideBar;
import com.fise.xw.ui.widget.SortSideBar.OnTouchingLetterChangedListener;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;


/*
 *  发送名片 (好友中)
 */

public class FriendsSelectFragment extends MainFragment
        implements OnTouchingLetterChangedListener {

    private static Logger logger = Logger.getLogger(FriendsSelectFragment.class);

    private View curView = null;
    private IMService imService; 
    /**列表视图
     * 1. 需要两种状态:选中的成员List  --》确定之后才会回话页面或者详情
     * 2. 已经被选的状态 -->已经在群中的成员
     * */
    private FriendsSelectAdapter adapter;
    private ListView contactListView;

    private SortSideBar sortSideBar;
    private TextView dialog;
    private SearchEditText searchEditText;
 
    private PeerEntity peerEntity; 
    private String curSessionKey;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        imServiceConnector.connect(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        imServiceConnector.disconnect(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_friends_member_select, topContentView); //tt_fragment_group_member_select
        super.init(curView);
        initRes();
        return curView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("groupselmgr#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            
            if(imService == null)
            {
            	return ;
            }
            Intent intent = getActivity().getIntent(); 
             curSessionKey = intent.getStringExtra(IntentConstant.KEY_SESSION_KEY);
             peerEntity = imService.getSessionManager().findPeerEntity(curSessionKey);
            initContactList();
        }

        @Override
        public void onServiceDisconnected() {}
    };


    private void initContactList() {
        // 根据拼音排序
        adapter = new FriendsSelectAdapter(getActivity(),imService);
        contactListView.setAdapter(adapter);

        //contactListView.setOnItemClickListener(adapter);
        contactListView.setOnItemLongClickListener(adapter);

        
        contactListView.setOnItemClickListener(new OnItemClickListener() {    
            
            @Override    
            public void onItemClick(AdapterView<?> arg0, View view,    
                    int position, long arg3) {    
//            	FriendsHolder holder = (FriendsHolder) view.getTag();     
                UserEntity contact = (UserEntity) (adapter.getItem(position));
                UserEntity loginInfo = imService.getLoginManager().getLoginInfo();
                CardMessage cardMessage = CardMessage.buildForSend(loginInfo, peerEntity, contact);
                imService.getMessageManager().sendCard(cardMessage); 
                FriendsSelectFragment.this.getActivity().finish();
                 
            }    
 
        }); 
       
        List<UserEntity> contactList = imService.getContactManager().getContactFriendsSortedList(); //guanweile getContactSortedList
        adapter.setAllUserList(contactList);
        
    }


    /**
     * @Description 初始化资源
     */
    private void initRes() {
    	 
        // 设置标题栏
        // todo eric
    	this.hideTopBar();
        setTopTitle(getString(R.string.choose_contact)); 
        setTopRightText(getActivity().getString(R.string.confirm));
        
        topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        setTopLeftText(getResources().getString(R.string.cancel));

  
        sortSideBar = (SortSideBar) curView.findViewById(R.id.sidrbar);
        sortSideBar.setOnTouchingLetterChangedListener(this);

        dialog = (TextView) curView.findViewById(R.id.dialog);
        sortSideBar.setTextView(dialog);
        
         
        contactListView = (ListView) curView.findViewById(R.id.all_contact_list);
        contactListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                 //如果存在软键盘，关闭掉
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                //txtName is a reference of an EditText Field
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
               }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            	 
            }
        });

       
        
        TextView group_left_txt = (TextView) curView.findViewById(R.id.group_left_txt);
        group_left_txt.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   getActivity().finish();
               }
           });
    	
    	  
        searchEditText = (SearchEditText) curView.findViewById(R.id.filter_edit);
        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                String key = s.toString();
                if(TextUtils.isEmpty(key)){
                    adapter.recover();
                    sortSideBar.setVisibility(View.VISIBLE);
                }else{
                    sortSideBar.setVisibility(View.INVISIBLE);
                    adapter.onSearch(key);
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


    public void onEventMainThread(GroupEvent event){
        switch (event.getEvent()){
            case CHANGE_GROUP_MEMBER_SUCCESS:
                handleGroupMemChangeSuccess(event);
                break;
            case CHANGE_GROUP_DELETE_SUCCESS:
                handleGroupMemChangeSuccess(event);
                break;
                
            case CHANGE_GROUP_MEMBER_FAIL:
            case CHANGE_GROUP_MEMBER_TIMEOUT:
                handleChangeGroupMemFail();
                break;
            case CREATE_GROUP_OK:
                handleCreateGroupSuccess(event);
                break;
            case CREATE_GROUP_FAIL:
            case CREATE_GROUP_TIMEOUT:
                handleCreateGroupFail();
                break;

        }
    }

    /**
     * 处理群创建成功、失败事件
     * @param event
     */
    private void handleCreateGroupSuccess(GroupEvent event) {
        logger.d("groupmgr#on CREATE_GROUP_OK");
        String groupSessionKey = event.getGroupEntity().getSessionKey();
        IMUIHelper.openChatActivity(getActivity(),groupSessionKey);
        getActivity().finish();
    }

    private void handleCreateGroupFail() {
        logger.d("groupmgr#on CREATE_GROUP_FAIL");
        hideProgressBar();
        Toast.makeText(getActivity(), getString(R.string.create_temp_group_failed), Toast.LENGTH_SHORT).show();
    }

    /**
     * 处理 群成员增加删除成功、失败事件
     * 直接返回群详情管理页面
     * @param event
     */
    private void handleGroupMemChangeSuccess(GroupEvent event) {
        logger.d("groupmgr#on handleGroupMemChangeSuccess");
        getActivity().finish();
    }


    private void handleChangeGroupMemFail() {
        logger.d("groupmgr#on handleChangeGroupMemFail");
        hideProgressBar();
        Toast.makeText(getActivity(), getString(R.string.change_temp_group_failed), Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void initHandler() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        // TODO Auto-generated method stub
        int position = adapter.getPositionForSection(s.charAt(0));
        if (position != -1) {
            contactListView.setSelection(position);
        }
    }

}
