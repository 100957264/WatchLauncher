package com.fise.xiaoyu.ui.fragment;

import android.content.Context;
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

import com.fise.xiaoyu.DB.DBInterface;
import com.fise.xiaoyu.DB.entity.CommonUserInfo;
import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.MessageConstant;
import com.fise.xiaoyu.imservice.entity.ImageMessage;
import com.fise.xiaoyu.imservice.entity.PostionMessage;
import com.fise.xiaoyu.imservice.entity.VedioMessage;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.adapter.MessageTansponddApter;
import com.fise.xiaoyu.ui.adapter.album.ImageItem;
import com.fise.xiaoyu.ui.widget.SearchEditText;
import com.fise.xiaoyu.ui.widget.SortSideBar;
import com.fise.xiaoyu.ui.widget.SortSideBar.OnTouchingLetterChangedListener;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;


/**
 * @YM  - -!
 * 1. 创建群的时候，跳到聊天页面
 * 2. 新增人员的时候，返回到聊天详情页面
 */
public class MessageTranspondFragment extends MainFragment
        implements OnTouchingLetterChangedListener {

    private static Logger logger = Logger.getLogger(GroupSelectFragment.class);

    private View curView = null;
    private IMService imService;
    private TextView right_txt;
    private MessageTansponddApter adapter;

    private ListView contactListView;

    private SortSideBar sortSideBar;
    private TextView dialog;
    private SearchEditText searchEditText;
    private UserEntity loginInfo;
    private ArrayList<String> listStr;
    private PeerEntity peerEntity;
    private UserEntity loginUser;
    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("groupselmgr#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
//            Intent intent = getActivity().getIntent();
//             curSessionKey = intent.getStringExtra(IntentConstant.KEY_SESSION_KEY);
//             peerEntity = imService.getSessionManager().findPeerEntity(curSessionKey);

             loginInfo = imService.getLoginManager().getLoginInfo();
             loginUser = imService.getLoginManager().getLoginInfo();
            initContactList();
        }

        @Override
        public void onServiceDisconnected() {}
    };
    private MessageEntity needTranspondMsg;
    private MessageEntity needTransPondentity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(getActivity());
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
        needTransPondentity = (MessageEntity) getActivity().getIntent().getSerializableExtra(IntentConstant.TANSPOND_MESSAGE);

        super.init(curView);
        initRes();
        return curView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    private void initContactList() {
        // 根据拼音排序
        adapter = new MessageTansponddApter(getActivity());
        contactListView.setAdapter(adapter);
        contactListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long arg3) {
                MessageTansponddApter.UserHolder holder = (MessageTansponddApter.UserHolder) view.getTag();
                holder.checkBox.toggle();// 在每次获取点击的item时改变checkbox的状态

                if(adapter.getCheckListSet().size()>0){
                    right_txt.setText("确定"+"(" +adapter.getCheckListSet().size()+")");
                }else
                {
                    right_txt.setText("确定");
                }
            }

        });

//        List<RecentInfo> recentSessionList = imService.getSessionManager().getRecentListInfo();
        List<CommonUserInfo> recentSessionList = imService.getContactManager().getCommonUserSortedList();
        List<CommonUserInfo> SessionList = new ArrayList<>();
        for (int i = 0; i < recentSessionList.size(); i++) {

            if (recentSessionList.get(i).getUserType() == DBConstant.SESSION_TYPE_SINGLE) {

                int peedId = recentSessionList.get(i).getUserInfoID();
                UserEntity user = imService.getContactManager().findContact(
                        peedId);

                if ((user != null) && (user.getAuth() != DBConstant.AUTH_TYPE_BLACK)) {
                    SessionList.add(recentSessionList.get(i));
                } else {
                    user = imService.getContactManager().findDeviceContact(
                            peedId);
                    if (user != null) {
                        SessionList.add(recentSessionList.get(i));
                    }
                }
            } else {
                SessionList.add(recentSessionList.get(i));
            }

        }


        //没有会话窗口 不弹出 屏蔽
        adapter.setService(imService);
        adapter.setData(SessionList);// recentSessionList

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
        TextView tvHintText = (TextView) curView.findViewById(R.id.hint_text);
        tvHintText.setText(getString(R.string.select_user));
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

        RelativeLayout select_group_member = (RelativeLayout) curView.findViewById(R.id.select_group_member);
        curView.findViewById(R.id.select_group_member_arrow).setVisibility(View.GONE);
//        select_group_member.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                IMUIHelper.openSelectGroupActivity(MessageTranspondFragment.this.getActivity(),false);
//            }
//        });


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

                List<String> listSelect = adapter.getCheckListSet();
                int msgType = needTransPondentity.getMsgType();
                switch (msgType){
                    case DBConstant.MSG_TYPE_GROUP_LOCATION:
                    case DBConstant.MSG_TYPE_SINGLE_LOCATION:
                        PostionMessage postionMessage =(PostionMessage) needTransPondentity;

                        for (int i = 0; i < listSelect.size() ; i++) {
                            String sessionKey = listSelect.get(i);
                            peerEntity = imService.getSessionManager().findPeerEntity(
                                    sessionKey);
                            PostionMessage sendPostionMessage = PostionMessage.buildForSend(postionMessage.getLat(), postionMessage.getLng(),postionMessage.getPostionName(),                                   postionMessage.getInformation(),loginInfo, peerEntity);
                            imService.getMessageManager().sendMessage(sendPostionMessage);
                        }
                        IMUIHelper.openChatActivity(getActivity() , listSelect.get(0));
                        getActivity().finish();

                        break;

                    case DBConstant.MSG_TYPE_SINGLE_IMAGE:
                    case DBConstant.MSG_TYPE_SINGLE_AUTH_IMAGE:
                    case DBConstant.MSG_TYPE_GROUP_IMAGE:
                        ImageMessage imageMessage =  (ImageMessage)needTransPondentity;
                        for (int i = 0; i < listSelect.size() ; i++) {
                            String sessionKey = listSelect.get(i);
                            peerEntity = imService.getSessionManager().findPeerEntity(sessionKey);

                            ImageItem item = new ImageItem();
                            item.setImagePath(imageMessage.getPath());
                            ImageMessage sendImage = ImageMessage.buildForSend(item, loginUser, peerEntity);
                            sendImage.setLoadStatus(MessageConstant.IMAGE_LOADED_SUCCESS);
                            sendImage.setStatus(MessageConstant.MSG_SUCCESS);
                            DBInterface.instance().insertOrUpdateMessage(sendImage);
                            String realImageURL = "";
                            try {
                                realImageURL = URLDecoder.decode(imageMessage.getImageUrl(), "utf-8");
                                logger.d("pic#realImageUrl:%s", realImageURL);
                            } catch (UnsupportedEncodingException e) {
                                logger.e(e.toString());
                            }
                            sendImage.setUrl(realImageURL);
                            imService.getMessageManager().sendMessage(sendImage);
                        }
                        IMUIHelper.openChatActivity(getActivity() , listSelect.get(0));
                        getActivity().finish();
                        break;

                    case DBConstant.MSG_TYPE_SINGLE_VIDIO:
                    case DBConstant.MSG_TYPE_GROUP_VIDIO:
                        VedioMessage videoMessage =  (VedioMessage)needTransPondentity;
                        for (int i = 0; i < listSelect.size() ; i++) {
                            String sessionKey = listSelect.get(i);
                            peerEntity = imService.getSessionManager().findPeerEntity(sessionKey);
                            VedioMessage sendVideo = VedioMessage.buildForSend(videoMessage.getVedioPath(),
                                    loginUser, peerEntity,videoMessage.getImagePath());
                            sendVideo.setImageUrl(videoMessage.getImageUrl());
                            sendVideo.setVedioUrl(videoMessage.getUrl());
//                            imService.getMessageManager().onVedioLoadSuccess(new MessageEvent(
//                                    MessageEvent.Event.VEDIO_UPLOAD_SUCCESS
//                                    ,sendVideo));
                            imService.getMessageManager().sendMessage(sendVideo);
                        }
                        IMUIHelper.openChatActivity(getActivity() , listSelect.get(0));
                        getActivity().finish();
                        break;

                }

            }

        });


        searchEditText = (SearchEditText) curView.findViewById(R.id.filter_edit);
        searchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                String key = s.toString();
                if(TextUtils.isEmpty(key)){
                    adapter.recover();
                    sortSideBar.setVisibility(View.VISIBLE);
                }else{
                    adapter.onSearch(key);
                    sortSideBar.setVisibility(View.INVISIBLE);
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
