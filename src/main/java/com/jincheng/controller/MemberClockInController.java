package com.jincheng.controller;

import com.jincheng.entity.ClockIn;
import com.jincheng.entity.Member;
import com.jincheng.mapper.ClockInMapper;
import com.jincheng.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/member/clockin")
public class MemberClockInController {

    @Autowired
    private ClockInMapper clockInMapper;

    @GetMapping
    public Result list(@SessionAttribute(name = "member", required = false) Member member) {
        try {
            if (member == null) {
                return Result.error("未登录");
            }
            Integer memberId = member.getMemberId();
            List<Map<String, Object>> records = clockInMapper.selectRecordDetailsByMemberId(memberId);
            int todayCount = clockInMapper.countTodayByMemberId(memberId);
            int monthCount = clockInMapper.countThisMonthByMemberId(memberId);
            Date lastTime = clockInMapper.selectLastClockTime(memberId);

            Map<String, Object> payload = new HashMap<>();
            payload.put("records", records);
            payload.put("todayClocked", todayCount > 0);
            payload.put("monthCount", monthCount);
            payload.put("lastClockTime", lastTime);
            return Result.success(payload);
        } catch (Exception e) {
            return Result.error("打卡记录加载失败: " + e.getMessage());
        }
    }

    @PostMapping
    public Result clockIn(@SessionAttribute(name = "member", required = false) Member member) {
        try {
            if (member == null) {
                return Result.error("未登录");
            }
            Integer memberId = member.getMemberId();
            if (clockInMapper.countTodayByMemberId(memberId) > 0) {
                return Result.error("今天已经打卡");
            }
            ClockIn clockIn = new ClockIn();
            clockIn.setMemberId(memberId);
            clockIn.setClockTime(new Date());
            clockIn.setClockStatus("COMPLETED");
            if (clockInMapper.insertStart(clockIn) > 0) {
                return Result.success("打卡成功");
            }
            return Result.error("打卡失败");
        } catch (Exception e) {
            return Result.error("打卡失败: " + e.getMessage());
        }
    }
}
