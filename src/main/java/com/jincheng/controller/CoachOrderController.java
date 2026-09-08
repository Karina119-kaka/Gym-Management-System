package com.jincheng.controller;

import com.jincheng.entity.Coach;
import com.jincheng.entity.CourseOrder;
import com.jincheng.mapper.CoachMapper;
import com.jincheng.mapper.CoachOrderNoticeMapper;
import com.jincheng.mapper.CourseOrderMapper;
import com.jincheng.util.OrderStatus;
import com.jincheng.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coach/order")
public class CoachOrderController {

    @Autowired
    private CourseOrderMapper courseOrderMapper;
    @Autowired
    private CoachMapper coachMapper;
    @Autowired
    private CoachOrderNoticeMapper coachOrderNoticeMapper;

    @GetMapping("/pending")
    public Result pending(@SessionAttribute(name = "coach", required = false) Coach coach) {
        if (coach == null) return Result.error("未登录");
        Integer coachId = getCoachId(coach);
        courseOrderMapper.expirePendingByCoachId(coachId);
        List<Map<String, Object>> list = courseOrderMapper.selectPendingByCoachId(coachId);
        return Result.success(list);
    }

    @GetMapping("/schedule")
    public Result schedule(@SessionAttribute(name = "coach", required = false) Coach coach) {
        if (coach == null) return Result.error("未登录");
        Integer coachId = getCoachId(coach);
        List<Map<String, Object>> list = courseOrderMapper.selectApprovedScheduleByCoachId(coachId);
        return Result.success(list);
    }

    @GetMapping("/attendance")
    public Result attendance(@SessionAttribute(name = "coach", required = false) Coach coach) {
        if (coach == null) return Result.error("未登录");
        Integer coachId = getCoachId(coach);
        return Result.success(courseOrderMapper.selectAttendanceByCoachId(coachId));
    }

    @GetMapping("/notice/unread")
    public Result unreadNotice(@SessionAttribute(name = "coach", required = false) Coach coach) {
        if (coach == null) return Result.error("未登录");
        Integer coachId = getCoachId(coach);
        courseOrderMapper.expirePendingByCoachId(coachId);
        int count = coachOrderNoticeMapper.countUnreadPending(coachId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("unreadCount", count);
        payload.put("hasUnread", count > 0);
        return Result.success(payload);
    }

    @PutMapping("/notice/read")
    public Result markNoticeRead(@SessionAttribute(name = "coach", required = false) Coach coach) {
        if (coach == null) return Result.error("未登录");
        Integer coachId = getCoachId(coach);
        coachOrderNoticeMapper.markAllPendingRead(coachId);
        return Result.success("已标记已读");
    }

    @PutMapping("/audit/{orderId}")
    public Result audit(@SessionAttribute(name = "coach", required = false) Coach coach,
                        @PathVariable Integer orderId,
                        @RequestParam String action) {
        if (coach == null) return Result.error("未登录");
        Integer coachId = getCoachId(coach);
        courseOrderMapper.expirePendingByCoachId(coachId);

        CourseOrder order = courseOrderMapper.selectByIdAndCoachId(orderId, coachId);
        if (order == null) return Result.error("预约不存在或无权限");
        String currentStatus = order.getOrderStatus();
        if (!OrderStatus.PENDING_REVIEW.equals(currentStatus) && !OrderStatus.RESERVED.equals(currentStatus)) {
            return Result.error("该预约已处理或已失效");
        }

        String status;
        if ("approve".equalsIgnoreCase(action)) status = OrderStatus.RESERVED_SUCCESS;
        else if ("reject".equalsIgnoreCase(action)) status = OrderStatus.REJECTED;
        else return Result.error("无效操作");

        if (courseOrderMapper.updateStatus(orderId, status) > 0) return Result.success("处理成功");
        return Result.error("处理失败");
    }

    @PutMapping("/attendance/{orderId}")
    public Result updateAttendance(@SessionAttribute(name = "coach", required = false) Coach coach,
                                   @PathVariable Integer orderId,
                                   @RequestParam String status) {
        if (coach == null) return Result.error("未登录");
        Integer coachId = getCoachId(coach);
        CourseOrder order = courseOrderMapper.selectByIdAndCoachId(orderId, coachId);
        if (order == null) return Result.error("课程不存在或无权限");
        if (!OrderStatus.ARRIVED.equals(status) && !OrderStatus.NOT_ARRIVED.equals(status)) return Result.error("状态不合法");
        if (!isToday(order.getOrderTime())) return Result.error("仅可操作当日课程考勤");
        if (!canAttendNow(order.getOrderTime())) return Result.error("未到预约时间，暂不可考勤");
        if (courseOrderMapper.updateStatus(orderId, status) > 0) return Result.success("考勤更新成功");
        return Result.error("考勤更新失败");
    }

    private Integer getCoachId(Coach coach) {
        Coach latestCoach = coachMapper.selectByCoachNo(coach.getCoachNo());
        return latestCoach != null ? latestCoach.getCoachId() : coach.getCoachId();
    }

    private boolean isToday(Date date) {
        if (date == null) return false;
        LocalDate d = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return LocalDate.now().equals(d);
    }

    private boolean canAttendNow(Date orderTime) {
        return orderTime != null && !orderTime.after(new Date());
    }
}

