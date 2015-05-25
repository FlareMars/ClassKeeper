package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 班级实体类
 */
public class ClassObject extends DataSupport{
    private int id;

    private String name;

    private String classID;

    private String headerPath;

    private int totalPeopleCount;

    private String inSchool;

    private String inAcademy;

    private Date time;

    private String creator;

    private String classCode;

    //0普通班级，1社团，2选修班
    private int type;

    public ClassObject() {
        name = "";
        classID = "";
        headerPath = "";
        totalPeopleCount = 0;
        inSchool = "";
        inAcademy = "";
        time = new Date();
        creator = "";
        classCode = "";
        type = 0;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    private List<ManagerObject> managers = new ArrayList<>();

    private List<SubClassObject> subClassObjectList = new ArrayList<>();

//    private List<UserObject> members = new ArrayList<>();

    private List<ClassMemberObject> members = new ArrayList<>();

    private List<FileObject> fileObjects = new ArrayList<>();

    private List<NoticeGroupObject> noticeGroupObjectList = new ArrayList<>();

    private List<AnnouncementObject> announcementObjectList = new ArrayList<>();

    private List<AlbumsObject> albumsObjectList = new ArrayList<>();

    public List<String> getMemberIds () {
        List<ClassMemberObject> memberObjectList = getMemberList();
        List<String> result = new ArrayList<>();
        for (ClassMemberObject temp : memberObjectList) {
            result.add(temp.getMemberID());
        }
        return result;
    }

    public List<ClassMemberObject> getMemberList() {
        return DataSupport.where("classobject_id=?",String.valueOf(id)).find(ClassMemberObject.class);
    }

    public List<ClassMemberObject> getMembers() {
        return members;
    }

    public void setMembers(List<ClassMemberObject> members) {
        this.members = members;
    }

    private List<RecentMessageObject> recentMessageObjectList = new ArrayList<>();

    public List<RecentMessageObject> getRecentMessageObjectList() {
        return recentMessageObjectList;
    }

    public void setRecentMessageObjectList(List<RecentMessageObject> recentMessageObjectList) {
        this.recentMessageObjectList = recentMessageObjectList;
    }

    public List<AlbumsObject> getAlbumsObjectList() {
        return albumsObjectList;
    }

    public void setAlbumsObjectList(List<AlbumsObject> albumsObjectList) {
        this.albumsObjectList = albumsObjectList;
    }

    public List<AnnouncementObject> getAnnouncementObjectList() {
        return announcementObjectList;
    }

    public void setAnnouncementObjectList(List<AnnouncementObject> announcementObjectList) {
        this.announcementObjectList = announcementObjectList;
    }
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<ManagerObject> getManagers() {
        return managers;
    }

    public void setManagers(List<ManagerObject> managers) {
        this.managers = managers;
    }

    public List<NoticeGroupObject> getNoticeGroupObjectList() {
        return noticeGroupObjectList;
    }

    public void setNoticeGroupObjectList(List<NoticeGroupObject> noticeGroupObjectList) {
        this.noticeGroupObjectList = noticeGroupObjectList;
    }

    public List<FileObject> getFileObjects() {
        return fileObjects;
    }

    public void setFileObjects(List<FileObject> fileObjects) {
        this.fileObjects = fileObjects;
    }

    public List<SubClassObject> getSubClassObjectList() {
        return subClassObjectList;
    }

    public void setSubClassObjectList(List<SubClassObject> subClassObjectList) {
        this.subClassObjectList = subClassObjectList;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
/*
    public List<UserObject> getMembers() {
        Cursor cursor = DataSupport.findBySQL("select userobject_id from classobject_userobject where classobject_id=?",String.valueOf(id));
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

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public String getHeaderPath() {
        return headerPath;
    }

    public void setHeaderPath(String headerPath) {
        this.headerPath = headerPath;
    }

    public int getTotalPeopleCount() {
        return totalPeopleCount;
    }

    public void setTotalPeopleCount(int totalPeopleCount) {
        this.totalPeopleCount = totalPeopleCount;
    }

    public String getInSchool() {
        return inSchool;
    }

    public void setInSchool(String inSchool) {
        this.inSchool = inSchool;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getInAcademy() {
        return inAcademy;
    }

    public void setInAcademy(String inAcademy) {
        this.inAcademy = inAcademy;
    }
}
