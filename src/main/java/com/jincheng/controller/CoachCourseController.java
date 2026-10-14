package com.jincheng.controller;

import com.jincheng.entity.Coach;
import com.jincheng.entity.Course;
import com.jincheng.mapper.CoachMapper;
import com.jincheng.mapper.CourseMapper;
import com.jincheng.util.CourseStatus;
import com.jincheng.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/coach/course")
public class CoachCourseController {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CoachMapper coachMapper;

    @GetMapping("/list")
    public Result list(@SessionAttribute(name = "coach", required = false) Coach coach) {
        if (coach == null) {
            return Result.error("未登录");
        }

        Coach latestCoach = coach;
        if (coach.getCoachNo() != null && !coach.getCoachNo().isBlank()) {
            Coach dbCoach = coachMapper.selectByCoachNo(coach.getCoachNo());
            if (dbCoach != null) {
                latestCoach = dbCoach;
            }
        }

        List<Course> courses = courseMapper.selectByCoachId(latestCoach.getCoachId());
        if ((courses == null || courses.isEmpty()) && latestCoach.getCoachNo() != null && !latestCoach.getCoachNo().isBlank()) {
            courses = courseMapper.selectByCoachNo(latestCoach.getCoachNo());
        }
        return Result.success(courses);
    }

    @PostMapping
    public Result add(@SessionAttribute(name = "coach", required = false) Coach coach,
                      @RequestBody Course course) {
        if (coach == null) {
            return Result.error("未登录");
        }
        if (course.getCourseName() == null || course.getCourseName().isBlank()) {
            return Result.error("课程名称不能为空");
        }

        Coach dbCoach = coachMapper.selectByCoachNo(coach.getCoachNo());
        Integer coachId = dbCoach != null ? dbCoach.getCoachId() : coach.getCoachId();

        course.setCoachId(coachId);
        course.setSaleStatus(CourseStatus.REVIEWING);
        course.setCreateTime(new Date());
        if (courseMapper.insert(course) > 0) {
            return Result.success("发布成功");
        }
        return Result.error("发布失败");
    }

    @PutMapping
    public Result update(@SessionAttribute(name = "coach", required = false) Coach coach,
                         @RequestBody Course course) {
        if (coach == null) {
            return Result.error("未登录");
        }
        if (course.getCourseId() == null) {
            return Result.error("课程ID不能为空");
        }

        Coach dbCoach = coachMapper.selectByCoachNo(coach.getCoachNo());
        Integer coachId = dbCoach != null ? dbCoach.getCoachId() : coach.getCoachId();

        Course dbCourse = courseMapper.selectByIdAndCoachId(course.getCourseId(), coachId);
        if (dbCourse == null) {
            return Result.error("课程不存在或无权限");
        }
        course.setCoachId(coachId);
        course.setCreateTime(dbCourse.getCreateTime());
        course.setSaleStatus(CourseStatus.REVIEWING);
        if (courseMapper.updateById(course) > 0) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @PutMapping("/status/{id}")
    public Result updateStatus(@SessionAttribute(name = "coach", required = false) Coach coach,
                               @PathVariable Integer id,
                               @RequestParam String status) {
        if (coach == null) {
            return Result.error("未登录");
        }

        Coach dbCoach = coachMapper.selectByCoachNo(coach.getCoachNo());
        Integer coachId = dbCoach != null ? dbCoach.getCoachId() : coach.getCoachId();

        Course dbCourse = courseMapper.selectByIdAndCoachId(id, coachId);
        if (dbCourse == null) {
            return Result.error("课程不存在或无权限");
        }
        // 教练仅可主动下架自己的课程；重新上架需提交管理员审核
        if (!CourseStatus.OFF_SALE.equals(status)) {
            return Result.error("教练只能将课程下架，重新上架请提交管理员审核");
        }
        dbCourse.setSaleStatus(CourseStatus.OFF_SALE);
        if (courseMapper.updateById(dbCourse) > 0) {
            return Result.success("课程已下架");
        }
        return Result.error("下架失败");
    }

    @DeleteMapping("/{id}")
    public Result delete(@SessionAttribute(name = "coach", required = false) Coach coach,
                         @PathVariable Integer id) {
        if (coach == null) {
            return Result.error("未登录");
        }

        Coach dbCoach = coachMapper.selectByCoachNo(coach.getCoachNo());
        Integer coachId = dbCoach != null ? dbCoach.getCoachId() : coach.getCoachId();

        Course dbCourse = courseMapper.selectByIdAndCoachId(id, coachId);
        if (dbCourse == null) {
            return Result.error("课程不存在或无权限");
        }
        if (courseMapper.deleteById(id) > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
