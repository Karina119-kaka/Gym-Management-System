package com.jincheng.mapper;

import com.jincheng.entity.CourseBuy;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseBuyMapper {

    @Insert("INSERT INTO course_buy (member_id, coach_id, course_id, pay_price, pay_status, buy_time) " +
            "VALUES (#{memberId}, #{coachId}, #{courseId}, #{payPrice}, #{payStatus}, #{buyTime})")
    int insert(CourseBuy courseBuy);

    @Select("SELECT b.buy_id AS buyId, b.member_id AS memberId, b.coach_id AS coachId, b.course_id AS courseId, " +
            "b.pay_price AS payPrice, b.pay_status AS payStatus, b.buy_time AS buyTime, " +
            "c.course_name AS courseName, c.course_intro AS courseIntro, c.course_detail AS courseDetail, c.course_price AS coursePrice, " +
            "coach.coach_name AS coachName, coach.coach_sex AS coachSex, coach.coach_age AS coachAge " +
            "FROM course_buy b LEFT JOIN course c ON c.course_id = b.course_id " +
            "LEFT JOIN coach ON coach.coach_id = b.coach_id " +
            "WHERE b.member_id = #{memberId} " +
            "AND b.pay_status IN ('已购买', 'PAID') " +
            "ORDER BY b.buy_time DESC, b.buy_id DESC")
    List<Map<String, Object>> selectPurchasedByMemberId(@Param("memberId") Integer memberId);

    @Select("SELECT * FROM course_buy WHERE buy_id = #{buyId} AND member_id = #{memberId} " +
            "AND pay_status IN ('已购买', 'PAID') LIMIT 1")
    CourseBuy selectPurchasedByBuyId(@Param("buyId") Integer buyId, @Param("memberId") Integer memberId);

    @Select("SELECT b.course_id AS courseId, MIN(b.coach_id) AS coachId, MAX(c.course_name) AS courseName, " +
            "MAX(c.course_intro) AS courseIntro, MAX(c.course_detail) AS courseDetail, MAX(c.course_price) AS coursePrice, " +
            "MAX(coach.coach_name) AS coachName, MAX(coach.coach_sex) AS coachSex, MAX(coach.coach_age) AS coachAge, " +
            "COUNT(*) AS totalBought, " +
            "(COUNT(*) - SUM(CASE WHEN used.buy_id IS NULL THEN 0 ELSE 1 END)) AS remainingCount " +
            "FROM course_buy b " +
            "LEFT JOIN course c ON c.course_id = b.course_id " +
            "LEFT JOIN coach ON coach.coach_id = b.coach_id " +
            "LEFT JOIN (SELECT DISTINCT buy_id FROM course_order " +
            "           WHERE order_status IN ('待教练查看', '预约成功', '已预约', '预约中')) used " +
            "ON used.buy_id = b.buy_id " +
            "WHERE b.member_id = #{memberId} " +
            "AND b.pay_status IN ('已购买', 'PAID') " +
            "GROUP BY b.course_id ORDER BY MAX(b.buy_time) DESC")
    List<Map<String, Object>> selectReservableSummaryByMemberId(@Param("memberId") Integer memberId);

    @Select("SELECT b.buy_id FROM course_buy b " +
            "LEFT JOIN course_order o ON o.buy_id = b.buy_id " +
            "AND o.order_status IN ('待教练查看', '预约成功', '已预约', '预约中') " +
            "WHERE b.member_id = #{memberId} AND b.course_id = #{courseId} " +
            "AND b.pay_status IN ('已购买', 'PAID') " +
            "AND o.order_id IS NULL " +
            "ORDER BY b.buy_time ASC, b.buy_id ASC LIMIT 1")
    Integer selectNextReservableBuyId(@Param("memberId") Integer memberId, @Param("courseId") Integer courseId);
}
