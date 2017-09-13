package com.photoselectortest;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.photoselector.model.PhotoModel;
import com.photoselector.ui.PhotoSelectorActivity;
import com.photoselector.util.CommonUtils;

public class MainActivity extends Activity implements OnClickListener {

	private TextView tvPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tvPath = (TextView) findViewById(R.id.tv_path);
	}

	@Override
	public void onClick(View v) {
		// CommonUtils是library中的一个工具类
		CommonUtils.launchActivityForResult(this, PhotoSelectorActivity.class, 0);
		tvPath.setText("");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0 && resultCode == RESULT_OK) {
			if (data != null && data.getExtras() != null) {
				@SuppressWarnings("unchecked")
				List<PhotoModel> photos = (List<PhotoModel>) data.getExtras().getSerializable("photos");
				if (photos == null || photos.isEmpty())
					return;
				StringBuffer sb = new StringBuffer();
				for (PhotoModel photo : photos) {
					sb.append(photo.getOriginalPath() + "\r\n");
				}
				tvPath.setText(sb.toString());
			}

		}
	}

}
