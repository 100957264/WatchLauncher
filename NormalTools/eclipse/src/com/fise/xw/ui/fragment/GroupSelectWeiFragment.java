package com.fise.xw.ui.fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xw.R;
import com.fise.xw.DB.entity.GroupEntity;
import com.fise.xw.DB.entity.PeerEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.GroupEvent;
import com.fise.xw.imservice.manager.IMContactManager;
import com.fise.xw.imservice.manager.IMGroupManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.ui.activity.AuthSelectActivity;
import com.fise.xw.ui.adapter.GroupSelectAdapter;
import com.fise.xw.ui.adapter.GroupSelectAdapter.UserHolder;
import com.fise.xw.ui.adapter.GroupWeiSelectAdapter;
import com.fise.xw.ui.adapter.GroupWeiSelectAdapter.WeiUserHolder;
import com.fise.xw.ui.widget.SearchEditText;
import com.fise.xw.ui.widget.SortSideBar;
import com.fise.xw.ui.widget.SortSideBar.OnTouchingLetterChangedListener;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;


/**
 * @YM  - -!
 * 1. 创建群的时候，跳到聊天页面
 * 2. 新增人员的时候，返回到聊天详情页面
 */
public class GroupSelectWeiFragment extends MainFragment
        implements OnTouchingLetterChangedListener {

    private static Logger logger = Logger.getLogger(GroupSelectWeiFragment.class);

    private View curView = null;
    private IMService imService;
    private TextView right_txt;
    /**列表视图
     * 1. 需要两种状态:选中的成员List  --》确定之后才会回话页面或者详情
     * 2. 已经被选的状态 -->已经在群中的成员
     * */
    private GroupWeiSelectAdapter adapter;
    private ListView contactListView;

    private SortSideBar sortSideBar;
    private TextView dialog;
    private SearchEditText searchEditText;
 
   // private PeerEntity peerEntity;
    private ArrayList<String> listStr;
    
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
        curView = inflater.inflate(R.layout.tt_fragment_group_wei_select, topContentView);
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
//        if(peerEntity == null){
//            Toast.makeText(getActivity(), getString(R.string.error_group_info), Toast.LENGTH_SHORT).show();
//            getActivity().finish();
//            logger.e("[fatal error,groupInfo is null,cause by SESSION_TYPE_GROUP]");
//            //return Collections.emptySet();
//        }
//        switch (peerEntity.getType()){
//            case DBConstant.SESSION_TYPE_GROUP:{
//                GroupEntity entity = (GroupEntity) peerEntity;
//                alreadyListSet.addAll(entity.getlistGroupMemberIds());
//            }break;
//
//            case DBConstant.SESSION_TYPE_SINGLE:{
//                int loginId = imService.getLoginManager().getLoginId();
//                alreadyListSet.add(loginId);
//                alreadyListSet.add(peerEntity.getPeerId());
//            }break;
//        }
        
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
            
            /**已经处于选中状态的list*/
            Set<Integer> alreadyList = getAlreadyCheckList();
 
            
            initContactList(alreadyList);
        }

        @Override
        public void onServiceDisconnected() {}
    };


    private void initContactList(final Set<Integer> alreadyList) {
        // 根据拼音排序
        adapter = new GroupWeiSelectAdapter(getActivity(),imService);
        contactListView.setAdapter(adapter);
 
        contactListView.setOnItemLongClickListener(adapter);

        
        contactListView.setOnItemClickListener(new OnItemClickListener() {    
            
            @Override    
            public void onItemClick(AdapterView<?> arg0, View view,    
                    int position, long arg3) {    
            	WeiUserHolder holder = (WeiUserHolder) view.getTag();    
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

        }); 
       
        List<UserEntity> contactList = imService.getContactManager().getContactWeiSelectList(); //guanweile 加上自己
        adapter.setAllUserList(contactList);
        adapter.setAlreadyListSet(alreadyList);
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

      
        
        RelativeLayout select_group = (RelativeLayout) curView.findViewById(R.id.select_group);
        select_group.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) { 
				IMUIHelper.openSelectGroupActivity(GroupSelectWeiFragment.this.getActivity(),true);
			}
		});
        
        
        TextView group_left_txt = (TextView) curView.findViewById(R.id.group_left_txt);
        group_left_txt.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   getActivity().finish();
               }
           });
    	
    	
    	
    	right_txt = (TextView) curView.findViewById(R.id.group_right_txt);
    	right_txt.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   logger.d("tempgroup#on 'save' btn clicked");

                   if(adapter.getCheckListSet().size()<=0){
                       Toast.makeText(getActivity(), getString(R.string.select_group_member_empty), Toast.LENGTH_SHORT).show();
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
                           groupMgr.reqCreateWeiGroup(tempGroupName,memberList); //reqCreateTempGroup
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

                   /**只有输入框中有值的时候,确定按钮才可以按下*/
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

                   /**对话框弹出的时候，下面的键盘也要跟上来*/
                   Timer timer = new Timer();
                   timer.schedule(new TimerTask(){
                       @Override
                       public void run() {
                           InputMethodManager inputManager =
                                   (InputMethodManager)editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

                           inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                       }
                   }, 100);
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
