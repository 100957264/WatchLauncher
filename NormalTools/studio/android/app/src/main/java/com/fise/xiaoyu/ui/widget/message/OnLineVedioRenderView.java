package com.fise.xiaoyu.ui.widget.message;

import android.content.Context;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.imservice.entity.OnLineVedioMessage;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.ui.helper.Emoparser;

/**
 *
 * 样式根据mine 与other不同可以分成两个
 */
public class OnLineVedioRenderView extends  BaseMsgRenderView {

    /** 文字消息体 */
    private TextView messageContent;
    private static Context mContext;
    private static Boolean isMinMessage;

    public static OnLineVedioRenderView inflater(Context context, ViewGroup viewGroup, boolean isMine){
        int resource = isMine?R.layout.tt_mine_online_vedio_message_item:R.layout.tt_other_online_vedio_message_item;

        isMinMessage = isMine;
        OnLineVedioRenderView onLineVedioRenderView = (OnLineVedioRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        onLineVedioRenderView.setMine(isMine);
        onLineVedioRenderView.setParentView(viewGroup);
        mContext = context;
        return onLineVedioRenderView;
    }

    public OnLineVedioRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        messageContent = (TextView) findViewById(R.id.message_content);
    }


    /**
     * 控件赋值
     * @param messageEntity
     * @param userEntity
     */
    @Override
    public void render(MessageEntity messageEntity, UserEntity userEntity,Context context,PeerEntity peerEntity) {
        super.render(messageEntity, userEntity,context,peerEntity);
        OnLineVedioMessage vedioReqMessage = (OnLineVedioMessage) messageEntity;
        // 按钮的长按也是上层设定的
        // url 路径可以设定 跳转哦哦

        String content;
        int msgType =  vedioReqMessage.getMsgType();
        String name;
        if (userEntity.getComment().equals("")) {
            name = userEntity.getMainName();
        } else {
            name = userEntity.getComment();
        }

        if(msgType == DBConstant.MSG_TYPE_VIDEO_CALL){

          //  ||msgType == DBConstant.MSG_TYPE_VIDEO_ANSWER
            content = name + "请求视频";
        }else  if(msgType == DBConstant.MSG_TYPE_VIDEO_ANSWER){
            content = name + "同意请求视频";

        } else if(msgType == DBConstant.MSG_TYPE_VIDEO_CLOSE ){
            OnLineVedioMessage  onLineMessageTest =OnLineVedioMessage.parseFromNoteDB(messageEntity);
            if(onLineMessageTest.getVedioStatus() == DBConstant.VEDIO_ONLINE_TALK){
                 content = "通话时长:" + onLineMessageTest.getTime();//onLineMessageTest.getTimeNum();
            }else  if(onLineMessageTest.getVedioStatus() == DBConstant.VEDIO_ONLINE_ING){
                 content = "正在通话中";
            }else if(onLineMessageTest.getVedioStatus() == DBConstant.VEDIO_ONLINE_REFUSE){
                if(vedioReqMessage.getFromId() == IMLoginManager.instance().getLoginId()){
                    content = "已拒绝";
                }else{
                    content = "已拒绝";
                }

            }else if(onLineMessageTest.getVedioStatus() == DBConstant.VEDIO_ONLINE_NO_CALL){
                if(vedioReqMessage.getFromId() == IMLoginManager.instance().getLoginId()){
                    content = "已取消";
                }else{
                    content = "已取消";
                }

            }else if(onLineMessageTest.getVedioStatus() == DBConstant.VEDIO_ONLINE_NO_TIMEOUT){
                if(vedioReqMessage.getFromId() == IMLoginManager.instance().getLoginId()){
                    content = "对方无应答";
                }else{
                    content = "已取消";
                }

            }else{
                content = "已取消";
            }

        } else{
            content = vedioReqMessage.getContent();
        }


        messageContent.setText(Emoparser.getInstance(getContext()).emoCharsequence(content)); // 所以上层还是处理好之后再给我 Emoparser 处理之后的
        extractUrl2Link(messageContent);

    }
    private static final String SCHEMA ="com.fise.xiaoyu://message_private_url";
    private static final String PARAM_UID ="uid";
    private String urlRegex = "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?((?:(?:[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}\\.)+(?:(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])|(?:biz|b[abdefghijmnorstvwyz])|(?:cat|com|coop|c[acdfghiklmnoruvxyz])|d[ejkmoz]|(?:edu|e[cegrstu])|f[ijkmor]|(?:gov|g[abdefghilmnpqrstuwy])|h[kmnrtu]|(?:info|int|i[delmnoqrst])|(?:jobs|j[emop])|k[eghimnrwyz]|l[abcikrstuvy]|(?:mil|mobi|museum|m[acdghklmnopqrstuvwxyz])|(?:name|net|n[acefgilopruz])|(?:org|om)|(?:pro|p[aefghklmnrstwy])|qa|r[eouw]|s[abcdeghijklmnortuvyz]|(?:tel|travel|t[cdfghjklmnoprtvwz])|u[agkmsyz]|v[aceginu]|w[fs]|y[etu]|z[amw]))|(?:(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])))(?:\\:\\d{1,5})?)(\\/(?:(?:[a-zA-Z0-9\\;\\/\\?\\:\\@\\&\\=\\#\\~\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?(?:\\b|$)";

    private void extractUrl2Link(TextView v) {
        java.util.regex.Pattern wikiWordMatcher = java.util.regex.Pattern.compile(urlRegex);
        String mentionsScheme = String.format("%s/?%s=",SCHEMA, PARAM_UID);
        Linkify.addLinks(v, wikiWordMatcher, mentionsScheme);
    }

    @Override
    public void msgFailure(MessageEntity messageEntity) {
        super.msgFailure(messageEntity);
    }

    /**----------------set/get---------------------------------*/
    public TextView getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(TextView messageContent) {
        this.messageContent = messageContent;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }

    public ViewGroup getParentView() {
        return parentView;
    }

    public void setParentView(ViewGroup parentView) {
        this.parentView = parentView;
    }
     
}
