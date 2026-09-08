package com.jincheng.controller;

import com.jincheng.entity.Admin;
import com.jincheng.entity.Coach;
import com.jincheng.entity.Member;
import com.jincheng.service.AdminService;
import com.jincheng.service.CoachService;
import com.jincheng.service.MemberService;
import com.jincheng.util.Result;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private CoachService coachService;

    @Autowired
    private AdminService adminService;

    @PostMapping("/member")
    public Result memberLogin(@RequestParam String memberNo,
                              @RequestParam String memberPwd,
                              @RequestParam String code,
                              HttpSession session) {
        Result codeError = validateCodeOrError(code, session);
        if (codeError != null) {
            return codeError;
        }

        Member member = memberService.getByMemberNo(memberNo);
        if (member != null && memberPwd.equals(member.getMemberPwd())) {
            session.setAttribute("member", member);
            session.setAttribute("role", "member");
            return Result.success("登录成功", member);
        }
        return Result.error("账号或密码错误");
    }

    @PostMapping("/coach")
    public Result coachLogin(@RequestParam String coachNo,
                             @RequestParam String coachPwd,
                             @RequestParam String code,
                             HttpSession session) {
        Result codeError = validateCodeOrError(code, session);
        if (codeError != null) {
            return codeError;
        }

        Coach coach = coachService.getByCoachNo(coachNo);
        if (coach != null && coachPwd.equals(coach.getCoachPwd())) {
            session.setAttribute("coach", coach);
            session.setAttribute("role", "coach");
            return Result.success("登录成功", coach);
        }
        return Result.error("账号或密码错误");
    }

    @PostMapping("/admin")
    public Result adminLogin(@RequestParam String adminNo,
                             @RequestParam String adminPwd,
                             @RequestParam String code,
                             HttpSession session) {
        Result codeError = validateCodeOrError(code, session);
        if (codeError != null) {
            return codeError;
        }

        Admin admin = adminService.getByAdminNo(adminNo);
        if (admin != null && adminPwd.equals(admin.getAdminPwd())) {
            session.setAttribute("admin", admin);
            session.setAttribute("role", "admin");
            return Result.success("登录成功", admin);
        }
        return Result.error("账号或密码错误");
    }

    @PostMapping("/logout")
    public Result logout(HttpSession session) {
        session.invalidate();
        return Result.success("退出成功");
    }

    private Result validateCodeOrError(String code, HttpSession session) {
        if (!validateCode(code, session)) {
            return Result.error("验证码错误");
        }
        return null;
    }

    private boolean validateCode(String code, HttpSession session) {
        String verifyCode = (String) session.getAttribute("verifyCode");
        return verifyCode != null && verifyCode.equalsIgnoreCase(code);
    }
}
