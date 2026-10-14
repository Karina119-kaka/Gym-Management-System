package com.jincheng.service.impl;

import com.jincheng.entity.Course;
import com.jincheng.mapper.CourseMapper;
import com.jincheng.service.CourseService;
import com.jincheng.util.CourseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public List<Course> getAllCourses() {
        return courseMapper.selectAll();
    }

    @Override
    public List<Course> getPendingCourses() {
        return courseMapper.selectByStatus(CourseStatus.REVIEWING);
    }

    @Override
    public boolean addCourse(Course course) {
        return courseMapper.insert(course) > 0;
    }

    @Override
    public boolean updateCourse(Course course) {
        return courseMapper.updateById(course) > 0;
    }

    @Override
    public boolean deleteCourse(Long id) {
        return courseMapper.deleteById(id.intValue()) > 0;
    }

    @Override
    public Course getCourseById(Long id) {
        return courseMapper.selectById(id.intValue());
    }

    @Override
    public boolean auditCourse(Long id, String status) {
        // 统一课程状态语义：待审核 -> (审核通过)上架 / (审核拒绝)下架
        if ("已通过".equals(status)) {
            status = CourseStatus.ON_SALE;
        } else if ("已拒绝".equals(status)) {
            status = CourseStatus.OFF_SALE;
        }
        Course course = getCourseById(id);
        if (course != null) {
            course.setSaleStatus(status);
            return updateCourse(course);
        }
        return false;
    }

    @Override
    public boolean updateCourseStatus(Long id, String status) {
        Course course = getCourseById(id);
        if (course != null) {
            course.setSaleStatus(status);
            return updateCourse(course);
        }
        return false;
    }
}
