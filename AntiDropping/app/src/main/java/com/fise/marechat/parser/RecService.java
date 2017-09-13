package com.fise.marechat.parser;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.fise.marechat.R;
import com.fise.marechat.bean.msg.TcpMsg;
import com.fise.marechat.constant.ActivityResultConstants;
import com.fise.marechat.function.photo.PhotoModel;
import com.fise.marechat.util.ToastUtils;
import com.foamtrace.photopicker.ImageCaptureManager;
import com.foamtrace.photopicker.SelectModel;
import com.foamtrace.photopicker.intent.PhotoPickerIntent;
import com.hss01248.lib.MyItemDialogListener;
import com.hss01248.lib.StytledDialog;

import org.greenrobot.greendao.annotation.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanyang on 2017/8/8.
 */

public class RecService {
    private final int SELECT_IMAGE_CODE = 1001;

    public String str_choosed_img = "";
    public List<PhotoModel> selected;
    public final int MAX_PHOTOS = 1;
    public Dialog dialog_choose_img_way;// 选择图片
    private ArrayList<String> imagePaths = null;
    private ImageCaptureManager captureManager = null; // 相机拍照处理类

    /**
     * 解析收到的消息
     *
     * @param msg [SG*123456789123459*000F*Time,1502178194]
     */
    public TcpMsg handleRecvMsg(TcpMsg msg) {
        return null;
    }

    /**
     * 发送消息解析
     *
     * @param ctx
     * @param msgType
     * @return
     */
    public void handleSendMsg(Activity ctx, String msgType) {
    }

    /**
     * 获取消息头部类型
     *
     * @param msg [SG*123456789123459*000F*Time,1502178194]
     * @return String SG*123456789123459*000F*Time;
     */
    public String getMsgHeader(TcpMsg msg) {
        return null;
    }

    /**
     * 获取消息内容
     *
     * @param msg [SG*123456789123459*000F*Time,1502178194]
     * @return String 1502178194
     */
    public String getMsgContent(TcpMsg msg) {
        return null;
    }

    /**
     * 获取要发送的消息内容
     *
     * @param msg [SG*123456789123459*000F*Time,1502178194]
     * @return String 1502178194
     */
    public String getReplyMsgContent(TcpMsg msg) {
        return null;
    }

    public boolean isContextNull(Context ctx) {
        return null == ctx;
    }

    public void showDialog(Context ctx, List<String> strings, final TcpMsg msg, @NotNull final DialogItemSelectedListener listener) {
        StytledDialog.showIosSingleChoose(ctx, strings, true, true, new MyItemDialogListener() {

            @Override
            public void onItemClick(String text, int position) {
                ToastUtils.showShort("item" + text + " -- " + position);
                listener.onItemSelected(text, msg, position);
            }

            @Override
            public void onBottomBtnClick() {
                ToastUtils.showShort("bottom click");
            }
        }).show();
    }

    public interface DialogItemSelectedListener {
        public void onItemSelected(String text, TcpMsg header, int position);
    }

    /**
     * 选择图片上传的方式
     *
     * @param ctx
     */
    public void showPhotoChooser(final Activity ctx) {
        selected = new ArrayList<PhotoModel>();
        dialog_choose_img_way = new Dialog(ctx, R.style.IosBottomSheetDialogStyle);
        dialog_choose_img_way.setContentView(R.layout.dialog_choose_picker);
        dialog_choose_img_way.setCanceledOnTouchOutside(true);
        dialog_choose_img_way.findViewById(R.id.other_view).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog_choose_img_way.cancel();
            }
        });
        dialog_choose_img_way.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog_choose_img_way.cancel();
            }
        });
        // 拍照上传
        dialog_choose_img_way.findViewById(R.id.choose_by_camera).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog_choose_img_way.cancel();
//                if (selected.size() > MAX_PHOTOS) {
//                    Toast.makeText(MainActivity.this, "最多上传" + MAX_PHOTOS + "张", Toast.LENGTH_SHORT).show();
//                } else {
//                    Util.selectPicFromCamera(MainActivity.this);
//                }
            }
        });
        // 本地上传
        dialog_choose_img_way.findViewById(R.id.choose_by_local).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog_choose_img_way.cancel();
                startSingleSelect(ctx);

            }
        });
        dialog_choose_img_way.show();
    }

    private void startSingleSelect(Activity ctx) {
        PhotoPickerIntent intent = new PhotoPickerIntent(ctx);
        intent.setSelectModel(SelectModel.SINGLE);
        intent.setShowCarema(true);
        ctx.startActivityForResult(intent, ActivityResultConstants.REQUEST_CAMERA_CODE);
    }

    private void startMuiltSelect(Activity ctx) {
        PhotoPickerIntent intent = new PhotoPickerIntent(ctx);
        intent.setSelectModel(SelectModel.MULTI);
        intent.setShowCarema(true); // 是否显示拍照
        intent.setMaxTotal(9); // 最多选择照片数量，默认为9
        intent.setSelectedPaths(imagePaths); // 已选中的照片地址， 用于回显选中状态
        ctx.startActivityForResult(intent, ActivityResultConstants. REQUEST_CAMERA_CODE);
    }

    public void startPhotoTaken(Activity ctx) {

        try {
            if (captureManager == null) {
                captureManager = new ImageCaptureManager(ctx);
            }
            Intent intent = captureManager.dispatchTakePictureIntent();
            ctx.startActivityForResult(intent, ActivityResultConstants.REQUEST_TAKE_PHOTO);
        } catch (IOException e) {
            Toast.makeText(ctx, com.foamtrace.photopicker.R.string.msg_no_camera, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public ImageCaptureManager getImageCaptureManager(){
        return captureManager;
    }
}
