package com.photoselector.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;

import com.photoselector.model.AlbumModel;
import com.photoselector.model.PhotoModel;

public class AlbumController {

	private ContentResolver resolver;

	public AlbumController(Context context) {
		resolver = context.getContentResolver();
	}

	/** 获取最近照片列表 */
	public List<PhotoModel> getCurrent(String path1) {
	//	Cursor cursor = resolver.query(Media.EXTERNAL_CONTENT_URI, new String[] { ImageColumns.DATA,
	//			ImageColumns.DATE_ADDED, ImageColumns.SIZE }, null, null, ImageColumns.DATE_ADDED);

	//	String aaa= Environment.getExternalStorageDirectory().getPath()+"/fise";//"file://"+
//		 String[] mColumns = new String[]{
//            MediaStore.Images.Media._ID,
//            MediaStore.Images.Media.DISPLAY_NAME,
//            MediaStore.Images.Media.DATA,
//            MediaStore.Images.Media.SIZE
//			};
//		 String order = MediaStore.Images.Media.DATE_MODIFIED + " desc";
//	        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//	        Cursor cursor =  resolver.query(uri, mColumns, null, null, order);
		
		/*
		List<PhotoModel> photos = new ArrayList<PhotoModel>();
		int ss =cursor.getCount();
		if (cursor !=null && cursor.getCount() > 0) {
			while(cursor.moveToNext()){
				String name1 = cursor.getString(cursor.getColumnIndex(ImageColumns.DATA));
				if(name1.startsWith(aaa))
				{
					if (cursor.getLong(cursor.getColumnIndex(ImageColumns.SIZE)) > 1024 * 10) {
						PhotoModel photoModel = new PhotoModel();
						photoModel.setOriginalPath(cursor.getString(cursor.getColumnIndex(ImageColumns.DATA)));
						photos.add(photoModel);
					}
				}

			}
		}
		*/
		File path =  new File(path1);
		File[] files = path.listFiles();
		List<PhotoModel> photos = new ArrayList<PhotoModel>();
		if (files != null) {// 先判断目录是否为空，否则会报空指针
			for (File file : files) {
				String fileName = file.getName();
				if (fileName.endsWith(".jpg")) {

					//String name = fileName.substring(0, fileName.lastIndexOf("."))
					//		.toString(); 
					String name = file.getAbsolutePath();
					PhotoModel photoModel = new PhotoModel();
					photoModel.setOriginalPath(name);
					photos.add(photoModel);
				}
			}
		}
	
		
		
//		if (cursor == null || !cursor.moveToNext())
//			return new ArrayList<PhotoModel>();
//		List<PhotoModel> photos = new ArrayList<PhotoModel>();
//		cursor.moveToLast();
//		do {
//			if (cursor.getLong(cursor.getColumnIndex(ImageColumns.SIZE)) > 1024 * 10) {
//				PhotoModel photoModel = new PhotoModel();
//				photoModel.setOriginalPath(cursor.getString(cursor.getColumnIndex(ImageColumns.DATA)));
//				photos.add(photoModel);
//			}
//		} while (cursor.moveToPrevious());
		return photos;
	}

	/** 获取所有相册列表 */
	public List<AlbumModel> getAlbums(String path1) {
		List<AlbumModel> albums = new ArrayList<AlbumModel>();
		Map<String, AlbumModel> map = new HashMap<String, AlbumModel>();
//		Cursor cursor = resolver.query(Media.EXTERNAL_CONTENT_URI, new String[] { ImageColumns.DATA,
//				ImageColumns.BUCKET_DISPLAY_NAME, ImageColumns.SIZE }, null, null, null);

		/*
		String aaa= Environment.getExternalStorageDirectory().getPath()+"/fise";//"file://"+
//		 File baseFile = new File(a+"/dcim/Camera/15.jpg");
	 
//		 Uri uri =  Uri.fromFile(baseFile) ;
		// Uri uri =  Uri.parse(a) ;
	// 读取SD卡中所有图片
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		String[] projection = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DISPLAY_NAME,
				MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE };
		String selection = MediaStore.Images.Media.MIME_TYPE + "=?";
		String[] selectionArg = { "image/jpeg" };
		Cursor cursor = resolver.query(uri, projection, selection,
				selectionArg, MediaStore.Images.Media.DATE_MODIFIED + " desc");
		int ss =cursor.getCount();
		if (cursor !=null && cursor.getCount() > 0) {
			while(cursor.moveToNext()){
				String name1 = cursor.getString(cursor.getColumnIndex(ImageColumns.DATA));
				if(name1.startsWith(aaa))
				{
					AlbumModel current = new AlbumModel("最近照片", 0, cursor.getString(cursor.getColumnIndex(ImageColumns.DATA)), true); // "最近照片"相册	 
					albums.add(current);
					
					current.increaseCount();
				 
					String name = cursor.getString(cursor.getColumnIndex(ImageColumns.DATA));
					AlbumModel album = new AlbumModel(name, 1, cursor.getString(cursor.getColumnIndex(ImageColumns.DATA)));
					map.put(name, album);
					albums.add(album);
				}

			}
		}
		*/
		
//		if (cursor == null || !cursor.moveToNext())
//			return new ArrayList<AlbumModel>();
//		cursor.moveToLast();
//		AlbumModel current = new AlbumModel("最近照片", 0, cursor.getString(cursor.getColumnIndex(ImageColumns.DATA)), true); // "最近照片"相册
//		albums.add(current);
//		do {
//			if (cursor.getInt(cursor.getColumnIndex(ImageColumns.SIZE)) < 1024 * 10)
//				continue;
//
//			current.increaseCount();
//			String name = cursor.getString(cursor.getColumnIndex(ImageColumns.BUCKET_DISPLAY_NAME));
//			if (map.keySet().contains(name))
//				map.get(name).increaseCount();
//			else {
//				AlbumModel album = new AlbumModel(name, 1, cursor.getString(cursor.getColumnIndex(ImageColumns.DATA)));
//				map.put(name, album);
//				albums.add(album);
//			}
//		} while (cursor.moveToPrevious());
		File path =  new File(path1);
		File[] files = path.listFiles();

		if (files != null) {// 先判断目录是否为空，否则会报空指针
			for (File file : files) {
				String fileName = file.getName();
				if (fileName.endsWith(".jpg")) {
				//	String name = fileName.substring(0, fileName.lastIndexOf("."))
					//		.toString(); 
					String name = file.getAbsolutePath();
					AlbumModel album = new AlbumModel(name, 1, name);
					map.put(name, album);
					albums.add(album);
				}
			}
		}
        
		return albums;
	}

	/** 获取对应相册下的照片 */
	public List<PhotoModel> getAlbum(String name,String path) {
		Cursor cursor = resolver.query(Media.EXTERNAL_CONTENT_URI, new String[] { ImageColumns.BUCKET_DISPLAY_NAME,
				ImageColumns.DATA, ImageColumns.DATE_ADDED, ImageColumns.SIZE }, "bucket_display_name = ?",
				new String[] { name }, ImageColumns.DATE_ADDED);
		if (cursor == null || !cursor.moveToNext())
			return new ArrayList<PhotoModel>();
		List<PhotoModel> photos = new ArrayList<PhotoModel>();
		cursor.moveToLast();
		do {
			if (cursor.getLong(cursor.getColumnIndex(ImageColumns.SIZE)) > 1024 * 10) {
				PhotoModel photoModel = new PhotoModel();
				photoModel.setOriginalPath(cursor.getString(cursor.getColumnIndex(ImageColumns.DATA)));
				photos.add(photoModel);
			}
		} while (cursor.moveToPrevious());
		return photos;
	}
}
