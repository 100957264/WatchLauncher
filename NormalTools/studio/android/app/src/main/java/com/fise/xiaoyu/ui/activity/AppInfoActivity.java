package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.bean.AppSimpleInfo;
import com.fise.xiaoyu.bean.BaseResponse;
import com.fise.xiaoyu.config.UrlConstant;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.model.JsonCallback;
import com.fise.xiaoyu.ui.base.AppBaseActivity;
import com.fise.xiaoyu.ui.widget.ToolBarView;
import com.fise.xiaoyu.utils.Logger;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class AppInfoActivity extends AppBaseActivity {

    private static final int UPDATE_APP_MARKET_LIST = 1;

    private Logger logger = Logger.getLogger(AppInfoActivity.class);
    private IMService imService;
    private AppInfoAdapter appInfoAdapter;
    private RecyclerView recyclerView;
    private ArrayList<String> appImgs = new ArrayList<>();
    private AppSimpleInfo appSimpleInfo;

    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_APP_MARKET_LIST: {
                    toolBarView.setTitle(appSimpleInfo.appName);
                    install.setVisibility(View.VISIBLE);
                    Glide.with(AppInfoActivity.this)
                            .load(appSimpleInfo.iconUrl)
                            .into(appIcon);
                    appInfoAdapter.notifyDataSetChanged();
                    appType.setText(appSimpleInfo.className + "类 | " + appSimpleInfo.packageSize);
                    appDescrition.setText(appSimpleInfo.descrition);
                }
                break;
            }
            super.handleMessage(msg);
        }
    };
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            logger.d("login#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                // 后台服务启动链接失败
                return;
            }
            requestAppInfo();
        }
    };
    private ToolBarView toolBarView;
    private TextView appType;
    private TextView appDescrition;
    private ImageView appIcon;
    private Button install;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(AppInfoActivity.this);
            }

    // 为什么会有两个这个
    // 可能是 兼容性的问题 导致两种方法onBackPressed
    @Override
    public void onBackPressed() {
        logger.d("login#onBackPressed");
        // imLoginMgr.cancel();
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * ----------------------------event 事件驱动----------------------------
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginEvent event) {
        switch (event) {
            case LOCAL_LOGIN_SUCCESS:
            case LOGIN_OK:
                break;
        }
    }

    private void requestAppInfo() {
        OkGo.<BaseResponse<AppSimpleInfo>>post(UrlConstant.APP_INFO_URL)
                .tag(this)
                .execute(new JsonCallback<BaseResponse<AppSimpleInfo>>() {
                    @Override
                    public void onSuccess(Response<BaseResponse<AppSimpleInfo>> response) {
                        appSimpleInfo = response.body().data;
                        appImgs.addAll(Arrays.asList(appSimpleInfo.imageList));
                        uiHandler.sendEmptyMessage(UPDATE_APP_MARKET_LIST);
                    }

                    @Override
                    public void onError(Response<BaseResponse<AppSimpleInfo>> response) {
                        response.getException().printStackTrace();
                        super.onError(response);
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_appinfo);

        imServiceConnector.connect(this);

        toolBarView = (ToolBarView) findViewById(R.id.toolbar_app_info);
        toolBarView.setOnToolBarClickListener(new ToolBarView.OnToolBarClickListener() {
            @Override
            public void onLeftButtonClicked(View view) {
                finish();
            }

            @Override
            public void onCenterButtonClicked(View view) {

            }

            @Override
            public void onRightButtonClicked(View view) {

            }
        });

        appType = (TextView) findViewById(R.id.app_type);
        appDescrition = (TextView) findViewById(R.id.app_descrition);
        appIcon = (ImageView) findViewById(R.id.app_icon);
        install = (Button) findViewById(R.id.app_install);
        install.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                install.setText("等待授权");
                install.setClickable(false);
            }
        });
        install.setVisibility(View.INVISIBLE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_app_info);
        appInfoAdapter = new AppInfoAdapter();
        recyclerView.setAdapter(appInfoAdapter);
        recyclerView.setLayoutManager(layoutManager);

        cleanDownloadDir();
    }

    class AppInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        public AppInfoAdapter() {
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(AppInfoActivity.this);
            LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(10, 0, 10, 0);
            imageView.setLayoutParams(layoutParams);
            imageView.setAdjustViewBounds(true);
            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ViewHolder) {
                final ViewHolder viewHolder = (ViewHolder) holder;
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.warning)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
                Glide.with(AppInfoActivity.this)
                        .load(appImgs.get(position))
                        .apply(requestOptions)
                        .into(viewHolder.imageView);
            }
        }

        @Override
        public int getItemCount() {
            return appImgs.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;
            }
        }
    }

    /**
     * 清除历史版本下载文件
     */
    private void cleanDownloadDir() {
        String path = Environment.getExternalStorageDirectory() + "/amosdownload/";
        File file = new File(path);
        if (file.isDirectory()) {
            deleteDir(file);
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

}
