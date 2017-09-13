package com.fise.xw.ui.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
 
import com.mogujie.tools.ScreenTools;
import com.fise.xw.R;
import com.fise.xw.app.IMApplication;
import com.fise.xw.config.DBConstant;
import com.fise.xw.ui.activity.NewFriendActivity;
import com.fise.xw.ui.activity.SearchActivity; 
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Logger;
import com.fise.xw.ui.menu.PopMenu;
import com.fise.xw.ui.widget.SearchEditText;
import com.fise.xw.ui.widget.TopTabButton;

public abstract class TTBaseFragment extends Fragment {
	protected ImageView topLeftBtn;
	protected ImageView topRightBtn;
	protected TextView topTitleTxt;
	protected TextView titleNameTxt;
	protected TextView chatNameTxt;
	
	
	
	protected TextView topLetTitleTxt;
	protected TextView topRightTitleTxt;

	protected ViewGroup topBar;
	//protected TopTabButton topContactTitle;
	protected SearchEditText topSearchEdt;
	protected ViewGroup topContentView;
	protected RelativeLayout topLeftContainerLayout;
	//protected Button searchFrameLayout;
	protected FrameLayout topContactFrame;
//	protected Button searchFrameButton;
	protected Button addFriendsFrame; 
	private PopMenu FriendsMenu;
	//protected Button searchbarFrame; 
	
	
	protected TextView base_friends_title;
	
	
	protected float x1, y1, x2, y2 = 0;
	protected static Logger logger = Logger.getLogger(TTBaseFragment.class);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		topContentView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.tt_fragment_base, null);
 
		FriendsMenu = new PopMenu(getActivity());
		FriendsMenu.addItems(new String[]{"发起群聊", "添加朋友", "扫一扫"});
 
		 
		topBar = (ViewGroup) topContentView.findViewById(R.id.topbar);
		topTitleTxt = (TextView) topContentView.findViewById(R.id.base_fragment_title);
		titleNameTxt = (TextView) topContentView.findViewById(R.id.base_fragment_title_zhong);
		
		chatNameTxt = (TextView) topContentView.findViewById(R.id.base_fragment_title_chat);
		
		base_friends_title = (TextView) topContentView.findViewById(R.id.base_friends_title);
		
		topLetTitleTxt = (TextView) topContentView.findViewById(R.id.left_txt);
		topRightTitleTxt = (TextView) topContentView.findViewById(R.id.right_txt);
		topLeftBtn = (ImageView) topContentView.findViewById(R.id.left_btn);
		topRightBtn = (ImageView) topContentView.findViewById(R.id.right_btn);
		//topContactTitle = (TopTabButton) topContentView.findViewById(R.id.contact_tile);
		topSearchEdt = (SearchEditText) topContentView.findViewById(R.id.chat_title_search);
		topLeftContainerLayout=(RelativeLayout)topContentView.findViewById(R.id.top_left_container);
		//searchFrameLayout = (Button)topContentView.findViewById(R.id.searchbar_button);
		//searchFrameButton = (Button)topContentView.findViewById(R.id.searchbar_friends_button); 
		topContactFrame = (FrameLayout)topContentView.findViewById(R.id.contactTopBar);
				
		addFriendsFrame = (Button)topContentView.findViewById(R.id.addfriend_button);
//		searchbarFrame = (Button)topContentView.findViewById(R.id.searchbar_button); 
//		
//		searchbarFrame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//            	IMUIHelper.openSearchFriendActivity(TTBaseFragment.this.getActivity(),
//						DBConstant.SEACHFRIENDS);
//            } 
//        });
		
		
		Button addfriend_button_all = (Button)topContentView.findViewById(R.id.addfriend_button_all);
		 
		addFriendsFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
            	FriendsMenu.showAsDropDown(arg0);
            }
        });
	    
		addfriend_button_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
            	FriendsMenu.showAsDropDown(arg0);
            }
        });
	    
		
		
		topTitleTxt.setVisibility(View.GONE);
		topRightBtn.setVisibility(View.GONE);
		topLeftBtn.setVisibility(View.GONE);
		topLetTitleTxt.setVisibility(View.GONE);
		topRightTitleTxt.setVisibility(View.GONE);
		titleNameTxt.setVisibility(View.GONE);
		//topContactTitle.setVisibility(View.GONE);
		chatNameTxt.setVisibility(View.GONE);
		topSearchEdt.setVisibility(View.GONE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg,
			Bundle bundle) {
		if (null != topContentView) {
			((ViewGroup) topContentView.getParent()).removeView(topContentView);
			return topContentView;
		}
		return topContentView;
	}

	protected void setTopTitleBold(String title) {
		if (title == null) {
			return;
		}
		if (title.length() > 12) {
			title = title.substring(0, 11) + "...";
		}
		// 设置字体为加粗
//		TextPaint paint =  topTitleTxt.getPaint();  
//		paint.setFakeBoldText(true); 
		
		topTitleTxt.setText(title);
		topTitleTxt.setVisibility(View.VISIBLE);
				
	}
	
	protected void setTopTitle(String title) {
		if (title == null) {
			return;
		}
		if (title.length() > 12) {
			title = title.substring(0, 11) + "...";
		}
		topTitleTxt.setText(title);
		topTitleTxt.setVisibility(View.VISIBLE);
	}
	
	protected void setTopNameTitle(String title) {
		if (title == null) {
			return;
		}
		if (title.length() > 12) {
			title = title.substring(0, 11) + "...";
		}
		titleNameTxt.setText(title);
		titleNameTxt.setVisibility(View.VISIBLE);
	}
 
	
	protected void setTopChatName(String title) {
		if (title == null) {
			return;
		}
		if (title.length() > 12) {
			title = title.substring(0, 11) + "...";
		}
		chatNameTxt.setText(title);
		chatNameTxt.setVisibility(View.VISIBLE);
	}
	
	
	
	protected void hideTopTitle() {
		topTitleTxt.setVisibility(View.GONE);
	}
	
	
	protected void hideTopLeftTitle() {
		base_friends_title.setVisibility(View.GONE);
	}
	 

	protected void showContactTopBar() {
		topContactFrame.setVisibility(View.VISIBLE);
		//topContactTitle.setVisibility(View.VISIBLE);
	}
	
	 
	

	protected void setTopLeftButton(int resID) {
		if (resID <= 0) {
			return;
		}

		topLeftBtn.setImageResource(resID);
		topLeftBtn.setVisibility(View.VISIBLE);
	}

    protected void setTopLeftButtonPadding(int l,int t,int r,int b) {
        topLeftBtn.setPadding(l,t,r,b);
    }

	protected void hideTopLeftButton() {
		topLeftBtn.setVisibility(View.GONE);
	}

	protected void setTopLeftText(String text) {
		if (null == text) {
			return;
		}
		topLetTitleTxt.setText(text);
		topLetTitleTxt.setVisibility(View.VISIBLE);
	}

	protected void setTopRightText(String text) {
		if (null == text) {
			return;
		}
		topRightTitleTxt.setText(text);
		topRightTitleTxt.setVisibility(View.VISIBLE);
	}

	protected void setTopRightButton(int resID) {
		if (resID <= 0) {
			return;
		}

		topRightBtn.setImageResource(resID);
		topRightBtn.setVisibility(View.VISIBLE);
	}

	protected void hideTopRightButton() {
		topRightBtn.setVisibility(View.GONE);
	}

	protected void setTopBar(int resID) {
		if (resID <= 0) {
			return;
		}
		topBar.setBackgroundResource(resID);
	}

    protected void hideTopBar()
    {
        topBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) topContactFrame.getLayoutParams();
      //  linearParams.height = ScreenTools.instance(getActivity()).dip2px(57);
        topContactFrame.setLayoutParams(linearParams);
      //  topContactFrame.setPadding(0,ScreenTools.instance(getActivity()).dip2px(10),0,0);
    }

	protected void showTopSearchBar() {
		topSearchEdt.setVisibility(View.VISIBLE);
	}

	protected void hideTopSearchBar() {
		topSearchEdt.setVisibility(View.GONE);
	}
	
	protected void showSearchFrameLayout(){
//		searchFrameLayout.setVisibility(View.VISIBLE);
//        /**还是以前的页面，没有看psd是否改过*/
//        searchFrameLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                showSearchView();
//            }
//        });
//        
//		searchFrameButton.setVisibility(View.VISIBLE);
//        /**还是以前的页面，没有看psd是否改过*/
//        searchFrameButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                showSearchView();
//            }
//        });
        
        //下面的历史代码
        //tryHandleSearchAction(action);
	}
	
	   
	protected abstract void initHandler();

	@Override
	public void onActivityCreated(Bundle bundle) {
		logger.d("Fragment onActivityCreate:" + getClass().getName());
		super.onActivityCreated(bundle);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	protected void initSearch() {
		setTopRightButton(R.drawable.tt_top_search);
		topRightBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showSearchView();
			}
		});
	}

	public void showSearchView() {
		startActivity(new Intent(getActivity(), SearchActivity.class));
	}
	
	protected void onSearchDataReady() {
		initSearch();
	}

	public void onDestoryView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}
	
	
	
}
