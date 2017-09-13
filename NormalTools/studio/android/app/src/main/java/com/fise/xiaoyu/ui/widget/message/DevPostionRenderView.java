package com.fise.xiaoyu.ui.widget.message;

import android.content.Context;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.MessageEntity;
import com.fise.xiaoyu.DB.entity.PeerEntity;
import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.DBConstant;
import com.fise.xiaoyu.imservice.entity.PostionMessage;
import com.fise.xiaoyu.imservice.manager.IMContactManager;
import com.fise.xiaoyu.imservice.manager.IMLoginManager;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.ui.helper.Emoparser;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;
import com.fise.xiaoyu.utils.IMUIHelper;

/**
 * DevRenderView
 *
 *        样式根据mine 与other不同可以分成两个
 */
public class DevPostionRenderView extends BaseMsgRenderView {

	/** 文字消息体 */
	private TextView messageContent;
	private ImageView touxiang;
	private TextView message_content1;
	private TextView message_content2;
	private TextView message_contentTime;
	private ImageView showPositionIcon;
	private TextView message_content_title;
	private ImageView position_icon;
	private static Context mContext;
	private TextView message_position;
	private IMBaseImageView  user_portrait;
	public static DevPostionRenderView inflater(Context context, ViewGroup viewGroup,
												boolean isMine) {
		//int resource = R.layout.tt_other_dev_postion_message_item;// isMine?R.layout.tt_mine_text_message_item:R.layout.tt_other_text_message_item;
		int resource = isMine?R.layout.tt_mine_dev_postion_message_item:R.layout.tt_other_dev_postion_message_item;//
		mContext = context;
		DevPostionRenderView textRenderView = (DevPostionRenderView) LayoutInflater.from(
				context).inflate(resource, viewGroup, false);
		textRenderView.setMine(isMine);
		textRenderView.setParentView(viewGroup);
		return textRenderView;
	}

	public DevPostionRenderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	protected void onFinishInflate() {
		super.onFinishInflate();
		messageContent = (TextView) findViewById(R.id.message_content);
		touxiang = (ImageView) findViewById(R.id.message_state_touxiang);
		showPositionIcon = (ImageView) findViewById(R.id.show_position_icon);
		message_content1 = (TextView) findViewById(R.id.message_content1);
		message_content2 = (TextView) findViewById(R.id.message_content2);
		message_contentTime = (TextView) findViewById(R.id.message_content_time);
		message_content_title = (TextView) findViewById(R.id.message_content_title);
		message_position = (TextView) findViewById(R.id.message_position);
		position_icon = (ImageView) findViewById(R.id.show_position_icon);
		user_portrait = (IMBaseImageView) findViewById(R.id.user_portrait);
		position_icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(position_icon != null){

				}
			}
		});


	}

	public IMBaseImageView getIMBaseImageView(){
		return user_portrait;
	}

	/**
	 * 设置显示设备消息记录
	 * @param
	 * @param devPostionMessage
	 */
	public void showDevMessage(PostionMessage devPostionMessage) {

		int type = devPostionMessage.getType();
		String messageTime = devPostionMessage.getMessageTime();
		String address = devPostionMessage.getInformation();
		int locationType = devPostionMessage.getLocationType();

		String locationString="";
		if(IMBaseDefine.PosFromType.POS_FROM_GPS.ordinal() == locationType){
			locationString = "GPS定位";
		}else if(IMBaseDefine.PosFromType.POS_FROM_BASE.ordinal() == locationType){
			locationString = "基站定位";
		}else if(IMBaseDefine.PosFromType.POS_FROM_WIFI.ordinal() == locationType){
			locationString = "WIFI定位";
		}


		// 实时位置信息上送事件
		touxiang.setBackgroundResource(R.drawable.icon_remind_position);
		message_content_title.setText(mContext.getString(R.string.device_xiaowei));
		if(address.equals("")){
			message_content1.setText(address + "定位方式:"+ ""); //locationString
			//message_content1.setVisibility(GONE);
			message_position.setText("实时位置:数据异常");
		}else{
			message_content1.setText( "定位方式:"+ locationString);
			message_position.setText("实时位置:" + address);

		}

		message_contentTime.setText(messageTime);

	}


	public void showPositionText(String addressStr){

		message_content2.setVisibility(View.VISIBLE);
		message_content2.setText(addressStr);
	}


	public ImageView getPositionIcon() {
		return showPositionIcon;
	}


	public TextView getShowPositionText() {
		return message_position;
	}



	/**
	 * 控件赋值
	 *
	 * @param messageEntity
	 * @param userEntity
	 */
	@Override
	public void render(final MessageEntity messageEntity, final UserEntity userEntity,
					   Context context, PeerEntity peerEntity) {
		super.render(messageEntity, userEntity, context, peerEntity);
		PostionMessage devPostionMessage = (PostionMessage) messageEntity;
		// 按钮的长按也是上层设定的
		// url 路径可以设定 跳转哦哦
		String content = devPostionMessage.getContent();
		messageContent.setText(Emoparser.getInstance(getContext())
				.emoCharsequence(content)); // 所以上层还是处理好之后再给我 Emoparser 处理之后的


		extractUrl2Link(messageContent);

	}

	private static final String SCHEMA = "com.fise.xiaoyu://message_private_url";
	private static final String PARAM_UID = "uid";
	private String urlRegex = "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?((?:(?:[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}\\.)+(?:(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])|(?:biz|b[abdefghijmnorstvwyz])|(?:cat|com|coop|c[acdfghiklmnoruvxyz])|d[ejkmoz]|(?:edu|e[cegrstu])|f[ijkmor]|(?:gov|g[abdefghilmnpqrstuwy])|h[kmnrtu]|(?:info|int|i[delmnoqrst])|(?:jobs|j[emop])|k[eghimnrwyz]|l[abcikrstuvy]|(?:mil|mobi|museum|m[acdghklmnopqrstuvwxyz])|(?:name|net|n[acefgilopruz])|(?:org|om)|(?:pro|p[aefghklmnrstwy])|qa|r[eouw]|s[abcdeghijklmnortuvyz]|(?:tel|travel|t[cdfghjklmnoprtvwz])|u[agkmsyz]|v[aceginu]|w[fs]|y[etu]|z[amw]))|(?:(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])))(?:\\:\\d{1,5})?)(\\/(?:(?:[a-zA-Z0-9\\;\\/\\?\\:\\@\\&\\=\\#\\~\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?(?:\\b|$)";

	private void extractUrl2Link(TextView v) {
		java.util.regex.Pattern wikiWordMatcher = java.util.regex.Pattern
				.compile(urlRegex);
		String mentionsScheme = String.format("%s/?%s=", SCHEMA, PARAM_UID);
		Linkify.addLinks(v, wikiWordMatcher, mentionsScheme);
	}

	@Override
	public void msgFailure(MessageEntity messageEntity) {
		super.msgFailure(messageEntity);
	}

	/** ----------------set/get--------------------------------- */
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
