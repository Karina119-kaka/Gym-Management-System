package com.jincheng.mapper;

import com.jincheng.entity.CourseOrder;
import com.jincheng.util.OrderStatus;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface CourseOrderMapper {

    @Insert("INSERT INTO course_order (member_id, coach_id, course_id, buy_id, order_time, order_status) " +
            "VALUES (#{memberId}, #{coachId}, #{courseId}, #{buyId}, #{orderTime}, #{orderStatus})")
    int insert(CourseOrder order);

    @Select("SELECT COUNT(1) FROM course_order WHERE member_id = #{memberId} AND buy_id = #{buyId} " +
            "AND DATE(order_time) = CURDATE() " +
            "AND order_status IN (" + OrderStatus.SQL_ACTIVE_IN + ")")
    int countTodayActiveByBuyId(@Param("memberId") Integer memberId, @Param("buyId") Integer buyId);

    @Select("SELECT COUNT(1) FROM course_order WHERE member_id = #{memberId} AND course_id = #{courseId} " +
            "AND order_time = #{orderTime} " +
            "AND order_status IN (" + OrderStatus.SQL_ACTIVE_IN + ")")
    int countByCourseAndTime(@Param("memberId") Integer memberId,
                             @Param("courseId") Integer courseId,
                             @Param("orderTime") Date orderTime);

    @Select("SELECT COUNT(1) FROM course_order WHERE member_id = #{memberId} " +
            "AND order_status IN (" + OrderStatus.SQL_ACTIVE_IN + ") " +
            "AND #{orderTime} < DATE_ADD(order_time, INTERVAL 1 HOUR) " +
            "AND order_time < DATE_ADD(#{orderTime}, INTERVAL 1 HOUR)")
    int countMemberTimeConflicts(@Param("memberId") Integer memberId, @Param("orderTime") Date orderTime);

    @Select("SELECT COUNT(1) FROM course_order WHERE coach_id = #{coachId} " +
            "AND order_status IN (" + OrderStatus.SQL_ACTIVE_IN + ") " +
            "AND #{orderTime} < DATE_ADD(order_time, INTERVAL 1 HOUR) " +
            "AND order_time < DATE_ADD(#{orderTime}, INTERVAL 1 HOUR)")
    int countCoachTimeConflicts(@Param("coachId") Integer coachId, @Param("orderTime") Date orderTime);

    @Select("SELECT o.order_id AS orderId, o.order_time AS orderTime, o.order_status AS orderStatus, " +
            "o.course_id AS courseId, c.course_name AS courseName, c.course_intro AS courseIntro, " +
            "coach.coach_name AS coachName, coach.coach_tel AS coachTel " +
            "FROM course_order o LEFT JOIN course c ON c.course_id = o.course_id " +
            "LEFT JOIN coach ON coach.coach_id = o.coach_id " +
            "WHERE o.member_id = #{memberId} ORDER BY o.order_time DESC, o.order_id DESC")
    List<Map<String, Object>> selectScheduleByMemberId(@Param("memberId") Integer memberId);

    @Select("SELECT o.order_id AS orderId, o.order_time AS orderTime, o.order_status AS orderStatus, " +
            "o.course_id AS courseId, o.member_id AS memberId, c.course_name AS courseName, " +
            "c.course_intro AS courseIntro, m.member_name AS memberName, m.member_tel AS memberTel " +
            "FROM course_order o LEFT JOIN course c ON c.course_id = o.course_id " +
            "LEFT JOIN member m ON m.member_id = o.member_id " +
            "WHERE o.coach_id = #{coachId} " +
            "AND o.order_status IN (" + OrderStatus.SQL_PENDING_IN + ") " +
            "ORDER BY o.order_time ASC, o.order_id ASC")
    List<Map<String, Object>> selectPendingByCoachId(@Param("coachId") Integer coachId);

    @Select("SELECT * FROM course_order WHERE order_id = #{orderId} AND coach_id = #{coachId} LIMIT 1")
    CourseOrder selectByIdAndCoachId(@Param("orderId") Integer orderId, @Param("coachId") Integer coachId);

    @Update("UPDATE course_order SET order_status = #{status} WHERE order_id = #{orderId}")
    int updateStatus(@Param("orderId") Integer orderId, @Param("status") String status);

    @Update("UPDATE course_order SET order_status = '" + OrderStatus.EXPIRED + "' " +
            "WHERE coach_id = #{coachId} AND order_time < NOW() " +
            "AND order_status IN (" + OrderStatus.SQL_PENDING_IN + ")")
    int expirePendingByCoachId(@Param("coachId") Integer coachId);

    @Update("UPDATE course_order SET order_status = '" + OrderStatus.EXPIRED + "' " +
            "WHERE order_time < NOW() AND order_status IN (" + OrderStatus.SQL_PENDING_IN + ")")
    int expireAllPending();

    @Select("SELECT o.order_id AS orderId, o.order_time AS orderTime, o.order_status AS orderStatus, " +
            "m.member_name AS memberName, m.member_tel AS memberTel, c.course_name AS courseName, c.course_intro AS courseIntro " +
            "FROM course_order o LEFT JOIN member m ON m.member_id = o.member_id " +
            "LEFT JOIN course c ON c.course_id = o.course_id " +
            "WHERE o.coach_id = #{coachId} AND o.order_status IN ('" + OrderStatus.RESERVED_SUCCESS + "') ORDER BY o.order_time ASC")
    List<Map<String, Object>> selectApprovedScheduleByCoachId(@Param("coachId") Integer coachId);

    @Select("SELECT o.order_id AS orderId, o.order_time AS orderTime, o.order_status AS orderStatus, " +
            "m.member_name AS memberName, m.member_tel AS memberTel, c.course_name AS courseName " +
            "FROM course_order o LEFT JOIN member m ON m.member_id = o.member_id " +
            "LEFT JOIN course c ON c.course_id = o.course_id " +
            "WHERE o.coach_id = #{coachId} " +
            "AND o.order_status IN ('" + OrderStatus.RESERVED_SUCCESS + "', '" + OrderStatus.ARRIVED + "', '" + OrderStatus.NOT_ARRIVED + "') " +
            "ORDER BY o.order_time ASC")
    List<Map<String, Object>> selectAttendanceByCoachId(@Param("coachId") Integer coachId);

    @Select("SELECT o.order_id AS orderId, o.order_time AS orderTime, " +
            "CASE WHEN o.order_status IN ('" + OrderStatus.EXPIRED + "', '" + OrderStatus.REJECTED + "') " +
            "THEN '" + OrderStatus.FAILED + "' ELSE o.order_status END AS orderStatus, " +
            "m.member_name AS memberName, m.member_tel AS memberTel, c.course_name AS courseName, co.coach_name AS coachName " +
            "FROM course_order o LEFT JOIN member m ON m.member_id = o.member_id " +
            "LEFT JOIN course c ON c.course_id = o.course_id " +
            "LEFT JOIN coach co ON co.coach_id = o.coach_id " +
            "ORDER BY o.order_time DESC, o.order_id DESC")
    List<Map<String, Object>> selectAllWithNames();
}
