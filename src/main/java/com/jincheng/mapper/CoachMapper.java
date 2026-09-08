package com.jincheng.mapper;

import com.jincheng.entity.Coach;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface CoachMapper {
    
    @Select("SELECT * FROM coach WHERE coach_no = #{coachNo}")
    Coach selectByCoachNo(@Param("coachNo") String coachNo);
    
    @Select("SELECT * FROM coach")
    List<Coach> selectAll();
    
    @Insert("INSERT INTO coach (coach_no, coach_name, coach_sex, coach_age, coach_tel, good_at, coach_status, coach_pwd, create_time) " +
            "VALUES (#{coachNo}, #{coachName}, #{coachSex}, #{coachAge}, #{coachTel}, #{goodAt}, #{coachStatus}, #{coachPwd}, #{createTime})")
    int insert(Coach coach);

    @Update("UPDATE coach SET coach_no=#{coachNo}, coach_name=#{coachName}, coach_sex=#{coachSex}, coach_age=#{coachAge}, " +
            "coach_tel=#{coachTel}, good_at=#{goodAt}, coach_status=#{coachStatus}, coach_pwd=#{coachPwd} WHERE coach_id=#{coachId}")
    int updateById(Coach coach);

    @Delete("DELETE FROM coach WHERE coach_id = #{id}")
    int deleteById(@Param("id") Integer id);
    
    @Select("SELECT * FROM coach WHERE coach_id = #{id}")
    Coach selectById(@Param("id") Integer id);
}
