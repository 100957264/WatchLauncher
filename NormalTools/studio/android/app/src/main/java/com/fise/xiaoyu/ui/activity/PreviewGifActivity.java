
package com.fise.xiaoyu.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.fise.xiaoyu.R;
import com.fise.xiaoyu.config.IntentConstant;
import com.fise.xiaoyu.ui.base.TTBaseActivity;
import com.fise.xiaoyu.ui.helper.Emoparser;
import com.fise.xiaoyu.ui.widget.GifLoadTask;
import com.fise.xiaoyu.ui.widget.GifView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * PreviewGifActivity
 */
public class PreviewGifActivity extends TTBaseActivity implements View.OnClickListener {
	 
	
    GifView gifView = null;
    ImageView backView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_preview_gif);
        gifView = (GifView) findViewById(R.id.gif);
        backView = (ImageView)findViewById(R.id.back_btn);
        backView.setOnClickListener(this);
        String content = getIntent().getStringExtra(IntentConstant.PREVIEW_TEXT_CONTENT);
        if(Emoparser.getInstance(this).isMessageGif(content))
        {
            InputStream is = getResources().openRawResource(Emoparser.getInstance(this).getResIdByCharSequence(content));
            int lenght = 0;
            try {
                lenght = is.available();
                byte[]  buffer = ByteBuffer.allocate(lenght).array();
                is.read(buffer);
                gifView.setBytes(buffer);
                gifView.startAnimation();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            new GifLoadTask() {
                @Override
                protected void onPostExecute(byte[] bytes) {
                    gifView.setBytes(bytes);
                    gifView.startAnimation();
                }
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }
            }.execute(content);
        }


    }

    @Override
    public void onClick(View view) {
        gifView.stopAnimation();
        PreviewGifActivity.this.finish();
    }
}
