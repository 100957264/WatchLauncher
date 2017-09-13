package com.fise.xw.ui.adapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.PolylineOptions;
import com.fise.xw.R;
import com.fise.xw.DB.DBInterface;
import com.fise.xw.DB.entity.FamilyConcernEntity;
import com.fise.xw.DB.entity.MessageEntity;
import com.fise.xw.DB.entity.PeerEntity;
import com.fise.xw.DB.entity.UserEntity;
import com.fise.xw.DB.entity.WeiEntity;
import com.fise.xw.config.DBConstant;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.config.MessageConstant;
import com.fise.xw.imservice.entity.AddFriendsMessage;
import com.fise.xw.imservice.entity.AudioMessage;
import com.fise.xw.imservice.entity.CardMessage;
import com.fise.xw.imservice.entity.DevMessage;
import com.fise.xw.imservice.entity.ImageMessage;
import com.fise.xw.imservice.entity.MixMessage;
import com.fise.xw.imservice.entity.NoticeMessage;
import com.fise.xw.imservice.entity.PostionMessage;
import com.fise.xw.imservice.entity.TextMessage;
import com.fise.xw.imservice.entity.VedioMessage;
import com.fise.xw.imservice.event.MessageEvent;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.protobuf.IMDevice.AlarmType;
import com.fise.xw.ui.activity.ActivityReqVerification;
import com.fise.xw.ui.activity.PostionTouchActivity;
import com.fise.xw.ui.activity.PreviewGifActivity;
import com.fise.xw.ui.activity.PreviewMessageImagesActivity;
import com.fise.xw.ui.activity.PreviewTextActivity;
import com.fise.xw.ui.helper.AudioPlayerHandler;
import com.fise.xw.ui.helper.Emoparser;
import com.fise.xw.ui.helper.listener.OnDoubleClickListener;
import com.fise.xw.ui.widget.BubbleImageView;
import com.fise.xw.ui.widget.GifView;
import com.fise.xw.ui.widget.SpeekerToast;
import com.fise.xw.ui.widget.message.AddFriendsRenderView;
import com.fise.xw.ui.widget.message.AudioRenderView;
import com.fise.xw.ui.widget.message.CardRenderView;
import com.fise.xw.ui.widget.message.DevRenderView;
import com.fise.xw.ui.widget.message.EmojiRenderView;
import com.fise.xw.ui.widget.message.GifImageRenderView;
import com.fise.xw.ui.widget.message.ImageRenderView;
import com.fise.xw.ui.widget.message.MessageOperatePopup;
import com.fise.xw.ui.widget.message.NoticeRenderView;
import com.fise.xw.ui.widget.message.PostionRenderView;
import com.fise.xw.ui.widget.message.RenderType;
import com.fise.xw.ui.widget.message.TextRenderView;
import com.fise.xw.ui.widget.message.TimeRenderView;
import com.fise.xw.ui.widget.message.TitleRenderView;
import com.fise.xw.ui.widget.message.VedioRenderView;
import com.fise.xw.ui.widget.message.WeiRenderView;
import com.fise.xw.utils.CommonUtil;
import com.fise.xw.utils.DateUtil;
import com.fise.xw.utils.FileUtil;
import com.fise.xw.utils.IMUIHelper;
import com.fise.xw.utils.Logger;
import com.fise.xw.utils.Utils;

import de.greenrobot.event.EventBus;

/**
 * @author : yingmu on 15-1-8.
 * @email : yingmu@mogujie.com.
 */
@SuppressLint({ "ResourceAsColor", "NewApi" })
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
	// private boolean isReqWei;
	private int toId;
	private int actId;
	private WeiEntity weiReq;

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
				// Toast.makeText(ctx, "保存路径:" + msg.obj, 0).show();
				Toast.makeText(ctx, "保存成功", 0).show();
				break;
			// 下载失败
			case LOAD_ERROR:

				Toast.makeText(ctx, "图片保存失败", 0).show();

				break;
			}

		};
	};

	public MessageAdapter(Context ctx) {
		this.ctx = ctx;

	}

	/**
	 * ----------------------init 的时候需要设定-----------------
	 */

	public void setImService(IMService imService, UserEntity loginUser,
			int toId, int actId, PeerEntity peerEntity) { // boolean isReqWei,,
															// WeiEntity weiReq
		this.imService = imService;
		this.loginUser = loginUser;
		this.toId = toId;
		this.actId = actId;
		this.peerEntity = peerEntity;
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
			// if(isReqWei){
			// object = msgObjectList.get(getCount() - 2); //guanweile -1
			// }else{
			// object = msgObjectList.get(getCount() - 1); //guanweile -1
			// }
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
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (null == msgObjectList) {
			return 0;
		} else {

			// if(isReqWei){
			// return msgObjectList.size() + 1; // guanweile
			// }else{
			// return msgObjectList.size(); // guanweile
			// }
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

			// if(isReqWei){
			// if(position == (getCount()-1))
			// {
			// type = RenderType.MESSAGE_TYPE_WEI_FRIENDS;
			// return type.ordinal();
			// }
			// }

			Object obj = msgObjectList.get(position);
			if (obj instanceof Integer) {
				type = RenderType.MESSAGE_TYPE_TIME_TITLE;
			} else if (obj instanceof MessageEntity) {
				MessageEntity info = (MessageEntity) obj;
				boolean isMine = info.getFromId() == loginUser.getPeerId();

				if (info.getDelete() == 1) // MESSAGE_TYPE_INVALID
				{
					type = RenderType.MESSAGE_TYPE_INVALID;
					return type.ordinal();
				}
				if (info.getMsgId() == 0) {
					isMine = false;
				}

				// if (info.getMsgType() == DBConstant.MSG_TYPE_CONFIRM_FRIEND)
				// {
				// type = RenderType.MESSAGE_TYPE_TEXT_TITLE;
				//
				// } else
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

					case DBConstant.SHOW_MIX_TEXT:
						//
						logger.e("混合的消息类型%s", obj);
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

	public void saveImage(Bitmap bmp, VedioMessage vedioMessage) {
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

			// vedioMessage.setVedioPath(file.getAbsolutePath());
			vedioMessage.setImagePath(file.getAbsolutePath());
			DBInterface.instance().insertOrUpdateMessage(vedioMessage);

			EventBus.getDefault().post(
					new MessageEvent(
							MessageEvent.Event.HANDLER_VEDIO_UPLOAD_SUCCESS,
							vedioMessage));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		final ImageView messageImage = vedioRenderView.getMessageImage();

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
							items = new String[] { "删除", "重发" };
						} else {
							items = new String[] { "删除" };
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

		// if(Utils.fileIsExists(vedioMessage.getImagePath())){

		if ((vedioMessage.getImagePath() != null)
				&& (!vedioMessage.getImagePath().equals(""))) {

			BitmapFactory.Options options = new BitmapFactory.Options();
			Bitmap img = BitmapFactory.decodeFile(vedioMessage.getImagePath(),
					options);
			if (img != null) {
				BitmapDrawable bd = new BitmapDrawable(img);
				messageImage.setBackgroundDrawable(bd);
			}

		} else {

			// if(Utils.fileIsExists(vedioMessage.getVedioPath())){
			if ((vedioMessage.getVedioPath() != null)
					&& (!vedioMessage.getVedioPath().equals(""))) {

				new Thread() {
					public void run() {
						final Bitmap bitmap = Utils
								.createVideoThumbnail(vedioMessage
										.getVedioPath());
						if (bitmap != null) {
							saveImage(bitmap, vedioMessage);
						}
					}
				}.start();

			} else {

				new Thread() {
					public void run() {

						final Bitmap bitmap = Utils.createVideoThumbnailUrlSuo(
								vedioMessage.getVedioUrl(), 200, 200);

						if (bitmap != null) {
							saveImage(bitmap, vedioMessage);
						}
					}
				}.start();
			}
		}

		// vedioRenderView
		// .setBtnImageListener(new VedioRenderView.BtnImageListener() {
		// @Override
		// public void onMsgFailure() {
		// }
		//
		// // DetailPortraitActivity 以前用的是DisplayImageActivity 这个类
		// @Override
		// public void onMsgSuccess() {
		// if (Utils.fileIsExists(vedioMessage.getVedioPath())) {
		// IMUIHelper.openVedioPlayActivity(ctx, vedioPath, 0,
		// vedioMessage);
		// } else {
		//
		// IMUIHelper.openVedioPlayActivity(ctx,
		// vedioMessage.getVedioUrl(), 1, vedioMessage);
		// }
		// }
		// });
		//
		// // 设定触发loadImage的事件
		// vedioRenderView
		// .setImageLoadListener(new VedioRenderView.ImageLoadListener() {
		//
		// @Override
		// public void onLoadComplete(String loaclPath) {
		// logger.d("chat#pic#save image ok");
		// logger.d("pic#setsavepath:%s", loaclPath);
		// // String name = vedioMessage.getPath();
		// //
		// savePicture(imageMessage.getUrl(),imageMessage.getPath(),peerEntity.getPeerId());
		// vedioMessage.setVedioPath(loaclPath);// 下载的本地路径不再存储
		// vedioMessage
		// .setLoadStatus(MessageConstant.VEDIO_LOADED_SUCCESS);
		// updateItemState(vedioMessage);
		//
		// }
		//
		// @Override
		// public void onLoadFailed() {
		// logger.d("chat#pic#onBitmapFailed");
		// vedioMessage
		// .setLoadStatus(MessageConstant.VEDIO_LOADED_FAILURE);
		// updateItemState(vedioMessage);
		// logger.d("download failed");
		// }
		// });

		ImageView messageVedio = vedioRenderView.getMessage_vedio();
		messageVedio.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (Utils.fileIsExists(vedioMessage.getVedioPath())) {
					IMUIHelper.openVedioPlayActivity(ctx, vedioPath, 0,
							vedioMessage);
				} else {

					IMUIHelper.openVedioPlayActivity(ctx,
							vedioMessage.getVedioUrl(), 1, vedioMessage);
				}
			}
		});

		messageImage.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// 创建一个pop对象，然后 分支判断状态，然后显示需要的内容
				boolean bResend = vedioMessage.getStatus() == MessageConstant.MSG_FAILURE;

				AlertDialog.Builder builder = new AlertDialog.Builder(
						new ContextThemeWrapper(ctx,
								android.R.style.Theme_Holo_Light_Dialog));

				String[] items;
				if (bResend) {
					items = new String[] { "删除", "重发" };
				} else {
					items = new String[] { "删除" };
				}

				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0: {
							DeleteMessage(vedioMessage, position);
						}
							break;
						case 1: {
							Resend(vedioMessage, vedioMessage.getDisplayType(),
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

					// UserEntity loginUser = imService.getLoginManager()
					// .getLoginInfo();
					// String content = "你和" + loginUser.getMainName()
					// + "是朋友,现在可以聊天了";
					// imService.getUserActionManager().addReqFriends(user,
					// content);

				}
			}
		});

		if (noticMessage != null) {

			if (noticMessage.getDisplayType() == DBConstant.SHOW_TYPE_NOTICE_BLACK) {
				add_title.setVisibility(View.GONE);
				noticeRenderView.getTextPrompt().setText("消息已发出,但被对方拒收了");
			} else if (noticMessage.getDisplayType() == DBConstant.CHANGE_NOT_FRIEND) {
				add_title.setVisibility(View.VISIBLE);
				noticeRenderView.getTextPrompt().setText("对方不是你的好友请");
			}
		}

		return noticeRenderView;
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
						// 重发或者重新加载
						MessageOperatePopup popup = getPopMenu(parent,
								new OperateItemClickListener(cardMessage,
										position));
						popup.show(messageLayout, DBConstant.SHOW_TYPE_CARD,
								true, isMine);
					}
				});
		cardRenderView.render(cardMessage, userEntity, ctx, peerEntity);

		return cardRenderView;
	}

	/**
	 * 
	 * Postion消息类型的render
	 * 
	 * @param position
	 * @param convertView
	 * @param parent
	 * @param isMine
	 * @return
	 */
	private View postionMsgRender(final int position, View convertView,
			final ViewGroup parent, final boolean isMine) {
		PostionRenderView postionRenderView;
		final PostionMessage postionMessage = (PostionMessage) msgObjectList
				.get(position);
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

		postionRenderView
				.setBtnImageListener(new PostionRenderView.BtnImageListener() {
					@Override
					public void onMsgFailure() {
						/**
						 * 多端同步也不会拉到本地失败的数据 只有isMine才有的状态，消息发送失败 1.
						 * 图片上传失败。点击图片重新上传??[也是重新发送] 2. 图片上传成功，但是发送失败。 点击重新发送??
						 */
						if (FileUtil.isSdCardAvailuable()) {
							// imageMessage.setLoadStatus(MessageStatus.IMAGE_UNLOAD);//如果是图片已经上传成功呢？
							postionMessage
									.setStatus(MessageConstant.MSG_SENDING);
							if (imService != null) {

								imService.getMessageManager().resendMessage(
										postionMessage, false);
							}
							updateItemState(msgId, postionMessage);
						} else {
							Toast.makeText(ctx,
									ctx.getString(R.string.sdcard_unavaluable),
									Toast.LENGTH_LONG).show();
						}
					}

					// DetailPortraitActivity 以前用的是DisplayImageActivity 这个类
					@Override
					public void onMsgSuccess() {
						Intent intent = new Intent(ctx,
								PostionTouchActivity.class);
						intent.putExtra(IntentConstant.POSTION_LAT, lat);
						intent.putExtra(IntentConstant.POSTION_LNG, lng);
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
				ctx.startActivity(intent);
			}
		});

		// 设定触发loadImage的事件
		postionRenderView
				.setImageLoadListener(new PostionRenderView.ImageLoadListener() {

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
				// MessageOperatePopup popup = getPopMenu(parent,
				// new OperateItemClickListener(postionMessage, position));
				// boolean bResend = (postionMessage.getStatus() ==
				// MessageConstant.MSG_FAILURE)
				// || (postionMessage.getLoadStatus() ==
				// MessageConstant.IMAGE_UNLOAD);
				// popup.show(messageLayout, DBConstant.SHOW_IMAGE_TYPE,
				// bResend,
				// isMine);

				boolean bResend = (postionMessage.getStatus() == MessageConstant.MSG_FAILURE)
						|| (postionMessage.getLoadStatus() == MessageConstant.IMAGE_UNLOAD);

				AlertDialog.Builder builder = new AlertDialog.Builder(
						new ContextThemeWrapper(ctx,
								android.R.style.Theme_Holo_Light_Dialog));

				String[] items;
				if (bResend) {
					items = new String[] { "复制", "删除", "重发" };
				} else {
					items = new String[] { "复制", "删除" };
				}

				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0: {
							Copy(postionMessage);
						}
							break;
						case 1: {
							DeleteMessage(postionMessage, position);
						}
							break;
						case 2: {
							Resend(postionMessage,
									postionMessage.getDisplayType(), position);
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
						MessageOperatePopup popup = getPopMenu(parent,
								new OperateItemClickListener(postionMessage,
										position));
						popup.show(messageLayout, DBConstant.SHOW_IMAGE_TYPE,
								true, isMine);
					}
				});
		postionRenderView.render(postionMessage, userEntity, ctx, peerEntity);

		return postionRenderView;
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
		imageRenderView
				.setBtnImageListener(new ImageRenderView.BtnImageListener() {
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
										imageMessage, true);
							}
							updateItemState(msgId, imageMessage);
						} else {
							Toast.makeText(ctx,
									ctx.getString(R.string.sdcard_unavaluable),
									Toast.LENGTH_LONG).show();
						}
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
		imageRenderView
				.setImageLoadListener(new ImageRenderView.ImageLoadListener() {

					@Override
					public void onLoadComplete(String loaclPath) {
						logger.d("chat#pic#save image ok");
						logger.d("pic#setsavepath:%s", loaclPath);
						String name = imageMessage.getPath();
						// savePicture(imageMessage.getUrl(),imageMessage.getPath(),peerEntity.getPeerId());
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

				String[] items;
				if (bResend) {
					items = new String[] { "保存图片", "删除", "重发" };
				} else {
					items = new String[] { "保存图片", "删除" };
				}

				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0: {
							new Thread(new Runnable() {
								public void run() {

									savePicture(imageMessage.getUrl(),
											imageMessage.getPath(),
											peerEntity.getPeerId());
								}
							}).start();
						}
							break;
						case 1: {
							DeleteMessage(imageMessage, position);
						}
							break;
						case 2: {
							Resend(imageMessage, imageMessage.getDisplayType(),
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

		/** 父类控件中的发送失败view */
		imageRenderView.getMessageFailed().setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// 重发或者重新加载
						MessageOperatePopup popup = getPopMenu(parent,
								new OperateItemClickListener(imageMessage,
										position));
						popup.show(messageLayout, DBConstant.SHOW_IMAGE_TYPE,
								true, isMine);
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

							String[] items;
							if (bResend) {
								items = new String[] { "听筒模式", "删除", "重发" };
							} else {
								items = new String[] { "听筒模式", "删除" };
							}

							builder.setItems(items,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											switch (which) {
											case 0: {
												Speaker();
											}
												break;
											case 1: {
												DeleteMessage(audioMessage,
														position);
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
						MessageOperatePopup popup = getPopMenu(parent,
								new OperateItemClickListener(audioMessage,
										position));
						popup.show(messageLayout, DBConstant.SHOW_AUDIO_TYPE,
								true, isMine);
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
						MessageOperatePopup popup = getPopMenu(viewGroup,
								new OperateItemClickListener(textMessage,
										position));
						popup.show(textView, DBConstant.SHOW_ORIGIN_TEXT_TYPE,
								true, isMine);
					}
				});

		textView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// 弹窗类型
				// MessageOperatePopup popup = getPopMenu(viewGroup,
				// new OperateItemClickListener(textMessage, position));
				// boolean bResend = textMessage.getStatus() ==
				// MessageConstant.MSG_FAILURE;
				// popup.show(textView, DBConstant.SHOW_ORIGIN_TEXT_TYPE,
				// bResend,
				// isMine);

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

		userEntity = imService.getContactManager().findContact(
				textMessage.getFromId());
		// UserEntity userEntity;
		// if(textMessage.getMsgId() == 0){
		// userEntity = imService.getLoginManager().getLoginInfo();
		// }else{
		// userEntity = imService.getContactManager().findContact(
		// textMessage.getFromId());
		//
		// }
		if (null == convertView) {
			textRenderView = TextRenderView.inflater(ctx, viewGroup, isMine); // new
																				// TextRenderView(ctx,viewGroup,isMine);
		} else {
			textRenderView = (TextRenderView) convertView;
		}

		final TextView textView = textRenderView.getMessageContent();

		// 失败事件添加
		textRenderView.getMessageFailed().setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						MessageOperatePopup popup = getPopMenu(viewGroup,
								new OperateItemClickListener(textMessage,
										position));
						popup.show(textView, DBConstant.SHOW_ORIGIN_TEXT_TYPE,
								true, isMine);
					}
				});

		textView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// 弹窗类型
				// MessageOperatePopup popup = getPopMenu(viewGroup,
				// new OperateItemClickListener(textMessage, position));
				// boolean bResend = textMessage.getStatus() ==
				// MessageConstant.MSG_FAILURE;
				// popup.show(textView, DBConstant.SHOW_ORIGIN_TEXT_TYPE,
				// bResend,
				// isMine);

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

		DevRenderView devRenderView;
		final DevMessage devMessage = (DevMessage) msgObjectList.get(position);

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

		String name = "";
		if (devMessage.getType() == AlarmType.ALARM_TYPE_AUTH_SLIENCE_CALL
				.ordinal()
				|| devMessage.getType() == AlarmType.ALARM_TYPE_AUTH_NORMAL_CALL
						.ordinal()) {

			UserEntity user;
			user = imService.getContactManager().findContact(
					devMessage.getSendId());
			if (user == null) {
				user = imService.getContactManager().findXiaoWeiContact(
						devMessage.getSendId());
			}

			if (user == null) {

				ArrayList<Integer> userIdStats = new ArrayList<>(1);
				// just single type
				userIdStats.add(devMessage.getSendId());
				imService.getContactManager().reqGetDetaillUsers(userIdStats);

			} else {
				// name = user.getMainName();
				FamilyConcernEntity entity = imService.getDeviceManager()
						.findFamilyConcern(user.getPeerId(),
								devMessage.getToId());
				if (entity == null) {

					if (user.getComment().equals("")) {
						name = user.getMainName();
					} else {
						name = user.getComment();
					}

				} else {
					name = entity.getIdentity();
				}

			}

		}

		if (devMessage.getType() == AlarmType.ALARM_TYPE_DEVICE_BILL.ordinal()) {
			devRenderView.setType(devMessage.getType(),
					devMessage.getContent(), userEntity.getMainName(),
					devMessage.getAddress(), devMessage.getMessageTime());
		} else {
			devRenderView.setType(devMessage.getType(), name,
					userEntity.getMainName(), devMessage.getAddress(),
					devMessage.getMessageTime());
		}

		final TextView textView = devRenderView.getMessageContent();

		// 失败事件添加
		devRenderView.getMessageFailed().setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						MessageOperatePopup popup = getPopMenu(viewGroup,
								new OperateItemClickListener(devMessage,
										position));
						popup.show(textView, DBConstant.SHOW_ORIGIN_TEXT_TYPE,
								true, isMine);
					}
				});

		textView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// 弹窗类型
				// MessageOperatePopup popup = getPopMenu(viewGroup,
				// new OperateItemClickListener(devMessage, position));
				// boolean bResend = devMessage.getStatus() ==
				// MessageConstant.MSG_FAILURE;
				// popup.show(textView, DBConstant.SHOW_ORIGIN_TEXT_TYPE,
				// bResend,
				// isMine);

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
						MessageOperatePopup popup = getPopMenu(viewGroup,
								new OperateItemClickListener(textMessage,
										position));
						popup.show(imageView, DBConstant.SHOW_GIF_TYPE, true,
								isMine);
					}
				});

		imageView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// MessageOperatePopup popup = getPopMenu(viewGroup,
				// new OperateItemClickListener(textMessage, position));
				// boolean bResend = textMessage.getStatus() ==
				// MessageConstant.MSG_FAILURE;
				// popup.show(imageView, DBConstant.SHOW_GIF_TYPE, bResend,
				// isMine);
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
	public View getView(int position, View convertView, ViewGroup parent) {
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
						Toast.makeText(ctx,
								ctx.getString(R.string.image_path_unavaluable),
								Toast.LENGTH_LONG).show();
						return;
					}
				}
				mMsgInfo.setStatus(MessageConstant.MSG_SENDING);
				msgObjectList.remove(mPosition);
				addItem(mMsgInfo);
				if (imService != null) {
					imService.getMessageManager().resendMessage(mMsgInfo, true);
				}

			} catch (Exception e) {
				logger.e("chat#exception:" + e.toString());
			}
		}

		@Override
		public void onSpeakerClick() {
			AudioPlayerHandler audioPlayerHandler = AudioPlayerHandler
					.getInstance();
			if (audioPlayerHandler.getAudioMode(ctx) == AudioManager.MODE_NORMAL) {
				audioPlayerHandler.setAudioMode(AudioManager.MODE_IN_CALL, ctx);
				SpeekerToast.show(ctx, ctx.getText(R.string.audio_in_call),
						Toast.LENGTH_SHORT);
			} else {
				audioPlayerHandler.setAudioMode(AudioManager.MODE_NORMAL, ctx);
				SpeekerToast.show(ctx, ctx.getText(R.string.audio_in_speeker),
						Toast.LENGTH_SHORT);
			}
		}
	}

	// 下载图片的主方法
	private void savePicture(String urlPath, String path, int id) {

		try {
			URL url = new URL(urlPath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(6 * 1000); // 注意要设置超时，设置时间不要超过10秒，避免被android系统回收
			if (conn.getResponseCode() != 200)
				throw new RuntimeException("请求url失败");
			InputStream inSream = conn.getInputStream();

			SimpleDateFormat sDateFormat = new SimpleDateFormat(
					"yyyy-MM-dd    hh:mm:ss");
			String date = sDateFormat.format(new java.util.Date());
			File file = new File(Environment.getExternalStorageDirectory()
					+ "/" + "fise" + "/" + id);
			if (!file.exists()) {
				// file.createNewFile();
				file.mkdirs();
			}
			// 把图片保存到项目的根目录
			File imageFile = new File(file.getAbsolutePath() + "/" + date
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
					Toast.makeText(ctx,
							ctx.getString(R.string.image_path_unavaluable),
							Toast.LENGTH_LONG).show();
					return;
				}
			}
			mMsgInfo.setStatus(MessageConstant.MSG_SENDING);
			msgObjectList.remove(mPosition);
			addItem(mMsgInfo);
			if (imService != null) {
				imService.getMessageManager().resendMessage(mMsgInfo, true);
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
			items = new String[] { "复制", "删除", "重发" };
		} else {
			items = new String[] { "复制", "删除" };
		}

		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0: {
					Copy(mMsgInfo);
				}
					break;
				case 1: {
					DeleteMessage(mMsgInfo, position);
				}
					break;
				case 2: {
					Resend(mMsgInfo, mMsgInfo.getDisplayType(), position);
				}
					break;
				}
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.setCanceledOnTouchOutside(true);
		alertDialog.show();

	}

	public void Speaker() {

		AudioPlayerHandler audioPlayerHandler = AudioPlayerHandler
				.getInstance();
		if (audioPlayerHandler.getAudioMode(ctx) == AudioManager.MODE_NORMAL) {
			audioPlayerHandler.setAudioMode(AudioManager.MODE_IN_CALL, ctx);
			SpeekerToast.show(ctx, ctx.getText(R.string.audio_in_call),
					Toast.LENGTH_SHORT);
		} else {
			audioPlayerHandler.setAudioMode(AudioManager.MODE_NORMAL, ctx);
			SpeekerToast.show(ctx, ctx.getText(R.string.audio_in_speeker),
					Toast.LENGTH_SHORT);
		}
	}
		
}
