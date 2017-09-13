package com.fise.marechat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fise.marechat.bean.TargetInfo;
import com.fise.marechat.bean.msg.TcpMsg;
import com.fise.marechat.client.GlobalSettings;
import com.fise.marechat.client.TcpClient;
import com.fise.marechat.client.TcpConnConfig;
import com.fise.marechat.client.helper.stickpackage.AbsStickPackageHelper;
import com.fise.marechat.constant.ActivityResultConstants;
import com.fise.marechat.event.PhotoSelectedEvent;
import com.fise.marechat.event.PhotoTakeEvent;
import com.fise.marechat.listener.OnTagItemClickListener;
import com.fise.marechat.listener.TcpClientListener;
import com.fise.marechat.manager.NetManager;
import com.fise.marechat.net.IPSubscribe;
import com.fise.marechat.net.TaskSubscriber;
import com.fise.marechat.parser.MsgParser;
import com.fise.marechat.parser.MsgRecService;
import com.fise.marechat.task.CommonTask;
import com.fise.marechat.task.TaskBean;
import com.fise.marechat.task.TaskManager;
import com.fise.marechat.thread.HeartBeatThread;
import com.fise.marechat.util.ImageUtils;
import com.fise.marechat.util.SnackbarUtils;
import com.fise.marechat.util.ToastUtils;
import com.fise.marechat.utils.LogUtils;
import com.fise.marechat.utils.NetworkUtil;
import com.fise.marechat.utils.StringValidationUtils;
import com.fise.marechat.widget.ConsoleLayout;
import com.fise.marechat.widget.StaticPackageLayout;
import com.fise.marechat.widget.TagService;
import com.foamtrace.photopicker.ImageCaptureManager;
import com.foamtrace.photopicker.PhotoPreviewActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TcpclientActivity extends AppCompatActivity implements View.OnClickListener, TcpClientListener, IPSubscribe.IPCallBack, TaskBean.TaskCallback, OnTagItemClickListener {

    private TextView headerTxt, headContentTxt;
    private Button tcpclientBuConnect;
    private TextInputLayout contentTxt;
    private EditText tcpclientEditIp;
    private Button tcpclientBuSend;
    private StaticPackageLayout tcpclientStaticpackagelayout;
    private ConsoleLayout tcpclientConsole;
    private SwitchCompat tcpclientSwitchReconnect;
    private TcpClient xTcpClient;
    TagService tagService;
    NestedScrollView scrolllView;
    private ImageView previewPhoto;
    View tagRoot;

    List<String> imagePaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpclient);
        TaskManager.instance().exeTask(new CommonTask() {
            @Override
            public void exeTask() {
                super.exeTask();
                findId();
            }
        }, new TaskSubscriber(this, this));
        EventBus.getDefault().register(this);

    }

    private void findId() {
        scrolllView = (NestedScrollView) findViewById(R.id.root_scroll_view);
        tcpclientBuConnect = (Button) findViewById(R.id.tcpclient_bu_connect);
        headContentTxt = (TextView) findViewById(R.id.head_content_text);
        headerTxt = (TextView) findViewById(R.id.head_text);
        contentTxt = (TextInputLayout) findViewById(R.id.content_text);
        tcpclientBuSend = (Button) findViewById(R.id.tcpclient_bu_send);
        tcpclientStaticpackagelayout = (StaticPackageLayout) findViewById(R.id.tcpclient_staticpackagelayout);
        tcpclientEditIp = (EditText) findViewById(R.id.tcpclient_edit_ip);
        tcpclientConsole = (ConsoleLayout) findViewById(R.id.tcpclient_console);
        tcpclientSwitchReconnect = (SwitchCompat) findViewById(R.id.tcpclient_switch_reconnect);
        tagRoot = findViewById(R.id.tagview);
        previewPhoto = (ImageView) findViewById(R.id.preview_photo);
        tcpclientBuConnect.setOnClickListener(this);
        tcpclientBuSend.setOnClickListener(this);
        EditText et_content = contentTxt.getEditText();
        headerTxt.setMovementMethod(ScrollingMovementMethod.getInstance());
        headContentTxt.setMovementMethod(ScrollingMovementMethod.getInstance());
        et_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateText();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        NetworkUtil.domain2IP(GlobalSettings.DOMAIN_NAME, new IPSubscribe<String>(this, this));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tcpclient_bu_connect) {
            tcpclientConsole.clearConsole();
            if (xTcpClient != null && xTcpClient.isConnected()) {
                xTcpClient.disconnect();
            } else {
                AbsStickPackageHelper stickHelper = tcpclientStaticpackagelayout.getStickPackageHelper();
                if (stickHelper == null) {
                    addMsg("粘包参数设置错误");
                    return;
                }
                if (!NetworkUtil.isNetworkAvailable(this)) {
                    addMsg("网络异常");
                    return;
                }

                String temp = tcpclientEditIp.getText().toString().trim();
                String[] temp2 = temp.split(":");
                if (temp2.length == 2 && StringValidationUtils.validateRegex(temp2[0], StringValidationUtils.RegexIP)
                        && StringValidationUtils.validateRegex(temp2[1], StringValidationUtils.RegexPort)) {
                    TargetInfo targetInfo = new TargetInfo(temp2[0], Integer.parseInt(temp2[1]));
                    GlobalSettings.instance().setConfig(new TcpConnConfig.Builder()
                            .setStickPackageHelper(stickHelper)//粘包
                            .setIsReconnect(tcpclientSwitchReconnect.isChecked())
                            .create());
                    xTcpClient = TcpClient.instance();
                    xTcpClient.addTcpClientListener(this);
                    if (xTcpClient.isDisconnected()) {
                        xTcpClient.connect();
                    } else {
                        addMsg("已经存在该连接");
                    }
                } else {
                    addMsg("服务器地址必须是 ip:port 形式");
                }
            }
        } else {//send msg
            sendMsg();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (xTcpClient != null) {
            xTcpClient.removeTcpClientListener(this);
//            xTcpClient.disconnect();//activity销毁时断开tcp连接
        }
    }

    private void addMsg(String msg) {
        this.tcpclientConsole.addLog(msg);
    }

    @Override
    public void onConnected(TcpClient client) {
        addMsg(client.getTargetInfo().getIp() + "连接成功");
    }

    @Override
    public void onSended(TcpClient client, TcpMsg tcpMsg) {
        addMsg("我:" + tcpMsg.getSourceDataString());
    }

    @Override
    public void onDisconnected(TcpClient client, String msg, Exception e) {
        addMsg(client.getTargetInfo().getIp() + "断开连接 " + msg + e);
        boolean isNetAvailable = NetManager.instance().isNetAvailable();
        HeartBeatThread.instance().setAvailable(isNetAvailable);
        SnackbarUtils.with(scrolllView).setMessage("网络异常").showError();
        if (!isNetAvailable) {
            SnackbarUtils.with(scrolllView).setMessage(getResources().getString(R.string.network_disavilable_text)).showError();
        }
    }

    @Override
    public void onReceive(TcpClient client, TcpMsg msg) {
//        byte[][] res = msg.getEndDecodeData();
//        byte[] bytes = new byte[0];
//        for (byte[] i : res) {
//            bytes = i;
//            break;
//        }
//        addMsg(client.getTargetInfo().getIp() + ":" + " len= " + bytes.length + ", "
//                + msg.getSourceDataString() + " bytes=" + Arrays.toString(bytes));
        addMsg("服务器: " + client.getTargetInfo().getIp() + ":" + client.getTargetInfo().getPort() + ", " + msg.getSourceDataString());
        String[] headerAndContent = MsgParser.instance().getHeaderAndContent(msg.getSourceDataString());
        LogUtils.e("ssss" + Arrays.toString(headerAndContent));
    }

    @Override
    public void onValidationFail(TcpClient client, TcpMsg tcpMsg) {

    }

    @Override
    public void onItemClick(String tag, int pos) {
        String header = MsgParser.instance().getHeaderByType(tag);
        String content = "";
        updateText(header, content);
    }

    @Override
    public void getIP(String ip) {
        if (TextUtils.isEmpty(ip)) return;
        tcpclientEditIp.setText(ip + ":" + GlobalSettings.PORT);
        GlobalSettings.instance().setIP(ip);
        tcpclientBuConnect.performClick();
    }

    @Override
    public void doTaskBack() {
        tagService = new TagService(this, tagRoot, this);
    }

    private void updateText() {
        String content = contentTxt.getEditText().getText().toString().trim();
        String header = headerTxt.getText().toString().trim();
        updateText(header, content);
    }

    private void updateText(String header, String content) {

        if (!TextUtils.isEmpty(header)) {
            headerTxt.setText(header);
        } else {
            return;
        }

        if (!TextUtils.isEmpty(content)) {
            contentTxt.getEditText().setText(content);
        }

        String msg = MsgParser.instance().composedHeaderContent(header, content);
        headContentTxt.setText(msg);
    }

    private void sendMsg() {
        String content = contentTxt.getEditText().getText().toString().trim();
        String header = headerTxt.getText().toString().trim();
        if (TextUtils.isEmpty(header)) {
            ToastUtils.showShort("头部不能为空~~");
            return;
        }
        String msg = headContentTxt.getText().toString().trim();
        if (xTcpClient != null) {
            xTcpClient.sendMsg(msg);
        } else {
            addMsg("还没有连接到服务器");
        }
    }

    private void loadAdpater(ArrayList<String> paths) {
        imagePaths.clear();
        imagePaths.addAll(paths);

        try {
            JSONArray obj = new JSONArray(imagePaths);
            Log.e("--", obj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                // 选择照片
                case ActivityResultConstants.REQUEST_CAMERA_CODE:
                    loadAdpater(data.getStringArrayListExtra(com.foamtrace.photopicker.PhotoPickerActivity.EXTRA_RESULT));
                    break;
                // 预览
                case ActivityResultConstants.REQUEST_PREVIEW_CODE:
                    loadAdpater(data.getStringArrayListExtra(PhotoPreviewActivity.EXTRA_RESULT));
                    break;
                // 调用相机拍照
                case ActivityResultConstants.REQUEST_TAKE_PHOTO:
                    ImageCaptureManager captureManager = MsgRecService.instance().getImageCaptureManager();
                    if (captureManager.getCurrentPhotoPath() != null) {
                        captureManager.galleryAddPic();
                        ArrayList<String> paths = new ArrayList<>();
                        paths.add(captureManager.getCurrentPhotoPath());
                        loadAdpater(paths);
                    }
                    break;

            }
        }
    }

    @Subscribe
    public void onEventMainThread(PhotoTakeEvent event) {
        Bitmap bmp = event.photoBmp;
        if (null == bmp) return;
        bmp = ImageUtils.scale(bmp, 150, 150);
        previewPhoto.setImageBitmap(bmp);
    }

    @Subscribe
    public void onEventMainThread(PhotoSelectedEvent event) {

        Bitmap bmp = event.photoBmp;
        if (null == bmp) return;
        bmp = ImageUtils.scale(bmp, 150, 150);
        previewPhoto.setImageBitmap(bmp);
    }

}

