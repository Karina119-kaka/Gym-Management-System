package com.jincheng.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 健身打卡记录，对应 clock_in 表。
 * 打卡成功后可通过 update 补充体重/体脂/BMI/卡路里等训练数据。
 */
public class ClockIn implements Serializable {
    private Integer clockId;
    private Integer memberId;
    private Date clockTime;
    private Double weightKg;
    private Double bodyFatPct;
    private Double bmi;
    private Integer calories;
    private String dataSource;

    public Integer getClockId() {
        return clockId;
    }

    public void setClockId(Integer clockId) {
        this.clockId = clockId;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public Date getClockTime() {
        return clockTime;
    }

    public void setClockTime(Date clockTime) {
        this.clockTime = clockTime;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Double getBodyFatPct() {
        return bodyFatPct;
    }

    public void setBodyFatPct(Double bodyFatPct) {
        this.bodyFatPct = bodyFatPct;
    }

    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}
