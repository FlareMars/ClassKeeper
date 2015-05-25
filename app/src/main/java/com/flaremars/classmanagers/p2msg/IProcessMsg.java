package com.flaremars.classmanagers.p2msg;

import android.content.Context;

import com.flaremars.classmanagers.model.UserObject;

import org.json.JSONObject;

/**
 * 分析处理网络消息的接口
 */
public interface IProcessMsg {
    void process(JSONObject content,Context context,UserObject thisUser);
}
