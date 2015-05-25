package com.flaremars.classmanagers.p2msg;

import android.content.Context;

import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.ManagerObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.model.MessageConst;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

/**
 * 处理被取消管理员资格
 */
public class ProcessRemoveManager implements IProcessMsg {
    @Override
    public void process(JSONObject content, Context context, UserObject thisUser) {
        try {
            ClassObject target = DataSupport.where("classID=?", content.getString(MessageConst.CONTENT_IN_CLASS)).
                    find(ClassObject.class).get(0);
            //由于非创建者管理员的数据对其无用，所以可以直接清空
            DataSupport.deleteAll(ManagerObject.class, "classobject_id=?", String.valueOf(target.getId()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
