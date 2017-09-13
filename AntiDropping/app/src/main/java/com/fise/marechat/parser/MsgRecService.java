package com.fise.marechat.parser;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.fise.marechat.bean.dao.CenterSettings;
import com.fise.marechat.bean.dao.MessagePhrase;
import com.fise.marechat.bean.dao.PhoneBook;
import com.fise.marechat.bean.msg.TcpMsg;
import com.fise.marechat.client.GlobalSettings;
import com.fise.marechat.client.msg.MsgInOut;
import com.fise.marechat.client.msg.MsgType;
import com.fise.marechat.manager.MsgManager;
import com.fise.marechat.prenster.dao.CenterSettingsUtils;
import com.fise.marechat.prenster.dao.PhoneBookUtils;
import com.fise.marechat.utils.DigitalConvert;
import com.fise.marechat.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.fise.marechat.client.msg.MsgType.Time;

/**
 * 处理接收的信息
 * Created by fanyang on 2017/8/8.
 */
public class MsgRecService extends RecService implements RecService.DialogItemSelectedListener {

    /**
     * 直接发送获取do other task
     */
    private enum TypeIntent {
        DIRECTLY_SENT, OTHER_TASK
    }

    private static final String NULL_CONTENT = "null content";
    private static final String AL = "180916,064153,A,22.570512,N,113.8623267,E,0.00,154.8,0.0,11,10\n" +
            "0,100,0,0,00100018,7,0,460,1,9529,21809,155,9529,21242,132,9529,21405,131,9529,63554,\n" +
            "131,9529,63555,130,9529,63556,118,9529,21869,116,0,12.4";
    private static final String AL_CDMA = "180916,064153,A,22.570512,N,113.8623267,E,0.00,154.8,0.0,\n" +
            "11,100,100,0,0,00100018,,,,70";
    private static final String WT = "260916,020049,V,22.683546,N,113.9907380,E,0.00,0.0,0.0,0,100,7\n" +
            "7,0,0,00000000,5,0,460,0,9346,4711,167,9346,4712,126,9360,4151,125,9346,4713,122,9360,\n" +
            "4081,119";
    private static final String WT2 = "260916,020049,V,22.683546,N,113.9907380,E,0.00,0.0,0.0,0,100,\n" +
            "77,0,0,00000000,5,0,460,0,9346,4711,167,9346,4712,126,9360,4151,125,9346,4713,122,936\n" +
            "0,4081,119";
    private static final String VOICE = "5,160429110950,";

    private MsgRecService() {

    }

    @Override
    public void onItemSelected(String text, TcpMsg msg, int position) {
        String header = msg.getHeader();
        boolean isBlank = TextUtils.isEmpty(text) || NULL_CONTENT.equals(text);
        msg.setContent(isBlank ? null : text);
        msg.setTime();
        LogUtils.e(msg.toString());
        MsgManager.instance().sendCompleteMsg(header, text);
    }

    private static class SingletonHolder {
        private static final MsgRecService INSTANCE = new MsgRecService();
    }

    public static MsgRecService instance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public TcpMsg handleRecvMsg(TcpMsg msg) {
        String completeMsg = msg.getSourceDataString();
        String[] headerContent = MsgParser.instance().getHeaderAndContent(completeMsg);
        String header = headerContent[0];
        String content = null;
        String replyCcntent = null;
        if (null == headerContent) return msg;
        if (headerContent.length > 1) {
            content = headerContent[1];
        }
        boolean hasContent = null == content;
        boolean needReply = false;
        boolean rightgNow = true;//接收完后是否立即回复
        String headerType = MsgParser.instance().parseHeaderTypeByHeader(header);
        String replyHeaderType = "";
        TcpMsg replyMsg = null;
        MsgInOut inOut = msg.getMsgInOut();//default
        switch (headerType) {
            case MsgType.LK:
                break;
            case MsgType.UD:
                replyCcntent = "180916,025723,A,22.570733,N,113.8626083,E,0.00,249.5,0.0,6,100,\n" +
                        "60,0,0,00000010,7,255,460,1,9529,21809,158,9529,63555,133,9529,63554,129,9529,21405,1\n" +
                        "26,9529,21242,124,9529,21151,120,9529,63556,119,0";
                needReply = true;
                break;
            case MsgType.UD_CDMA:
                break;
            case MsgType.UD2:
                break;
            case MsgType.UD_CDMA2:
                break;
            case MsgType.AL:
                break;
            case MsgType.AL_CDMA:
                break;
            case Time:
                break;
            case MsgType.WT:
                break;
            case MsgType.WT2:
                break;
            case MsgType.img:
                break;
            case MsgType.VOICE:
                break;
            case MsgType.UPLOAD:
                break;
            /*中心号码设置*/
            case MsgType.CENTER:
                //存入数据库
                CenterSettings settings = new CenterSettings(GlobalSettings.instance().getImei());
                settings.setCenterPhoneNum(content);
                CenterSettingsUtils.instance().updateCenterPhoneNum(settings);
                needReply = true;
                rightgNow = true;
                break;
            case MsgType.PW:
                break;
            case MsgType.CALL:
                break;
            case MsgType.MONITOR:
                break;
            case MsgType.SOS:
                break;
            case MsgType.SOS3:
                break;
            case MsgType.IP:
                break;
            case MsgType.FACTORY:
                break;
            case MsgType.LZ:
                break;
            case MsgType.SOSSMS:
                break;
            case MsgType.LOWBAT:
                break;
            case MsgType.VERNO:
                break;
            case MsgType.RESET:
                break;
            case MsgType.CR:
                break;
            case MsgType.POWEROFF:
                break;
            case MsgType.REMOVE:
                break;
            case MsgType.REMOVESMS:
                break;
            case MsgType.PEDO:
                break;
            case MsgType.WALKTIME:
                break;
            case MsgType.ANY:
                break;
            case MsgType.SLEEPTIME:
                break;
            case MsgType.SILENCETIME:
                break;
            case MsgType.FIND:
                break;
            case MsgType.FLOWER:
                break;
            case MsgType.REMIND:
                break;
            case MsgType.TK:
                break;
            case MsgType.TKQ:
                break;
            case MsgType.WHITELIST2:
                break;
            case MsgType.MESSAGE:
                //存入数据库
                String recvMsg = DigitalConvert.hexNone0x2String(content);
                String sourceFrom = msg.getSourceFrom();
                PhoneBook phoneBook = new PhoneBook(GlobalSettings.instance().getImei());
                long messagePhrase_id = Long.parseLong(phoneBook.getImei());
                MessagePhrase messagePhrase = new MessagePhrase(messagePhrase_id);
                messagePhrase.setContent(content);
                messagePhrase.setTime();
                messagePhrase.setMessage_sourceFrom(sourceFrom);
                List<MessagePhrase> phrases = Arrays.asList(new MessagePhrase[]{messagePhrase});
                PhoneBookUtils.instance().updatePhraseMsg(phoneBook);
                needReply = true;
                rightgNow = true;
                break;
            case MsgType.WHITELIST1:
                break;
            case MsgType.PHB:
                break;
            case MsgType.PHB2:
                break;
            case MsgType.PHL:
                break;
            case MsgType.profile:
                break;
            case MsgType.FALLDOWN:
                break;
            case MsgType.rcapture:
                //执行拍照
                needReply = true;
                replyHeaderType = MsgType.img;
                //触发img指令
                break;
            case MsgType.hrtstart:
                break;
            case MsgType.heart:
                break;
            default:
                break;
        }
        msg.setNeedReply(needReply);
        if (needReply) {
            String dataString = MsgParser.instance().composedTypeContent(replyHeaderType, replyCcntent);
            replyMsg = new TcpMsg(replyHeaderType, replyCcntent, dataString, MsgInOut.Send);
        } else {
            replyMsg = msg;
        }
        return replyMsg;
    }

    @Override
    public void handleSendMsg(Activity ctx, String msgType) {
        String content = null;
        TcpMsg msg = new TcpMsg();
        MsgInOut inOut = MsgInOut.Send;
        String header = null;
        List<String> contentPickers = new ArrayList<>();
        TypeIntent typeIntent = TypeIntent.DIRECTLY_SENT;
        switch (msgType) {
            case MsgType.LK:
                //两种情况
                contentPickers.add(NULL_CONTENT);
                contentPickers.add("[CS*YYYYYYYYYY*LEN*LK,50,100,100]");//步数,翻滚次数,电量百分数

                break;
            case MsgType.UD:
                contentPickers.add("180916,025723,A,22.570733,N,113.8626083,E,0.00,249.5,0.0,6,100,\n" +
                        "60,0,0,00000010,7,255,460,1,9529,21809,158,9529,63555,133,9529,63554,129,9529,21405,1\n" +
                        "26,9529,21242,124,9529,21151,120,9529,63556,119,0");
                break;
            case MsgType.UD_CDMA:
                contentPickers.add("180916,025723,A,22.570733,N,113.8626083,E,0.00,249.5,0.0,\n" +
                        "6,100,60,0,0,00000010,,,,60");
                break;
            case MsgType.UD2:
                contentPickers.add("180916,064032,A,22.570512,N,113.8623267,E,0.00,154.8,0.0,11,1\n" +
                        "00,100,0,0,00000010,7,255,460,1,9529,21809,157,9529,21405,131,9529,63555,130,9529,212\n" +
                        "42,129,9529,63554,126,9529,63556,120,9529,21151,113,0,12.2");
                break;
            case MsgType.UD_CDMA2:
                contentPickers.add("180916,025723,A,22.570733,N,113.8626083,E,0.00,249.5,0.0,\n" +
                        "6,100,60,0,0,00000010,,,,80");
                break;
            case MsgType.AL:
                contentPickers.add(AL);
                break;
            case MsgType.AL_CDMA:
                contentPickers.add(AL_CDMA);
                break;
            case Time:
                contentPickers.add(this.NULL_CONTENT);
                break;
            case MsgType.WT:
                contentPickers.add(this.WT);
                break;
            case MsgType.WT2:
                contentPickers.add(this.WT2);
                break;
            case MsgType.img:
                typeIntent = TypeIntent.OTHER_TASK;
                StringBuffer img = new StringBuffer("5,160429110950,");
                showPhotoChooser(ctx);
//                String img
//                img.append()
//                contentPickers.add(this.img);
                break;
            case MsgType.VOICE:
                contentPickers.add(this.VOICE);
                break;
            case MsgType.UPLOAD:
                break;
            /*中心号码设置*/
            case MsgType.CENTER:
                //存入数据库
                CenterSettings settings = new CenterSettings(GlobalSettings.instance().getImei());
                settings.setCenterPhoneNum(content);
                CenterSettingsUtils.instance().updateCenterPhoneNum(settings);
                break;
            case MsgType.PW:
                break;
            case MsgType.CALL:
                break;
            case MsgType.MONITOR:
                break;
            case MsgType.SOS:
                break;
            case MsgType.SOS3:
                break;
            case MsgType.IP:
                break;
            case MsgType.FACTORY:
                break;
            case MsgType.LZ:
                break;
            case MsgType.SOSSMS:
                break;
            case MsgType.LOWBAT:
                break;
            case MsgType.VERNO:
                break;
            case MsgType.RESET:
                break;
            case MsgType.CR:
                break;
            case MsgType.POWEROFF:
                break;
            case MsgType.REMOVE:
                break;
            case MsgType.REMOVESMS:
                break;
            case MsgType.PEDO:
                break;
            case MsgType.WALKTIME:
                break;
            case MsgType.ANY:
                break;
            case MsgType.SLEEPTIME:
                break;
            case MsgType.SILENCETIME:
                break;
            case MsgType.FIND:
                break;
            case MsgType.FLOWER:
                break;
            case MsgType.REMIND:
                break;
            case MsgType.TK:
                break;
            case MsgType.TKQ:
                break;
            case MsgType.WHITELIST2:
                break;
            case MsgType.MESSAGE:
                //存入数据库
                String recvMsg = DigitalConvert.hexNone0x2String(content);
                String sourceFrom = msg.getSourceFrom();
                PhoneBook phoneBook = new PhoneBook(GlobalSettings.instance().getImei());
                long messagePhrase_id = Long.parseLong(phoneBook.getImei());
                MessagePhrase messagePhrase = new MessagePhrase(messagePhrase_id);
                messagePhrase.setContent(content);
                messagePhrase.setTime();
                messagePhrase.setMessage_sourceFrom(sourceFrom);
                List<MessagePhrase> phrases = Arrays.asList(new MessagePhrase[]{messagePhrase});
                PhoneBookUtils.instance().updatePhraseMsg(phoneBook);
                break;
            case MsgType.WHITELIST1:
                break;
            case MsgType.PHB:
                break;
            case MsgType.PHB2:
                break;
            case MsgType.PHL:
                break;
            case MsgType.profile:
                break;
            case MsgType.FALLDOWN:
                break;
            case MsgType.rcapture:
                //执行拍照
                //....
                //触发img指令
                break;
            case MsgType.hrtstart:
                break;
            case MsgType.heart:
                break;
            default:
                break;
        }
        msg.setHeaderType(msgType);
        header = MsgParser.instance().getHeaderByType(msgType);
        msg.setHeader(header);
        msg.setContent(content);
        msg.setMsgInOut(inOut);
        msg.setTime();
        switch (typeIntent) {
            case DIRECTLY_SENT:
                showDialog(ctx, contentPickers, msg, this);
                break;
            case OTHER_TASK:

                break;

            default:
                break;
        }

    }

    @Override
    public String getMsgHeader(TcpMsg msg) {
        return null;
    }

    @Override
    public String getMsgContent(TcpMsg msg) {
        return null;
    }

    @Override
    public String getReplyMsgContent(TcpMsg msg) {
        return null;
    }

    private void showPhotoPickerDialog() {
        //showChooseIMG_WAYDialog
    }
}
