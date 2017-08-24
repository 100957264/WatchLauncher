package com.android.fisewatchlauncher2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.icu.text.DisplayContext;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class FiseDialerTestActivity extends Activity  {
   EditText mFiseNumber;
   Button mFiseDialButton;
   PhoneNumberUtils pmu;
   @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
	    setContentView(R.layout.fise_dial_test_activity);
        mFiseNumber = (EditText) findViewById(R.id.fise_number);
       mFiseNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        final String number = mFiseNumber.getText().toString();
        mFiseDialButton = (Button)findViewById(R.id.fise_dial_button);

       mFiseDialButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent dialIntent = new Intent(Intent.ACTION_CALL);
               Log.d("fengqing","number" + number);
               dialIntent.addCategory(Intent.CATEGORY_DEFAULT);
               dialIntent.setData(Uri.parse("tel:13888888888"));
               startActivity(dialIntent);
           }
       });

    }
   }



