package com.fise.xiaoyu.ui.fragment;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.GroupEvent;
import com.fise.xiaoyu.imservice.manager.IMGroupManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.protobuf.IMDevice;
import com.fise.xiaoyu.ui.activity.GroupSelectActivity;
import com.fise.xiaoyu.ui.adapter.GroupSelectAdapter;
import com.fise.xiaoyu.ui.adapter.GroupSelectAdapter.UserHolder;
import com.fise.xiaoyu.ui.widget.PassDialog;
import com.fise.xiaoyu.ui.widget.SearchEditText;
import com.fise.xiaoyu.ui.widget.SortSideBar;
import com.fise.xiaoyu.ui.widget.SortSideBar.OnTouchingLetterChangedListener;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.OnkeyBackListener;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @YM  - -!
 * 1. 创建群的时候，跳到聊天页面
 * 2. 新增人员的时候，返回到聊天详情页面
 */
public class GroupSelectFragment extends MainFragment
        implements OnTouchingLetterChangedListener ,OnkeyBackListener{

    private static Logger logger = Logger.getLogger(GroupSelectFragment.class);

    private View curView = null;
    private IMService imService;
    private TextView right_txt;
    /**列表视图
     * 1. 需要两种状态:选中的成员List  --》确定之后才会回话页面或者详情
     * 2. 已经被选的状态 -->已经在群中的成员
     * */
    private GroupSelectAdapter adapter;
    private ListView contactListView;

    private SortSideBar sortSideBar;
    private TextView dialog;
    private SearchEditText searchEditText;

   // private PeerEntity peerEntity;
    private ArrayList<String> listStr;
    private Boolean hideCheckBox;
    private RelativeLayout select_group_member;
    private TextView group_left_txt;
    private final int RESULT_CODE_WATCH_WHITE_NUMBER = 1000 ;
    private final int RESULT_CODE_TO_LIST_CONFIRM = 1001 ;
    private List<UserEntity> contactList;
    private int mDeviceId;
    private UserEntity mCurUserEntity;
    private int selPhoneNmberType;
    private final int SEL_SOS_NUMBER = 1;
    private final int SEL_WHITE_NUMBER = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                imServiceConnector.connect(getActivity());
        GroupSelectActivity activity = (GroupSelectActivity) getActivity();
        activity.setBackListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
                imServiceConnector.disconnect(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_group_member_select, topContentView); 
        super.init(curView);
        initRes();
        return curView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    /**
     * 获取列表中 默认选中成员列表
     * @return
     */
    private Set<Integer> getAlreadyCheckList(){
        Set<Integer> alreadyListSet = new HashSet<>();


       int loginId = imService.getLoginManager().getLoginId();
       alreadyListSet.add(loginId); 
       
        return alreadyListSet;
    }

    private IMServiceConnector imServiceConnector = new IMServiceConnector(){


        @Override
        public void onIMServiceConnected() {
            logger.d("groupselmgr#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            Intent intent = getActivity().getIntent();
           // curSessionKey = intent.getStringExtra(IntentConstant.KEY_SESSION_KEY);
           // peerEntity = imService.getSessionManager().findPeerEntity(curSessionKey);

            hideCheckBox = intent.getBooleanExtra(IntentConstant.HIDE_SELECT_CHECK_BOX ,false);
            mDeviceId = intent.getIntExtra(IntentConstant.KEY_PEERID, 0);
            selPhoneNmberType = intent.getIntExtra(IntentConstant.SEL_PHONE_NUMBER , -1);
            if(mDeviceId != 0 ){
                mCurUserEntity = imService.getContactManager().findDeviceContact(mDeviceId);
            }
            /**已经处于选中状态的list*/
            Set<Integer> alreadyList = getAlreadyCheckList();
            initContactList(alreadyList);
            if(hideCheckBox){
//                setTopRightText(getActivity().getString(R.string.confirm));
                select_group_member.setVisibility(View.GONE);
                group_left_txt.setText(R.string.regist_black);
                right_txt.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onServiceDisconnected() {}
    };


    private void initContactList(final Set<Integer> alreadyList) {
        // 根据拼音排序
        adapter = new GroupSelectAdapter(getActivity(),imService);
        contactListView.setAdapter(adapter);

        //contactListView.setOnItemClickListener(adapter);
        contactListView.setOnItemLongClickListener(adapter);

        
        contactListView.setOnItemClickListener(new OnItemClickListener() {    
            
            @Override    
            public void onItemClick(AdapterView<?> arg0, View view,    
                    int position, long arg3) {
                if(hideCheckBox){
                   //finish 跳转
                    if(mCurUserEntity != null){
//                        if( mCurUserEntity.getUserType() ==  IMBaseDefine.ClientType.CLIENT_TYPE_FISE_DEVICE_VALUE  || selPhoneNmberType == SEL_SOS_NUMBER){
                            //卡片机  ==》直接向服务器请求，分为sos设置和白名单设置，需要区分 ,或者是儿童手表SOS
                            if(selPhoneNmberType == SEL_SOS_NUMBER){
                                imService.getDeviceManager().settingWhite(
                                        mDeviceId,
                                        contactList.get(position).getMainName()+":"+contactList.get(position).getPhone(),
                                        IMDevice.SettingType.SETTING_TYPE_ALARM_MOBILE,
                                        DBConstant.ADD);
                            }else{
                                imService.getDeviceManager().settingWhite(
                                        mDeviceId,
                                        contactList.get(position).getMainName()+":"+contactList.get(position).getPhone(),
                                        IMDevice.SettingType.SETTING_TYPE_ALLOW_MOBILE,
                                        DBConstant.ADD);
                            }
                            Intent intent = new Intent();
                            intent.putExtra("phone_number" , contactList.get(position).getPhone());
                            intent.putExtra("phone_name" , contactList.get(position).getMainName());
                            getActivity().setResult(RESULT_CODE_TO_LIST_CONFIRM , intent);

//                        }
                          //手表和卡片机暂时一样
//                        else  if( mCurUserEntity.getUserType() ==  IMBaseDefine.ClientType.CLIENT_TYPE_FISE_WATCH_VALUE){
//                            //手表 中白名单设置
//                            Intent intent = new Intent();
//                            intent.putExtra("phone_number" , contactList.get(position).getPhone());
//                            getActivity().setResult(RESULT_CODE_WATCH_WHITE_NUMBER , intent);
//
//                        }

                        getActivity().finish();

                    }


                }else{
                    UserHolder holder = (UserHolder) view.getTag();
                    holder.checkBox.toggle();// 在每次获取点击的item时改变checkbox的状态

                    UserEntity contact = (UserEntity) (adapter.getItem(position));
                    int loginId = imService.getLoginManager().getLoginId();

                    //已经选中的不再处理
                    boolean disable = false;
                    if (contact != null) {
                        disable = adapter.getAlreadyListSet().contains(
                                contact.getPeerId());
                    }
                    if(disable){
                        return ;
                    }


                    if (holder.checkBox.isChecked() == true&&((contact.getPeerId()!=loginId))) {
                        listStr.add(holder.nameView.getText().toString());
                        right_txt.setTextColor(getResources().getColor(R.color.cancel_color));
                        adapter.setCheck(contact.getPeerId(),holder.checkBox.isChecked());
                    } else {

                        listStr.remove(holder.nameView.getText().toString());
                        if(listStr.size()<=0){
                            right_txt.setTextColor(getResources().getColor(R.color.select_group_disabled));
                        }
                        adapter.setCheck(contact.getPeerId(),holder.checkBox.isChecked());
                    }

                    if(listStr.size()>0){
                        right_txt.setText("确定"+"(" +listStr.size()+")");
                    }else
                    {
                        right_txt.setText("确定");
                    }
                }


            }    
 
        });

        //guanweile getContactSortedList
        contactList = imService.getContactManager().getContactFriendsSortedList();
        adapter.setAllUserList(contactList);
        adapter.setAlreadyListSet(alreadyList);
        adapter.setCheckBoxShowOrHide(hideCheckBox);
    }


    /**
     * @Description 初始化资源
     */
    private void initRes() {
    	 
        // 设置标题栏
        // todo eric
    	this.hideTopBar();
        setTopTitle(getString(R.string.choose_contact)); 

        
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
        
        
        listStr = new ArrayList<String>();  

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


        select_group_member = (RelativeLayout) curView.findViewById(R.id.select_group_member);
        select_group_member.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) { 
				IMUIHelper.openSelectGroupActivity(GroupSelectFragment.this.getActivity());
			}
		});


        group_left_txt = (TextView) curView.findViewById(R.id.group_left_txt);
        group_left_txt.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent();
                   intent.putExtra("phone_number" , "");
                   getActivity().setResult(RESULT_CODE_WATCH_WHITE_NUMBER , intent);
                   getActivity().finish();
               }
           });
    	
    	
    	
    	right_txt = (TextView) curView.findViewById(R.id.group_right_txt);
    	right_txt.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   logger.d("tempgroup#on 'save' btn clicked");

                   if(adapter.getCheckListSet().size()<=0){
                       Utils.showToast(getActivity(), getString(R.string.select_group_member_empty));
                       return;
                   }

                   int size = adapter.getCheckListSet().size();
                   Set<Integer> checkListSet =  adapter.getCheckListSet();
                   IMGroupManager groupMgr = imService.getGroupManager();
                   int loginId = imService.getLoginManager().getLoginId();
                   checkListSet.add(loginId);
                   
                   
                   if(checkListSet.size()>=3)
                   { 
                	   
                	 ShowDialogForTempGroupname(groupMgr, checkListSet);
                	 
                   }else 
                   {
                	   Iterator it= checkListSet.iterator();
                       while(it.hasNext())
                       {
                           int userId =(int)it.next();
                           if(userId != loginId)
                           {
                        	   UserEntity entity =  imService.getContactManager().findContact(userId);
                        	   IMUIHelper.openChatActivity(getActivity(),entity.getSessionKey());
                               getActivity().finish();
                               
                           }  
                       }
                       
                	   
                   }
                    
                   
                   //从个人过来的，创建群，默认自己是加入的，对方的sessionId也是加入的
                   //自己与自己对话，也能创建群的，这个时候要判断，群组成员一定要大于2个
//                   int sessionType = peerEntity.getType();
//                   if (sessionType == DBConstant.SESSION_TYPE_SINGLE) {
//                       int loginId = imService.getLoginManager().getLoginId();
//                       logger.d("tempgroup#loginId:%d", loginId);
//                       checkListSet.add(loginId);
//                       checkListSet.add(peerEntity.getPeerId());
//                       logger.d("tempgroup#memberList size:%d", checkListSet.size());
//                       ShowDialogForTempGroupname(groupMgr, checkListSet);
//                   } else if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
//                       showProgressBar();
//                       imService.getGroupManager().reqAddGroupMember(peerEntity.getPeerId(),checkListSet);
//                   }
               }
               
               
               private void ShowDialogForTempGroupname(final IMGroupManager groupMgr,final Set<Integer> memberList) {


                   final PassDialog myDialog = new PassDialog(getActivity(),PassDialog.PASS_DIALOG_TYPE.PASS_DIALOG_WITHOUT_MESSAGE);

                   myDialog.setTitle(getActivity().getString(R.string.create_temp_group_dialog_title));//设置内容
                   myDialog.dialog.show();//显示
                   //确认按键回调，按下确认后在此做处理
                   myDialog.setMyDialogOnClick(new PassDialog.MyDialogOnClick() {
                       @Override
                       public void ok()
                       {
                           if (TextUtils.isEmpty(myDialog.getEditText().getText().toString().trim())) {
                               Utils.showToast(getActivity(),"讨论组名字不能为空");
                           }else{
                               String tempGroupName = myDialog.getEditText().getText().toString();
                               tempGroupName = tempGroupName.trim();
                               showProgressBar();
                               groupMgr.reqCreateTempGroup(tempGroupName,memberList);
                               myDialog.dialog.dismiss();

                           }
                       }
                   });


                   /*
                   AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),
                           android.R.style.Theme_Holo_Light_Dialog));

                   LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                   View dialog_view = inflater.inflate(R.layout.tt_custom_dialog, null);
                   final EditText editText = (EditText)dialog_view.findViewById(R.id.dialog_edit_content);
                   TextView textText = (TextView)dialog_view.findViewById(R.id.dialog_title);
                   textText.setText(R.string.create_temp_group_dialog_title);
                   builder.setView(dialog_view);

                   builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {

                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           String tempGroupName = editText.getText().toString();
                           tempGroupName = tempGroupName.trim();
                           showProgressBar();
                           groupMgr.reqCreateTempGroup(tempGroupName,memberList);
                       }
                   });
                   builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {
                           InputMethodManager inputManager =
                                   (InputMethodManager)editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                           inputManager.hideSoftInputFromWindow(editText.getWindowToken(),0);
                       }
                   });
                   final AlertDialog alertDialog = builder.create();


                   editText.addTextChangedListener(new TextWatcher() {
                       @Override
                       public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                       @Override
                       public void onTextChanged(CharSequence s, int start, int before, int count) {}

                       @Override
                       public void afterTextChanged(Editable s) {
                          if(TextUtils.isEmpty(s.toString().trim())){
                              alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                           }else{
                              alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                          }
                       }
                   });

                   alertDialog.show();
                   alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);


                   Timer timer = new Timer();
                   timer.schedule(new TimerTask(){
                       @Override
                       public void run() {
                           InputMethodManager inputManager =
                                   (InputMethodManager)editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

                           inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                       }
                   }, 100);
                   */
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



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GroupEvent event){
        switch (event.getEvent()){
            case CHANGE_GROUP_MEMBER_SUCCESS:
                handleGroupMemChangeSuccess(event);
                break;
            case CHANGE_GROUP_DELETE_SUCCESS:
                handleGroupMemChangeSuccess(event);
                break;
            case USER_GROUP_DELETE_SUCCESS:
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
        Utils.showToast(getActivity(), getString(R.string.create_temp_group_failed));
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
        Utils.showToast(getActivity(), getString(R.string.change_temp_group_failed));
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



    @Override
    public void onkeyBack() {
        Intent intent = new Intent();
        intent.putExtra("phone_number" , "");
        getActivity().setResult(RESULT_CODE_WATCH_WHITE_NUMBER , intent);
        getActivity().finish();

    }
}
