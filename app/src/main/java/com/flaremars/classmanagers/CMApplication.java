package com.flaremars.classmanagers;

import android.util.Log;
import android.widget.Toast;

import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.flaremars.classmanagers.handler.CMConversationHandler;
import com.flaremars.classmanagers.handler.CMMessageHandler;
import com.flaremars.classmanagers.networkmodel.ICMAlbum;
import com.flaremars.classmanagers.utils.BitmapUtils;
import com.flaremars.classmanagers.utils.FlareBitmapUtils;
import com.flaremars.classmanagers.utils.MessagePostUtil;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.utils.NotificationUtils;

import org.litepal.LitePalApplication;

/**
 * 全局Context
 */
public class CMApplication extends LitePalApplication{

    public static CMApplication GLOBAL_CONTEXT;

    public static AVIMClient imClient;

    private static boolean hasToast = false;

    public static boolean isClientOpened = false;

    @Override
    public void onCreate() {
        super.onCreate();
        GLOBAL_CONTEXT = this;
        isClientOpened = false;
        FlareBitmapUtils.INSTANCE.init(this);
        BitmapUtils.INSTANCE.initBitmapUtils(this);
        AVObject.registerSubclass(ICMAlbum.class);

        AVOSCloud.setNetworkTimeout(5000);
//        AVCloud.setProductionMode(false); //调用测试环境云代码
        AVOSCloud.setDebugLogEnabled(true);
        AVOSCloud.initialize(this, "q77fhkht4neg4ixnybwjnjmodatcoxy4wplq6ocb9lrzy5hs", "vhvdk35bg5p6zsxdsp5boqz2hckljc2djbc7c12834bdj5mv");

        AVIMMessageManager.registerMessageHandler(AVIMTypedMessage.class, new CMMessageHandler());
        AVIMMessageManager.setConversationEventHandler(new CMConversationHandler());
        AVUser user = AVUser.getCurrentUser();
        if (user != null) {
            openIMClient(user.getObjectId());
        }
        MessagePostUtil.INSTANCE.init();
        NotificationUtils.INSTANCE.init(this);
    }

    public static void openIMClient(final String userId) {
        if (isClientOpened) {
            isClientOpened = false;
            imClient.close(new AVIMClientCallback() {
                @Override
                public void done(AVIMClient avimClient, AVException e) {

                }
            });
        }

        if (!isClientOpened) {
            imClient = AVIMClient.getInstance(userId);
            imClient.open(new AVIMClientCallback() {
                @Override
                public void done(AVIMClient avimClient, AVException e) {
                    if (e != null) {
                        if (!NormalUtils.INSTANCE.isNetworkRegularWork(CMApplication.GLOBAL_CONTEXT)) {
                            return;
                        }
                        Log.e("TAG", e.getMessage());
                        if (!hasToast) {
                            hasToast = true;
                            Toast.makeText(GLOBAL_CONTEXT, "当前网络状态不佳,请检查网络配置", Toast.LENGTH_SHORT).show();
                        }
                        openIMClient(userId);
                    } else {
                        Log.e("TAG", "IMClient Open Success~ : " + avimClient.getClientId());
                        isClientOpened = true;
                    }
                }
            });
        }
    }


}
