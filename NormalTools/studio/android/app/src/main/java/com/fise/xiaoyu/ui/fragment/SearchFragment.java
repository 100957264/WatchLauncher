package com.fise.xiaoyu.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.ReqFriendsEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.activity.SearchFriednsActivity;
import com.fise.xiaoyu.ui.adapter.SearchAdapter;
import com.fise.xiaoyu.ui.widget.SearchEditText;
import com.fise.xiaoyu.utils.Logger;
import com.mogujie.tools.ScreenTools;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * @yingmu  modify
 */
public class SearchFragment extends MainFragment {

	private Logger logger = Logger.getLogger(SearchFragment.class);
	private View curView = null;
	private ListView listView;
    private View noSearchResultView;
     
    private TextView searchString;
	private SearchAdapter adapter;
	IMService imService;
	protected SearchEditText topSearchEdt_tt;
	private int type;
	private ProgressBar pb;

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
	private LinearLayout touchLayout;


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
				curView = inflater.inflate(R.layout.tt_fragment_search, topContentView);
        noSearchResultView = curView.findViewById(R.id.layout_no_search_result);
        searchString = (TextView) curView.findViewById(R.id.search_string);
		pb = (ProgressBar) curView.findViewById(R.id.progress_bar);
		touchLayout = (LinearLayout)curView.findViewById(R.id.touch_view);
		touchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter.getCount() == 0){
                    SearchFragment.this. getActivity().onBackPressed();//销毁自己
                }
            }
        });
        noSearchResultView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
              //  getActivity().finish();
				pb.setVisibility(View.VISIBLE);

            	String friends = topSearchEdt_tt.getText().toString();
            	UserEntity contact = imService.getContactManager().getSearchContact(friends);//先从本地查找
            	
            	if(contact == null)
            	{
            		imService.getUserActionManager().reqFriends(friends);
            	}else{
            		
            		//contact.setFriend(1);
            		imService.getUserActionManager().setSearchInfo(contact); 
            		
					pb.setVisibility(View.GONE);
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

	@SuppressLint({ "NewApi", "ResourceAsColor" })
	private void initTopBar() {
        showContactTopBar();
        hideTopBar();
        hideTopLeftTitle();
        setTopNameTitle("位信");
		setTopBar(R.drawable.tt_top_default_bk);
		//setTopLeftButton(R.drawable.tt_top_back);
		topRightTitleTxt.setPadding(0, 0, ScreenTools.instance(getActivity()).dip2px(30), 0);
		topRightTitleTxt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                getActivity().finish();
            }
        });
		 
		topSearchEdt_tt = (SearchEditText) curView.findViewById(R.id.chat_title_search_tt);
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
                    touchLayout.setVisibility(View.VISIBLE);
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

	
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ReqFriendsEvent event){
        switch (event){  
            case REQ_FRIENDS_SUCCESS:
                pb.setVisibility(View.GONE);
                Intent intent = new Intent(this.getActivity(), SearchFriednsActivity.class);
                this.getActivity().startActivity(intent);
                break;
            case REQ_FRIENDS_FAILED:
                pb.setVisibility(View.GONE);
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
		if(adapter.getCount() > 0 ){
            touchLayout.setVisibility(View.INVISIBLE);
		}else{
            touchLayout.setVisibility(View.VISIBLE);
        }


	}

  	@Override
	protected void initHandler() {
	}

    @Override
    public void onDestroy() {
        imServiceConnector.disconnect(getActivity());
                super.onDestroy();
    }
}
