package com.jincheng.service;

import com.jincheng.entity.Course;

import java.util.List;

public interface CourseService {
    List<Course> getAllCourses();
    List<Course> getPendingCourses();
    boolean addCourse(Course course);
    boolean updateCourse(Course course);
    boolean deleteCourse(Long id);
    Course getCourseById(Long id);
    boolean auditCourse(Long id, String status);
    boolean updateCourseStatus(Long id, String status);
}
