package com.jincheng.controller;

import com.jincheng.entity.Course;
import com.jincheng.service.CourseService;
import com.jincheng.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/list")
    public Result list() {
        List<Course> courses = courseService.getAllCourses();
        return Result.success(courses);
    }

    @GetMapping("/pending")
    public Result getPendingCourses() {
        List<Course> courses = courseService.getPendingCourses();
        return Result.success(courses);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        if (course != null) {
            return Result.success(course);
        }
        return Result.error("课程不存在");
    }

    @PostMapping
    public Result add(@RequestBody Course course) {
        if (courseService.addCourse(course)) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping
    public Result update(@RequestBody Course course) {
        if (courseService.updateCourse(course)) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        if (courseService.deleteCourse(id)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    @PutMapping("/audit/{id}")
    public Result audit(@PathVariable Long id, @RequestParam String status) {
        if (courseService.auditCourse(id, status)) {
            return Result.success("审核成功");
        }
        return Result.error("审核失败");
    }

    @PutMapping("/status/{id}")
    public Result updateStatus(@PathVariable Long id, @RequestParam String status) {
        if (courseService.updateCourseStatus(id, status)) {
            return Result.success("状态更新成功");
        }
        return Result.error("状态更新失败");
    }
}
