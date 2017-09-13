package com.fise.xiaoyu.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.ReqFriendsEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.adapter.SearchMessageAdapter;
import com.fise.xiaoyu.ui.base.TTBaseFragment;
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
public class SearchMessageFragment extends TTBaseFragment {

	private Logger logger = Logger.getLogger(SearchMessageFragment.class);
	private View curView = null;
	private ListView listView;
    private View noSearchResultView;
     
    private TextView searchString;
	private SearchMessageAdapter adapter;
	IMService imService;
	private ProgressDialog progressDialog;
	protected SearchEditText topSearchEdt_tt;
	protected String curSessionKey;
	
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
		
				curView = inflater.inflate(R.layout.tt_fragment_search_message, topContentView); //tt_fragment_search
        noSearchResultView = curView.findViewById(R.id.layout_no_search_result);
        searchString = (TextView) curView.findViewById(R.id.search_string);
        progressDialog = new ProgressDialog(this.getActivity());  
        
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);   
        progressDialog.setMessage("搜索好友");  
        progressDialog.setCancelable(true);  
        
        curSessionKey =  this.getActivity().getIntent().getStringExtra(IntentConstant.KEY_SESSION_KEY);
        
        noSearchResultView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
              //  getActivity().finish();
            	progressDialog.show();
            	String friends = topSearchEdt_tt.getText().toString();
            	
            //	UserEntity contact = imService.getContactManager().getSearchContact(friends);
//            	if(contact == null)
//            	{
//            		imService.getUserActionManager().reqFriends(friends);
//            		
//            	}else{
//            		
//            		//contact.setFriend(1);
//            		imService.getUserActionManager().setSearchInfo(contact); 
//            		
//                   	progressDialog.dismiss();
//                    Intent intent = new Intent(SearchMessageFragment.this.getActivity(), SearchFriednsActivity.class); 
//                    SearchMessageFragment.this.getActivity().startActivity(intent);
//            	}
//            	 
            	
            }
        });
        
		initTopBar();
        listView = (ListView) curView.findViewById(R.id.search);
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

	
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ReqFriendsEvent event){
        switch (event){  
            case REQ_FRIENDS_SUCCESS:  
            	progressDialog.dismiss();
//                Intent intent = new Intent(this.getActivity(), SearchFriednsActivity.class);
//                this.getActivity().startActivity(intent);
                break;
            case REQ_FRIENDS_FAILED:  
            	progressDialog.dismiss();
            	searchString.setText("该用户不存在");
            	 
                break;
        }
    }
  
    private void initAdapter(){
        adapter = new SearchMessageAdapter(getActivity(),imService);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter);
        listView.setOnItemLongClickListener(adapter);
        
        
    }

    // 文字高亮search 模块
	private void searchEntityLists(String key) {
        List<MessageEntity> contactList = imService.getMessageManager().searchMessageAll(curSessionKey,key);
        int contactSize = contactList.size();
        adapter.putMessageList(contactList);
   
        int sum = contactSize ;
        adapter.notifyDataSetChanged();
        if(sum <= 0){
            noSearchResultView.setVisibility(View.GONE);
            searchString.setText("搜索:" + key);
        }else{
            noSearchResultView.setVisibility(View.GONE);
            searchString.setText("搜索:" + key);
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
