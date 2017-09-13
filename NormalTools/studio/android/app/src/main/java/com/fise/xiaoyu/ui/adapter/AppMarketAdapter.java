package com.fise.xiaoyu.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fise.xiaoyu.R;
import com.fise.xiaoyu.bean.AppSimpleInfo;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.imservice.service.IMService;
import com.fise.xiaoyu.ui.activity.AppInfoActivity;
import com.fise.xiaoyu.ui.widget.IMBaseImageView;

import java.util.ArrayList;

/**
 * Created by xiejianghong on 2017/8/28.
 */

public class AppMarketAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NORMAL = 1;

    private Context context;
    private IMService imService;
    private LayoutInflater layoutInflater;
    private ArrayList<AppSimpleInfo> appInfos;
    private View mHeaderView;
    private OnItemClickListener mListener;
    private boolean dividerShowed = false;

    public AppMarketAdapter(Context context, ArrayList<AppSimpleInfo> appInfos) {
        this.context = context;
        this.appInfos = appInfos;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderView != null && viewType == TYPE_HEADER) {
            return new ViewHolder(mHeaderView);
        }
        View layout = layoutInflater.inflate(R.layout.tt_item_app_market, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) return;

        final int pos = getRealPosition(holder);
        final AppSimpleInfo appInfo = appInfos.get(pos);
        if (holder instanceof ViewHolder) {
            final ViewHolder viewHolder = (ViewHolder) holder;
            if (!dividerShowed || getRealPosition(holder) == 0) {
                viewHolder.divider.setVisibility(View.GONE);
            } else {
                viewHolder.divider.setVisibility(View.VISIBLE);
            }
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_launcher)
                    .error(R.drawable.warning)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
            Glide.with(context)
                    .load(appInfo.iconUrl)
                    .apply(requestOptions)
                    .into(viewHolder.app_icon);
            // viewHolder.app_icon.setImageUrl(appInfo.iconUrl);
            viewHolder.appName.setText(appInfo.appName);
            viewHolder.appType.setText(appInfo.className + "类 | " + appInfo.packageSize);
            viewHolder.appDescrition.setText(appInfo.descrition);
            viewHolder.appInstall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent(context, AppInfoActivity.class);
                    intent.putExtra(IntentConstant.INFO_APP_ID, appInfo.appId);
                    context.startActivity(intent);
                }
            });
            if (mListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onItemClick(pos, appInfo);
                    }
                });
            }
        }
    }

    private int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return mHeaderView == null ? position : position - 1;
    }

    @Override
    public int getItemCount() {
        return mHeaderView == null ? appInfos.size() : appInfos.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mHeaderView != null) {
            return TYPE_HEADER;
        }
        return TYPE_NORMAL;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    public IMService getImService() {
        return imService;
    }

    public void setImService(IMService imService) {
        this.imService = imService;
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public void addAppInfos(ArrayList<AppSimpleInfo> appInfos) {
        this.appInfos.addAll(appInfos);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public View divider;
        public IMBaseImageView app_icon;
        public TextView appName;
        public TextView appType;
        public TextView appDescrition;
        public Button appInstall;

        public ViewHolder(View itemView) {
            super(itemView);
            if (itemView != mHeaderView) {
                divider = (View) itemView.findViewById(R.id.divider);
                app_icon = (IMBaseImageView) itemView.findViewById(R.id.app_icon);
                appName = (TextView) itemView.findViewById(R.id.app_name);
                appType = (TextView) itemView.findViewById(R.id.app_type);
                appDescrition = (TextView) itemView.findViewById(R.id.app_descrition);
                appInstall = (Button) itemView.findViewById(R.id.app_install);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, AppSimpleInfo appInfo);
    }
}
