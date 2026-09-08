package com.jincheng.controller;

import com.jincheng.entity.Member;
import com.jincheng.mapper.ClockInMapper;
import com.jincheng.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/member/dashboard")
public class MemberDashboardController {

    @Autowired
    private ClockInMapper clockInMapper;

    @PostMapping("/metrics")
    public Result saveMetrics(@SessionAttribute(name = "member", required = false) Member member,
                              @RequestBody Map<String, Object> body) {
        try {
            if (member == null) {
                return Result.error("未登录");
            }
            Double weightKg = toDouble(body.get("weightKg"));
            Double bodyFatPct = toDouble(body.get("bodyFatPct"));
            Double bmi = toDouble(body.get("bmi"));
            Integer sessionCalories = toInt(body.get("sessionCalories"));
            String dataSource = Objects.toString(body.get("dataSource"), "manual");

            Integer latestClockId = clockInMapper.selectTodayLatestClockId(member.getMemberId());
            if (latestClockId == null) {
                return Result.error("请先完成今日打卡，再保存训练数据");
            }
            int rows = clockInMapper.updateMetricsByClockId(
                    latestClockId, weightKg, bodyFatPct, bmi, sessionCalories, dataSource
            );
            if (rows > 0) {
                return Result.success("训练数据保存成功");
            }
            return Result.error("请先完成今日打卡，再保存训练数据");
        } catch (Exception e) {
            return Result.error("保存训练数据失败: " + e.getMessage());
        }
    }

    @GetMapping
    public Result getDashboard(@SessionAttribute(name = "member", required = false) Member member) {
        try {
            if (member == null) {
                return Result.error("未登录");
            }

            Map<String, Object> latestMetricsRaw = clockInMapper.selectLatestMetricsByMemberId(member.getMemberId());
            int monthClockCount = clockInMapper.countThisMonthByMemberId(member.getMemberId());
            int monthCalories = toInt(clockInMapper.sumThisMonthCaloriesByMemberId(member.getMemberId()));

            Map<String, Object> payload = new HashMap<>();
            payload.put("latestMetrics", normalizeLatestMetrics(latestMetricsRaw));
            Map<String, Object> monthlySummary = new HashMap<>();
            monthlySummary.put("totalDurationMin", 0);
            monthlySummary.put("totalCalories", monthCalories);
            monthlySummary.put("sessions", monthClockCount);
            payload.put("monthlySummary", monthlySummary);
            return Result.success(payload);
        } catch (Exception e) {
            return Result.error("首页数据加载失败: " + e.getMessage());
        }
    }

    private Map<String, Object> normalizeLatestMetrics(Map<String, Object> raw) {
        Map<String, Object> latestMetrics = new HashMap<>();
        if (raw == null) return latestMetrics;

        latestMetrics.put("weightKg", pick(raw, "weightKg", "weight_kg", "weightkg"));
        latestMetrics.put("bodyFatPct", pick(raw, "bodyFatPct", "body_fat_pct", "bodyfatpct"));
        latestMetrics.put("bmi", pick(raw, "bmi"));
        latestMetrics.put("dataSource", pick(raw, "dataSource", "data_source", "datasource"));
        latestMetrics.put("recordDate", pick(raw, "recordDate", "record_date", "recorddate"));
        return latestMetrics;
    }

    private Object pick(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            if (source.containsKey(key)) return source.get(key);
            for (String existing : source.keySet()) {
                if (existing.equalsIgnoreCase(key)) return source.get(existing);
            }
        }
        return null;
    }

    private int toInt(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return 0;
        if (value instanceof Integer i) return i;
        if (value instanceof Long l) return l.intValue();
        if (value instanceof BigDecimal b) return b.intValue();
        return Integer.parseInt(String.valueOf(value));
    }

    private Double toDouble(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        if (value instanceof Double d) return d;
        if (value instanceof Float f) return (double) f;
        if (value instanceof Integer i) return i.doubleValue();
        if (value instanceof Long l) return l.doubleValue();
        if (value instanceof BigDecimal b) return b.doubleValue();
        return Double.parseDouble(String.valueOf(value));
    }
}
