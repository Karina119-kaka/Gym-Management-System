package com.jincheng.service;

import com.jincheng.entity.Admin;

public interface AdminService {
    Admin getByAdminNo(String adminNo);
    Admin getById(Integer adminId);
    boolean updateAdmin(Admin admin);
}
