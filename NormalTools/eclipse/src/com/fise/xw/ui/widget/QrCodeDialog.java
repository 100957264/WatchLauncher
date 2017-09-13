/**
 * 
 */
package com.fise.xw.ui.widget;

import com.fise.xw.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;


/** * @author  cw 
 * @类说明	自定义弹出框
 * @date 创建时间：2015-07-14上午10:51:55 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  */
public class QrCodeDialog extends Dialog {
	public QrCodeDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		
	}

	public QrCodeDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		setContentView(R.layout.qr_code_dialog);
	}

	public QrCodeDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
}
