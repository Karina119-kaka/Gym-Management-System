package com.jincheng.mapper;

import com.jincheng.entity.ClockIn;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface ClockInMapper {

    @Insert("INSERT INTO clock_in (member_id, clock_time) VALUES (#{memberId}, #{clockTime})")
    int insertStart(ClockIn clockIn);

    @Select("SELECT * FROM clock_in WHERE member_id = #{memberId} ORDER BY clock_time DESC")
    List<ClockIn> selectByMemberId(@Param("memberId") Integer memberId);

    @Select("""
            SELECT
              c.clock_id AS clockId,
              c.member_id AS memberId,
              c.clock_time AS clockTime,
              c.weight_kg AS weightKg,
              c.body_fat_pct AS bodyFatPct,
              c.bmi AS bmi,
              c.calories AS sessionCalories,
              c.data_source AS dataSource
            FROM clock_in c
            WHERE c.member_id = #{memberId}
            ORDER BY c.clock_time DESC
            """)
    List<Map<String, Object>> selectRecordDetailsByMemberId(@Param("memberId") Integer memberId);

    @Select("""
            SELECT COUNT(1)
            FROM clock_in
            WHERE member_id = #{memberId}
              AND DATE(clock_time) = CURDATE()
            """)
    int countTodayByMemberId(@Param("memberId") Integer memberId);

    @Select("""
            SELECT COUNT(1)
            FROM clock_in
            WHERE member_id = #{memberId}
              AND DATE_FORMAT(clock_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')
            """)
    int countThisMonthByMemberId(@Param("memberId") Integer memberId);

    @Select("SELECT MAX(clock_time) FROM clock_in WHERE member_id = #{memberId}")
    Date selectLastClockTime(@Param("memberId") Integer memberId);

    @Select("""
            SELECT
              weight_kg AS weightKg,
              body_fat_pct AS bodyFatPct,
              bmi AS bmi,
              data_source AS dataSource,
              DATE(clock_time) AS recordDate
            FROM clock_in
            WHERE member_id = #{memberId}
            ORDER BY clock_time DESC, clock_id DESC
            LIMIT 1
            """)
    Map<String, Object> selectLatestMetricsByMemberId(@Param("memberId") Integer memberId);

    @Select("""
            SELECT COALESCE(SUM(calories), 0)
            FROM clock_in
            WHERE member_id = #{memberId}
              AND DATE_FORMAT(clock_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')
            """)
    Integer sumThisMonthCaloriesByMemberId(@Param("memberId") Integer memberId);

    @Select("""
            SELECT clock_id
            FROM clock_in
            WHERE member_id = #{memberId}
              AND DATE(clock_time) = CURDATE()
            ORDER BY clock_time DESC, clock_id DESC
            LIMIT 1
            """)
    Integer selectTodayLatestClockId(@Param("memberId") Integer memberId);

    @Update("""
            UPDATE clock_in
            SET
              weight_kg = #{weightKg},
              body_fat_pct = #{bodyFatPct},
              bmi = #{bmi},
              calories = #{calories},
              data_source = #{dataSource}
            WHERE clock_id = #{clockId}
            """)
    int updateMetricsByClockId(@Param("clockId") Integer clockId,
                                 @Param("weightKg") Double weightKg,
                                 @Param("bodyFatPct") Double bodyFatPct,
                                 @Param("bmi") Double bmi,
                                 @Param("calories") Integer calories,
                                 @Param("dataSource") String dataSource);
}
