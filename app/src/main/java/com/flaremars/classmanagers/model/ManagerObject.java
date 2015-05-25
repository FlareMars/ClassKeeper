package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

/**
 * 管理员实体类
 */
public class ManagerObject extends DataSupport {
    private int id;

    private String name;

    private String managerID;

    private ClassObject inClass;

    public ClassObject getInClass() {
        return inClass;
    }

    public void setInClass(ClassObject inClass) {
        this.inClass = inClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ManagerObject that = (ManagerObject) o;

        return !(name != null ? !name.equals(that.name) : that.name != null) && !(managerID != null ? !managerID.equals(that.managerID) : that.managerID != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (managerID != null ? managerID.hashCode() : 0);
        return result;
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

    public String getManagerID() {
        return managerID;
    }

    public void setManagerID(String managerID) {
        this.managerID = managerID;
    }
}
