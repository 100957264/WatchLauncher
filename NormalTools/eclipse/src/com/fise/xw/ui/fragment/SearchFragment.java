package com.fise.xw.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mogujie.tools.ScreenTools;
import com.fise.xw.DB.entity.GroupEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.R;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.imservice.event.LoginEvent;
import com.fise.xw.imservice.event.ReqFriendsEvent;
import com.fise.xw.imservice.event.UserInfoEvent;
import com.fise.xw.imservice.support.IMServiceConnector;
import com.fise.xw.imservice.service.IMService; 
import com.fise.xw.ui.activity.SearchFriednsActivity;
import com.fise.xw.ui.adapter.SearchAdapter;
import com.fise.xw.ui.base.TTBaseFragment;
import com.fise.xw.ui.widget.SearchEditText;
import com.fise.xw.utils.Logger;

import de.greenrobot.event.EventBus;

import java.util.List;

/**
 * @yingmu  modify
 */
public class SearchFragment extends TTBaseFragment {

	private Logger logger = Logger.getLogger(SearchFragment.class);
	private View curView = null;
	private ListView listView;
    private View noSearchResultView;
     
    private TextView searchString;
	private SearchAdapter adapter;
	IMService imService;
	private ProgressDialog progressDialog;
	protected SearchEditText topSearchEdt_tt;
	private int type;
	
    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("config#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            //init set adapter service
            initAdapter();
        }
        @Override
        public void onServiceDisconnected() {
        }
    };

     
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		imServiceConnector.connect(this.getActivity());
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		
		type = SearchFragment.this.getActivity().getIntent().getIntExtra(
				IntentConstant.KEY_SEARCH_TYPE, 0);
 
		EventBus.getDefault().register(this);
		curView = inflater.inflate(R.layout.tt_fragment_search, topContentView);
        noSearchResultView = curView.findViewById(R.id.layout_no_search_result);
        searchString = (TextView) curView.findViewById(R.id.search_string);
        progressDialog = new ProgressDialog(this.getActivity());  
        
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
       // progressDialog.setIcon(R.drawable.alert_dialog_icon);  
        progressDialog.setMessage("搜索好友");  
        progressDialog.setCancelable(true);  
        
        
        noSearchResultView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
              //  getActivity().finish();
            	progressDialog.show();
            	String friends = topSearchEdt_tt.getText().toString();
            	
            	UserEntity contact = imService.getContactManager().getSearchContact(friends);//先从本地查找
            	
            	if(contact == null)
            	{
            		imService.getUserActionManager().reqFriends(friends);
            		
            	}else{
            		
            		//contact.setFriend(1);
            		imService.getUserActionManager().setSearchInfo(contact); 
            		
                   	progressDialog.dismiss();
                    Intent intent = new Intent(SearchFragment.this.getActivity(), SearchFriednsActivity.class); 
                    SearchFragment.this.getActivity().startActivity(intent);
            	}
            	
            	
            	   
            }
        });
		initTopBar();
        listView = (ListView) curView.findViewById(R.id.search);
        
    	//TextView search_cancel = (TextView) curView.findViewById(R.id.search_cancel); 
        TextView search_cancel = (TextView) topContentView.findViewById(R.id.search_cancel);
    	
		search_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                getActivity().finish();
            }
        });         
		
		
		return curView;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@SuppressLint({ "NewApi", "ResourceAsColor" }) private void initTopBar() {
		setTopBar(R.drawable.tt_top_default_bk);
		showTopSearchBar();
		//setTopLeftButton(R.drawable.tt_top_back); 
		this.hideTopBar();
		topRightTitleTxt.setPadding(0, 0, ScreenTools.instance(getActivity()).dip2px(30), 0);
		topRightTitleTxt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                getActivity().finish();
            }
        });
		 
		topSearchEdt_tt = (SearchEditText) topContentView.findViewById(R.id.chat_title_search_tt);
		//chat_title_search_tt 
		topSearchEdt_tt.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String key = s.toString();
				adapter.setSearchKey(key);
                if(key.isEmpty())
                {
                    adapter.clear();
                    noSearchResultView.setVisibility(View.GONE);
                }else{
                    searchEntityLists(key);
                }
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	
    public void onEventMainThread(ReqFriendsEvent event){ 
        switch (event){  
            case REQ_FRIENDS_SUCCESS:  
            	progressDialog.dismiss();
                Intent intent = new Intent(this.getActivity(), SearchFriednsActivity.class); 
                this.getActivity().startActivity(intent);
                break;
            case REQ_FRIENDS_FAILED:  
            	progressDialog.dismiss();
            	searchString.setText("该用户不存在");
            	 
                break;
        }
    }
  
    private void initAdapter(){
        adapter = new SearchAdapter(getActivity(),imService,type);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter);
        listView.setOnItemLongClickListener(adapter);
        
        
    }

    // 文字高亮search 模块
	private void searchEntityLists(String key) {
		
		if(type == DBConstant.SEACHDEVICE){
			
		      List<UserEntity> contactList = imService.getContactManager().getSearchDeviceList(key);
		        int contactSize = contactList.size();
		        adapter.putUserList(contactList,type); 
		        adapter.notifyDataSetChanged();
		        
		        noSearchResultView.setVisibility(View.GONE);
		        searchString.setVisibility(View.GONE);
		  
		        
		}else{
		      List<UserEntity> contactList = imService.getContactManager().getSearchContactList(key);
		        int contactSize = contactList.size();
		        adapter.putUserList(contactList,type);

		        List<GroupEntity> groupList = imService.getGroupManager().getSearchAllGroupList(key);
		        if(groupList.size()<=0)
		        {
		        	groupList = imService.getGroupManager().getSearchAllWeiGroupList(key);
		        }
		        
		        int groupSize = groupList.size();
		        adapter.putGroupList(groupList,type);

//		        List<DepartmentEntity> departmentList = imService.getContactManager().getSearchDepartList(key);
//		        int deptSize = departmentList.size();
//		        adapter.putDeptList(departmentList);

		        int sum = contactSize + groupSize ;
		        adapter.notifyDataSetChanged();
		        if(sum <= 0){
		            noSearchResultView.setVisibility(View.VISIBLE);
		            searchString.setText("搜索:" + key);
		        }else{
		            noSearchResultView.setVisibility(View.VISIBLE);
		            searchString.setText("搜索:" + key);
		        } 
		}
  
	}

  	@Override
	protected void initHandler() {
	}

    @Override
    public void onDestroy() {
        imServiceConnector.disconnect(getActivity());
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
