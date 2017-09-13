package com.fise.xiaoyu.imservice.event;

/**
 * Created by lenovo on 2017/6/20.
 */

public class VideoInfoEvent {

    public  String videoName;
    public  String videoUrl;
    public  Event event;

    public enum Event {
       GET_NET_VIDEO_URL_SUCCESS

    }



}
