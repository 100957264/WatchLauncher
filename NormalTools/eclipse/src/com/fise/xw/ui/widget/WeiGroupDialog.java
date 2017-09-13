package com.fise.xw.ui.widget;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fise.xw.R;
import com.fise.xw.DB.entity.GroupEntity;
import com.fise.xw.imservice.manager.IMGroupManager;
import com.fise.xw.imservice.service.IMService;
import com.fise.xw.ui.activity.WeiGroupListActivity;
import com.fise.xw.ui.adapter.NewGroupAdspter;
import com.fise.xw.utils.IMUIHelper;

public class WeiGroupDialog {
	Context context;
	Dialogcallback dialogcallback;
	Dialog dialog;
	Button sure;

	private List<GroupEntity> groupList;
	private ListView listView = null;
	private NewGroupAdspter adapter;
	private IMService imService;
	private TextView group_text ;

	/**
	 * init the dialog
	 * 
	 * @return
	 */
	public WeiGroupDialog(Context con, IMService imService) {
		this.context = con;
		dialog = new Dialog(context,R.style.dialog);
		dialog.setContentView(R.layout.wei_group_dialog);
		sure = (Button) dialog.findViewById(R.id.button1);
		sure.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		}); 
		
		this.imService = imService; 
		listView = (ListView) dialog.findViewById(R.id.list);
		groupList = IMGroupManager.instance().getNormalWeiGroupList();
		adapter = new NewGroupAdspter(this.context, groupList, this.imService);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				Object object = groupList.get(arg2);
				if (object instanceof GroupEntity) {
					GroupEntity groupEntity = (GroupEntity) object;
					IMUIHelper.openChatActivity(context,
							groupEntity.getSessionKey());
				}
			}
		});
		
		group_text = (TextView) dialog.findViewById(R.id.group_tishi_text);
		if(groupList.size()<=0)
		{
			group_text.setVisibility(View.VISIBLE);
		}else{
			group_text.setVisibility(View.GONE);
		}
	}

	/**
	 * 设定一个interfack接口,使mydialog可以處理activity定義的事情
	 * 
	 * @author sfshine
	 * 
	 */
	public interface Dialogcallback {
		public void dialogdo(String string);
	}

	public void setDialogCallback(Dialogcallback dialogcallback) {
		this.dialogcallback = dialogcallback;
	}

	public void show() {
		dialog.show();
	}

	public void hide() {
		dialog.hide();
	}

	public void dismiss() {
		dialog.dismiss();
	}
}
