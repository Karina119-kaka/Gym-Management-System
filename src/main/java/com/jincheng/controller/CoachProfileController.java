package com.jincheng.controller;

import com.jincheng.entity.Coach;
import com.jincheng.service.CoachService;
import com.jincheng.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/coach")
public class CoachProfileController {

    @Autowired
    private CoachService coachService;

    @GetMapping("/info")
    public Result getInfo(@SessionAttribute(name = "coach", required = false) Coach coach) {
        if (coach != null) {
            Coach dbCoach = coachService.getCoachById(Long.valueOf(coach.getCoachId()));
            return Result.success(dbCoach);
        }
        return Result.error("未登录");
    }

    @PutMapping("/update")
    public Result updateInfo(@SessionAttribute(name = "coach", required = false) Coach coach,
                             @RequestBody Coach updatedCoach,
                             HttpSession session) {
        if (coach != null) {
            Coach dbCoach = coachService.getCoachById(Long.valueOf(coach.getCoachId()));
            if (dbCoach == null) {
                return Result.error("教练不存在");
            }
            updatedCoach.setCoachId(coach.getCoachId());
            updatedCoach.setCoachNo(dbCoach.getCoachNo());
            updatedCoach.setCoachName(dbCoach.getCoachName());
            updatedCoach.setCoachSex(dbCoach.getCoachSex());
            updatedCoach.setCoachStatus(dbCoach.getCoachStatus());
            updatedCoach.setCoachPwd(dbCoach.getCoachPwd());
            updatedCoach.setCreateTime(dbCoach.getCreateTime());
            coachService.updateCoach(updatedCoach);
            session.setAttribute("coach", updatedCoach);
            return Result.success("更新成功", updatedCoach);
        }
        return Result.error("未登录");
    }

    @PutMapping("/password")
    public Result updatePassword(@SessionAttribute(name = "coach", required = false) Coach coach,
                                 @RequestBody Map<String, String> passwordForm,
                                 HttpSession session) {
        if (coach == null) {
            return Result.error("未登录");
        }
        String oldPassword = passwordForm.get("oldPassword");
        String newPassword = passwordForm.get("newPassword");
        if (oldPassword == null || newPassword == null || oldPassword.isBlank() || newPassword.isBlank()) {
            return Result.error("请填写旧密码和新密码");
        }

        Coach dbCoach = coachService.getCoachById(Long.valueOf(coach.getCoachId()));
        if (dbCoach == null || !oldPassword.equals(dbCoach.getCoachPwd())) {
            return Result.error("旧密码错误");
        }

        dbCoach.setCoachPwd(newPassword);
        coachService.updateCoach(dbCoach);
        session.setAttribute("coach", dbCoach);
        return Result.success("密码修改成功");
    }
}
