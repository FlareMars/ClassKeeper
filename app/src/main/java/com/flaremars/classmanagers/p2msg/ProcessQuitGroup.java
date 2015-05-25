package com.flaremars.classmanagers.p2msg;

import android.content.Context;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetCallback;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.model.RecentMessageObject;
import com.flaremars.classmanagers.model.SubClassObject;
import com.flaremars.classmanagers.model.UserObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.Collections;
import java.util.List;

/**
 * 处理退群请求
 */
public class ProcessQuitGroup implements IProcessMsg {
    @Override
    public void process(JSONObject content, final Context context, UserObject thisUser) {
        try {
            final String targetId = content.getString(MessageConst.CONTENT_TARGET_ID);
            List<SubClassObject> subClasses = DataSupport.where("subClassID=?", targetId).find(SubClassObject.class);
            if (subClasses.size() == 0) {
                return;
            }
            final SubClassObject targetSubClass = subClasses.get(0);
            List<RecentMessageObject> recentMessageObjects = DataSupport.where("targetID=? and type=2",
                    String.valueOf(targetSubClass.getId())).
                    find(RecentMessageObject.class);
            if (recentMessageObjects.size() > 0) {
                DataSupport.delete(RecentMessageObject.class, recentMessageObjects.get(0).getId());
            }

            AVQuery<AVUser> query = AVUser.getQuery();
            query.getInBackground(MainActivity.BASE_GLOBAL_DATA.getUserID(), new GetCallback<AVUser>() {
                @Override
                public void done(AVUser avUser, AVException e) {
                    if (e == null) {
                        avUser.removeAll("subClassIds", Collections.singletonList(targetId));
                        avUser.saveInBackground();
                    }
                }
            });

            DataSupport.delete(SubClassObject.class, targetSubClass.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
