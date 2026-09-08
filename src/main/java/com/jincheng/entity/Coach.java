package com.jincheng.entity;

import java.io.Serializable;
import java.util.Date;

public class Coach implements Serializable {
    private Integer coachId;
    private String coachNo;
    private String coachName;
    private String coachSex;
    private Integer coachAge;
    private String coachTel;
    private String goodAt;
    private String coachPwd;
    private String coachStatus;
    private Date createTime;

    public Integer getCoachId() {
        return coachId;
    }

    public void setCoachId(Integer coachId) {
        this.coachId = coachId;
    }

    public String getCoachNo() {
        return coachNo;
    }

    public void setCoachNo(String coachNo) {
        this.coachNo = coachNo;
    }

    public String getCoachName() {
        return coachName;
    }

    public void setCoachName(String coachName) {
        this.coachName = coachName;
    }

    public String getCoachSex() {
        return coachSex;
    }

    public void setCoachSex(String coachSex) {
        this.coachSex = coachSex;
    }

    public Integer getCoachAge() {
        return coachAge;
    }

    public void setCoachAge(Integer coachAge) {
        this.coachAge = coachAge;
    }

    public String getCoachTel() {
        return coachTel;
    }

    public void setCoachTel(String coachTel) {
        this.coachTel = coachTel;
    }

    public String getCoachPwd() {
        return coachPwd;
    }

    public void setCoachPwd(String coachPwd) {
        this.coachPwd = coachPwd;
    }

    public String getGoodAt() {
        return goodAt;
    }

    public void setGoodAt(String goodAt) {
        this.goodAt = goodAt;
    }

    public String getCoachStatus() {
        return coachStatus;
    }

    public void setCoachStatus(String coachStatus) {
        this.coachStatus = coachStatus;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
