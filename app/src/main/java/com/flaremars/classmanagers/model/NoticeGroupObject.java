package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知组实体类
 */
public class NoticeGroupObject extends DataSupport{
    private int id;

    private String name;

    private String noticeGroupID;

    private ClassObject inClass;

    private boolean isAllMember;

    private boolean isCustomName;

    //如果为一对一关系，那么只要删除一方，另一方都会被删除
    private List<RecentNoticeGroupMsg> recentNoticeGroupMsgs;

//    private List<UserObject> members = new ArrayList<>();

    private List<NoticeGroupMemberObject> members = new ArrayList<>();

    private List<ExcelTaskObject> excelTaskObjects = new ArrayList<>();

    private List<NoticeTaskObject> noticeTaskObjects = new ArrayList<>();

    public boolean isCustomName() {
        return isCustomName;
    }

    public void setCustomName(boolean isCustomName) {
        this.isCustomName = isCustomName;
    }

    public boolean isAllMember() {
        return isAllMember;
    }

    public void setAllMember(boolean isAllMember) {
        this.isAllMember = isAllMember;
    }

    public List<ExcelTaskObject> getExcelTaskObjectList() {
        return DataSupport.where("noticegroupobject_id=?",String.valueOf(id)).find(ExcelTaskObject.class);
    }

    public List<NoticeTaskObject> getNoticeTaskObjectList() {
        return DataSupport.where("noticegroupobject_id=?",String.valueOf(id)).find(NoticeTaskObject.class);
    }

    public List<ExcelTaskObject> getExcelTaskObjects() {
        return excelTaskObjects;
    }

    public void setExcelTaskObjects(List<ExcelTaskObject> excelTaskObjects) {
        this.excelTaskObjects = excelTaskObjects;
    }

    public List<NoticeTaskObject> getNoticeTaskObjects() {
        return noticeTaskObjects;
    }

    public void setNoticeTaskObjects(List<NoticeTaskObject> noticeTaskObjects) {
        this.noticeTaskObjects = noticeTaskObjects;
    }

    public List<NoticeGroupMemberObject> getMemberList() {
        return DataSupport.where("noticegroupobject_id=?",String.valueOf(id)).find(NoticeGroupMemberObject.class);
    }

    public List<NoticeGroupMemberObject> getMembers() {
        return members;
    }

    public void setMembers(List<NoticeGroupMemberObject> members) {
        this.members = members;
    }

    public List<RecentNoticeGroupMsg> getRecentNoticeGroupMsgs() {
        return recentNoticeGroupMsgs;
    }

    public void setRecentNoticeGroupMsgs(List<RecentNoticeGroupMsg> recentNoticeGroupMsgs) {
        this.recentNoticeGroupMsgs = recentNoticeGroupMsgs;
    }

    /*
        public RecentNoticeGroupMsg getRecentNoticeGroupMsg() {
            return recentNoticeGroupMsg;
        }

        public void setRecentNoticeGroupMsg(RecentNoticeGroupMsg recentNoticeGroupMsg) {
            this.recentNoticeGroupMsg = recentNoticeGroupMsg;
        }
        */

    public ClassObject getInClass() {
        return inClass;
    }

    public void setInClass(ClassObject inClass) {
        this.inClass = inClass;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNoticeGroupID() {
        return noticeGroupID;
    }

    public void setNoticeGroupID(String noticeGroupID) {
        this.noticeGroupID = noticeGroupID;
    }
/*
    public List<UserObject> getMembers() {
        Cursor cursor = DataSupport.findBySQL("select userobject_id from noticegroupobject_userobject where noticegroupobject_id=?",String.valueOf(id));
        List<Long> ids = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(0));
            }
        }
        long[] longIds = new long[ids.size()];
        for (int i = 0;i < ids.size();i++) {
            longIds[i] = ids.get(i);
        }
        members = DataSupport.findAll(UserObject.class,longIds);
        return members;
    }

    public void setMembers(List<UserObject> members) {
        this.members = members;
    }
*/
}
