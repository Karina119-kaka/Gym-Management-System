package com.jincheng.controller;

import com.jincheng.entity.Course;
import com.jincheng.entity.CourseBuy;
import com.jincheng.entity.CourseOrder;
import com.jincheng.entity.Member;
import com.jincheng.mapper.CourseBuyMapper;
import com.jincheng.mapper.CourseMapper;
import com.jincheng.mapper.CourseOrderMapper;
import com.jincheng.util.CourseStatus;
import com.jincheng.util.OrderStatus;
import com.jincheng.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/member/order")
public class MemberCourseOrderController {

    @Autowired
    private CourseBuyMapper courseBuyMapper;

    @Autowired
    private CourseOrderMapper courseOrderMapper;

    @Autowired
    private CourseMapper courseMapper;

    @GetMapping("/purchased")
    public Result purchased(@SessionAttribute(name = "member", required = false) Member member) {
        if (member == null) return Result.error("未登录");
        List<Map<String, Object>> list = courseBuyMapper.selectReservableSummaryByMemberId(member.getMemberId());
        return Result.success(list);
    }

    @GetMapping("/my")
    public Result mySchedule(@SessionAttribute(name = "member", required = false) Member member) {
        if (member == null) return Result.error("未登录");
        courseOrderMapper.expireAllPending();
        List<Map<String, Object>> list = courseOrderMapper.selectScheduleByMemberId(member.getMemberId());
        return Result.success(list);
    }

    @PostMapping("/reserve")
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public Result reserve(@SessionAttribute(name = "member", required = false) Member member,
                          @RequestBody Map<String, Object> body) {
        if (member == null) return Result.error("未登录");

        Integer courseId = toInt(body.get("courseId"));
        String orderTimeText = body.get("orderTime") == null ? "" : String.valueOf(body.get("orderTime")).trim();
        if (courseId == null) return Result.error("课程不能为空");
        if (orderTimeText.isEmpty()) return Result.error("请选择预约时间");

        Date orderTime;
        try {
            orderTime = parseDateTime(orderTimeText);
        } catch (ParseException e) {
            return Result.error("预约时间格式不正确");
        }
        if (orderTime.before(new Date())) {
            return Result.error("预约时间必须晚于当前时间");
        }

        courseOrderMapper.expireAllPending();

        Course course = courseMapper.selectById(courseId);
        if (course == null) return Result.error("课程不存在");
        if (!isCourseReservable(course.getSaleStatus())) return Result.error("该课程当前不可预约");

        Integer buyId = courseBuyMapper.selectNextReservableBuyId(member.getMemberId(), courseId);
        if (buyId == null) return Result.error("该课程无可用购买次数");

        CourseBuy buy = courseBuyMapper.selectPurchasedByBuyId(buyId, member.getMemberId());
        if (buy == null) return Result.error("只能预约自己已购买的课程");

        if (courseOrderMapper.countByCourseAndTime(member.getMemberId(), courseId, orderTime) > 0) {
            return Result.error("该课程该时间已预约，请更换时间");
        }
        if (courseOrderMapper.countMemberTimeConflicts(member.getMemberId(), orderTime) > 0) {
            return Result.error("该时间段你已有其他课程预约");
        }
        if (courseOrderMapper.countCoachTimeConflicts(buy.getCoachId(), orderTime) > 0) {
            return Result.error("该教练该时间段已有预约，请更换时间");
        }

        CourseOrder order = new CourseOrder();
        order.setMemberId(buy.getMemberId());
        order.setCoachId(buy.getCoachId());
        order.setCourseId(buy.getCourseId());
        order.setBuyId(buy.getBuyId());
        order.setOrderTime(orderTime);
        order.setOrderStatus(OrderStatus.PENDING_REVIEW);

        if (courseOrderMapper.insert(order) > 0) {
            return Result.success("已提交预约，待教练查看", order);
        }
        return Result.error("预约失败");
    }

    private boolean isCourseReservable(String saleStatus) {
        return CourseStatus.isOnSale(saleStatus);
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) return null;
        return Integer.parseInt(text);
    }

    private Date parseDateTime(String text) throws ParseException {
        String normalized = text.replace("T", " ");
        if (normalized.length() == 16) normalized += ":00";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setLenient(false);
        return formatter.parse(normalized);
    }
}
