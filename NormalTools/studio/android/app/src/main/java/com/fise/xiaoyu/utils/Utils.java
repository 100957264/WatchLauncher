package com.fise.xiaoyu.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.widget.Toast;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.protobuf.IMBaseDefine.ClientType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by lsy on 15-6-14.
 */
@SuppressLint("NewApi")
public class Utils {

	/** @hide */
	public static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
	/** @hide */
	public static final int SIGNAL_STRENGTH_POOR = 1;
	/** @hide */
	public static final int SIGNAL_STRENGTH_MODERATE = 2;
	/** @hide */
	public static final int SIGNAL_STRENGTH_GOOD = 3;
	/** @hide */
	public static final int SIGNAL_STRENGTH_GREAT = 4;
	/** @hide */
	public static final int NUM_SIGNAL_STRENGTH_BINS = 5;

	/** @hide */

	public static int getIconLevel(int num) {
		int level;

		// ASU ranges from 0 to 31 - TS 27.007 Sec 8.5
		// asu = 0 (-113dB or less) is very weak
		// signal, its better to show 0 bars to the user in such cases.
		// asu = 99 is a special case, where the signal strength is unknown.

		// int asu = getGsmSignalStrength();
		int asu = num;
		if (asu <= 2)
			level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN; // || asu == 99
		else if (asu >= 12)
			level = SIGNAL_STRENGTH_GREAT;
		else if (asu >= 8)
			level = SIGNAL_STRENGTH_GOOD;
		else if (asu >= 5)
			level = SIGNAL_STRENGTH_MODERATE;
		else
			level = SIGNAL_STRENGTH_POOR;
		// if (DBG) log("getGsmLevel=" + level);
		return level;

	}
   //时间戳转化为时间格式
	public static String timeStamp2Date(String seconds,String format) {

		if(seconds == null || seconds.isEmpty() || seconds.equals("null")){
			return "";
		}
		if(format == null || format.isEmpty()){
			format = "yyyy-MM-dd HH:mm";

		}

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date(Long.valueOf(seconds+"000")));
	}

    public static List<File> getSpecificFileFromLocal(String filePath , String fileSuffix){



		return null;
	}

	public static void textCopy(Context context ,String text){

		if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
		} else {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData.newPlainText(
					text, text);
			clipboard.setPrimaryClip(clip);
		}

//		AlertHelper.createAlertConfirm(WsInviteActivity.this, "提示：", "复制成功！").show();
		showToast(context,"复制成功");
	}




	public static Bitmap createVideoThumbnail(String filePath) {
		// MediaMetadataRetriever is available on API Level 8
		// but is hidden until API Level 10
		Class<?> clazz = null;
		Object instance = null;
		try {
			clazz = Class.forName("android.media.MediaMetadataRetriever");
			instance = clazz.newInstance();

			Method method = clazz.getMethod("setDataSource", String.class);
			method.invoke(instance, filePath);

			// The method name changes between API Level 9 and 10.
			if (Build.VERSION.SDK_INT <= 9) {
				return (Bitmap) clazz.getMethod("captureFrame")
						.invoke(instance);
			} else {
				byte[] data = (byte[]) clazz.getMethod("getEmbeddedPicture")
						.invoke(instance);
				if (data != null) {
					Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
							data.length);
					if (bitmap != null)
						return bitmap;
				}
				Bitmap bitmap = (Bitmap) clazz.getMethod("getFrameAtTime").invoke(
						instance);
				if(bitmap != null){
					return bitmap;
				}

			}
		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
		} catch (RuntimeException ex) {
			// Assume this is a corrupt video file.
		} catch (InstantiationException e) {
			Log.e("TAG", "createVideoThumbnail", e);
		} catch (InvocationTargetException e) {
			Log.e("TAG", "createVideoThumbnail", e);
		} catch (ClassNotFoundException e) {
			Log.e("TAG", "createVideoThumbnail", e);
		} catch (NoSuchMethodException e) {
			Log.e("TAG", "createVideoThumbnail", e);
		} catch (IllegalAccessException e) {
			Log.e("TAG", "createVideoThumbnail", e);
		} finally {
			try {
				if (instance != null) {
					clazz.getMethod("release").invoke(instance);
				}
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	public static boolean fileIsExists(String filePath) {
		try {
			File f = new File(filePath);
			if (!f.exists()) {
				return false;
			}

		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		return true;
	}

	/**
	 * Create a video thumbnail for a video. May return null if the video is
	 * corrupt or the format is not supported.
	 * 
	 * @param url the path of video file
	 *
	 * @param width could be MINI_KIND or MICRO_KIND
	 *
	 */

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static Bitmap createVideoThumbnailUrlSuo(String url, int width,
			int height) {

		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		int kind = MediaStore.Video.Thumbnails.MINI_KIND;
		try {
			if (Build.VERSION.SDK_INT >= 14) {
				retriever.setDataSource(url, new HashMap<String, String>());
			} else {
				retriever.setDataSource(url);
			}
			bitmap = retriever.getFrameAtTime();
		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
		} catch (RuntimeException ex) {
			// Assume this is a corrupt video file.
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
				// Ignore failures while cleaning up.
			}
		}
		if (kind == Images.Thumbnails.MICRO_KIND && bitmap != null) {
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
					ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		}
		return bitmap;

	}

	// 验证手机号是否ֻ
	public static boolean isMobileNO(String mobiles) {

		// Pattern p = Pattern
		// .compile("^((13[0-9])|(14[5,7])|(15[^4,\\D])|(18[0,5-9])|(17[5,6,7]))\\d{8}$");
		// Matcher m = p.matcher(mobiles);
		boolean result = mobiles.matches("[0-9]+");
		if ((mobiles.length() == 11) && result) {
			return true;
		} else {
			return false;
		}

		// return m.matches();

	}

	/*
	 * 合法E-mail地址： 1. 必须包含一个并且只有一个符号“@” 2. 第一个字符不得是“@”或者“.” 3. 不允许出现“@.”或者.@ 4.
	 * 结尾不得是字符“@”或者“.” 5. 允许“@”前的字符中出现“＋” 6. 不允许“＋”在最前面，或者“＋@”
	 * 
	 * 正则表达式如下：
	 * -----------------------------------------------------------------------
	 * ^(
	 * \w+((-\w+)|(\.\w+))*)\+\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0
	 * -9]+)*\.[A-Za-z0-9]+$
	 * -----------------------------------------------------------------------
	 * 
	 * 字符描述： ^ ：匹配输入的开始位置。 \：将下一个字符标记为特殊字符或字面值。 ：匹配前一个字符零次或几次。 + ：匹配前一个字符一次或多次。
	 * (pattern) 与模式匹配并记住匹配。 x|y：匹配 x 或 y。 [a-z] ：表示某个范围内的字符。与指定区间内的任何字符匹配。 \w
	 * ：与任何单词字符匹配，包括下划线。 $ ：匹配输入的结尾。
	 */

	// email
	public static boolean isEmail(String email) {

		String str = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";// "^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);
		return m.matches();

	}

	public static boolean isClientType(UserEntity entity) {
		boolean isClientType = false;
		if ( entity.getUserType() == ClientType.CLIENT_TYPE_MOBILE_DEVICE_VALUE
				) {
			isClientType = true;
		}


		return isClientType;
	}


	public static  boolean isNumeric(String str){
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if( !isNum.matches() ){
			return false;
		}
		return true;
	}


	public static void  addContact(Context context ,String name, String phoneNumber) {
		// 创建一个空的ContentValues
		if(phoneNumber.equals("")){
			return;
		}
		ContentValues values = new ContentValues();

		// 向RawContacts.CONTENT_URI空值插入，
		// 先获取Android系统返回的rawContactId
		// 后面要基于此id插入值
		Uri rawContactUri = context.getContentResolver().insert(RawContacts.CONTENT_URI, values);
		if(rawContactUri == null){
			showToast( context,context.getString(R.string.please_check_contacts_authority));
			return;
		}
		long rawContactId = ContentUris.parseId(rawContactUri);
		values.clear();

		values.put(Data.RAW_CONTACT_ID, rawContactId);
		// 内容类型
		values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
		// 联系人名字
		values.put(StructuredName.GIVEN_NAME, name);
		// 向联系人URI添加联系人名字
		context.getContentResolver().insert(Data.CONTENT_URI, values);
		values.clear();

		values.put(Data.RAW_CONTACT_ID, rawContactId);
		values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
		// 联系人的电话号码
		values.put(Phone.NUMBER, phoneNumber);
		// 电话类型
		values.put(Phone.TYPE, Phone.TYPE_MOBILE);
		// 向联系人电话号码URI添加电话号码
		context.getContentResolver().insert(Data.CONTENT_URI, values);
		values.clear();

//        values.put(Data.RAW_CONTACT_ID, rawContactId);
//        values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
//        // 联系人的Email地址
//        values.put(Email.DATA, "@xxx.com");
//        // 电子邮件的类型
//        values.put(Email.TYPE, Email.TYPE_WORK);
//        // 向联系人Email URI添加Email数据
//        getContentResolver().insert(Data.CONTENT_URI, values);

		showToast( context,context.getString(R.string.add_contact_success));
	}


	/**
	 *  Toast
	 * @param context
	 * @param text
     */
	public static void  showToast(Context context,CharSequence text){
		Toast.makeText(context.getApplicationContext(), text,
				Toast.LENGTH_SHORT).show();
	}

	public static void  showToast(Context context,int id){
		Toast.makeText(context.getApplicationContext(), id,
				Toast.LENGTH_SHORT).show();
	}

	public static Toast  getToast(Context context,CharSequence text){
		return  Toast.makeText(context.getApplicationContext(), text,
				Toast.LENGTH_SHORT);
	}

	public static Toast  getToast(Context context,int  id){
		return  Toast.makeText(context.getApplicationContext(), id,
				Toast.LENGTH_SHORT);
	}



	public static String saveImageUrl(Bitmap bmp) {
		String path = "";
		File appDir = new File(Environment.getExternalStorageDirectory(),
				"Boohee");
		if (!appDir.exists()) {
			appDir.mkdir();
		}
		String fileName = System.currentTimeMillis() + ".jpg";
		File file = new File(appDir, fileName);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();

			path = file.getAbsolutePath();


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}


	public static  String getTimeNum(int time) {


		String timeNum = "";

		int h=  time/3600;
		int m=(time-h*3600)/60;
		int s = (time-h*3600) % 60;

		String min;
		if(m>9){
			min =  "" + m;
		}else{
			min = "0" +m;
		}

		String second;
		if(s>9){
			second =  "" + s;
		}else{
			second = "0" +s;
		}

		String hour;
		if(h>9){
			hour =  "" + h;
		}else{
			hour = "0" +h;
		}
		if(h>0){
			timeNum = hour + ":" + min +":" + second;
		}else{
			timeNum = min +":" + second;
		}

		return timeNum;
	}



	public static boolean isDataNumeric(String str){
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if( !isNum.matches() ){
			return false;
		}
		return true;
	}
	;

	public static String timesTwo(String time) {
		SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd");
		@SuppressWarnings("unused")
		long lcc = Long.valueOf(time);
		//	int i = Integer.parseInt(time);
		String times = sdr.format(new Date(lcc * 1000L));
		return times;

	}



}
