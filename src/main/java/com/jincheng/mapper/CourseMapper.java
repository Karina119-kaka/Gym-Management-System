package com.jincheng.mapper;

import com.jincheng.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseMapper {
    
    @Select("SELECT * FROM course")
    List<Course> selectAll();

    @Select("""
            SELECT
                c.course_id AS courseId,
                c.course_name AS courseName,
                c.coach_id AS coachId,
                c.course_price AS coursePrice,
                c.course_intro AS courseIntro,
                c.course_detail AS courseDetail,
                c.sale_status AS saleStatus,
                c.create_time AS createTime,
                co.coach_name AS coachName,
                co.coach_sex AS coachSex,
                co.coach_age AS coachAge
            FROM course c
            LEFT JOIN coach co ON c.coach_id = co.coach_id
            WHERE c.sale_status = 'ON_SALE' OR c.sale_status = '上架'
            ORDER BY c.create_time DESC, c.course_id DESC
            """)
    List<Map<String, Object>> selectOnSaleWithCoach();

    @Select("""
            SELECT
                c.course_id AS courseId,
                c.course_name AS courseName,
                c.coach_id AS coachId,
                c.course_price AS coursePrice,
                c.course_intro AS courseIntro,
                c.course_detail AS courseDetail,
                c.sale_status AS saleStatus,
                c.create_time AS createTime,
                co.coach_name AS coachName,
                co.coach_sex AS coachSex,
                co.coach_age AS coachAge
            FROM course c
            LEFT JOIN coach co ON c.coach_id = co.coach_id
            WHERE c.course_id = #{id}
            """)
    Map<String, Object> selectDetailWithCoachById(@Param("id") Integer id);
    
    @Select("SELECT * FROM course WHERE sale_status = #{status}")
    List<Course> selectByStatus(@Param("status") String status);
    
    @Insert("INSERT INTO course (course_name, coach_id, course_price, course_intro, course_detail, sale_status, create_time) " +
            "VALUES (#{courseName}, #{coachId}, #{coursePrice}, #{courseIntro}, #{courseDetail}, #{saleStatus}, #{createTime})")
    int insert(Course course);

    @Update("UPDATE course SET course_name=#{courseName}, coach_id=#{coachId}, course_price=#{coursePrice}, " +
            "course_intro=#{courseIntro}, course_detail=#{courseDetail}, sale_status=#{saleStatus} WHERE course_id=#{courseId}")
    int updateById(Course course);

    @Delete("DELETE FROM course WHERE course_id = #{id}")
    int deleteById(@Param("id") Integer id);
    
    @Select("SELECT * FROM course WHERE course_id = #{id}")
    Course selectById(@Param("id") Integer id);

    @Delete("DELETE FROM course WHERE coach_id = #{coachId}")
    int deleteByCoachId(@Param("coachId") Integer coachId);

    @Select("SELECT * FROM course WHERE coach_id = #{coachId} ORDER BY create_time DESC, course_id DESC")
    List<Course> selectByCoachId(@Param("coachId") Integer coachId);

    @Select("""
            SELECT c.* FROM course c
            INNER JOIN coach co ON c.coach_id = co.coach_id
            WHERE co.coach_no = #{coachNo}
            ORDER BY c.create_time DESC, c.course_id DESC
            """)
    List<Course> selectByCoachNo(@Param("coachNo") String coachNo);

    @Select("SELECT * FROM course WHERE course_id = #{courseId} AND coach_id = #{coachId}")
    Course selectByIdAndCoachId(@Param("courseId") Integer courseId, @Param("coachId") Integer coachId);
}
