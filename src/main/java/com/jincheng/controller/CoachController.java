package com.jincheng.controller;

import com.jincheng.entity.Coach;
import com.jincheng.service.CoachService;
import com.jincheng.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coach")
public class CoachController {

    @Autowired
    private CoachService coachService;

    @GetMapping("/list")
    public Result list() {
        List<Coach> coaches = coachService.getAllCoaches();
        return Result.success(coaches);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Coach coach = coachService.getCoachById(id);
        if (coach != null) {
            return Result.success(coach);
        }
        return Result.error("教练不存在");
    }

    @PostMapping
    public Result add(@RequestBody Coach coach) {
        if (coachService.addCoach(coach)) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping
    public Result update(@RequestBody Coach coach) {
        if (coachService.updateCoach(coach)) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        if (coachService.deleteCoach(id)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    @PutMapping("/status/{id}")
    public Result updateStatus(@PathVariable Long id, @RequestParam String status) {
        Coach coach = coachService.getCoachById(id);
        if (coach != null) {
            coach.setCoachStatus(status);
            coachService.updateCoach(coach);
            return Result.success("状态更新成功");
        }
        return Result.error("教练不存在");
    }
}
