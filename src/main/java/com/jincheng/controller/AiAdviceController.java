package com.jincheng.controller;

import com.jincheng.entity.Member;
import com.jincheng.service.AiAdviceService;
import com.jincheng.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/member/ai")
public class AiAdviceController {

    private static final int MAX_QUESTION_LENGTH = 200;

    @Autowired
    private AiAdviceService aiAdviceService;

    @GetMapping("/info")
    public Result engineInfo() {
        return Result.success(aiAdviceService.engineInfo());
    }

    @PostMapping("/advice")
    public Result advice(@SessionAttribute(name = "member", required = false) Member member,
                         @RequestBody(required = false) Map<String, Object> body) {
        if (member == null) {
            return Result.error("未登录");
        }
        String question = body == null ? "" : Objects.toString(body.get("question"), "");
        if (question.length() > MAX_QUESTION_LENGTH) {
            question = question.substring(0, MAX_QUESTION_LENGTH);
        }
        return Result.success(aiAdviceService.advise(member, question));
    }
}
