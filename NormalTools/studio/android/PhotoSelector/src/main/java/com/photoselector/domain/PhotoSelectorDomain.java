package com.photoselector.domain;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.photoselector.controller.AlbumController;
import com.photoselector.model.AlbumModel;
import com.photoselector.model.PhotoModel;
import com.photoselector.ui.PhotoSelectorActivity.OnLocalAlbumListener;
import com.photoselector.ui.PhotoSelectorActivity.OnLocalReccentListener;

@SuppressLint("HandlerLeak")
public class PhotoSelectorDomain {

	private AlbumController albumController;
	private int peeid;

	public PhotoSelectorDomain(Context context,int peeid) {
		albumController = new AlbumController(context);
		this.peeid = peeid;
	} 

	public void getReccent(final OnLocalReccentListener listener) {
		final Handler handler = new Handler() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				listener.onPhotoLoaded((List<PhotoModel>) msg.obj);
			}
		};
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<PhotoModel> photos = albumController.getCurrent(Environment.getExternalStorageDirectory() +"/" + "fise" +"/"+peeid);
				Message msg = new Message();
				msg.obj = photos;
				handler.sendMessage(msg);
			}
		}).start();
	}

	public void updateAlbum(final OnLocalAlbumListener listener,final String path) {
		final Handler handler = new Handler() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				listener.onAlbumLoaded((List<AlbumModel>) msg.obj);
			}
		};
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<AlbumModel> albums = albumController.getAlbums(path);
				Message msg = new Message();
				msg.obj = albums;
				handler.sendMessage(msg);
			}
		}).start();
	}

	public void getAlbum(final String name, final OnLocalReccentListener listener) {
		final Handler handler = new Handler() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				listener.onPhotoLoaded((List<PhotoModel>) msg.obj);
			}
		};
		new Thread(new Runnable() {
			@Override
			public void run() {
				String path = Environment.getExternalStorageDirectory() +"/" + "fise";
				List<PhotoModel> photos = albumController.getAlbum(name,path);
				Message msg = new Message();
				msg.obj = photos;
				handler.sendMessage(msg);
			}
		}).start();
	}

}
