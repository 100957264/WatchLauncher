package com.android.fisewatchlauncher;

import android.app.Activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FiseSocketTestActivity extends Activity implements View.OnClickListener {

	private EditText mIPEdt, mPortEdt, mSocketIDEdt, mMessageEdt;
	private static TextView mConsoleTxt;

	private static StringBuffer mConsoleStr = new StringBuffer();
	private Socket mSocket;
	private boolean isStartRecieveMsg;

	private SocketHandler mHandler;
	protected BufferedReader mReader;//BufferedWriter 用于推送消息
	protected BufferedWriter mWriter;
   @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
	    setContentView(R.layout.fise_socket_test);
	   initView();

   }
	private void initView() {
		mIPEdt = (EditText) findViewById(R.id.ip_edt);
		mPortEdt = (EditText) findViewById(R.id.port_edt);
		mSocketIDEdt = (EditText) findViewById(R.id.socket_id_edt);
		mMessageEdt = (EditText) findViewById(R.id.msg_edt);
		mConsoleTxt = (TextView) findViewById(R.id.console_txt);
		findViewById(R.id.start_btn).setOnClickListener(this);
		findViewById(R.id.send_btn).setOnClickListener(this);
		findViewById(R.id.clear_btn).setOnClickListener(this);
		mHandler = new SocketHandler();
	}
	private void initSocket() {
		//新建一个线程，用于初始化socket和检测是否有接收到新的消息
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				String ip = mIPEdt.getText().toString();//IP
				int port = Integer.parseInt(mPortEdt.getText().toString());//Socket

				try {
					isStartRecieveMsg = true;
					mSocket = new Socket(ip, port);
					mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "utf-8"));
					mWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream(), "utf-8"));
					while(isStartRecieveMsg) {
						if(mReader.ready()) {
                            /*读取一行字符串，读取的内容来自于客户机
                            reader.readLine()方法是一个阻塞方法，
                            从调用这个方法开始，该线程会一直处于阻塞状态，
                            直到接收到新的消息，代码才会往下走*/
							String data = mReader.readLine();
							//handler发送消息，在handleMessage()方法中接收
							mHandler.obtainMessage(0, data).sendToTarget();
						}
						Thread.sleep(200);
					}
					mWriter.close();
					mReader.close();
					mSocket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.send_btn:
				send();
				break;
			case R.id.clear_btn:
				mConsoleStr.delete(0, mConsoleStr.length());
				mConsoleTxt.setText(mConsoleStr.toString());
				break;
			case R.id.start_btn:
				if(!isStartRecieveMsg) {
					initSocket();
				}
				break;
			default:
				break;
		}
	}

	/**
	 * 发送
	 */
	private void send() {
		new AsyncTask<String, Integer, String>() {

			@Override
			protected String doInBackground(String... params) {
				sendMsg();
				return null;
			}
		}.execute();
	}
	/**
	 * 发送消息
	 */
	protected void sendMsg() {
		try {
			String socketID = mSocketIDEdt.getText().toString().trim();
			String msg = "";

			mWriter.write(msg);
			mWriter.flush();
			mConsoleStr.append("我:" +msg+"   "+getTime(System.currentTimeMillis())+"\n");
			mConsoleTxt.setText(mConsoleStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class SocketHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);





		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		isStartRecieveMsg = false;
	}

	private static String getTime(long millTime) {
		Date d = new Date(millTime);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(d);
	}
}



