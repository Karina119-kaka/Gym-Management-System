package com.jincheng.service;

import com.jincheng.entity.Member;

import java.util.List;
import java.util.Map;

/**
 * AI 健身顾问服务。
 * 支持两种引擎：
 * 1) local：内置健身知识库规则引擎（默认，离线可用）
 * 2) llm：接入 OpenAI 兼容协议的大模型（需在 application.yml 配置 ai.base-url / api-key / model）
 * 大模型不可用时会自动回退到本地知识库，保证功能永远可用。
 *
 * 多轮对话：调用方传入历史消息（role=user/assistant, content=文本），
 * LLM 模式会原样转发给大模型；本地模式会把上一轮用户提问拼到上下文里，
 * 使规则引擎的回答至少能承接上文。
 */
public interface AiAdviceService {

    /**
     * 根据会员提问、个人数据与历史对话给出健身建议（同步返回完整结果）。
     */
    Map<String, Object> advise(Member member, String question, List<Map<String, String>> history);

    /**
     * 流式输出：通过 handler 推送元信息与文本增量，便于前端 SSE 逐字渲染。
     * 实现方在开始输出前先回调 onMeta（携带 context/tips/fallback），
     * 随后对每个文本片段回调 onChunk。
     */
    void adviseStream(Member member, String question, List<Map<String, String>> history, StreamHandler handler);

    /**
     * 当前使用的引擎信息，供前端展示。
     */
    Map<String, Object> engineInfo();

    /** 流式回调：onMeta 推送一次元信息，onChunk 推送若干文本增量 */
    interface StreamHandler {
        default void onMeta(Map<String, Object> meta) throws Exception {}
        void onChunk(String chunk) throws Exception;
    }
}
