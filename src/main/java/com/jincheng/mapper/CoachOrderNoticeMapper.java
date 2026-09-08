package com.jincheng.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CoachOrderNoticeMapper {

    @Select("""
            SELECT COUNT(1)
            FROM course_order o
            WHERE o.coach_id = #{coachId}
              AND o.order_status IN ('待教练查看', '已预约')
              AND NOT EXISTS (
                SELECT 1
                FROM coach_order_notice_read r
                WHERE r.coach_id = o.coach_id
                  AND r.order_id = o.order_id
              )
            """)
    int countUnreadPending(@Param("coachId") Integer coachId);

    @Insert("""
            INSERT IGNORE INTO coach_order_notice_read (coach_id, order_id, read_time)
            SELECT o.coach_id, o.order_id, NOW()
            FROM course_order o
            WHERE o.coach_id = #{coachId}
              AND o.order_status IN ('待教练查看', '已预约')
            """)
    int markAllPendingRead(@Param("coachId") Integer coachId);
}
