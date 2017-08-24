package com.android.fisewatchlauncher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class FiseAnalogClockSelect extends Activity implements OnItemClickListener{
	    int mStyleAnalogClock = 1;
	    GridView mGridView;
	    private List<Map<String,Object>> data_list;
	    private SimpleAdapter clockAdapter;
	    private int[] icon = {
	    		R.drawable.style_analog_clock_0,
	    		R.drawable.style_analog_clock_1,
	    		R.drawable.style_analog_clock_2,
	    		R.drawable.style_analog_clock_3,
	    		R.drawable.style_analog_clock_4,
	    		R.drawable.style_analog_clock_5
	    };
        @Override
        protected void onCreate(Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);
        	setContentView(R.layout.fise_analog_clock_select);
        	mGridView = (GridView) findViewById(R.id.grid_view_analog_clock);
        	data_list = new ArrayList<Map<String,Object>>();
        	//getData();
        	clockAdapter = new SimpleAdapter(this, getData(), R.xml.items, new String[]{"image"},new int[]{R.id.image} );
        	mGridView.setAdapter(clockAdapter);
        	mGridView.setOnItemClickListener(this);
        }
        
		private List<Map<String,Object>>  getData() {
			for(int i=0;i<icon.length;i++){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("image", icon[i]);
				data_list.add(map);
			}
			return data_list;
		}

		private void closeActivity(){
        	Intent data = new Intent();
        	data.putExtra("style", mStyleAnalogClock);
        	setResult(Activity.RESULT_OK, data);
        	this.finish();
        }

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mStyleAnalogClock = position;
//			Toast.makeText(FiseAnalogClockSelect.this, position+"位置被点了 ", Toast.LENGTH_SHORT).show();
			 closeActivity();
		}
}
