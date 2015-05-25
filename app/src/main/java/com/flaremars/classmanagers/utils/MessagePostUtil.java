package com.flaremars.classmanagers.utils;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVPush;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.SendCallback;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.model.DataWrapper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 网络数据的处理，缓存队列，发出，重发，广播发出消息
 */
public enum MessagePostUtil {
    INSTANCE;

    public static final String MESSAGE_POST_FINISHED = "com.flaremars.classmanagers.action.message_post_finished";

    //从初始化之后自增的消息队列序号，用户分发广播id
    private int messageQueueNum;

    private SparseArray<DataWrapper> messageArray;

    public void init() {
        messageQueueNum = 0;
        if (messageArray != null) {
            messageArray.clear();
        }
        messageArray = new SparseArray<>(100);

    }

    private class PostMessageTaskForLeanCloud extends AsyncTask<Integer,Integer,Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            final DataWrapper data = messageArray.get(params[0]);
            if (data == null) {
                Log.e("TAG", "out of expectation~data is null");
            } else {
                AVQuery<AVInstallation> pushQuery = AVInstallation.getQuery();
                if (data.getTargetIds().size() == 1) {
                    pushQuery.whereEqualTo("userId", data.getTargetIds().get(0));
                } else {
                    pushQuery.whereContainedIn("userId", data.getTargetIds());
                }
                JSONObject jsonObject = data.getContent();
                try {
                    jsonObject.put("action","cm.action.MESSAGE");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                AVPush.sendDataInBackground(jsonObject, pushQuery, new SendCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e != null) {
                            NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT, e);
                        }
                    }
                });
                publishProgress(params[0]);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int messageId = values[0];
            Intent messagePostFinished = new Intent(MESSAGE_POST_FINISHED);
            messagePostFinished.putExtra("message_id",messageId);
            CMApplication.GLOBAL_CONTEXT.sendBroadcast(messagePostFinished);

            messageArray.remove(messageId);
        }
    }

    /**
     * 将待发送数据放到缓存数组中
     * @param data 待发送数据
     * @return 此次发送的流水id
     */
    public int wannaPostMessage(DataWrapper data) {
        messageArray.put(++messageQueueNum,data);
        toPostMessages(messageQueueNum);
        return messageQueueNum;
    }

    private boolean isHandling = false;

    /**
     * 尝试将消息缓存数据中的数据重发,考虑使用定时算法
     */
    public void postAllInMsgArray() {
        if (!NormalUtils.INSTANCE.isNetworkRegularWork(CMApplication.GLOBAL_CONTEXT)) {
            NormalUtils.INSTANCE.showToast(CMApplication.GLOBAL_CONTEXT,"当前网络不可用，请检查网络配置");
        } else {
            if (!isHandling) {
                isHandling = true;
                int size = messageArray.size();
                for (int i = 0;i < size;i++) {
                    int key = messageArray.keyAt(i);
                    toPostMessages(key);
                }
                isHandling = false;
            }
        }
    }

    /*将指定消息缓存数组中的数据发出*/
    private void toPostMessages(int messageQueueNum) {
        if (!NormalUtils.INSTANCE.isNetworkRegularWork(CMApplication.GLOBAL_CONTEXT)) {
            NormalUtils.INSTANCE.showToast(CMApplication.GLOBAL_CONTEXT,"当前网络不可用，请检查网络配置");
        } else {
            new PostMessageTaskForLeanCloud().execute(messageQueueNum);
        }
    }
}
