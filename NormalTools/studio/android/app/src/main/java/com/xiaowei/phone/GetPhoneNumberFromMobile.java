package com.xiaowei.phone;

import java.util.ArrayList;
import java.util.List;

import com.fise.xiaoyu.DB.entity.UserEntity;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class GetPhoneNumberFromMobile {
	private List<PhoneMemberBean> list;

	private UserEntity loginContact;
	public List<PhoneMemberBean> getPhoneNumberFromMobile(Context context,UserEntity loginContact) {
		// TODO Auto-generated constructor stub
		list = new ArrayList<PhoneMemberBean>();
		this.loginContact = loginContact;
		Cursor cursor = context.getContentResolver().query(Phone.CONTENT_URI,
				null, null, null, null);
		// moveToNext方法返回的是一个boolean类型的数据
		while (cursor.moveToNext()) {
			// 读取通讯录的姓名
			String name = cursor.getString(cursor
					.getColumnIndex(Phone.DISPLAY_NAME));
			// 读取通讯录的号码
			String number = cursor.getString(cursor
					.getColumnIndex(Phone.NUMBER)).replace(" ", "");
			
			if(!(loginContact.getPhone().equals(number)))
			{ 
				PhoneMemberBean phoneInfo = new PhoneMemberBean();
				phoneInfo.setName(name);
				phoneInfo.setNumber(number);
				list.add(phoneInfo);
			}
			
			
		}
		return list;
	}
}