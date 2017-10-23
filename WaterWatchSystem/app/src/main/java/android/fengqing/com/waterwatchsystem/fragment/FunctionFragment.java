package android.fengqing.com.waterwatchsystem.fragment;

import android.app.Fragment;
import android.fengqing.com.waterwatchsystem.R;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class FunctionFragment extends Fragment {
    private static class SingletonHolder {
        private static final FunctionFragment INSTANCE = new FunctionFragment();
    }
    public static FunctionFragment instance() {
        return SingletonHolder.INSTANCE;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.function_fragment,container,false);
    }
}
