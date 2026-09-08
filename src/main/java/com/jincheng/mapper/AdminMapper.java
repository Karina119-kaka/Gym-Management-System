package com.jincheng.mapper;

import com.jincheng.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminMapper {
    
    @Select("SELECT * FROM admin WHERE admin_no = #{adminNo}")
    Admin selectByAdminNo(@Param("adminNo") String adminNo);

    @Select("SELECT * FROM admin WHERE admin_id = #{adminId}")
    Admin selectById(@Param("adminId") Integer adminId);

    @Update("UPDATE admin SET admin_name=#{adminName}, admin_sex=#{adminSex}, admin_tel=#{adminTel}, admin_pwd=#{adminPwd} WHERE admin_id=#{adminId}")
    int updateById(Admin admin);
}
