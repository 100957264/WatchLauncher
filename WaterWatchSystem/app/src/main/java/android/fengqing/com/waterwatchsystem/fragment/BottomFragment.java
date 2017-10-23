package android.fengqing.com.waterwatchsystem.fragment;

import android.app.Fragment;
import android.fengqing.com.waterwatchsystem.R;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;


public class BottomFragment extends Fragment implements View.OnClickListener{
    ImageView mHaierStore;
    ImageView mShiTeLaoSi;
    ImageView mXiangRiKui;
    private static class SingletonHolder {
        private static final BottomFragment INSTANCE = new BottomFragment();
    }
    public static BottomFragment instance() {
        return SingletonHolder.INSTANCE;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_fragment,container,false);
        initView(view);
        return view;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_haier_store:
               // startTargetIntent("http://www.ehaier.com/");
                break;
            case R.id.iv_shitelaosi_store:
              //  startTargetIntent("http://www.hswzyj.com/shop/");
                break;
            case R.id.iv_xiangrikui_store:
             //   startTargetIntent("http://www.hisunflower.com/");
                break;
        }
    }
    private void initView(View view) {
        mHaierStore =(ImageView) view.findViewById(R.id.iv_haier_store);
        mHaierStore.setOnClickListener(this);
        mShiTeLaoSi =(ImageView) view.findViewById(R.id.iv_shitelaosi_store);
        mShiTeLaoSi.setOnClickListener(this);
        mXiangRiKui =(ImageView) view.findViewById(R.id.iv_xiangrikui_store);
        mXiangRiKui.setOnClickListener(this);
    }
}
