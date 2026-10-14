package com.jincheng.controller;

import com.jincheng.entity.Course;
import com.jincheng.entity.CourseBuy;
import com.jincheng.entity.Member;
import com.jincheng.mapper.CourseBuyMapper;
import com.jincheng.mapper.CourseMapper;
import com.jincheng.service.CourseService;
import com.jincheng.util.CourseStatus;
import com.jincheng.util.PayStatus;
import com.jincheng.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/member/course")
public class MemberCourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseBuyMapper courseBuyMapper;

    @GetMapping("/list")
    public Result list() {
        return Result.success(courseMapper.selectOnSaleWithCoach());
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable Integer id) {
        Map<String, Object> detail = courseMapper.selectDetailWithCoachById(id);
        if (detail == null || detail.isEmpty()) {
            return Result.error("课程不存在");
        }
        Object saleStatus = detail.get("saleStatus");
        if (saleStatus == null || !CourseStatus.isOnSale(String.valueOf(saleStatus))) {
            return Result.error("课程未上架");
        }
        return Result.success(detail);
    }

    @PostMapping("/buy/{id}")
    public Result buy(@SessionAttribute(name = "member", required = false) Member member,
                      @PathVariable Long id) {
        if (member == null) {
            return Result.error("未登录");
        }

        Course course = courseService.getCourseById(id);
        if (course == null) {
            return Result.error("课程不存在");
        }
        if (!CourseStatus.isOnSale(course.getSaleStatus())) {
            return Result.error("课程未上架，暂不能购买");
        }

        CourseBuy courseBuy = new CourseBuy();
        courseBuy.setMemberId(member.getMemberId());
        courseBuy.setCoachId(course.getCoachId());
        courseBuy.setCourseId(course.getCourseId());
        courseBuy.setPayPrice(course.getCoursePrice());
        courseBuy.setPayStatus(PayStatus.PAID);
        courseBuy.setBuyTime(new Date());

        if (courseBuyMapper.insert(courseBuy) > 0) {
            return Result.success("购买成功", courseBuy);
        }
        return Result.error("购买失败");
    }
}
