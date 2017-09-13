
package com.fise.xw.ui.activity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.fise.xw.R;
import com.fise.xw.config.IntentConstant;
import com.fise.xw.ui.base.TTBaseActivity;
import com.fise.xw.ui.helper.Emoparser;
import com.fise.xw.ui.widget.GifLoadTask;
import com.fise.xw.ui.widget.GifView;

/**
 * @author : fengzi on 15-1-25.
 * @email : fengzi@mogujie.com.
 *
 * preview a GIF image when click on the gif message
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
