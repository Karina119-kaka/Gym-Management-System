package com.jincheng.service.impl;

import com.jincheng.entity.Admin;
import com.jincheng.mapper.AdminMapper;
import com.jincheng.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public Admin getByAdminNo(String adminNo) {
        return adminMapper.selectByAdminNo(adminNo);
    }

    @Override
    public Admin getById(Integer adminId) {
        return adminMapper.selectById(adminId);
    }

    @Override
    public boolean updateAdmin(Admin admin) {
        return adminMapper.updateById(admin) > 0;
    }
}
