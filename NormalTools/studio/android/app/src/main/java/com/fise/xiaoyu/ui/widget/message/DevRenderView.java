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
import com.fise.xiaoyu.imservice.entity.DevMessage;
import com.fise.xiaoyu.protobuf.IMBaseDefine;
import com.fise.xiaoyu.ui.helper.Emoparser;

/**
 * DevRenderView
 *
 *        样式根据mine 与other不同可以分成两个
 */
public class DevRenderView extends BaseMsgRenderView {

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
	public static DevRenderView inflater(Context context, ViewGroup viewGroup,
										 boolean isMine) {
		int resource = R.layout.tt_other_dev_message_item;// isMine?R.layout.tt_mine_text_message_item:R.layout.tt_other_text_message_item;

		mContext = context;
		DevRenderView textRenderView = (DevRenderView) LayoutInflater.from(
				context).inflate(resource, viewGroup, false);
		textRenderView.setMine(isMine);
		textRenderView.setParentView(viewGroup);
		return textRenderView;
	}

	public DevRenderView(Context context, AttributeSet attrs) {
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
		position_icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(position_icon != null){

				}
			}
		});
	}

	/**
	 * 设置显示设备消息记录
	 * @param
	 * @param devName
	 * @param devMessage
	 */
	public void showDevMessage(String msg,String devName, DevMessage devMessage) {

		int type = devMessage.getType();
		String messageTime = devMessage.getMessageTime();
		String address = devMessage.getAddress();
		int locationType = devMessage.getLocationType();

		String locationString="";
		if(IMBaseDefine.PosFromType.POS_FROM_GPS.ordinal() == locationType){
			locationString = "GPS定位";
		}else if(IMBaseDefine.PosFromType.POS_FROM_BASE.ordinal() == locationType){
			locationString = "基站定位";
		}else if(IMBaseDefine.PosFromType.POS_FROM_WIFI.ordinal() == locationType){
			locationString = "WIFI定位";
		}

		if (type == IMBaseDefine.EventKey.EVENT_KEY_LOW_BATTERY.ordinal()) {
			//低电量
			touxiang.setBackgroundResource(R.drawable.icon_remind_electricity);
			message_content_title.setText(R.string.safety_low_electric_warning);
			message_content1.setText(devName + mContext.getString( R.string.electric_safety_warn));
			message_contentTime.setText(messageTime);


		}  else if (type == IMBaseDefine.EventKey.EVENT_KEY_CROSS_SAFE_AREA.ordinal()) {
			//超出电子围栏
			touxiang.setBackgroundResource(R.drawable.icon_remind_fence);
			message_content_title.setText(R.string.safety_fence_warning);
			message_content1.setText(devName + mContext.getString(R.string.fencing_safety_warn)+msg); //
			message_contentTime.setText(messageTime);


		}else if (type == IMBaseDefine.EventKey.EVENT_KEY_ENTER_SAFE_AREA.ordinal()) {
			//进入电子围栏
			touxiang.setBackgroundResource(R.drawable.icon_remind_fence);
			message_content_title.setText(R.string.safety_fence_warning);
			message_content1.setText(devName + "进入"+msg);
			message_contentTime.setText(messageTime);

		} else if (type == IMBaseDefine.EventKey.EVENT_KEY_SOS.ordinal()) {
			//紧急求助
			touxiang.setBackgroundResource(R.drawable.icon_remind_sos);
			message_content_title.setText(R.string.safety_sos_warning);
			message_content1.setText("请注意"+devName + mContext.getString(R.string.sos_safety_warn));
			message_contentTime.setText(messageTime);

		}  else if (type == IMBaseDefine.EventKey.EVENT_KEY_CURRENT_INFO.ordinal()) {
			// 实时位置信息上送事件
			touxiang.setBackgroundResource(R.drawable.icon_remind_position);
			message_content_title.setText(mContext.getString(R.string.device_xiaowei));
			message_content1.setText(devName+"当前实时位置");
			message_contentTime.setText(messageTime);


		}  else if (type == IMBaseDefine.EventKey.EVENT_KEY_SHUTDOWN.ordinal()) {
			//设备关机
			touxiang.setBackgroundResource(R.drawable.icon_remind_off_line);
			message_content_title.setText(R.string.safety_off_line_warning);
			message_content1.setText(mContext.getString(R.string.off_line_safety_warn_one)+devName + mContext.getString( R.string.off_line_safety_warn));
			message_contentTime.setText(messageTime);


		}else if (type == IMBaseDefine.EventKey.EVENT_KEY_DROP_DOWN.ordinal()) {
			//设备脱落
			touxiang.setBackgroundResource(R.drawable.icon_remind_dev_drop);
			message_content_title.setText(R.string.safety_dev_drop_warning);
			message_content1.setText(mContext.getString(R.string.dev_drop_safety_warn )+devName );
			message_contentTime.setText(messageTime);

		}else if (type == IMBaseDefine.EventKey.EVENT_KEY_WEAR_ON.ordinal()) {
			//设备戴上
			touxiang.setBackgroundResource(R.drawable.icon_remind_dev_pick_up);
			message_content_title.setText(R.string.safety_wear_on_warning);
			message_content1.setText(mContext.getString(R.string.dev_wear_on )+devName );
			message_contentTime.setText(messageTime);

		}else if (type == IMBaseDefine.EventKey.EVENT_KEY_REPORT_BILL.ordinal()) {
			//话费上报 约定话费具体内容放在key_map中key_name为“content”
			touxiang.setBackgroundResource(R.drawable.icon_remind_bill);
			message_content_title.setText(R.string.safety_charge_warning);
			message_content1.setText(devName+devMessage.getContent()+"");
			message_contentTime.setText(messageTime);

		}else if (type == IMBaseDefine.EventKey.EVENT_KEY_BEGIN_CHARGING.ordinal()) { //进入充电
			//开始充电
			touxiang.setBackgroundResource(R.drawable.icon_remind_charge);
			message_content_title.setText(R.string.charing_remind);
			message_content1.setText(devName+"充电已开始");
			message_contentTime.setText(messageTime);


		} else if (type == IMBaseDefine.EventKey.EVENT_KEY_END_CHARGING.ordinal()) { //结束充电状态
			//结束充电
			touxiang.setBackgroundResource(R.drawable.icon_remind_charge);
			message_content_title.setText(R.string.charing_remind);
			message_content1.setText(devName+"充电已结束");
			message_contentTime.setText(messageTime);


		}else if (type == IMBaseDefine.EventKey.EVENT_KEY_CALL_IN.ordinal()) {
			//接听电话
			touxiang.setBackgroundResource(R.drawable.icon_anqtx_anq);
			message_content_title.setText("通话记录");
			message_content1.setText(devName + "接听电话");
			message_contentTime.setText(messageTime);

		} else if (type == IMBaseDefine.EventKey.EVENT_KEY_CALL_OUT.ordinal()) {
			//主叫
			touxiang.setBackgroundResource(R.drawable.icon_anqtx_jingg);
			message_content_title.setText("通话记录");
			message_content1.setText(devName + "拒绝电话");
			message_contentTime.setText(messageTime);

		}
		else {

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_jingg);
			message_content_title.setText("");
			message_content1.setText("");
			message_contentTime.setText(messageTime);
		}

//		if (address != null) {
//			if(type == IMBaseDefine.EventKey.EVENT_KEY_CURRENT_INFO.ordinal()){
//				if (!(address.equals(""))) {
//					message_content2.setText(address+":" + locationString);
//					message_content2.setVisibility(View.VISIBLE);
//				}
//			} else {
//				message_content2.setVisibility(View.GONE);
//
//			    }
//		}

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
	public void render(MessageEntity messageEntity, UserEntity userEntity,
					   Context context, PeerEntity peerEntity) {
		super.render(messageEntity, userEntity, context, peerEntity);
		DevMessage devMessage = (DevMessage) messageEntity;
		// 按钮的长按也是上层设定的
		// url 路径可以设定 跳转哦哦
		String content = devMessage.getContent();
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
