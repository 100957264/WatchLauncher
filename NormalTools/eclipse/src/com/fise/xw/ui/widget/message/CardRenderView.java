package com.fise.xw.ui.widget.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.DB.entity.MessageEntity;
import com.fise.xw.DB.entity.PeerEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.imservice.entity.CardMessage; 
import com.fise.xw.ui.widget.BubbleImageView;
import com.fise.xw.ui.widget.MGProgressbar;
import com.fise.xw.utils.FileUtil;
import com.fise.xw.utils.Logger;

/**
 * @author : yingmu on 15-1-9.
 * @email : yingmu@mogujie.com.
 *
 */
public class CardRenderView extends BaseMsgRenderView {
    private Logger logger = Logger.getLogger(CardRenderView.class);

    // 上层必须实现的接口
    private ImageLoadListener imageLoadListener;
    private BtnImageListener btnImageListener;

    /** 可点击的view*/
    private View messageLayout;
    /**图片消息体*/
    private BubbleImageView messageImage;
    
    private TextView nick_name;
   // private TextView xiao_name;
    
    /** 图片状态指示*/
    private MGProgressbar imageProgress;

    public CardRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static CardRenderView inflater(Context context,ViewGroup viewGroup,boolean isMine){
        int resource = isMine?R.layout.tt_mine_card_message_item:R.layout.tt_other_card_message_item;
        CardRenderView imageRenderView = (CardRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        imageRenderView.setMine(isMine);
        imageRenderView.setParentView(viewGroup);
        return imageRenderView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        messageLayout = findViewById(R.id.message_layout);
        messageImage = (BubbleImageView) findViewById(R.id.message_image);
        nick_name = (TextView) findViewById(R.id.nick_name);
        //xiao_name = (TextView) findViewById(R.id.xiao_name);
        
        imageProgress = (MGProgressbar) findViewById(R.id.tt_image_progress);
        imageProgress.setShowText(false);
        imageProgress.setVisibility(View.GONE);
    }

    /**
     *
     * */

    /**
     * 控件赋值
     * @param messageEntity
     * @param userEntity
     *
     * 对于mine。 图片send_success 就是成功了直接取地址
     * 对于sending  就是正在上传
     *
     * 对于other，消息一定是success，接受成功额
     * 2. 然后分析loadStatus 判断消息的展示状态
     */
    @Override
    public void render(final MessageEntity messageEntity,final UserEntity userEntity,Context ctx,PeerEntity peerEntity) {
        super.render(messageEntity, userEntity,ctx,peerEntity);
    }



    /**
     * 多端同步也不会拉到本地失败的数据
     * 只有isMine才有的状态，消息发送失败
     * 1. 图片上传失败。点击图片重新上传??[也是重新发送]
     * 2. 图片上传成功，但是发送失败。 点击重新发送??
     * 3. 比较悲剧的是 图片上传失败和消息发送失败都是这个状态 不过可以通过另外一个状态来区别 图片load状态
     * @param entity
     */
    @Override
    public void msgFailure(final MessageEntity entity) {
        super.msgFailure(entity);
        messageImage.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                /**判断状态，重新发送resend*/
                btnImageListener.onMsgFailure();
            }
        });
        messageImage.setImageUrl(((CardMessage)entity).getAvatar());
        
        imageProgress.hideProgress();
    }


    @Override
    public void msgStatusError(final MessageEntity entity) {
        super.msgStatusError(entity);
        imageProgress.hideProgress();
    }


    /**
     * 图片信息正在发送的过程中
     * 1. 上传图片
     * 2. 发送信息
     */
    @Override
    public void msgSendinging(final MessageEntity entity) {
        if(isMine())
        {
            messageImage.setImageLoaddingCallback(new BubbleImageView.ImageLoaddingCallback() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                    imageProgress.hideProgress();
                }

                @Override
                public void onLoadingStarted(String imageUri, View view) {
                  //  imageProgress.showProgress();
                    imageProgress.hideProgress();
                }

                @Override
                public void onLoadingCanceled(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view) {
                    imageProgress.hideProgress();
                }
            });
            messageImage.setImageUrl(((CardMessage)entity).getAvatar());
            
        }

    }


    /**
     * 消息成功
     * 1. 对方图片消息
     * 2. 自己多端同步的消息
     * 说明imageUrl不会为空的
     */
    @Override
    public void msgSuccess(final MessageEntity entity) {
        super.msgSuccess(entity);
        CardMessage cardMessage = (CardMessage)entity;
       
        final String url = cardMessage.getAvatar(); 
        
        if(TextUtils.isEmpty(url)){
            /**消息状态异常*/
            msgStatusError(entity);
            return;
        }
 
    }  

    /**---------------------图片下载相关、点击、以及事件回调start-----------------------------------*/
    public interface  BtnImageListener{
        public void onMsgSuccess();
        public void onMsgFailure();
    }

    public void setBtnImageListener(BtnImageListener btnImageListener){
        this.btnImageListener = btnImageListener;
    }


    public interface ImageLoadListener{
        public void onLoadComplete(String path);
        // 应该把exception 返回结构放进去
        public void onLoadFailed();

    }
    
    


    public void setImageLoadListener(ImageLoadListener imageLoadListener){
        this.imageLoadListener = imageLoadListener;
    }

    /**---------------------图片下载相关、以及事件回调 end-----------------------------------*/


    /**----------------------set/get------------------------------------*/
    public View getMessageLayout() {
        return messageLayout;
    }

    public BubbleImageView getMessageImage() {
        return messageImage;
    }
    
    
    public TextView getNickName() {
        return nick_name;
    }
    
//    public TextView getXiaoName() {
//        return xiao_name;
//    }

    public MGProgressbar getImageProgress() {
        return imageProgress;
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
