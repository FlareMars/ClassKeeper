package com.flaremars.classmanagers.handler;

import android.util.Log;

import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessageHandler;
import com.avos.avoscloud.im.v2.messages.AVIMFileMessage;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.model.MsgType;
import com.flaremars.classmanagers.model.PictureObject;
import com.flaremars.classmanagers.model.SubClassObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.p2msg.ProcessChattingMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.List;

/**
 * 处理聊天消息handler
 */
public class CMMessageHandler extends AVIMTypedMessageHandler<AVIMTypedMessage> {
    @Override
    public void onMessage(AVIMTypedMessage message, AVIMConversation conversation, AVIMClient client) {
        Log.e("TAG","onMessage : " + message.getFrom() + " " + conversation.getConversationId());
        JSONObject content = new JSONObject();
        if (!client.getClientId().equals(MainActivity.BASE_GLOBAL_DATA.getUserID())) {
            return;
        }
        try {
            content.put(MessageConst.CONTENT_FROM_ID,message.getFrom());
            List<SubClassObject> check = DataSupport.where("subClassID=?",conversation.getConversationId()).find(SubClassObject.class);

            content.put(MessageConst.CONTENT_IN_SUB_CLASS, check.size() == 0? "": conversation.getConversationId());
            Integer type = 0;
            String inClass = "";
            switch(AVIMReservedMessageType.getAVIMReservedMessageType(message.getMessageType())) {
                case TextMessageType:
                    AVIMTextMessage textMsg = (AVIMTextMessage)message;
//                    Log.d("CMMessageHandler", "收到文本消息:" + textMsg.getText() + ", msgId:" + textMsg.getMessageId());
                    type = (Integer)textMsg.getAttrs().get("type");
                    inClass = (String)textMsg.getAttrs().get("inClass");
                    if (type == MsgType.EXCEL_OTHERS.ordinal()) {
                        content.put(MessageConst.CONTENT_MSG,"excel_task:"+textMsg.getText());
                    } else {
                        content.put(MessageConst.CONTENT_MSG,"normal:"+textMsg.getText());
                    }
                    break;

                case FileMessageType:
                    AVIMFileMessage fileMsg = (AVIMFileMessage)message;
//                    Log.d("CMMessageHandler","收到文件消息。msgId=" + fileMsg.getMessageId() + ", url=" + fileMsg.getFileUrl());
                    type = (Integer)fileMsg.getAttrs().get("type");
                    inClass = (String)fileMsg.getAttrs().get("inClass");
                    String fileName = (String) fileMsg.getAttrs().get("fileName");
                    content.put(MessageConst.CONTENT_MSG,"file:" + fileMsg.getAVFile().getObjectId() + "&" + fileName);
                    break;

                case ImageMessageType:
                    AVIMImageMessage imageMsg = (AVIMImageMessage)message;
//                    Log.d("CMMessageHandler","收到图片消息。msgId=" + imageMsg.getMessageId() + ", url=" + imageMsg.getFileUrl() + ", width=" + imageMsg.getWidth() + ", height=" + imageMsg.getHeight());
                    type = (Integer)imageMsg.getAttrs().get("type");
                    inClass = (String)imageMsg.getAttrs().get("inClass");

                    //创建一个PictureObject对象
                    AVFile photo = imageMsg.getAVFile();
                    PictureObject pictureObject = new PictureObject();
                    pictureObject.setName(photo.getOriginalName());
                    pictureObject.setType(2);
                    pictureObject.setPictureId(photo.getObjectId());
                    pictureObject.setThumbnail(false);
                    pictureObject.setHeight(imageMsg.getHeight());
                    pictureObject.setWidth(imageMsg.getWidth());
                    pictureObject.setTime(new Date().getTime());
                    pictureObject.save();
                    content.put(MessageConst.CONTENT_MSG,"picture:" + pictureObject.getPictureId());
                    break;

//            case AudioMessageType:
//                AVIMAudioMessage audioMsg = (AVIMAudioMessage)message;
//                Log.d("收到音频消息。msgId=" + audioMsg.getMessageId() + ", url=" + audioMsg.getFileUrl() + ", duration=" + audioMsg.getDuration());
//                break;
                default:
            }

            content.put(MessageConst.CONTENT_TYPE, type);
            content.put(MessageConst.CONTENT_IN_CLASS,inClass);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        UserObject userObject = new UserObject();
        userObject.setUserId(MainActivity.BASE_GLOBAL_DATA.getUserID());
        new ProcessChattingMessage().process(content, CMApplication.GLOBAL_CONTEXT,userObject);
    }
}
