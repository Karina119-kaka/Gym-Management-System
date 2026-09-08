package com.jincheng.controller;

import com.jincheng.entity.Member;
import com.jincheng.service.MemberService;
import com.jincheng.util.Result;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/info")
    public Result getInfo(@SessionAttribute(name = "member", required = false) Member member) {
        if (member != null) {
            Member dbMember = memberService.getMemberById(Long.valueOf(member.getMemberId()));
            return Result.success(dbMember);
        }
        return Result.error("未登录");
    }

    @PutMapping("/update")
    public Result updateInfo(@SessionAttribute(name = "member", required = false) Member member,
                             @RequestBody Member updatedMember,
                             HttpSession session) {
        if (member == null) {
            return Result.error("未登录");
        }

        Member dbMember = memberService.getMemberById(Long.valueOf(member.getMemberId()));
        if (dbMember == null) {
            return Result.error("会员不存在");
        }

        updatedMember.setMemberId(dbMember.getMemberId());
        updatedMember.setMemberNo(dbMember.getMemberNo());
        updatedMember.setCardStatus(dbMember.getCardStatus());
        updatedMember.setMemberPwd(dbMember.getMemberPwd());
        updatedMember.setCreateTime(dbMember.getCreateTime());
        memberService.updateMember(updatedMember);

        Member latest = memberService.getMemberById(Long.valueOf(dbMember.getMemberId()));
        session.setAttribute("member", latest);
        return Result.success("更新成功", latest);
    }

    @PutMapping("/password")
    public Result updatePassword(@SessionAttribute(name = "member", required = false) Member member,
                                 @RequestBody Map<String, String> passwordForm,
                                 HttpSession session) {
        if (member == null) {
            return Result.error("未登录");
        }
        String oldPassword = passwordForm.get("oldPassword");
        String newPassword = passwordForm.get("newPassword");
        if (oldPassword == null || newPassword == null || oldPassword.isBlank() || newPassword.isBlank()) {
            return Result.error("请填写旧密码和新密码");
        }

        Member dbMember = memberService.getMemberById(Long.valueOf(member.getMemberId()));
        if (dbMember == null || !oldPassword.equals(dbMember.getMemberPwd())) {
            return Result.error("旧密码错误");
        }

        dbMember.setMemberPwd(newPassword);
        memberService.updateMember(dbMember);
        session.setAttribute("member", dbMember);
        return Result.success("密码修改成功");
    }
}
