package com.fise.xiaoyu.ui.activity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.fise.xiaoyu.DB.entity.PhoneNumberItemInfo;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.widget.SortSideBar;
import com.fise.xiaoyu.ui.widget.SortSideBar.OnTouchingLetterChangedListener;
import com.fise.xiaoyu.utils.pinyin.PinYin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PhoneContactListActivity extends TTBaseActivity implements OnTouchingLetterChangedListener{

    Context mContext = null;

    /**获取库Phon表字段**/
    private static final String[] PHONES_PROJECTION = new String[] {
            Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID,Phone.CONTACT_ID };

    /**联系人显示名称**/
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;

    /**电话号码**/
    private static final int PHONES_NUMBER_INDEX = 1;

    /**头像ID**/
    private static final int PHONES_PHOTO_ID_INDEX = 2;

    /**联系人的ID**/
    private static final int PHONES_CONTACT_ID_INDEX = 3;

    private final int ACTIVITY_RESULT_CODE = 1000 ;
    private ArrayList<PhoneNumberItemInfo> mContactsInfoList = new ArrayList<PhoneNumberItemInfo>();

    ListView mListView = null;
    MyListAdapter myAdapter = null;
    private SortSideBar sortSideBar;
    private TextView dialog;
    private ContentResolver resolver;
    private boolean mStartFromDevice = false;
    private int keyPeerid;
    private int selPhoneNmberType;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.tt_activity_sel_phone_number);
        mListView = (ListView) findViewById(R.id.phone_number_list);

        sortSideBar = (SortSideBar) findViewById(R.id.sidrbar);

        findViewById(R.id.back_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("phone_number" , "");
                PhoneContactListActivity.this.setResult(ACTIVITY_RESULT_CODE , intent);
                finish();
            }
        });
        sortSideBar.setOnTouchingLetterChangedListener(this);

        dialog = (TextView) findViewById(R.id.dialog);
        sortSideBar.setTextView(dialog);
        keyPeerid = getIntent().getIntExtra(IntentConstant.KEY_PEERID , -1);
        selPhoneNmberType = getIntent().getIntExtra(IntentConstant.SEL_PHONE_NUMBER , -1);
        if(keyPeerid != -1){
            mStartFromDevice = true;
        }
        /**得到手机通讯录联系人信息**/
        TelephonyManager manager = (TelephonyManager) this
                .getSystemService(TELEPHONY_SERVICE);// 取得相关系统服务
//        String imsi = manager.getSubscriberId(); // 取出IMSI
//        if (imsi != null && !(imsi.length() <= 0)) {
//            getSIMContacts();
//        }
        getPhoneContacts();
        getSortedContactList();
        myAdapter = new MyListAdapter(this);
        mListView.setAdapter(myAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {

               if(mStartFromDevice){
                   //卡片机  ==》返回到设置界面，分为sos设置和白名单设置，需要区分

                   Intent intent = new Intent(PhoneContactListActivity.this ,WhitePhoneNumberSetActivity.class);
                   intent.putExtra("phone_number" , mContactsInfoList.get(position).getContactPhoneNumber());
                   intent.putExtra("phone_name" , mContactsInfoList.get(position).getContactName());
                   intent.putExtra(IntentConstant.KEY_PEERID, keyPeerid);
                   intent.putExtra(IntentConstant.SEL_PHONE_NUMBER, selPhoneNmberType);
                   startActivity(intent);
                   finish();
               }else{
                   //手表
                   Intent intent = new Intent();
                   intent.putExtra("phone_number" , mContactsInfoList.get(position).getContactPhoneNumber());
                   PhoneContactListActivity.this.setResult(ACTIVITY_RESULT_CODE , intent);
                   finish();
               }

            }
        });

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            Intent intent = new Intent();
            intent.putExtra("phone_number" , "");
            PhoneContactListActivity.this.setResult(ACTIVITY_RESULT_CODE , intent);

        }

        return super.onKeyDown(keyCode, event);
    }

    private void getSortedContactList() {
//        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(mContactsInfoList, new Comparator<PhoneNumberItemInfo>() {
            @Override
            public int compare(PhoneNumberItemInfo entity1, PhoneNumberItemInfo entity2) {
                if (entity2.getPinyinElement().pinyin != null && entity2.getPinyinElement().pinyin.startsWith("#")) {
                    return -1;
                } else if (entity1.getPinyinElement().pinyin != null && entity1.getPinyinElement().pinyin.startsWith("#")) {
                    // todo eric guess: latter is > 0
                    return 1;
                } else {
                    if (entity1.getPinyinElement().pinyin == null) {
                        PinYin.getPinYin(entity1.getContactName(),
                                entity1.getPinyinElement());
                    }
                    if (entity2.getPinyinElement().pinyin == null) {
                        PinYin.getPinYin(entity2.getContactName(),
                                entity2.getPinyinElement());
                    }
                    return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
                }
            }
        });
    }

    /**得到手机通讯录联系人信息**/
    private void getPhoneContacts() {
        resolver = mContext.getContentResolver();

        // 获取手机联系人
        Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,PHONES_PROJECTION, null, null, null);


        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {

                //得到手机号码
                String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                //当手机号码为空的或者为空字段 跳过当前循环
                if (TextUtils.isEmpty(phoneNumber))
                    continue;

                //得到联系人名称
                String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);

                //得到联系人ID
                Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);

                //得到联系人头像ID
                Long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);

                //得到联系人头像Bitamp
                Bitmap contactPhoto = null;

                PhoneNumberItemInfo item = new PhoneNumberItemInfo();
                item.setContactName(contactName);
                item.setContactPhoneNumber(phoneNumber);
                //photoid 大于0 表示联系人有头像 如果没有给此人设置头像则给他一个默认的
                if(photoid > 0 ) {
                    item.setContactId(contactid);
                }else {
                }

                mContactsInfoList.add(item);

            }

            phoneCursor.close();
        }
    }

    /**得到手机SIM卡联系人人信息**/
    private void getSIMContacts() {
        ContentResolver resolver = mContext.getContentResolver();
        // 获取Sims卡联系人
        Uri uri = Uri.parse("content://icc/adn");
        Cursor phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null,
                null);

        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {

                // 得到手机号码
                String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                // 当手机号码为空的或者为空字段 跳过当前循环
                if (TextUtils.isEmpty(phoneNumber))
                    continue;
                // 得到联系人名称
                String contactName = phoneCursor
                        .getString(PHONES_DISPLAY_NAME_INDEX);

                //Sim卡中没有联系人头像
                PhoneNumberItemInfo item = new PhoneNumberItemInfo();
                item.setContactName(contactName);
                item.setContactPhoneNumber(phoneNumber);
                mContactsInfoList.add(item);
            }

            phoneCursor.close();
        }
    }

    class MyListAdapter extends BaseAdapter implements SectionIndexer {
        public MyListAdapter(Context context) {
            mContext = context;
        }

        public int getCount() {
            //设置绘制数量
            return mContactsInfoList.size();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.phone_number_list_item, null);
                holder = new ViewHolder();
                holder.iamge = (ImageView) convertView.findViewById(R.id.color_image);
                holder.title = (TextView) convertView.findViewById(R.id.color_title);
                holder.text = (TextView) convertView.findViewById(R.id.color_text);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();

            }
            PhoneNumberItemInfo itemInfo = mContactsInfoList.get(position);
            //绘制联系人名称
            holder.title.setText(itemInfo.getContactName());
            //绘制联系人号码
            holder.text.setText(itemInfo.getContactPhoneNumber());
            //绘制联系人头像
            holder.iamge.setImageBitmap(getPhotoBitmap(itemInfo));
            return convertView;
        }

        @Override
        public Object[] getSections() {
            return new Object[0];
        }

        @Override
        public int getPositionForSection(int sectionIndex) {

            int index = 0;
            for(PhoneNumberItemInfo entity:mContactsInfoList){

                if(entity.getPinName().length() > 0){
                    int firstCharacter = entity.getPinName().charAt(0);
                    if (firstCharacter == sectionIndex) {
                        return index;
                    }
                }

                index++;
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }
    }



    class ViewHolder{
        private ImageView iamge;
        private TextView title ;
        private TextView text ;
    }

    private Bitmap getPhotoBitmap(PhoneNumberItemInfo itemInfo) {
        Bitmap bitmap = null;
        if(itemInfo.getContactId() > 0 ) {
            Uri uri =ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,itemInfo.getContactId());
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
            bitmap = BitmapFactory.decodeStream(input);
        }else {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_head_man);
        }

        return bitmap;
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        int position = myAdapter.getPositionForSection(s.charAt(0));
        if (position != -1) {
            mListView.setSelection(position);
        }
    }




}