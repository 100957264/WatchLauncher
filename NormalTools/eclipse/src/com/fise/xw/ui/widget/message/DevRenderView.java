package com.fise.xw.ui.widget.message;

import android.content.Context;
import android.text.util.Linkify;
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
import com.fise.xw.imservice.entity.DevMessage;
import com.fise.xw.protobuf.IMDevice.AlarmType;
import com.fise.xw.ui.helper.Emoparser;

/**
 * @author : yingmu on 15-1-9.
 * @email : yingmu@mogujie.com.
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

	private TextView message_content_title;

	public static DevRenderView inflater(Context context, ViewGroup viewGroup,
			boolean isMine) {
		int resource = R.layout.tt_other_dev_message_item;// isMine?R.layout.tt_mine_text_message_item:R.layout.tt_other_text_message_item;

		DevRenderView textRenderView = (DevRenderView) LayoutInflater.from(
				context).inflate(resource, viewGroup, false);
		textRenderView.setMine(isMine);
		textRenderView.setParentView(viewGroup);
		return textRenderView;
	}

	public DevRenderView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected void onFinishInflate() {
		super.onFinishInflate();
		messageContent = (TextView) findViewById(R.id.message_content);
		touxiang = (ImageView) findViewById(R.id.message_state_touxiang);

		message_content1 = (TextView) findViewById(R.id.message_content1);
		message_content2 = (TextView) findViewById(R.id.message_content2);
		message_contentTime = (TextView) findViewById(R.id.message_content_time);
		message_content_title = (TextView) findViewById(R.id.message_content_title);

	}

	public void setType(int type, String name1, String name2, String address,
			String messageTime) {

		if (type == AlarmType.ALARM_TYPE_LOW_BATTARY.ordinal()) {

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_jingg);
			message_content_title.setText("安全提醒");
			message_content1.setText("电量低于20%");
			message_contentTime.setText(messageTime);

		} else if (type == AlarmType.ALARM_TYPE_OUT_FENCE.ordinal()) {

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_jingg);
			message_content_title.setText("安全提醒");
			message_content1.setText(name2 + "离开安全围栏");
			message_contentTime.setText(messageTime);

		} else if (type == AlarmType.ALARM_TYPE_URGENCY.ordinal()) {

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_jingg);
			message_content_title.setText("安全提醒");
			message_content1.setText(name2 + "进入紧急情况");
			message_contentTime.setText(messageTime);

		} else if (type == AlarmType.ALARM_TYPE_INT_FENCE.ordinal()) {

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_anq);
			message_content_title.setText("安全提醒");
			message_content1.setText(name2 + "进入安全围栏");
			message_contentTime.setText(messageTime);

		} else if (type == AlarmType.ALARM_TYPE_ANSWER_CALL.ordinal()) {

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_anq);
			message_content_title.setText("通话记录");
			message_content1.setText(name2 + "接听电话");
			message_contentTime.setText(messageTime);

		} else if (type == AlarmType.ALARM_TYPE_REJECT_CALL.ordinal()) {

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_jingg);
			message_content_title.setText("通话记录");
			message_content1.setText(name2 + "拒绝电话");
			message_contentTime.setText(messageTime);

		} else if (type == AlarmType.ALARM_TYPE_LOGIN.ordinal()) {

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_anq);
			message_content_title.setText("安全提醒");
			message_content1.setText(name2 + "上线了");
			message_contentTime.setText(messageTime);

		} else if (type == AlarmType.ALARM_TYPE_LOGOUT.ordinal()) {

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_jingg);
			message_content_title.setText("安全提醒");
			message_content1.setText(name2 + "下线了");
			message_contentTime.setText(messageTime);

		} else if (type == AlarmType.ALARM_TYPE_AUTH_NORMAL_CALL.ordinal()) {

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_tongh);
			message_content_title.setText("通话记录");
			message_content1.setText(name1 + "通话了一次" + name2);
			message_contentTime.setText(messageTime);

		} else if (type == AlarmType.ALARM_TYPE_AUTH_SLIENCE_CALL.ordinal()) {

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_jingm);
			message_content_title.setText("静默监听记录");
			message_content1.setText(name1 + "静默监听了一次" + name2);
			message_contentTime.setText(messageTime);

		} else if (type == AlarmType.ALARM_TYPE_CONFIG.ordinal()) {
			touxiang.setBackgroundResource(R.drawable.icon_anqtx_jingm);
			message_content_title.setText("安全提醒"); // 设备同步信息
			message_content1.setText("设备已获取最新配置");
			message_contentTime.setText(messageTime);

		} else if (type == AlarmType.ALARM_TYPE_DEVICE_BILL.ordinal()) {

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_jingg);
			message_content_title.setText("话费查询");
			message_content1.setText("" + name1);
			message_contentTime.setText(messageTime);

		} else if (type == AlarmType.ALARM_TYPE_UPD_LOCATION.ordinal()) {

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_jingm);
			message_content_title.setText("安全提醒");
			message_content1.setText("设备当前实时位置");
			message_contentTime.setText(messageTime);

		}  else if (type == AlarmType.ALARM_TYPE_DEVICE_BEGIN_CHARGE.ordinal()) { //进入充电

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_jingm);
			message_content_title.setText("安全提醒");
			message_content1.setText("设备进入充电状态");
			message_contentTime.setText(messageTime);

		} else if (type == AlarmType.ALARM_TYPE_DEVICE_END_CHARGE.ordinal()) { //结束充电状态

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_jingm);
			message_content_title.setText("安全提醒");
			message_content1.setText("设备结束充电状态");
			message_contentTime.setText(messageTime);
                 
		}else {   

			touxiang.setBackgroundResource(R.drawable.icon_anqtx_jingm);
			message_content_title.setText("");
			message_content1.setText("");
			message_contentTime.setText(messageTime);
		}

		if (address != null) {
			if (!(address.equals(""))) {
				message_content2.setText("" + address);
				message_content2.setVisibility(View.VISIBLE);
			}
		} else {
			message_content2.setVisibility(View.GONE);
		}

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

	private static final String SCHEMA = "com.fise.xw://message_private_url";
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
