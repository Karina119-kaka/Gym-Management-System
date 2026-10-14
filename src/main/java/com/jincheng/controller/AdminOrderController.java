package com.jincheng.controller;

import com.jincheng.mapper.CoachMapper;
import com.jincheng.mapper.CourseMapper;
import com.jincheng.mapper.CourseOrderMapper;
import com.jincheng.mapper.MemberMapper;
import com.jincheng.util.OrderStatus;
import com.jincheng.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    @Autowired
    private CourseOrderMapper courseOrderMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private CoachMapper coachMapper;
    @Autowired
    private CourseMapper courseMapper;

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String status) {
        List<Map<String, Object>> rows = courseOrderMapper.selectAllWithNames();
        if (status != null && !status.isBlank()) {
            rows = rows.stream()
                    .filter(item -> status.equals(String.valueOf(item.get("orderStatus"))))
                    .collect(Collectors.toList());
        }
        return Result.success(rows);
    }

    @GetMapping("/stats")
    public Result stats() {
        List<Map<String, Object>> rows = courseOrderMapper.selectAllWithNames();
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        ZoneId zone = ZoneId.systemDefault();

        long todayArrivedCount = rows.stream()
                .filter(x -> OrderStatus.ARRIVED.equals(normalizeStatus(String.valueOf(x.get("orderStatus")))))
                .filter(x -> isSameDay(x.get("orderTime"), today, zone))
                .count();

        long monthApprovedCount = rows.stream()
                .filter(x -> OrderStatus.RESERVED_SUCCESS.equals(normalizeStatus(String.valueOf(x.get("orderStatus")))))
                .filter(x -> isCurrentMonth(x.get("orderTime"), monthStart, zone))
                .count();

        long monthPendingCount = rows.stream()
                .filter(x -> {
                    String status = normalizeStatus(String.valueOf(x.get("orderStatus")));
                    return OrderStatus.PENDING_REVIEW.equals(status) || OrderStatus.RESERVED.equals(status);
                })
                .filter(x -> isCurrentMonth(x.get("orderTime"), monthStart, zone))
                .count();

        long monthFailedCount = rows.stream()
                .filter(x -> OrderStatus.FAILED.equals(normalizeStatus(String.valueOf(x.get("orderStatus")))))
                .filter(x -> isCurrentMonth(x.get("orderTime"), monthStart, zone))
                .count();

        Map<String, Object> payload = new HashMap<>();
        payload.put("memberCount", memberMapper.selectAll().size());
        payload.put("coachCount", coachMapper.selectAll().size());
        payload.put("courseCount", courseMapper.selectAll().size());
        payload.put("orderCount", rows.size());
        payload.put("arrivedCount", todayArrivedCount);
        payload.put("approvedCount", rows.stream().filter(x -> OrderStatus.RESERVED_SUCCESS.equals(normalizeStatus(String.valueOf(x.get("orderStatus"))))).count());
        payload.put("pendingCount", rows.stream().filter(x -> {
            String status = normalizeStatus(String.valueOf(x.get("orderStatus")));
            return OrderStatus.PENDING_REVIEW.equals(status) || OrderStatus.RESERVED.equals(status);
        }).count());
        payload.put("failedCount", rows.stream().filter(x -> OrderStatus.FAILED.equals(normalizeStatus(String.valueOf(x.get("orderStatus"))))).count());
        payload.put("monthApprovedCount", monthApprovedCount);
        payload.put("monthPendingCount", monthPendingCount);
        payload.put("monthFailedCount", monthFailedCount);
        payload.put("monthOrderCount", monthApprovedCount + monthPendingCount + monthFailedCount);
        return Result.success(payload);
    }

    private boolean isSameDay(Object rawDate, LocalDate today, ZoneId zone) {
        if (!(rawDate instanceof Date)) return false;
        Date date = (Date) rawDate;
        LocalDate day = date.toInstant().atZone(zone).toLocalDate();
        return today.equals(day);
    }

    private boolean isCurrentMonth(Object rawDate, LocalDate monthStart, ZoneId zone) {
        if (!(rawDate instanceof Date)) return false;
        Date date = (Date) rawDate;
        LocalDate day = date.toInstant().atZone(zone).toLocalDate();
        return !day.isBefore(monthStart) && day.getYear() == monthStart.getYear() && day.getMonthValue() == monthStart.getMonthValue();
    }

    private String normalizeStatus(String status) {
        if (status == null) return "";
        // 已失效 / 已拒绝 统一按「预约失败」统计（查询层已聚合，此处兜底）
        if (OrderStatus.EXPIRED.equals(status) || OrderStatus.REJECTED.equals(status)) {
            return OrderStatus.FAILED;
        }
        return status;
    }
}
