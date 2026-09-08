package com.jincheng.controller;

import com.jincheng.entity.Admin;
import com.jincheng.service.AdminService;
import com.jincheng.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/profile")
public class AdminProfileController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/info")
    public Result getInfo(@SessionAttribute(name = "admin", required = false) Admin admin) {
        if (admin == null) {
            return Result.error("未登录");
        }
        Admin dbAdmin = adminService.getById(admin.getAdminId());
        return Result.success(dbAdmin);
    }

    @PutMapping("/update")
    public Result updateInfo(@SessionAttribute(name = "admin", required = false) Admin admin,
                             @RequestBody Admin updatedAdmin,
                             HttpSession session) {
        if (admin == null) {
            return Result.error("未登录");
        }
        Admin dbAdmin = adminService.getById(admin.getAdminId());
        if (dbAdmin == null) {
            return Result.error("管理员不存在");
        }
        dbAdmin.setAdminName(updatedAdmin.getAdminName());
        if (updatedAdmin.getAdminSex() != null) {
            dbAdmin.setAdminSex(updatedAdmin.getAdminSex());
        }
        if (updatedAdmin.getAdminTel() != null) {
            dbAdmin.setAdminTel(updatedAdmin.getAdminTel());
        }
        adminService.updateAdmin(dbAdmin);
        session.setAttribute("admin", dbAdmin);
        return Result.success("更新成功", dbAdmin);
    }

    @PutMapping("/password")
    public Result updatePassword(@SessionAttribute(name = "admin", required = false) Admin admin,
                                 @RequestBody Map<String, String> passwordForm,
                                 HttpSession session) {
        if (admin == null) {
            return Result.error("未登录");
        }
        String oldPassword = passwordForm.get("oldPassword");
        String newPassword = passwordForm.get("newPassword");
        if (oldPassword == null || newPassword == null || oldPassword.isBlank() || newPassword.isBlank()) {
            return Result.error("请填写旧密码和新密码");
        }

        Admin dbAdmin = adminService.getById(admin.getAdminId());
        if (dbAdmin == null || !oldPassword.equals(dbAdmin.getAdminPwd())) {
            return Result.error("旧密码错误");
        }

        dbAdmin.setAdminPwd(newPassword);
        adminService.updateAdmin(dbAdmin);
        session.setAttribute("admin", dbAdmin);
        return Result.success("密码修改成功");
    }
}
