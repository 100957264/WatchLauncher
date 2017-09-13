package com.fise.xw.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.fise.xw.R;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.ui.widget.SlowlyProgressBar;

public class UpdateAppActivity extends TTBaseActivity {

    private SlowlyProgressBar slowlyProgressBar;
    private FrameLayout web_top_color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_version);
        
        web_top_color = (FrameLayout) findViewById(R.id.web_top_color); 
        
        slowlyProgressBar =
                new SlowlyProgressBar
                        (
                                findViewById(R.id.android_update_url),
                                getWindowManager().getDefaultDisplay().getWidth()
                        )
                .setViewHeight(3);

        WebView webView = (WebView) findViewById(R.id.webview);
        WebSettings ws = webView.getSettings();
       // ws.setUseWideViewPort(true);
       // ws.setJavaScriptEnabled(true);   
        ws.setSupportZoom(true); //设置可以支持缩放
        ws.setDefaultZoom(WebSettings.ZoomDensity.FAR);  
        ws.setBuiltInZoomControls(true);//设置出现缩放工具
        webView.setDownloadListener(new MyWebViewDownLoadListener());
        
        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                slowlyProgressBar.setProgress(newProgress);
                web_top_color.setVisibility(View.VISIBLE);
            }

        });
        
        webView.getSettings().setUseWideViewPort(true);
        webView.setInitialScale(10);
        String url = getIntent().getStringExtra("url");  
        webView.loadUrl(url);
    }

private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }

    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
        if(slowlyProgressBar!=null){
            slowlyProgressBar.destroy();
            slowlyProgressBar = null;
        }
	}

}
