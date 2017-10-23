package android.fengqing.com.waterwatchsystem.acty;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.fengqing.com.waterwatchsystem.R;
import android.fengqing.com.waterwatchsystem.fragment.BottomFragment;
import android.fengqing.com.waterwatchsystem.fragment.FunctionFragment;
import android.fengqing.com.waterwatchsystem.fragment.VideoFragment;
import android.fengqing.com.waterwatchsystem.fragment.WebFragment;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class WaterWatchMainActivity extends Activity  {

    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_water_watch_main);
        initFragment();
    }
    private void initFragment(){
        mFragmentManager = getFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.add(R.id.video_web_section, VideoFragment.instance(),null);
        mFragmentTransaction.add(R.id.ff_bottom_section,BottomFragment.instance(),null);
        mFragmentTransaction.add(R.id.ff_section,FunctionFragment.instance(),null);
        mFragmentTransaction.commit();
    }
    public static void switchVideoFragmentToWeb(){

    }
}
