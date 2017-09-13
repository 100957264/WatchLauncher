package com.fise.xiaoyu.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.fise.xiaoyu.DB.entity.UserEntity;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.event.UserInfoEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.ui.adapter.SortPhoneMemberAdapter;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.utils.PermissionUtil;
import com.fise.xiaoyu.utils.Utils;
import com.xiaowei.phone.CharacterParser;
import com.xiaowei.phone.ClearEditText;
import com.xiaowei.phone.GetPhoneNumberFromMobile;
import com.xiaowei.phone.PhoneMemberBean;
import com.xiaowei.phone.PinyinComparator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 通讯录中好友
 */
public class PhoneInfoActivity extends TTBaseActivity implements SectionIndexer {
	private ListView sortListView;
	// private SideBar sideBar;
	private TextView dialog;
	private SortPhoneMemberAdapter adapter;
	private ClearEditText mClearEditText;
	private ImageView line_image;
	private Button mSearch;

	private LinearLayout titleLayout;
	private TextView title;
	private TextView tvNofriends;
	private static IMService imService;
	public List<PhoneMemberBean> SourceList = new ArrayList();

	public TextView left_text;
	private int lastFirstVisibleItem = -1;
	private boolean isOpen;
	private CharacterParser characterParser;

	/**
	 * 
	 */
	private PinyinComparator pinyinComparator;

	private UserEntity loginContact;
	private GetPhoneNumberFromMobile getPhoneNumberFromMobile;
	private List<PhoneMemberBean> SourceDateList;

	private IMServiceConnector imServiceConnector = new IMServiceConnector() {
		@Override
		public void onIMServiceConnected() {
			imService = imServiceConnector.getIMService();
			if (imService == null) {
				// 后台服务启动链接失败
				return;
			}

			loginContact = imService.getLoginManager().getLoginInfo();
			requestRunPermisssion(new String[]{
					Manifest.permission.WRITE_CONTACTS,
					Manifest.permission.READ_CONTACTS,
			}, new PermissionListener() {
				@Override
				protected void onGranted() {
					SourceDateList = getPhoneNumberFromMobile.getPhoneNumberFromMobile(
							PhoneInfoActivity.this, loginContact);
					List<String> list = new ArrayList();
					for (int i = 0; i < SourceDateList.size(); i++) {
						list.add(SourceDateList.get(i).getNumber());
					}

					imService.getUserActionManager().onPhoneBookFriendReq(
							loginContact.getPeerId(), list);
				}

				@Override
				protected void onDenied(List<String> deniedPermission) {
					Toast.makeText(PhoneInfoActivity.this, PermissionUtil.getPermissionString(PhoneInfoActivity.this, deniedPermission), Toast.LENGTH_SHORT).show();
					finish();
				}
			});
		}

		@Override
		public void onServiceDisconnected() {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_add_phone);

		getPhoneNumberFromMobile = new GetPhoneNumberFromMobile();
		imServiceConnector.connect(this);
				initViews();

	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(UserInfoEvent event) {
		switch (event) {

		case USER_PHONE_SUCCESS:
			// ad
			SourceList.clear();
			List<UserEntity> phoneList = imService.getUserActionManager()
					.getPhoneUserList();
			for (int i = 0; i < SourceDateList.size(); i++) {
				for (int j = 0; j < phoneList.size(); j++) {
					if (SourceDateList.get(i).getNumber()
							.equals(phoneList.get(j).getPhone())) {

						PhoneMemberBean temp = SourceDateList.get(i);
						temp.setUserEntity(phoneList.get(j));
						SourceList.add(temp);
					}
				}
			}
			filledData(SourceList);
			adapter.putPhoneList(SourceList);
			imService.getUserActionManager().setPhoneMemberBeanList(SourceList);
			break;
		case USER_PHONE_FAIL:
			Utils.showToast(PhoneInfoActivity.this, "同步通讯录失败");
			break;
		case USER_INFO_UPDATE:
			SourceList.clear();
			List<UserEntity> phoneList1 = imService.getUserActionManager()
					.getPhoneUserList();
			List<UserEntity> user = imService.getContactManager()
					.getContactFriendsList();

			for (int i = 0; i < user.size(); i++) {
				for (int j = 0; j < phoneList1.size(); j++) {
					if (phoneList1.get(j).getPeerId() == user.get(i)
							.getPeerId()) {
						phoneList1.get(j).setFriend(user.get(i).getIsFriend());
					}
				}
			}

			for (int i = 0; i < SourceDateList.size(); i++) {
				for (int j = 0; j < phoneList1.size(); j++) {
					if (SourceDateList.get(i).getNumber()
							.equals(phoneList1.get(j).getPhone())) {
						PhoneMemberBean temp = SourceDateList.get(i);
						temp.setUserEntity(phoneList1.get(j));
						SourceList.add(temp);
					}
				}
			}
			filledData(SourceList);
			adapter.putPhoneList(SourceList);
			imService.getUserActionManager().setPhoneMemberBeanList(SourceList);

			break;
		case USER_INFO_REQ_FRIENDS_SUCCESS:
			break;

		case USER_INFO_REQ_FRIENDS_FAIL:
			break;

		}
	}

	private void initViews() {
		titleLayout = (LinearLayout) findViewById(R.id.title_layout);
		title = (TextView) this.findViewById(R.id.title_layout_catalog);
		tvNofriends = (TextView) this
				.findViewById(R.id.title_layout_no_friends);

		left_text = (TextView) this.findViewById(R.id.left_text_text);

		Button life_icon_arrow = (Button) this
				.findViewById(R.id.life_icon_arrow);

		life_icon_arrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (isOpen) {
					mClearEditText.setVisibility(View.GONE);
					line_image.setVisibility(View.GONE);

					mSearch.setVisibility(View.VISIBLE);
					left_text.setVisibility(View.VISIBLE);
					isOpen = false;
				} else {
					PhoneInfoActivity.this.finish();
				}

			}
		});



		TextView left_text_text = (TextView) this.findViewById(R.id.left_text_text);

		left_text_text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (isOpen) {
					mClearEditText.setVisibility(View.GONE);
					line_image.setVisibility(View.GONE);

					mSearch.setVisibility(View.VISIBLE);
					left_text.setVisibility(View.VISIBLE);
					isOpen = false;
				} else {
					PhoneInfoActivity.this.finish();
				}

			}
		});

		//
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		dialog = (TextView) findViewById(R.id.dialog);

		sortListView = (ListView) findViewById(R.id.country_lvcountry);

		// SourceDateList = SourceList;//
		// getPhoneNumberFromMobile.getPhoneNumberFromMobile(this);
		filledData(SourceList);

		Collections.sort(SourceList, pinyinComparator);
		adapter = new SortPhoneMemberAdapter(PhoneInfoActivity.this, SourceList);
		sortListView.setAdapter(adapter);
		sortListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				Intent intent = new Intent(PhoneInfoActivity.this,
						PhoneFriendsActivity.class);
				intent.putExtra(IntentConstant.PHONE_ID, arg2);
				PhoneInfoActivity.this.startActivity(intent);

			}
		});

		if (SourceList.size() > 0) {
			sortListView.setOnScrollListener(new OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView view,
						int scrollState) {

				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					int section = getSectionForPosition(firstVisibleItem);

					if (firstVisibleItem != lastFirstVisibleItem) {
						MarginLayoutParams params = (MarginLayoutParams) titleLayout
								.getLayoutParams();
						params.topMargin = 0;
						titleLayout.setLayoutParams(params);
						title.setText(SourceList.get(
								getPositionForSection(section))
								.getSortLetters());
					}

					lastFirstVisibleItem = firstVisibleItem;
				}
			});
		}

		mSearch = (Button) findViewById(R.id.search_button);
		mSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mClearEditText.setVisibility(View.VISIBLE);
				line_image.setVisibility(View.VISIBLE);

				mSearch.setVisibility(View.GONE);
				left_text.setVisibility(View.GONE);

				isOpen = true;

			}
		});

		line_image = (ImageView) findViewById(R.id.line_image);
		mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
		mClearEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				titleLayout.setVisibility(View.GONE);
				filterData(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			// return true;//返回真表示返回键被屏蔽掉
			if (isOpen) {
				mClearEditText.setVisibility(View.GONE);
				line_image.setVisibility(View.GONE);

				mSearch.setVisibility(View.VISIBLE);
				left_text.setVisibility(View.VISIBLE);
				isOpen = false;
			} else {
				PhoneInfoActivity.this.finish();
			}

		}
		return true;

		// return super.onKeyDown(keyCode, event);
	}

	/**
	 * ΪListVie
	 * 
	 * @param date
	 * @return
	 */
	private void filledData(List<PhoneMemberBean> date) {
		// List<GroupMemberBean> mSortList = new ArrayList<GroupMemberBean>();

		for (int i = 0; i < date.size(); i++) {
			// GroupMemberBean sortModel = new GroupMemberBean();
			// sortModel.setName(date[i]);
			String pinyin = characterParser.getSelling(date.get(i).getName());
			String sortString = pinyin.substring(0, 1).toUpperCase();

			if (sortString.matches("[A-Z]")) {
				date.get(i).setSortLetters(sortString.toUpperCase());
			} else {
				date.get(i).setSortLetters("#");
			}

		}

	}

	/**
	 * 
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		List<PhoneMemberBean> filterDateList = new ArrayList<PhoneMemberBean>();

		if (TextUtils.isEmpty(filterStr)) {
			filterDateList = SourceList;
			titleLayout.setVisibility(View.GONE);
			tvNofriends.setVisibility(View.GONE);
		} else {
			filterDateList.clear();
			for (PhoneMemberBean sortModel : SourceList) {
				String name = sortModel.getName();
				String phone = sortModel.getUserEntity().getPhone();
				String mainName = sortModel.getUserEntity().getMainName();
				if (name.indexOf(filterStr.toString()) != -1
						|| characterParser.getSelling(name).startsWith(
								filterStr.toString())
						|| characterParser.getSelling(phone).startsWith(
								filterStr.toString())
						|| characterParser.getSelling(mainName).startsWith(
								filterStr.toString())) {
					filterDateList.add(sortModel);
				}
			}
		}

		//
		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
		if (filterDateList.size() == 0) {

			titleLayout.setVisibility(View.VISIBLE);
			tvNofriends.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}

	public int getSectionForPosition(int position) {
		return SourceList.get(position).getSortLetters().charAt(0);
	}

	public int getPositionForSection(int section) {
		for (int i = 0; i < SourceList.size(); i++) {
			String sortStr = SourceList.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void onDestroy() {
				imServiceConnector.disconnect(this);
		super.onDestroy();
	}
}
