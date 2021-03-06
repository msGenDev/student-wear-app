package com.aidangrabe.common.model;

import com.orm.SugarRecord;

import java.util.Date;

/**
 * Created by aidan on 02/02/15.
 * This class describes a particular result for a given module
 */
public class Result extends SugarRecord<Result> {

    // the module this result belongs to
    private Module module;

    // the result's grade in range 0-100
    private float grade;

    // the time in millis when this result was created
    private long createTime;

    // needed by Sugar ORM
    public Result() {}

    public Result(float grade) {
        this.grade = grade;
        this.createTime = new Date().getTime();
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public float getGrade() {
        return grade;
    }

    public void setGrade(float grade) {
        this.grade = grade;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
