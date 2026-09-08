package com.jincheng.service.impl;

import com.jincheng.entity.Course;
import com.jincheng.mapper.CourseMapper;
import com.jincheng.service.CourseService;
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
        return courseMapper.selectByStatus("待审核");
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
