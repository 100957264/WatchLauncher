package android.fengqing.com.waterwatchsystem.fragment;

import android.app.Fragment;
import android.fengqing.com.waterwatchsystem.R;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebFragment extends Fragment {

    WebView mWebView;
    WebSettings mWebSettings;
    private static class SingletonHolder {
        private static final WebFragment INSTANCE = new WebFragment();
    }
    public static WebFragment instance() {
        return SingletonHolder.INSTANCE;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.web_fragment,container,false);
        mWebView = (WebView) view.findViewById(R.id.web_view);
        mWebSettings = mWebView.getSettings();
        return view;
    }
    private void startTargetIntent(String address){
        mWebView.loadUrl(address);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

}
