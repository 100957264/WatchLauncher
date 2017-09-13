package com.example.weileiguan.volley;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //创建请求队列
    RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
    StringRequest mStringRequest = new StringRequest(Request.Method.GET, "http://www.baidu.com",
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("wangshu", response);
                }
            }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("wangshu", error.getMessage(), error);
        }
    });
//将请求添加在请求队列中
    mQueue.add(mStringRequest);
}
