package com.fise.xiaoyu.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.services.core.LatLonPoint;
import com.fise.xiaoyu.DB.DBInterface;
import com.fise.xiaoyu.DB.entity.GroupEntity;
import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.DB.entity.WeiEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.config.HandlerConstant;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.MessageConstant;
import com.fise.xiaoyu.imservice.entity.AddFriendsMessage;
import com.fise.xiaoyu.imservice.entity.AudioMessage;
import com.fise.xiaoyu.imservice.entity.CardMessage;
import com.fise.xiaoyu.imservice.entity.DevMessage;
import com.fise.xiaoyu.imservice.entity.ImageMessage;
import com.fise.xiaoyu.imservice.entity.MixMessage;
import com.fise.xiaoyu.imservice.entity.NoticeMessage;
import com.fise.xiaoyu.imservice.entity.OnLineVedioMessage;
import com.fise.xiaoyu.imservice.entity.PostionMessage;
import com.fise.xiaoyu.imservice.entity.TextMessage;
import com.fise.xiaoyu.imservice.entity.VedioMessage;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.ui.activity.ActivityReqVerification;
import com.fise.xiaoyu.ui.activity.HosterActivity;
import com.fise.xiaoyu.ui.activity.MessageActivity;
import com.fise.xiaoyu.ui.activity.MessageTranspondActivity;
import com.fise.xiaoyu.ui.activity.PostionTouchActivity;
import com.fise.xiaoyu.ui.activity.PreviewGifActivity;
import com.fise.xiaoyu.ui.activity.PreviewMessageImagesActivity;
import com.fise.xiaoyu.ui.activity.PreviewTextActivity;
import com.fise.xiaoyu.ui.helper.AudioPlayerHandler;
import com.fise.xiaoyu.ui.helper.Emoparser;
import com.fise.xiaoyu.ui.helper.listener.OnDoubleClickListener;
import com.fise.xiaoyu.ui.widget.BubbleImageView;
import com.fise.xiaoyu.ui.widget.GifView;
import com.fise.xiaoyu.ui.widget.SpeekerToast;
import com.fise.xiaoyu.ui.widget.message.AddFriendsRenderView;
import com.fise.xiaoyu.ui.widget.message.AudioRenderView;
import com.fise.xiaoyu.ui.widget.message.CardRenderView;
import com.fise.xiaoyu.ui.widget.message.DevPostionRenderView;
import com.fise.xiaoyu.ui.widget.message.DevRenderView;
import com.fise.xiaoyu.ui.widget.message.EmojiRenderView;
import com.fise.xiaoyu.ui.widget.message.GifImageRenderView;
import com.fise.xiaoyu.ui.widget.message.ImageRenderView;
import com.fise.xiaoyu.ui.widget.message.MessageOperatePopup;
import com.fise.xiaoyu.ui.widget.message.NoticeRenderView;
import com.fise.xiaoyu.ui.widget.message.OnLineVedioRenderView;
import com.fise.xiaoyu.ui.widget.message.PostionRenderView;
import com.fise.xiaoyu.ui.widget.message.RenderType;
import com.fise.xiaoyu.ui.widget.message.TextRenderView;
import com.fise.xiaoyu.ui.widget.message.TimeRenderView;
import com.fise.xiaoyu.ui.widget.message.TitleRenderView;
import com.fise.xiaoyu.ui.widget.message.VedioRenderView;
import com.fise.xiaoyu.ui.widget.message.WeiRenderView;
import com.fise.xiaoyu.utils.CommonUtil;
import com.fise.xiaoyu.utils.DateUtil;
import com.fise.xiaoyu.utils.FileUtil;
import com.fise.xiaoyu.utils.IMUIHelper;
import com.fise.xiaoyu.utils.Logger;
import com.fise.xiaoyu.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * MessageAdapter  消息的adapter
 */
@SuppressLint({"ResourceAsColor", "NewApi"})
public class MessageAdapter extends BaseAdapter {
    private Logger logger = Logger.getLogger(MessageAdapter.class);

    private ArrayList<Object> msgObjectList = new ArrayList<>();

    /**
     * 弹出气泡
     */
    private MessageOperatePopup currentPop;
    private Context ctx;
    /**
     * 依赖整体session状态的
     */
    private UserEntity loginUser;
    private IMService imService;
    private PeerEntity peerEntity;
    private int toId;
    private int actId;
    private WeiEntity weiReq;
    boolean isVedio = false;

    // 加载成功
    private static final int LOAD_SUCCESS = 1;
    // 加载失败
    private static final int LOAD_ERROR = -1;
    // 用于异步的显示图片
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what) {
                // 下载成功
                case LOAD_SUCCESS:
                    // 获取图片的文件对象
                    Utils.showToast(ctx, "保存成功");
                    break;
                // 下载失败
                case LOAD_ERROR:

                    Utils.showToast(ctx, "图片保存失败");

                    break;
            }

        }

        ;
    };

    public MessageAdapter(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * ----------------------init 的时候需要设定-----------------
     */

    public void setImService(IMService imService, UserEntity loginUser,
                             int toId, int actId, PeerEntity peerEntity) {
        this.imService = imService;
        this.loginUser = loginUser;
        this.toId = toId;
        this.actId = actId;
        this.peerEntity = peerEntity;
        isVedio = false;
    }

    /**
     * ----------------------添加历史消息-----------------
     */
    public void addItem(final MessageEntity msg) {
        if (msg.getDisplayType() == DBConstant.MSG_TYPE_SINGLE_TEXT) {
            if (isMsgGif(msg)) {
                msg.setGIfEmo(true);
            } else {
                msg.setGIfEmo(false);
            }
        }
        int nextTime = msg.getCreated();
        if (getCount() > 0) {
            Object object;
            object = msgObjectList.get(getCount() - 1); // guanweile -1

            if (object instanceof MessageEntity) {
                int preTime = ((MessageEntity) object).getCreated();
                boolean needTime = DateUtil.needDisplayTime(preTime, nextTime);
                if (needTime) {
                    Integer in = nextTime;
                    msgObjectList.add(in);
                }
            }
        } else {
            Integer in = msg.getCreated();
            msgObjectList.add(in);
        }
        /** 消息的判断 */
        if (msg.getDisplayType() == DBConstant.SHOW_MIX_TEXT) {
            MixMessage mixMessage = (MixMessage) msg;
            msgObjectList.addAll(mixMessage.getMsgList());
        } else {
            msgObjectList.add(msg);
        }
        if (msg instanceof ImageMessage) {
            ImageMessage.addToImageMessageList((ImageMessage) msg);
        }
        logger.d("#messageAdapter#addItem");
        notifyDataSetChanged();

    }

    private boolean isMsgGif(MessageEntity msg) {
        String content = msg.getContent();
        // @YM 临时处理 牙牙表情与消息混合出现的消息丢失
        if (TextUtils.isEmpty(content)
                || !(content.startsWith("[") && content.endsWith("]"))) {
            return false;
        }
        return Emoparser.getInstance(this.ctx).isMessageGif(msg.getContent());
    }

    public MessageEntity getTopMsgEntity() {
        if (msgObjectList.size() <= 0) {
            return null;
        }
        for (Object result : msgObjectList) {
            if (result instanceof MessageEntity) {
                return (MessageEntity) result;
            }
        }
        return null;
    }

    public static class MessageTimeComparator implements
            Comparator<MessageEntity> {
        @Override
        public int compare(MessageEntity lhs, MessageEntity rhs) {
            if (lhs.getCreated() == rhs.getCreated()) {
                return lhs.getMsgId() - rhs.getMsgId();
            }
            return lhs.getCreated() - rhs.getCreated();
        }
    }

    /**
     * 下拉载入历史消息,从最上面开始添加
     */
    public void loadHistoryList(final List<MessageEntity> historyList) {
        logger.d("#messageAdapter#loadHistoryList");
        if (null == historyList || historyList.size() <= 0) {
            return;
        }
        Collections.sort(historyList, new MessageTimeComparator());
        ArrayList<Object> chatList = new ArrayList<>();
        int preTime = 0;
        int nextTime = 0;
        for (MessageEntity msg : historyList) {
            if (msg.getDisplayType() == DBConstant.MSG_TYPE_SINGLE_TEXT) {
                if (isMsgGif(msg)) {
                    msg.setGIfEmo(true);
                } else {
                    msg.setGIfEmo(false);
                }
            }

            nextTime = msg.getCreated();
            boolean needTimeBubble = DateUtil
                    .needDisplayTime(preTime, nextTime);
            if (needTimeBubble) {
                Integer in = nextTime;
                chatList.add(in);
            }
            preTime = nextTime;
            if (msg.getDisplayType() == DBConstant.SHOW_MIX_TEXT) {
                MixMessage mixMessage = (MixMessage) msg;
                chatList.addAll(mixMessage.getMsgList());
            } else {
                chatList.add(msg);
            }
        }
        // 如果是历史消息，从头开始加
        msgObjectList.addAll(0, chatList);
        getImageList();
        logger.d("#messageAdapter#addItem");
        notifyDataSetChanged();
    }

    /**
     * 获取图片消息列表
     */
    private void getImageList() {
        for (int i = msgObjectList.size() - 1; i >= 0; --i) {
            Object item = msgObjectList.get(i);
            if (item instanceof ImageMessage) {
                ImageMessage.addToImageMessageList((ImageMessage) item);
            }
        }
    }

    /**
     * EventBus回调
     */

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_DEV_POS_DATA_SUCCESS:


                break;
        }
    }

    /**
     * 临时处理，一定要干掉
     */
    public void hidePopup() {
        if (currentPop != null) {
            currentPop.hidePopup();
        }
    }

    public void clearItem() {
        msgObjectList.clear();
    }

    /**
     * msgId 是消息ID localId是本地的ID position 是list 的位置
     * <p/>
     * 只更新item的状态 刷新单条记录
     * <p/>
     */
    public void updateItemState(int position, final MessageEntity messageEntity) {
        // 更新DB
        // 更新单条记录
        imService.getDbInterface().insertOrUpdateMessage(messageEntity);
        notifyDataSetChanged();
    }

    /**
     * 对于混合消息的特殊处理
     */
    public void updateItemState(final MessageEntity messageEntity) {
        long dbId = messageEntity.getId();
        int msgId = messageEntity.getMsgId();
        int len = msgObjectList.size();
        for (int index = len - 1; index > 0; index--) {
            Object object = msgObjectList.get(index);
            if (object instanceof MessageEntity) {
                MessageEntity entity = (MessageEntity) object;
                if (object instanceof ImageMessage) {
                    ImageMessage.addToImageMessageList((ImageMessage) object);
                }
                if (entity.getId() == dbId && entity.getMsgId() == msgId) {
                    msgObjectList.set(index, messageEntity);
                    break;
                }

            }
        }

        if (toId == messageEntity.getToId()) {
            if (!msgObjectList.contains(messageEntity)) {
                //仅仅对转发的视频及图片、位置做处理
//               if(messageEntity.getMsgType() != DBConstant.MSG_TYPE_SINGLE_AUTH_IMAGE && messageEntity.getMsgType() != DBConstant.MSG_TYPE_GROUP_AUTH_IMAGE
//					   && messageEntity.getMsgType() != DBConstant.MSG_TYPE_SINGLE_AUTH_SOUND && messageEntity.getMsgType() != DBConstant.MSG_TYPE_GROUP_AUTH_SOUND
//                       && messageEntity.getMsgType() != DBConstant.MSG_TYPE_VIDEO_CALL && messageEntity.getMsgType() != DBConstant.MSG_TYPE_VIDEO_ANSWER){
                if (messageEntity.getMsgType() == DBConstant.MSG_TYPE_SINGLE_IMAGE || messageEntity.getMsgType() == DBConstant.MSG_TYPE_GROUP_IMAGE
                        || messageEntity.getMsgType() == DBConstant.MSG_TYPE_SINGLE_VIDIO || messageEntity.getMsgType() == DBConstant.MSG_TYPE_GROUP_VIDIO
                        || messageEntity.getMsgType() == DBConstant.MSG_TYPE_GROUP_LOCATION || messageEntity.getMsgType() == DBConstant.MSG_TYPE_SINGLE_LOCATION) {
                    Log.i("aaa", "updateItemState: msgObjectList.add");
//                    msgObjectList.add(messageEntity);
                    addItem(messageEntity);
                }
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        if (null == msgObjectList) {
            return 0;
        } else {

            return msgObjectList.size(); // guanweile
        }
    }

    @Override
    public int getViewTypeCount() {
        return RenderType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        try {
            /** 默认是失败类型 */
            RenderType type = RenderType.MESSAGE_TYPE_INVALID;

            //Object  MessageEntity
            Object obj = msgObjectList.get(position);


            if (obj instanceof Integer) {
                type = RenderType.MESSAGE_TYPE_TIME_TITLE;
            } else if (obj instanceof MessageEntity) {
                MessageEntity info = (MessageEntity) obj;
                int fromId = info.getFromId();
                int logId = loginUser.getPeerId();
                //boolean isMine = info.getFromId() == loginUser.getPeerId();
                boolean isMine = false;
                if (logId == fromId) {
                    isMine = true;
                }
                if (info.getMsgId() == 0) {
                    isMine = false;
                }

                if (info.getDelete() == 1) { // MESSAGE_TYPE_INVALID

                    type = RenderType.MESSAGE_TYPE_INVALID;
                    return type.ordinal();
                }

                if (info.getMsgType() == DBConstant.MSG_TYPE_SINGLE_NOTICE) {
                    type = RenderType.MESSAGE_TYPE_NOTICE;
                } else {
                    switch (info.getDisplayType()) {
                        case DBConstant.SHOW_AUDIO_TYPE:
                            type = isMine ? RenderType.MESSAGE_TYPE_MINE_AUDIO
                                    : RenderType.MESSAGE_TYPE_OTHER_AUDIO;
                            break;
                        case DBConstant.SHOW_IMAGE_TYPE:
                            ImageMessage imageMessage = (ImageMessage) info;
                            if (CommonUtil.gifCheck(imageMessage.getUrl())) {
                                type = isMine ? RenderType.MESSAGE_TYPE_MINE_GIF_IMAGE
                                        : RenderType.MESSAGE_TYPE_OTHER_GIF_IMAGE;
                            } else {
                                type = isMine ? RenderType.MESSAGE_TYPE_MINE_IMAGE
                                        : RenderType.MESSAGE_TYPE_OTHER_IMAGE;
                            }

                            break;
                        case DBConstant.SHOW_ORIGIN_TEXT_TYPE:
                            if (info.isGIfEmo()) {
                                type = isMine ? RenderType.MESSAGE_TYPE_MINE_GIF
                                        : RenderType.MESSAGE_TYPE_OTHER_GIF;
                            } else {
                                type = isMine ? RenderType.MESSAGE_TYPE_MINE_TETX
                                        : RenderType.MESSAGE_TYPE_OTHER_TEXT;
                            }

                            break;

                        case DBConstant.SHOW_TYPE_ADDFRIENDS:
                            type = RenderType.MESSAGE_TYPE_OTHER_ADDFRIENDS;
                            break;

                        case DBConstant.SHOW_TYPE_DEV_MESSAGE:
                            type = RenderType.MESSAGE_TYPE_OTHER_DEV;
                            break;

                        case DBConstant.CHANGE_NOT_FRIEND:
                            type = RenderType.MESSAGE_TYPE_NOTICE;
                            break;
                        case DBConstant.SHOW_TYPE_NOTICE_BLACK:
                            type = RenderType.MESSAGE_TYPE_NOTICE;
                            break;

                        case DBConstant.SHOW_TYPE_CARD:
                            type = isMine ? RenderType.MESSAGE_TYPE_MINE_CARD
                                    : RenderType.MESSAGE_TYPE_OTHER_CARD;
                            break;
                        case DBConstant.SHOW_TYPE_VEDIO:
                            type = isMine ? RenderType.MESSAGE_TYPE_MINE_VEDIO
                                    : RenderType.MESSAGE_TYPE_OTHER_VEDIO;
                            break;

                        case DBConstant.SHOW_TYPE_POSTION:
                            type = isMine ? RenderType.MESSAGE_TYPE_MINE_POSTION
                                    : RenderType.MESSAGE_TYPE_OTHER_POSTION;
                            break;

                        case DBConstant.SHOW_TYPE_ONLINE_VIDEO:
                            type = isMine ? RenderType.MESSAGE_TYPE_MINE_LINE_VEDIO
                                    : RenderType.MESSAGE_TYPE_OTHER_LINE_VEDIO;
                            break;

                        case DBConstant.SHOW_MIX_TEXT:
                            //
                            logger.e("混合的消息类型%s", obj);
                            break;
                        default:
                            break;
                    }
                }

            }


            return type.ordinal();
        } catch (Exception e) {
            logger.e(e.getMessage());

            return RenderType.MESSAGE_TYPE_INVALID.ordinal();
        }
    }

    @Override
    public Object getItem(int position) {

        if (position >= getCount() || position < 0) {
            return null;
        }

        return msgObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 时间气泡的渲染展示
     */
    private View timeBubbleRender(int position, View convertView,
                                  ViewGroup parent) {
        TimeRenderView timeRenderView;

        Integer timeBubble = (Integer) msgObjectList.get(position);
        if (null == convertView) {
            timeRenderView = TimeRenderView.inflater(ctx, parent);
        } else {
            // 不用再使用tag 标签了
            timeRenderView = (TimeRenderView) convertView;
        }
        timeRenderView.setTime(timeBubble);
        return timeRenderView;
    }

    /**
     * 提示框气泡的渲染展示
     */
    private View TitleBubbleRender(int position, View convertView,
                                   ViewGroup parent) {
        TitleRenderView titleRenderView;
        final MessageEntity textMessage = (MessageEntity) msgObjectList
                .get(position);
        // Integer timeBubble = (Integer) msgObjectList.get(position);
        if (null == convertView) {
            titleRenderView = TitleRenderView.inflater(ctx, parent);
        } else {
            // 不用再使用tag 标签了
            titleRenderView = (TitleRenderView) convertView;
        }
        titleRenderView.setCont(textMessage.getContent());
        return titleRenderView;
    }

    /**
     * 请求加位有的渲染展示
     */
    private View WeiFriendsRender(int position, View convertView,
                                  ViewGroup parent) {
        WeiRenderView weiRenderView;

        if (null == convertView) {
            weiRenderView = WeiRenderView.inflater(ctx, parent);
        } else {
            // 不用再使用tag 标签了
            weiRenderView = (WeiRenderView) convertView;
        }

        TextView agree = weiRenderView.getTextAgree();
        /** 父类控件中的发送失败view */
        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // agree
                // imService.getUserActionManager().confirmWeiFriends(toId,
                // actId,weiReq, ActionResult.ACTION_RESULT_YES);

            }
        });

        TextView rRefuse = weiRenderView.getTextRefuse();
        /** 父类控件中的发送失败view */
        rRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 拒绝
                // imService.getUserActionManager().confirmWeiFriends(toId,
                // actId,weiReq, ActionResult.ACTION_RESULT_NO);
            }
        });

        return weiRenderView;
    }


    public void saveImageTest(Bitmap bmp, VedioMessage vedioMessage) {
        File appDir = new File(Environment.getExternalStorageDirectory(),
                "Boohee");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            vedioMessage.setImagePath(file.getAbsolutePath());
            DBInterface.instance().insertOrUpdateMessage(vedioMessage);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 小视频展示
     */
    private View VedioRender(final int position, View convertView,
                             final ViewGroup parent, final boolean isMine) {

        final VedioRenderView vedioRenderView;
        final VedioMessage vedioMessage = (VedioMessage) msgObjectList
                .get(position);
        UserEntity userEntity = imService.getContactManager().findContact(
                vedioMessage.getFromId());

        /** 保存在本地的path */
        final String vedioPath = vedioMessage.getVedioPath();
        /** 消息中的image路径 */
        final String vedioUrl = vedioMessage.getVedioUrl();

        if (null == convertView) {
            vedioRenderView = VedioRenderView.inflater(ctx, parent, isMine);
        } else {
            vedioRenderView = (VedioRenderView) convertView;
        }


        // 失败事件添加
        vedioRenderView.getMessageFailed().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {

                        boolean bResend = vedioMessage.getStatus() == MessageConstant.MSG_FAILURE;

                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                new ContextThemeWrapper(ctx,
                                        android.R.style.Theme_Holo_Light_Dialog));

                        String[] items;
                        if (bResend) {
                            items = new String[]{"删除", "重发"};
                        } else {
                            items = new String[]{"删除"};
                        }

                        builder.setItems(items,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        switch (which) {
                                            case 0: {
                                                DeleteMessage(vedioMessage,
                                                        position);
                                            }
                                            break;
                                            case 1: {
                                                Resend(vedioMessage, vedioMessage
                                                        .getDisplayType(), position);
                                            }
                                            break;
                                        }
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.show();

                    }
                });


        BubbleImageView messageImage = vedioRenderView.getMessageImage();
        messageImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (Utils.fileIsExists(vedioMessage.getVedioPath())) {

                    IMUIHelper.openVedioPlayActivity(ctx, vedioPath, 0,
                            vedioMessage, false);
                } else {
                    IMUIHelper.openVedioPlayActivity(ctx,
                            vedioUrl, 1, vedioMessage, false);
                }
            }
        });

        messageImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        new ContextThemeWrapper(ctx,
                                android.R.style.Theme_Holo_Light_Dialog));

                String[] items = new String[]{"静音播放", "转发", "删除"};

                builder.setItems(items,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                switch (which) {
                                    case 0: {
                                        //
                                        if (Utils.fileIsExists(vedioMessage.getVedioPath())) {

                                            IMUIHelper.openVedioPlayActivity(ctx, vedioPath, 0,
                                                    vedioMessage, true);
                                        } else {
                                            IMUIHelper.openVedioPlayActivity(ctx,
                                                    vedioUrl, 1, vedioMessage, true);
                                        }
                                    }
                                    break;
                                    case 1: {

                                        Intent intent = new Intent(ctx, MessageTranspondActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra(IntentConstant.TANSPOND_MESSAGE, vedioMessage);
                                        ctx.startActivity(intent);

                                    }
                                    break;

                                    case 2:

                                        DeleteMessage(vedioMessage, position);

                                        break;
                                }
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();
                return true;
            }
        });

        final int msgId = vedioMessage.getMsgId();
        vedioRenderView.setBtnImageListener(new VedioRenderView.BtnImageListener() {

            @Override
            public void onMsgFailure() {
                /**
                 * 多端同步也不会拉到本地失败的数据 只有isMine才有的状态，消息发送失败 1.
                 * 图片上传失败。点击图片重新上传??[也是重新发送] 2. 图片上传成功，但是发送失败。 点击重新发送??
                 */
                if (FileUtil.isSdCardAvailuable()) {
                    // imageMessage.setLoadStatus(MessageStatus.IMAGE_UNLOAD);//如果是图片已经上传成功呢？
                    vedioMessage.setStatus(MessageConstant.MSG_SENDING);
//					if (imService != null) {
//						imService.getMessageManager().resendMessage(
//								vedioMessage, true);
//					}
                    updateItemState(msgId, vedioMessage);
                } else {
                    Utils.showToast(ctx,
                            ctx.getString(R.string.sdcard_unavaluable));
                }

                if (Utils.fileIsExists(vedioMessage.getVedioPath())) {
                    IMUIHelper.openVedioPlayActivity(ctx, vedioPath, 0,
                            vedioMessage, false);
                } else {
                    IMUIHelper.openVedioPlayActivity(ctx,
                            vedioUrl, 1, vedioMessage, false);
                }
            }

            @Override
            public void onMsgOverdue() {
                Utils.showToast(ctx, "消息过期　打开失败");

            }

            // DetailPortraitActivity 以前用的是DisplayImageActivity 这个类
            @Override
            public void onMsgSuccess() {
                if (Utils.fileIsExists(vedioMessage.getVedioPath())) {
                    IMUIHelper.openVedioPlayActivity(ctx, vedioPath, 0,
                            vedioMessage, false);
                } else {
                    IMUIHelper.openVedioPlayActivity(ctx,
                            vedioUrl, 1, vedioMessage, false);
                }
            }
        });

        // 设定触发loadImage的事件
        vedioRenderView.setImageLoadListener(new VedioRenderView.ImageLoadListener() {

            @Override
            public void onLoadComplete(String loaclPath) {
                logger.d("chat#pic#save image ok");
                logger.d("pic#setsavepath:%s", loaclPath);
                vedioMessage.setImagePath(loaclPath);//
//						String name = vedioMessage.getPath();
//						// savePicture(imageMessage.getUrl(),imageMessage.getPath(),peerEntity.getPeerId());
//						vedioMessage.setPath(loaclPath);// 下载的本地路径不再存储
                vedioMessage
                        .setLoadStatus(MessageConstant.VEDIO_LOADED_SUCCESS);
                updateItemState(vedioMessage);

            }

            @Override
            public void onLoadFailed() {
                logger.d("chat#pic#onBitmapFailed");
                vedioMessage
                        .setLoadStatus(MessageConstant.VEDIO_LOADED_FAILURE);
                updateItemState(vedioMessage);
                logger.d("download failed");
            }
        });

        vedioRenderView.render(vedioMessage, userEntity, ctx, peerEntity);
        return vedioRenderView;
    }

    /**
     * 请求有的渲染展示
     */
    private View NoticeFriendsRender(int position, View convertView,
                                     ViewGroup parent) {
        NoticeRenderView noticeRenderView;
        final NoticeMessage noticMessage = (NoticeMessage) msgObjectList
                .get(position);

        if (null == convertView) {
            noticeRenderView = NoticeRenderView.inflater(ctx, parent);
        } else {
            // 不用再使用tag 标签了
            noticeRenderView = (NoticeRenderView) convertView;
        }


        TextView add_title = noticeRenderView.getTextAddFriends();
        if (noticMessage != null) {

            if (noticMessage.getDisplayType() == DBConstant.SHOW_TYPE_NOTICE_BLACK) {
                add_title.setVisibility(View.GONE);
                noticeRenderView.getTextPrompt().setText("消息已发出,但被对方拒收了");
            } else if (noticMessage.getDisplayType() == DBConstant.CHANGE_NOT_FRIEND) {
                if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
                    noticeRenderView.getTextPrompt().setText("对方不是你的好友请");
                    add_title.setVisibility(View.VISIBLE);
                } else {
                    noticeRenderView.getTextPrompt().setText(noticMessage.getContent());
                    add_title.setVisibility(View.GONE);
                }

            }

            /** 父类控件中的发送失败view */
            add_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    if (peerEntity.getType() == DBConstant.SESSION_TYPE_SINGLE) {
                        UserEntity user = (UserEntity) peerEntity;

                        Intent intent = new Intent(ctx,
                                ActivityReqVerification.class);
                        intent.putExtra(IntentConstant.KEY_PEERID, user.getPeerId());
                        ctx.startActivity(intent);

                    }
                }
            });
        }

        return noticeRenderView;
    }

    /**
     * 1
     * <p/>
     * 图片消息类型的render
     *
     * @param position
     * @param convertView
     * @param parent
     * @param isMine
     * @return
     */
    private View cardMsgRender(final int position, View convertView,
                               final ViewGroup parent, final boolean isMine) {
        CardRenderView cardRenderView;
        final CardMessage cardMessage = (CardMessage) msgObjectList
                .get(position);
        UserEntity userEntity = imService.getContactManager().findContact(
                cardMessage.getFromId());

        /** 消息中的image路径 */
        final String imageUrl = cardMessage.getAvatar();

        if (null == convertView) {
            cardRenderView = CardRenderView.inflater(ctx, parent, isMine);
        } else {
            cardRenderView = (CardRenderView) convertView;
        }

        final BubbleImageView messageImage = cardRenderView.getMessageImage();
        final TextView nickName = cardRenderView.getNickName();
        // final TextView xiaoName = cardRenderView.getXiaoName();
        final int msgId = cardMessage.getMsgId();

        messageImage.setImageUrl(cardMessage.getAvatar());
        nickName.setText("" + cardMessage.getNick());
        // xiaoName.setText("" + cardMessage.getAccount());

        final View messageLayout = cardRenderView.getMessageLayout();
        messageImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 创建一个pop对象，然后 分支判断状态，然后显示需要的内容
                showMessagePopup(cardMessage, position);

                return true;
            }
        });

        cardRenderView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                CardMessage cardMessage1 = cardMessage;
                IMUIHelper.openCardInfoActivity(ctx,
                        Integer.parseInt(cardMessage.getUserId()));
            }
        });

        /** 父类控件中的发送失败view */
        cardRenderView.getMessageFailed().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
//						 重发或者重新加载

                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                new ContextThemeWrapper(ctx,
                                        android.R.style.Theme_Holo_Light_Dialog));

                        String[] items = new String[]{"重发"};

                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: {
                                        ResendCard(cardMessage,
                                                cardMessage.getDisplayType(), position);
                                    }
                                    break;
                                }
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.show();
                    }
                });

        messageImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        new ContextThemeWrapper(ctx,
                                android.R.style.Theme_Holo_Light_Dialog));

                final String[] items;
                items = new String[]{"删除"};

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {

                            case 0: {

                                DeleteMessage(cardMessage, position);
                            }
                            break;
                        }
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();
                return false;
            }
        });

        cardRenderView.render(cardMessage, userEntity, ctx, peerEntity);

        return cardRenderView;
    }

    /**
     * Postion消息类型的render
     *
     * @param position
     * @param convertView
     * @param parent
     * @param isMine
     * @return
     */
    private View postionMsgRender(final int position, final View convertView,
                                  final ViewGroup parent, final boolean isMine) {

        final PostionMessage postionMessage = (PostionMessage) msgObjectList.get(position);
        //	final PostionMessage postionMessage = PostionMessage.parseFromDB(messageEntity);

        if (postionMessage.getPotionType() == DBConstant.XIAOYU_POSTION_TYPE) {

            final DevPostionRenderView devRenderView;
            UserEntity userEntity = null;
            if (postionMessage.getFromId() == IMLoginManager.instance().getLoginId()) {
                userEntity = IMLoginManager.instance().getLoginInfo();
            } else {
                userEntity = imService.getContactManager().findParentContact(
                        postionMessage.getFromId());
            }

            if (null == convertView) {
                devRenderView = DevPostionRenderView.inflater(ctx, parent, isMine); // new
            } else {
                devRenderView = (DevPostionRenderView) convertView;
            }
            devRenderView.getPositionIcon().setVisibility(View.GONE);  //新改需求 如果有address去掉定位图
            devRenderView.getShowPositionText().setVisibility(View.VISIBLE);

            if (userEntity != null) {
                devRenderView.getIMBaseImageView().setImageUrl(userEntity.getAvatar());
            }

            if (postionMessage.getFromId() == IMLoginManager.instance().getLoginId()) {
                devRenderView.getIMBaseImageView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int friendsType = IMLoginManager.instance().getLoginInfo().getIsFriend();
                        boolean isYuFriends = false;
                        if (friendsType == DBConstant.FRIENDS_TYPE_YUYOU) {
                            isYuFriends = true;
                        }

                        IMUIHelper.openUserProfileActivity(ctx, IMLoginManager.instance().getLoginId(),
                                isYuFriends);
                    }
                });
            }
            devRenderView.showDevMessage(postionMessage);
            devRenderView.render(postionMessage, userEntity, ctx, peerEntity);
            return devRenderView;

        } else {

            PostionRenderView postionRenderView;

            UserEntity userEntity = imService.getContactManager().findContact(
                    postionMessage.getFromId());
            /** 保存在本地的path */
            final String imagePath = postionMessage.getPath();
            /** 消息中的image路径 */
            final String imageUrl = postionMessage.getUrl();

            if (null == convertView) {
                postionRenderView = PostionRenderView.inflater(ctx, parent, isMine);
            } else {
                postionRenderView = (PostionRenderView) convertView;
            }

            final double lat = postionMessage.getLat();
            final double lng = postionMessage.getLng();

            final ImageView messageImage = postionRenderView.getMessageImage();
            final int msgId = postionMessage.getMsgId();

            final TextView postionText = postionRenderView.getPostionText();
            postionText.setText("" + postionMessage.getPostionName());


            final TextView infoText = postionRenderView.getInfoText();
            infoText.setText("" + postionMessage.getInformation());


            postionRenderView.setBtnImageListener(new PostionRenderView.BtnImageListener() {
                @Override
                public void onMsgFailure() {
                    /**
                     * 多端同步也不会拉到本地失败的数据 只有isMine才有的状态，消息发送失败 1.
                     * 图片上传失败。点击图片重新上传??[也是重新发送] 2. 图片上传成功，但是发送失败。 点击重新发送??
                     */
                    if (FileUtil.isSdCardAvailuable()) {
                        // imageMessage.setLoadStatus(MessageStatus.IMAGE_UNLOAD);//如果是图片已经上传成功呢？
                        postionMessage.setStatus(MessageConstant.MSG_SENDING);
                        if (imService != null) {

                            imService.getMessageManager().resendMessage(postionMessage, false, peerEntity);
                        }
                        updateItemState(msgId, postionMessage);
                    } else {
                        Utils.showToast(ctx,
                                ctx.getString(R.string.sdcard_unavaluable));
                    }
                }

                // DetailPortraitActivity 以前用的是DisplayImageActivity 这个类
                @Override
                public void onMsgSuccess() {
                    Intent intent = new Intent(ctx,
                            PostionTouchActivity.class);
                    intent.putExtra(IntentConstant.POSTION_LAT, lat);
                    intent.putExtra(IntentConstant.POSTION_LNG, lng);

                    intent.putExtra(IntentConstant.POSTION_TYPE, DBConstant.POSTION_MESSAGE_INFO);
                    intent.putExtra(IntentConstant.MESSAGE_POSTION_TITLE, postionMessage.getInformation());
                    intent.putExtra(IntentConstant.MESSAGE_POSTION_INFO, postionMessage.getPostionName());

                    ctx.startActivity(intent);

                }
            });

            messageImage.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent(ctx, PostionTouchActivity.class);
                    intent.putExtra(IntentConstant.POSTION_LAT, lat);
                    intent.putExtra(IntentConstant.POSTION_LNG, lng);

                    intent.putExtra(IntentConstant.POSTION_TYPE, DBConstant.POSTION_MESSAGE_INFO);
                    intent.putExtra(IntentConstant.MESSAGE_POSTION_TITLE, postionMessage.getInformation());
                    intent.putExtra(IntentConstant.MESSAGE_POSTION_INFO, postionMessage.getPostionName());
                    ctx.startActivity(intent);


                }
            });

            // 设定触发loadImage的事件
            postionRenderView.setImageLoadListener(new PostionRenderView.ImageLoadListener() {

                @Override
                public void onLoadComplete(String loaclPath) {
                    logger.d("chat#pic#save image ok");
                    logger.d("pic#setsavepath:%s", loaclPath);
                    postionMessage.setPath(loaclPath);// 下载的本地路径不再存储
                    postionMessage
                            .setLoadStatus(MessageConstant.IMAGE_LOADED_SUCCESS);
                    updateItemState(postionMessage);
                }

                @Override
                public void onLoadFailed() {
                    logger.d("chat#pic#onBitmapFailed");
                    postionMessage
                            .setLoadStatus(MessageConstant.IMAGE_LOADED_FAILURE);
                    updateItemState(postionMessage);
                    logger.d("download failed");
                }
            });

            final View messageLayout = postionRenderView.getMessageLayout();
            messageImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // 创建一个pop对象，然后 分支判断状态，然后显示需要的内容

                    boolean bResend = (postionMessage.getStatus() == MessageConstant.MSG_FAILURE)
                            || (postionMessage.getLoadStatus() == MessageConstant.IMAGE_UNLOAD);

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            new ContextThemeWrapper(ctx,
                                    android.R.style.Theme_Holo_Light_Dialog));

                    final String[] items;

                    if (bResend) {
                        items = new String[]{ctx.getString(R.string.resend), "删除"};
                    } else {
                        items = new String[]{ctx.getString(R.string.transpond), "删除"};
                    }

                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {

                                case 0: {
                                    // DeleteMessage(postionMessage, position);
                                    if (items[0].equals(ctx.getString(R.string.resend))) {
                                        Resend(postionMessage, postionMessage.getDisplayType(), position);

                                    } else if (items[0].equals(ctx.getString(R.string.transpond))) {
                                        //直接跳转

                                        Intent intent = new Intent(ctx, MessageTranspondActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra(IntentConstant.TANSPOND_MESSAGE, postionMessage);
                                        ctx.startActivity(intent);

                                    }

                                }
                                break;

                                case 1: {
//                            Copy(postionMessage);
                                    DeleteMessage(postionMessage, position);
                                }
                                break;

                                case 2: {
                                    //	Resend(postionMessage,
                                    //			postionMessage.getDisplayType(), position);
                                }
                                break;
                            }
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.show();

                    return true;
                }
            });

            /** 父类控件中的发送失败view */
            postionRenderView.getMessageFailed().setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            // 重发或者重新加载

                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    new ContextThemeWrapper(ctx,
                                            android.R.style.Theme_Holo_Light_Dialog));

                            String[] items;
                            items = new String[]{"删除"};

                            builder.setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0: {
                                            Resend(postionMessage, postionMessage.getDisplayType(), position);
                                        }
                                        break;
                                    }
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.setCanceledOnTouchOutside(true);
                            alertDialog.show();

                        }
                    });

            postionRenderView.render(postionMessage, userEntity, ctx, peerEntity);
            return postionRenderView;
        }

    }

    /**
     * 1.头像事件 mine:事件 other事件 图片的状态 消息收到，没收到，图片展示成功，没有成功 触发图片的事件 【长按】
     * <p/>
     * 图片消息类型的render
     *
     * @param position
     * @param convertView
     * @param parent
     * @param isMine
     * @return
     */
    private View imageMsgRender(final int position, View convertView,
                                final ViewGroup parent, final boolean isMine) {
        ImageRenderView imageRenderView;
        final ImageMessage imageMessage = (ImageMessage) msgObjectList
                .get(position);
        UserEntity userEntity = imService.getContactManager().findContact(
                imageMessage.getFromId());

        /** 保存在本地的path */
        final String imagePath = imageMessage.getPath();
        /** 消息中的image路径 */
        final String imageUrl = imageMessage.getUrl();

        if (null == convertView) {
            imageRenderView = ImageRenderView.inflater(ctx, parent, isMine);
        } else {
            imageRenderView = (ImageRenderView) convertView;
        }

        final ImageView messageImage = imageRenderView.getMessageImage();
        final int msgId = imageMessage.getMsgId();
        imageRenderView.setBtnImageListener(new ImageRenderView.BtnImageListener() {
            @Override
            public void onMsgFailure() {
                /**
                 * 多端同步也不会拉到本地失败的数据 只有isMine才有的状态，消息发送失败 1.
                 * 图片上传失败。点击图片重新上传??[也是重新发送] 2. 图片上传成功，但是发送失败。 点击重新发送??
                 */
                if (FileUtil.isSdCardAvailuable()) {
                    // imageMessage.setLoadStatus(MessageStatus.IMAGE_UNLOAD);//如果是图片已经上传成功呢？
                    imageMessage.setStatus(MessageConstant.MSG_SENDING);
                    if (imService != null) {
                        imService.getMessageManager().resendMessage(
                                imageMessage, true, peerEntity);
                    }
                    updateItemState(msgId, imageMessage);
                } else {
                    Utils.showToast(ctx,
                            ctx.getString(R.string.sdcard_unavaluable));
                }
            }

            @Override
            public void onMsgOverdue() {
                Utils.showToast(ctx, "消息过期　打开失败");
            }

            // DetailPortraitActivity 以前用的是DisplayImageActivity 这个类
            @Override
            public void onMsgSuccess() {
                Intent i = new Intent(ctx,
                        PreviewMessageImagesActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(IntentConstant.CUR_MESSAGE,
                        imageMessage);
                i.putExtras(bundle);
                ctx.startActivity(i);
                ((Activity) ctx).overridePendingTransition(
                        R.anim.tt_image_enter, R.anim.tt_stay);
            }
        });

        // 设定触发loadImage的事件
        imageRenderView.setImageLoadListener(new ImageRenderView.ImageLoadListener() {

            @Override
            public void onLoadComplete(String loaclPath) {
                logger.d("chat#pic#save image ok");
                logger.d("pic#setsavepath:%s", loaclPath);
                String name = imageMessage.getPath();

                //
                imageMessage.setPath(loaclPath);// 下载的本地路径不再存储
                imageMessage
                        .setLoadStatus(MessageConstant.IMAGE_LOADED_SUCCESS);
                updateItemState(imageMessage);

            }

            @Override
            public void onLoadFailed() {
                logger.d("chat#pic#onBitmapFailed");
                imageMessage
                        .setLoadStatus(MessageConstant.IMAGE_LOADED_FAILURE);
                updateItemState(imageMessage);
                logger.d("download failed");
            }
        });

        final View messageLayout = imageRenderView.getMessageLayout();
        messageImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                boolean bResend = (imageMessage.getStatus() == MessageConstant.IMAGE_LOADED_SUCCESS)
                        || (imageMessage.getLoadStatus() == MessageConstant.IMAGE_UNLOAD);

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        new ContextThemeWrapper(ctx,
                                android.R.style.Theme_Holo_Light_Dialog));

                final String[] items;
                if (bResend) {
                    items = new String[]{ctx.getString(R.string.transpond), ctx.getString(R.string.save_photo), ctx.getString(R.string.delete)};
                } else {
                    items = new String[]{ctx.getString(R.string.transpond), ctx.getString(R.string.save_photo), ctx.getString(R.string.delete)};
                }

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                Intent intent = new Intent(ctx, MessageTranspondActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(IntentConstant.TANSPOND_MESSAGE, imageMessage);
                                ctx.startActivity(intent);

                            }
                            break;
                            case 1: {
//                            if(items[1].equals(ctx.getString(R.string.save_photo))){
//                                new Thread(new Runnable() {
//                                    public void run() {
//
//                                        savePicture(imageMessage.getUrl(),
//                                                imageMessage.getPath(),
//                                                peerEntity.getPeerId());
//                                    }
//                                }).start();
//                            }else if(items[1].equals(ctx.getString(R.string.delete))){
//                                DeleteMessage(imageMessage, position);
//                            }

                                File file = new File(Environment.getExternalStorageDirectory()
                                        + "/" + "fise" + "/" + peerEntity.getPeerId() + "/" + imageMessage.getMsgId()
                                        + ".jpg");

                                if (!file.exists()) {
                                    new Thread(new Runnable() {
                                        public void run() {

                                            savePicture(imageMessage.getUrl(),
                                                    imageMessage.getMsgId(),
                                                    peerEntity.getPeerId());
                                        }
                                    }).start();
                                } else {
                                    Utils.showToast(ctx, "保存成功");
                                }


                            }
                            break;
                            case 2: {

                                DeleteMessage(imageMessage, position);
                            }
                            break;
                        }
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();
                return true;
            }
        });

        /** 父类控件中的发送失败view */
        imageRenderView.getMessageFailed().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        // 重发或者重新加载

                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                new ContextThemeWrapper(ctx,
                                        android.R.style.Theme_Holo_Light_Dialog));

                        String[] items;
                        items = new String[]{"重发"};

                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: {
                                        Resend(imageMessage, imageMessage.getDisplayType(), position);
                                    }
                                    break;

                                }
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.show();

                    }
                });
        imageRenderView.render(imageMessage, userEntity, ctx, peerEntity);

        return imageRenderView;
    }


    private View GifImageMsgRender(final int position, View convertView,
                                   final ViewGroup parent, final boolean isMine) {
        GifImageRenderView imageRenderView;
        final ImageMessage imageMessage = (ImageMessage) msgObjectList
                .get(position);
        UserEntity userEntity = imService.getContactManager().findContact(
                imageMessage.getFromId());
        if (null == convertView) {
            imageRenderView = GifImageRenderView.inflater(ctx, parent, isMine);
        } else {
            imageRenderView = (GifImageRenderView) convertView;
        }
        GifView imageView = imageRenderView.getMessageContent();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final String url = imageMessage.getUrl();
                Intent intent = new Intent(ctx, PreviewGifActivity.class);
                intent.putExtra(IntentConstant.PREVIEW_TEXT_CONTENT, url);
                ctx.startActivity(intent);
                ((Activity) ctx).overridePendingTransition(
                        R.anim.tt_image_enter, R.anim.tt_stay);
            }
        });
        imageRenderView.render(imageMessage, userEntity, ctx, peerEntity);
        return imageRenderView;
    }

    /**
     * 语音的路径，判断收发的状态 展现的状态 播放动画相关 获取语音的读取状态/ 语音长按事件
     *
     * @param position
     * @param convertView
     * @param parent
     * @param isMine
     * @return
     */
    private View audioMsgRender(final int position, View convertView,
                                final ViewGroup parent, final boolean isMine) {
        AudioRenderView audioRenderView;
        final AudioMessage audioMessage = (AudioMessage) msgObjectList
                .get(position);
        UserEntity entity = imService.getContactManager().findContact(
                audioMessage.getFromId());
        if (null == convertView) {
            audioRenderView = AudioRenderView.inflater(ctx, parent, isMine);
        } else {
            audioRenderView = (AudioRenderView) convertView;
        }
        final String audioPath = audioMessage.getAudioPath();

        final View messageLayout = audioRenderView.getMessageLayout();
        if (!TextUtils.isEmpty(audioPath)) {
            // 播放的路径为空,这个消息应该如何展示
            messageLayout
                    .setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            boolean bResend = audioMessage.getStatus() == MessageConstant.MSG_FAILURE;

                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    new ContextThemeWrapper(
                                            ctx,
                                            android.R.style.Theme_Holo_Light_Dialog));


                            String mode = "";
                            //final boolean speaker = AudioPlayerHandler.getInstance().getMessageAudioMode(ctx);
                            SharedPreferences sp = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE);
                            boolean speaker = sp.getBoolean("speaker", false);
                            final boolean settingSpeaker;
                            if (speaker) {
                                mode = "扬声器模式";
                                settingSpeaker = false;
                            } else {
                                mode = "听筒模式";
                                settingSpeaker = true;
                            }
                            String[] items;
                            if (bResend) {
                                items = new String[]{mode, "删除", "重发"};
                            } else {
                                items = new String[]{mode, "删除"};
                            }

                            builder.setItems(items,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            switch (which) {
                                                case 0: {
                                                    Speaker(settingSpeaker);
                                                }
                                                break;
                                                case 1: {
                                                    AudioPlayerHandler.getInstance().stopPlayer();
                                                    DeleteMessage(audioMessage, position);
                                                    //停止播放

                                                }
                                                break;
                                                case 2: {
                                                    Resend(audioMessage,
                                                            audioMessage
                                                                    .getDisplayType(),
                                                            position);
                                                }
                                                break;
                                            }
                                        }
                                    });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.setCanceledOnTouchOutside(true);
                            alertDialog.show();

                            return true;
                        }
                    });
        }

        audioRenderView.getMessageFailed().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                new ContextThemeWrapper(ctx,
                                        android.R.style.Theme_Holo_Light_Dialog));

                        String mode = "";
                        SharedPreferences sp = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE);
                        boolean speaker = sp.getBoolean("speaker", false);
                        final boolean settingSpeaker;
                        if (speaker) {
                            mode = "扬声器模式";
                            settingSpeaker = false;
                        } else {
                            mode = "听筒模式";
                            settingSpeaker = true;
                        }

                        String[] items;
                        items = new String[]{mode, "重发"};

                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: {
                                        Speaker(settingSpeaker);
                                    }
                                    break;
                                    case 1: {
                                        Resend(audioMessage, audioMessage.getDisplayType(), position);

                                    }
                                    break;
                                }
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.show();

                    }
                });

        audioRenderView
                .setBtnImageListener(new AudioRenderView.BtnImageListener() {
                    @Override
                    public void onClickUnread() {
                        logger.d("chat#audio#set audio meessage read status");
                        audioMessage
                                .setReadStatus(MessageConstant.AUDIO_READED);
                        imService.getDbInterface().insertOrUpdateMessage(
                                audioMessage);

                    }

                    @Override
                    public void onClickReaded() {


                    }
                });
        audioRenderView.render(audioMessage, entity, ctx, peerEntity);
        return audioRenderView;
    }

    /**
     * text类型的: 1. 设定内容Emoparser 2. 点击事件 单击跳转、 双击方法、长按pop menu 点击头像的事件 跳转
     *
     * @param position
     * @param convertView
     * @param viewGroup
     * @param isMine
     * @return
     */
    private View AddFriendsMsgRender(final int position, View convertView,
                                     final ViewGroup viewGroup, final boolean isMine) {
        AddFriendsRenderView addFriendsRenderView;
        final AddFriendsMessage textMessage = (AddFriendsMessage) msgObjectList
                .get(position);

        UserEntity userEntity;
        userEntity = imService.getContactManager().findContact(
                textMessage.getFromId());

        if (null == convertView) {
            addFriendsRenderView = AddFriendsRenderView.inflater(ctx,
                    viewGroup, isMine); // new
            // TextRenderView(ctx,viewGroup,isMine);
        } else {
            addFriendsRenderView = (AddFriendsRenderView) convertView;
        }

        final TextView textView = addFriendsRenderView.getMessageContent();

        // 失败事件添加
        addFriendsRenderView.getMessageFailed().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                    }
                });

        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 弹窗类型

                showMessagePopup(textMessage, position);

                return true;
            }
        });

        // url 路径可以设定 跳转哦哦
        final String content = textMessage.getContent();
        textView.setOnTouchListener(new OnDoubleClickListener() {
            @Override
            public void onClick(View view) {
                // todo
            }

            @Override
            public void onDoubleClick(View view) {
                Intent intent = new Intent(ctx, PreviewTextActivity.class);
                intent.putExtra(IntentConstant.PREVIEW_TEXT_CONTENT, content);
                ctx.startActivity(intent);
            }
        });
        addFriendsRenderView.render(textMessage, userEntity, ctx, peerEntity);
        return addFriendsRenderView;
    }


    /**
     * vedio类型的: 1. 设定内容Emoparser 2. 点击事件 单击跳转、 双击方法、长按pop menu 点击头像的事件 跳转
     *
     * @param position
     * @param convertView
     * @param viewGroup
     * @param isMine
     * @return
     */
    private View OnLineVedioMsgRender(final int position, View convertView,
                                      final ViewGroup viewGroup, final boolean isMine) {
        OnLineVedioRenderView vedioRenderView;
        final OnLineVedioMessage onLineMessage = (OnLineVedioMessage) msgObjectList
                .get(position);

        UserEntity userEntity;
        userEntity = imService.getContactManager().findContact(
                onLineMessage.getFromId());

        if (null == convertView) {
            vedioRenderView = OnLineVedioRenderView.inflater(ctx, viewGroup, isMine); // new
        } else {
            vedioRenderView = (OnLineVedioRenderView) convertView;
        }

        final TextView textView = vedioRenderView.getMessageContent();

        // 失败事件添加
        vedioRenderView.getMessageFailed().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MessageOperatePopup popup = getPopMenu(viewGroup,
                                new OperateItemClickListener(onLineMessage,
                                        position));
                        popup.show(textView, DBConstant.SHOW_TYPE_ONLINE_VIDEO,
                                true, isMine);
                    }
                });

        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 弹窗类型
                showMessagePopup(onLineMessage, position);

                return true;
            }
        });

        // url 路径可以设定 跳转哦哦
        final String content = onLineMessage.getContent();


        //final String content = onLineMessage.getContent();
        textView.setOnTouchListener(new OnDoubleClickListener() {
            @Override
            public void onClick(View view) {
                // todo

                if (!isVedio) {

                    isVedio = true;
                    int peerId;
                    if (onLineMessage.getFromId() != IMLoginManager.instance().getLoginId()) {
                        peerId = onLineMessage.getFromId();
                    } else {
                        peerId = onLineMessage.getToId();
                    }

                    Message message = new Message();
                    message.what = 2001;
                    message.obj = peerId;
                    handlerHoster.sendMessage(message);
                }

            }

            @Override
            public void onDoubleClick(View view) {
//				Intent intent = new Intent(ctx, PreviewTextActivity.class);
//				intent.putExtra(IntentConstant.PREVIEW_TEXT_CONTENT, content);
//				ctx.startActivity(intent);
            }
        });
        vedioRenderView.render(onLineMessage, userEntity, ctx, peerEntity);
        return vedioRenderView;
    }


    //创建一个Handler
    private Handler handlerHoster = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2001: {
                    final int peerId = (int) msg.obj;
                    new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            //实现页面跳转
                            Intent intent = new Intent(ctx,
                                    HosterActivity.class);
                            intent.putExtra(IntentConstant.KEY_PEERID, peerId);
                            ctx.startActivity(intent);

                            isVedio = false;
                            return false;
                        }
                    }).sendEmptyMessageDelayed(0, 600);//表示延迟3秒发送任务
                }
                break;
                default:
                    break;
            }
        }
    };

    /**
     * text类型的: 1. 设定内容Emoparser 2. 点击事件 单击跳转、 双击方法、长按pop menu 点击头像的事件 跳转
     *
     * @param position
     * @param convertView
     * @param viewGroup
     * @param isMine
     * @return
     */
    private View textMsgRender(final int position, View convertView,
                               final ViewGroup viewGroup, final boolean isMine) {
        TextRenderView textRenderView;
        final TextMessage textMessage = (TextMessage) msgObjectList
                .get(position);

        UserEntity userEntity;

        userEntity = imService.getContactManager().findContact(textMessage.getFromId());


        if (null == convertView) {
            textRenderView = TextRenderView.inflater(ctx, viewGroup, isMine);
        } else {
            textRenderView = (TextRenderView) convertView;
        }


        final TextView textView = textRenderView.getMessageContent();
        // 失败事件添加
        textRenderView.getMessageFailed().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                new ContextThemeWrapper(ctx,
                                        android.R.style.Theme_Holo_Light_Dialog));

                        String[] items;
                        items = new String[]{"复制", "重发"};

                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: {
                                        Copy(textMessage);
                                    }
                                    break;
                                    case 1: {
                                        Resend(textMessage, textMessage.getDisplayType(), position);
                                    }
                                    break;
                                }
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.show();

                    }
                });

        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 弹窗类型
                //	showMessagePopup(textMessage, position);
                boolean bResend = textMessage.getStatus() == MessageConstant.MSG_FAILURE;

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        new ContextThemeWrapper(ctx,
                                android.R.style.Theme_Holo_Light_Dialog));

                String[] items;
                if (bResend) {
                    items = new String[]{"复制", "删除", "重发"};
                } else {
                    items = new String[]{"复制", "删除"};
                }

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                Copy(textMessage);
                            }
                            break;
                            case 1: {
                                DeleteMessage(textMessage, position);

                            }
                            break;
                            case 2: {
                                Resend(textMessage, textMessage.getDisplayType(), position);
                            }
                            break;
                        }
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();


                return true;
            }
        });

        // url 路径可以设定 跳转哦哦
        final String content = textMessage.getContent();
        textView.setOnTouchListener(new OnDoubleClickListener() {
            @Override
            public void onClick(View view) {
                // todo
            }

            @Override
            public void onDoubleClick(View view) {
                Intent intent = new Intent(ctx, PreviewTextActivity.class);
                intent.putExtra(IntentConstant.PREVIEW_TEXT_CONTENT, content);
                ctx.startActivity(intent);
            }
        });

        textRenderView.render(textMessage, userEntity, ctx, peerEntity);
        return textRenderView;
    }


    /**
     * dev类型的: 1. 设定内容Emoparser 2. 点击事件 单击跳转、 双击方法、长按pop menu 点击头像的事件 跳转
     *
     * @param position
     * @param convertView
     * @param viewGroup
     * @param isMine
     * @return
     */
    private View DevMessageMsgRender(final int position, View convertView,
                                     final ViewGroup viewGroup, final boolean isMine) {

        final DevRenderView devRenderView;
        MessageEntity messageEntity = (MessageEntity) msgObjectList.get(position);
        final DevMessage devMessage = DevMessage.parseFromDB(messageEntity);

        UserEntity userEntity;

        userEntity = imService.getContactManager().findContact(
                devMessage.getFromId());
        if (userEntity == null) {
            userEntity = imService.getContactManager().findDeviceContact(
                    devMessage.getFromId());
        }
        if (null == convertView) {
            devRenderView = DevRenderView.inflater(ctx, viewGroup, isMine); // new
            // TextRenderView(ctx,viewGroup,isMine);
        } else {
            devRenderView = (DevRenderView) convertView;
        }

        if (devMessage.getType() != IMBaseDefine.EventKey.EVENT_KEY_CURRENT_INFO.ordinal()) {
            if (devMessage.getAddress().equals("")) {
                if (devMessage.getLatitude() != 0.0 && devMessage.getLongitude() != 0.0) {
                    devRenderView.getPositionIcon().setVisibility(View.VISIBLE);
                    devRenderView.getShowPositionText().setVisibility(View.GONE);
                    devRenderView.getPositionIcon().setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    if (devMessage.getLatitude() != 0.0 && devMessage.getLongitude() != 0.0) {
                                        LatLonPoint latLonPoint = new LatLonPoint(devMessage.getLatitude(), devMessage.getLongitude());
                                        devMessage.getAddress(latLonPoint);
                                    }
                                }
                            });

                } else {
                    devRenderView.getPositionIcon().setVisibility(View.GONE);
                    devRenderView.getShowPositionText().setVisibility(View.GONE);
                }

            } else {
                // devRenderView.getPositionIcon().setVisibility(View.VISIBLE);
                devRenderView.getPositionIcon().setVisibility(View.GONE);  //新改需求 如果有address去掉定位图
                devRenderView.getShowPositionText().setVisibility(View.VISIBLE);
                devRenderView.getShowPositionText().setText("位置: " + devMessage.getAddress());
            }


        } else if (devMessage.getType() == IMBaseDefine.EventKey.EVENT_KEY_CURRENT_INFO.ordinal()) {  //如果是实时位置

            if (devMessage.getLatitude() == 0.0 || devMessage.getLongitude() == 0.0) {

                devRenderView.getPositionIcon().setVisibility(View.GONE);
                devRenderView.getShowPositionText().setVisibility(View.VISIBLE);
                devRenderView.getShowPositionText().setText("位置数据异常 ");

            } else {
                if (!devMessage.getAddress().equals("")) {
                    devRenderView.getShowPositionText().setVisibility(View.VISIBLE);
                    devRenderView.getShowPositionText().setText("位置: " + devMessage.getAddress());
                }

                devRenderView.getPositionIcon().setVisibility(View.GONE);
            }

        } else {

            if (!devMessage.getAddress().equals("")) {
                devRenderView.getShowPositionText().setVisibility(View.VISIBLE);
                devRenderView.getShowPositionText().setText("位置: " + devMessage.getAddress());
            }

            devRenderView.getPositionIcon().setVisibility(View.GONE);

        }
        devRenderView.showDevMessage("", userEntity.getMainName(), devMessage);

        final TextView textView = devRenderView.getMessageContent();

        // 失败事件添加
        devRenderView.getMessageFailed().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                new ContextThemeWrapper(ctx,
                                        android.R.style.Theme_Holo_Light_Dialog));

                        String[] items;
                        items = new String[]{"重发"};

                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: {
                                        Resend(devMessage, devMessage.getDisplayType(), position);
                                    }
                                    break;
                                }
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.show();
                    }
                });

        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 弹窗类型　
                showMessagePopup(devMessage, position);

                return true;
            }
        });

        // url 路径可以设定 跳转哦哦
        final String content = devMessage.getContent();
        textView.setOnTouchListener(new OnDoubleClickListener() {
            @Override
            public void onClick(View view) {
                // todo
            }

            @Override
            public void onDoubleClick(View view) {
                Intent intent = new Intent(ctx, PreviewTextActivity.class);
                intent.putExtra(IntentConstant.PREVIEW_TEXT_CONTENT, content);
                ctx.startActivity(intent);
            }
        });
        devRenderView.render(devMessage, userEntity, ctx, peerEntity);
        return devRenderView;
    }

    /**
     * 牙牙表情等gif类型的消息: 1. 设定内容Emoparser 2. 点击事件 单击跳转、 双击方法、长按pop menu 点击头像的事件 跳转
     *
     * @param position
     * @param convertView
     * @param viewGroup
     * @param isMine
     * @return
     */
    private View gifMsgRender(final int position, View convertView,
                              final ViewGroup viewGroup, final boolean isMine) {
        EmojiRenderView gifRenderView;
        final TextMessage textMessage = (TextMessage) msgObjectList
                .get(position);
        UserEntity userEntity = imService.getContactManager().findContact(
                textMessage.getFromId());
        if (null == convertView) {
            gifRenderView = EmojiRenderView.inflater(ctx, viewGroup, isMine); // new
            // TextRenderView(ctx,viewGroup,isMine);
        } else {
            gifRenderView = (EmojiRenderView) convertView;
        }

        final ImageView imageView = gifRenderView.getMessageContent();
        // 失败事件添加
        gifRenderView.getMessageFailed().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                new ContextThemeWrapper(ctx,
                                        android.R.style.Theme_Holo_Light_Dialog));

                        String[] items;
                        items = new String[]{"复制", "重发"};

                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: {
                                        Copy(textMessage);
                                    }
                                    break;
                                    case 1: {
                                        Resend(textMessage, textMessage.getDisplayType(), position);
                                    }
                                    break;
                                }
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.show();
                    }
                });

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                showMessagePopup(textMessage, position);
                return true;
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final String content = textMessage.getContent();
                Intent intent = new Intent(ctx, PreviewGifActivity.class);
                intent.putExtra(IntentConstant.PREVIEW_TEXT_CONTENT, content);
                ctx.startActivity(intent);
                ((Activity) ctx).overridePendingTransition(
                        R.anim.tt_image_enter, R.anim.tt_stay);
            }
        });

        gifRenderView.render(textMessage, userEntity, ctx, peerEntity);
        return gifRenderView;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {

            final int typeIndex = getItemViewType(position);
            RenderType renderType = RenderType.values()[typeIndex];
            // 改用map的形式
            switch (renderType) {
                case MESSAGE_TYPE_INVALID:
                    // 直接返回
                    logger.e("[fatal erro] render type:MESSAGE_TYPE_INVALID");
                    break;

                case MESSAGE_TYPE_TIME_TITLE:
                    convertView = timeBubbleRender(position, convertView, parent);
                    break;

                case MESSAGE_TYPE_TEXT_TITLE:
                    convertView = TitleBubbleRender(position, convertView, parent);
                    break;

                case MESSAGE_TYPE_MINE_AUDIO:
                    convertView = audioMsgRender(position, convertView, parent,
                            true);
                    break;
                case MESSAGE_TYPE_OTHER_AUDIO:
                    convertView = audioMsgRender(position, convertView, parent,
                            false);
                    break;
                case MESSAGE_TYPE_MINE_GIF_IMAGE:
                    convertView = GifImageMsgRender(position, convertView, parent,
                            true);
                    break;
                case MESSAGE_TYPE_OTHER_GIF_IMAGE:
                    convertView = GifImageMsgRender(position, convertView, parent,
                            false);
                    break;
                case MESSAGE_TYPE_MINE_IMAGE:
                    convertView = imageMsgRender(position, convertView, parent,
                            true);
                    break;
                case MESSAGE_TYPE_OTHER_IMAGE:
                    convertView = imageMsgRender(position, convertView, parent,
                            false);
                    break;
                case MESSAGE_TYPE_MINE_TETX:
                    convertView = textMsgRender(position, convertView, parent, true);
                    break;
                case MESSAGE_TYPE_OTHER_TEXT:
                    convertView = textMsgRender(position, convertView, parent,
                            false);
                    break;

                case MESSAGE_TYPE_OTHER_ADDFRIENDS:
                    convertView = AddFriendsMsgRender(position, convertView,
                            parent, false);
                    break;

                case MESSAGE_TYPE_OTHER_DEV:
                    convertView = DevMessageMsgRender(position, convertView,
                            parent, false);
                    break;

                case MESSAGE_TYPE_MINE_GIF:
                    convertView = gifMsgRender(position, convertView, parent, true);
                    break;
                case MESSAGE_TYPE_OTHER_GIF:
                    convertView = gifMsgRender(position, convertView, parent, false);
                    break;

                case MESSAGE_TYPE_NOTICE:
                    convertView = NoticeFriendsRender(position, convertView, parent);
                    break;
                case MESSAGE_TYPE_WEI_FRIENDS:
                    convertView = WeiFriendsRender(position, convertView, parent);
                    break;
                case MESSAGE_TYPE_CARD:
                    convertView = WeiFriendsRender(position, convertView, parent);
                    break;
                case MESSAGE_TYPE_MINE_VEDIO:
                    convertView = VedioRender(position, convertView, parent, true);
                    break;
                case MESSAGE_TYPE_OTHER_VEDIO:
                    convertView = VedioRender(position, convertView, parent, false);
                    break;

                case MESSAGE_TYPE_MINE_CARD:
                    convertView = cardMsgRender(position, convertView, parent, true);
                    break;
                case MESSAGE_TYPE_OTHER_CARD:
                    convertView = cardMsgRender(position, convertView, parent,
                            false);
                    break;

                case MESSAGE_TYPE_MINE_POSTION:
                    convertView = postionMsgRender(position, convertView, parent,
                            true);
                    break;
                case MESSAGE_TYPE_OTHER_POSTION:
                    convertView = postionMsgRender(position, convertView, parent,
                            false);
                    break;

                case MESSAGE_TYPE_MINE_LINE_VEDIO:
                    convertView = OnLineVedioMsgRender(position, convertView, parent,
                            true);

                case MESSAGE_TYPE_OTHER_LINE_VEDIO:
                    convertView = OnLineVedioMsgRender(position, convertView, parent,
                            false);

                    break;

            }

            return convertView;

        } catch (Exception e) {
            logger.e("chat#%s", e);
            return null;
        }

    }

    /**
     * 点击事件的定义
     */
    private MessageOperatePopup getPopMenu(ViewGroup parent,
                                           MessageOperatePopup.OnItemClickListener listener) {
        MessageOperatePopup popupView = MessageOperatePopup.instance(ctx,
                parent);
        currentPop = popupView;
        popupView.setOnItemClickListener(listener);
        return popupView;
    }

    private class OperateItemClickListener implements
            MessageOperatePopup.OnItemClickListener {

        private MessageEntity mMsgInfo;
        private int mType;
        private int mPosition;

        public OperateItemClickListener(MessageEntity msgInfo, int position) {
            mMsgInfo = msgInfo;
            mType = msgInfo.getDisplayType();
            mPosition = position;
        }

        @SuppressLint("NewApi")
        @Override
        public void onCopyClick() {
            try {
                ClipboardManager manager = (ClipboardManager) ctx
                        .getSystemService(Context.CLIPBOARD_SERVICE);

                logger.d("menu#onCopyClick content:%s", mMsgInfo.getContent());
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                    ClipData data = ClipData.newPlainText("data",
                            mMsgInfo.getContent());
                    manager.setPrimaryClip(data);
                } else {
                    manager.setText(mMsgInfo.getContent());
                }
            } catch (Exception e) {
                logger.e(e.getMessage());
            }
        }

        @Override
        public void onResendClick() {
            try {
                if (mType == DBConstant.SHOW_AUDIO_TYPE
                        || mType == DBConstant.SHOW_ORIGIN_TEXT_TYPE) {

                    if (mMsgInfo.getDisplayType() == DBConstant.SHOW_AUDIO_TYPE) {
                        if (mMsgInfo.getSendContent().length < 4) {
                            return;
                        }
                    }
                } else if (mType == DBConstant.SHOW_IMAGE_TYPE) {
                    logger.d("pic#resend");
                    // 之前的状态是什么 上传没有成功继续上传
                    // 上传成功，发送消息
                    ImageMessage imageMessage = (ImageMessage) mMsgInfo;
                    if (TextUtils.isEmpty(imageMessage.getPath())) {
                        Utils.showToast(ctx,
                                ctx.getString(R.string.image_path_unavaluable));
                        return;
                    }
                }
                mMsgInfo.setStatus(MessageConstant.MSG_SENDING);
                msgObjectList.remove(mPosition);
                addItem(mMsgInfo);
                if (imService != null) {
                    imService.getMessageManager().resendMessage(mMsgInfo, true, peerEntity);
                }

            } catch (Exception e) {
                logger.e("chat#exception:" + e.toString());
            }
        }

        @Override
        public void onSpeakerClick() {
            AudioPlayerHandler audioPlayerHandler = AudioPlayerHandler
                    .getInstance();
            boolean speaker = audioPlayerHandler.getMessageAudioMode(ctx);
            if (!speaker) {
                audioPlayerHandler.setAudioMode(ctx, true);
                SpeekerToast.show(ctx, ctx.getText(R.string.audio_in_call),
                        Toast.LENGTH_SHORT);
            } else {
                audioPlayerHandler.setAudioMode(ctx, false);
                SpeekerToast.show(ctx, ctx.getText(R.string.audio_in_speeker),
                        Toast.LENGTH_SHORT);
            }
        }
    }

    // 下载图片的主方法
    private void savePicture(String urlPath, int msgId, int id) {

        try {
            URL url = new URL(urlPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(6 * 1000); // 注意要设置超时，设置时间不要超过10秒，避免被android系统回收
            if (conn.getResponseCode() != 200)
                throw new RuntimeException("请求url失败");
            InputStream inSream = conn.getInputStream();

//			SimpleDateFormat sDateFormat = new SimpleDateFormat(
//					"yyyy-MM-dd    hh:mm:ss");
//			String date = sDateFormat.format(new java.util.Date());
            File file = new File(Environment.getExternalStorageDirectory()
                    + "/" + "fise" + "/" + id);
            if (!file.exists()) {
                // file.createNewFile();
                file.mkdirs();
            }
            // 把图片保存到项目的根目录
            File imageFile = new File(file.getAbsolutePath() + "/" + msgId
                    + ".jpg");
            readAsFile(inSream, imageFile);

            Message msg = handler.obtainMessage();
            msg.what = LOAD_SUCCESS;
            msg.obj = imageFile.getAbsolutePath();
            handler.sendMessage(msg);

            // adfs
        } catch (Exception e) {
            handler.sendEmptyMessage(LOAD_ERROR);
            e.printStackTrace();
        }

    }

    public static void readAsFile(InputStream inSream, File file)
            throws Exception {
        FileOutputStream outStream = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inSream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inSream.close();
    }

    public void Copy(MessageEntity mMsgInfo) {
        try {
            ClipboardManager manager = (ClipboardManager) ctx
                    .getSystemService(Context.CLIPBOARD_SERVICE);

            logger.d("menu#onCopyClick content:%s", mMsgInfo.getContent());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                ClipData data = ClipData.newPlainText("data",
                        mMsgInfo.getContent());
                manager.setPrimaryClip(data);
            } else {
                manager.setText(mMsgInfo.getContent());
            }
        } catch (Exception e) {
            logger.e(e.getMessage());
        }

    }

    public void Resend(MessageEntity mMsgInfo, int mType, int mPosition) {

        try {
            if (mType == DBConstant.SHOW_AUDIO_TYPE
                    || mType == DBConstant.SHOW_ORIGIN_TEXT_TYPE) {

                if (mMsgInfo.getDisplayType() == DBConstant.SHOW_AUDIO_TYPE) {
                    if (mMsgInfo.getSendContent().length < 4) {
                        return;
                    }
                }
            } else if (mType == DBConstant.SHOW_IMAGE_TYPE) {
                logger.d("pic#resend");
                // 之前的状态是什么 上传没有成功继续上传
                // 上传成功，发送消息
                ImageMessage imageMessage = (ImageMessage) mMsgInfo;
                if (TextUtils.isEmpty(imageMessage.getPath())) {
                    Utils.showToast(ctx,
                            ctx.getString(R.string.image_path_unavaluable));
                    return;
                }
            }
            mMsgInfo.setStatus(MessageConstant.MSG_SENDING);
            msgObjectList.remove(mPosition);
            addItem(mMsgInfo);
            if (imService != null) {
                imService.getMessageManager().resendMessage(mMsgInfo, true, peerEntity);
            }

        } catch (Exception e) {
            logger.e("chat#exception:" + e.toString());
        }

    }

    public void ResendCard(MessageEntity mMsgInfo, int mType, int mPosition) {

        try {
            if (mType == DBConstant.SHOW_AUDIO_TYPE
                    || mType == DBConstant.SHOW_ORIGIN_TEXT_TYPE) {

                if (mMsgInfo.getDisplayType() == DBConstant.SHOW_AUDIO_TYPE) {
                    if (mMsgInfo.getSendContent().length < 4) {
                        return;
                    }
                }
            } else if (mType == DBConstant.SHOW_IMAGE_TYPE) {
                logger.d("pic#resend");
                // 之前的状态是什么 上传没有成功继续上传
                // 上传成功，发送消息
                ImageMessage imageMessage = (ImageMessage) mMsgInfo;
                if (TextUtils.isEmpty(imageMessage.getPath())) {
                    Utils.showToast(ctx,
                            ctx.getString(R.string.image_path_unavaluable));
                    return;
                }
            }
            mMsgInfo.setStatus(MessageConstant.MSG_SENDING);
            msgObjectList.remove(mPosition);
            //addItem(mMsgInfo);
            notifyDataSetChanged();
            if (imService != null) {
                imService.getMessageManager().resendMessage(mMsgInfo, true, peerEntity);
            }

        } catch (Exception e) {
            logger.e("chat#exception:" + e.toString());
        }

    }

    public void DeleteMessage(MessageEntity mMsgInfo, int mPosition) {

        try {

            msgObjectList.remove(mPosition);
            imService.getMessageManager().deleteMessage(mMsgInfo);


        } catch (Exception e) {
            logger.e("chat#exception:" + e.toString());
        }
    }

    public void showMessagePopup(final MessageEntity mMsgInfo,
                                 final int position) {

        boolean bResend = mMsgInfo.getStatus() == MessageConstant.MSG_FAILURE;

        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(ctx,
                        android.R.style.Theme_Holo_Light_Dialog));

        String[] items;
        if (bResend) {
            items = new String[]{"删除", "重发"};
        } else {
            items = new String[]{"删除"};
        }

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        //Copy(mMsgInfo);
                        DeleteMessage(mMsgInfo, position);
                    }
                    break;
                    case 1: {
                        //DeleteMessage(mMsgInfo, position);
                        Resend(mMsgInfo, mMsgInfo.getDisplayType(), position);
                    }
                    break;
                    case 2: {
                        //	Resend(mMsgInfo, mMsgInfo.getDisplayType(), position);
                    }
                    break;
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();

    }

    public void Speaker(boolean speaker) {

        AudioPlayerHandler audioPlayerHandler = AudioPlayerHandler
                .getInstance();
        if (speaker) {
            audioPlayerHandler.setAudioMode(ctx, speaker);
            SpeekerToast.show(ctx, ctx.getText(R.string.audio_in_call),
                    Toast.LENGTH_SHORT);
        } else {
            audioPlayerHandler.setAudioMode(ctx, speaker);
            SpeekerToast.show(ctx, ctx.getText(R.string.audio_in_speeker),
                    Toast.LENGTH_SHORT);
        }

        Message Msg = new Message();
        Msg.what = HandlerConstant.RECEIVE_SPEAKER;
        Msg.obj = speaker;
        MessageActivity.getUiHandler().sendMessage(Msg);
    }

}
