package com.jincheng.mapper;

import com.jincheng.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface MemberMapper {
    
    @Select("SELECT * FROM member WHERE member_no = #{memberNo}")
    Member selectByMemberNo(@Param("memberNo") String memberNo);
    
    @Select("SELECT * FROM member")
    List<Member> selectAll();
    
    @Insert("INSERT INTO member (member_no, member_name, member_sex, member_age, member_tel, card_status, member_pwd, create_time) " +
            "VALUES (#{memberNo}, #{memberName}, #{memberSex}, #{memberAge}, #{memberTel}, #{cardStatus}, #{memberPwd}, #{createTime})")
    int insert(Member member);

    @Update("UPDATE member SET member_no=#{memberNo}, member_name=#{memberName}, member_sex=#{memberSex}, member_age=#{memberAge}, " +
            "member_tel=#{memberTel}, card_status=#{cardStatus}, member_pwd=#{memberPwd} WHERE member_id=#{memberId}")
    int updateById(Member member);

    @Delete("DELETE FROM member WHERE member_id = #{id}")
    int deleteById(@Param("id") Integer id);
    
    @Select("SELECT * FROM member WHERE member_id = #{id}")
    Member selectById(@Param("id") Integer id);
}
