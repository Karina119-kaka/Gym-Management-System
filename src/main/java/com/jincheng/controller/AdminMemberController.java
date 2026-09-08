package com.jincheng.controller;

import com.jincheng.entity.Member;
import com.jincheng.service.MemberService;
import com.jincheng.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/admin/member")
public class AdminMemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/list")
    public Result list() {
        List<Member> members = memberService.getAllMembers();
        return Result.success(members);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Member member = memberService.getMemberById(id);
        if (member != null) {
            return Result.success(member);
        }
        return Result.error("会员不存在");
    }

    @PostMapping
    public Result add(@RequestBody Member member) {
        member.setCreateTime(new Date());
        if (memberService.addMember(member)) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping
    public Result update(@RequestBody Member member) {
        if (memberService.updateMember(member)) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        if (memberService.deleteMember(id)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    @PutMapping("/status/{id}")
    public Result updateStatus(@PathVariable Long id, @RequestParam String status) {
        Member member = memberService.getMemberById(id);
        if (member != null) {
            member.setCardStatus(status);
            memberService.updateMember(member);
            return Result.success("状态更新成功");
        }
        return Result.error("会员不存在");
    }
}
