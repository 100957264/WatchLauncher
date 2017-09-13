package com.fise.xiaoyu.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.bean.AppSimpleInfo;
import com.fise.xiaoyu.bean.BaseResponse;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.config.UrlConstant;
import com.fise.xiaoyu.imservice.event.LoginEvent;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.imservice.support.IMServiceConnector;
import com.fise.xiaoyu.model.JsonCallback;
import com.fise.xiaoyu.ui.adapter.AppMarketAdapter;
import com.fise.xiaoyu.ui.base.AppBaseActivity;
import com.fise.xiaoyu.ui.widget.TextSwitchView;
import com.fise.xiaoyu.ui.widget.ToolBarView;
import com.fise.xiaoyu.utils.Logger;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.youth.banner.Banner;
import com.youth.banner.loader.ImageLoader;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppMarketActivity extends AppBaseActivity {

    private static final int UPDATE_APP_MARKET_LIST = 1;

    private Logger logger = Logger.getLogger(AppMarketActivity.class);
    private IMService imService;
    private AppMarketAdapter appMarketAdapter;
    private RecyclerView recyclerView;
    private ArrayList<AppSimpleInfo> appInfos = new ArrayList<>();
    private ArrayList<String> mTitleDataList;
    private ImageView imageView;
    private RelativeLayout searchLayout;
    private ToolBarView toolBarView;

    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_APP_MARKET_LIST: {
                    appMarketAdapter.notifyDataSetChanged();
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
            appMarketAdapter.setImService(imService);
            requestAppList();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_app_market);

        imServiceConnector.connect(AppMarketActivity.this);

        toolBarView = (ToolBarView) findViewById(R.id.toolbar_app_market);
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
        // imageView = (ImageView) findViewById(R.id.iv_search);
        searchLayout = (RelativeLayout) findViewById(R.id.layout_search);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setAutoMeasureEnabled(true);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_app_market);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        View header = LayoutInflater.from(this).inflate(R.layout.tt_header_app_market, recyclerView, false);
        appMarketAdapter = new AppMarketAdapter(AppMarketActivity.this, appInfos);
        appMarketAdapter.setOnItemClickListener(new AppMarketAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, AppSimpleInfo appInfo) {
                Intent intent = new Intent(AppMarketActivity.this, AppInfoActivity.class);
                intent.putExtra(IntentConstant.INFO_APP_ID, appInfo.appId);
                AppMarketActivity.this.startActivity(intent);
            }
        });
        appMarketAdapter.setHeaderView(header);
        recyclerView.setAdapter(appMarketAdapter);

        TextSwitchView textSwitcher = (TextSwitchView) findViewById(R.id.switcher_search);
        String[] res = new String[]{
                "搜索应用",
                "微信 - 13亿人都在用",
        };
        textSwitcher.setResources(res);
        textSwitcher.setTextStillTime(3000);

        mTitleDataList = new ArrayList<String>(Arrays.asList(new String[]{
                "5e62c2a3f97c76694b5ae576ade5b139.jpg",
                "8fb56fa03babba1e0cb4a7f9f53e3987.jpg",
                "70e0e04d8feea5ebe2e513a0a8ac06e4.jpg",
                "699dd0c3c1edb0f4b9383ebc881186f9.jpg",
        }));
        Banner banner = (Banner) findViewById(R.id.banner);
        for (int i = 0; i < mTitleDataList.size(); i++) {
            mTitleDataList.set(i, UrlConstant.APP_IMG_URL + mTitleDataList.get(i));
        }
        banner.setImageLoader(new GlideImageLoader());
        banner.setImages(mTitleDataList);
        banner.start();
    }

    public class GlideImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            /**
             注意：
             1.图片加载器由自己选择，这里不限制，只是提供几种使用方法
             2.返回的图片路径为Object类型，由于不能确定你到底使用的那种图片加载器，
             传输的到的是什么格式，那么这种就使用Object接收和返回，你只需要强转成你传输的类型就行，
             切记不要胡乱强转！
             */

            //Glide 加载图片简单用法
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
            Glide.with(context).load(path).apply(requestOptions).into(imageView);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(AppMarketActivity.this);
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

    public void requestAppList() {
        OkGo.<BaseResponse<List<AppSimpleInfo>>>post(UrlConstant.APP_LIST_URL)
                .tag(this)
                .execute(new JsonCallback<BaseResponse<List<AppSimpleInfo>>>() {
                    @Override
                    public void onSuccess(Response<BaseResponse<List<AppSimpleInfo>>> response) {
                        appInfos.addAll(response.body().data);
                        uiHandler.sendEmptyMessage(UPDATE_APP_MARKET_LIST);
                    }

                    @Override
                    public void onError(Response<BaseResponse<List<AppSimpleInfo>>> response) {
                        response.getException().printStackTrace();
                        super.onError(response);
                    }
                });
    }

}
