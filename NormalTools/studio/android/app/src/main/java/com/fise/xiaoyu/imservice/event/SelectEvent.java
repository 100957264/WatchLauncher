package com.fise.xiaoyu.imservice.event;

import com.fise.xiaoyu.ui.adapter.album.ImageItem;

import java.util.List;

/**
 * 选择图片事件
 */
public class SelectEvent {
    private List<ImageItem> list;
    public SelectEvent(List<ImageItem> list){
        this.list = list;
    }

    public List<ImageItem> getList() {
        return list;
    }

    public void setList(List<ImageItem> list) {
        this.list = list;
    }
}
