package com.jincheng.service;

import com.jincheng.entity.Member;

import java.util.Map;

/**
 * AI 健身顾问服务。
 * 支持两种引擎：
 * 1) local：内置健身知识库规则引擎（默认，离线可用）
 * 2) llm：接入 OpenAI 兼容协议的大模型（需在 application.yml 配置 ai.base-url / api-key / model）
 * 大模型不可用时会自动回退到本地知识库，保证功能永远可用。
 */
public interface AiAdviceService {

    /**
     * 根据会员提问与其个人数据给出健身建议。
     */
    Map<String, Object> advise(Member member, String question);

    /**
     * 当前使用的引擎信息，供前端展示。
     */
    Map<String, Object> engineInfo();
}
