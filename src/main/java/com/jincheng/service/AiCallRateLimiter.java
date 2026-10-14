package com.jincheng.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 调用限流器：每会员每天 N 次，内存计数，按天重置。
 *
 * 设计取舍：
 * - 用 ConcurrentHashMap 而非 Redis，单机部署足够；多实例需改 Redis。
 * - 不持久化计数，重启归零；兜底可查 ai_call_log 表，但当前不依赖。
 * - 命中限流时返回剩余次数，前端可友好提示。
 */
@Component
public class AiCallRateLimiter {

    @Value("${ai.rate-limit.daily:20}")
    private int dailyLimit;

    private final ConcurrentHashMap<Integer, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * 尝试消耗 1 次配额。
     * @return null 表示通过；非空字符串表示拒绝原因（前端可直接显示）
     */
    public String tryAcquire(Integer memberId) {
        if (memberId == null) return "未登录";
        LocalDate today = LocalDate.now();
        Bucket bucket = buckets.compute(memberId, (id, b) -> {
            if (b == null || !today.equals(b.day)) {
                return new Bucket(today);
            }
            return b;
        });
        synchronized (bucket) {
            // 跨日重置检查（防御 compute 后又被其他线程改写）
            if (!today.equals(bucket.day)) {
                bucket.day = today;
                bucket.count = 0;
            }
            if (bucket.count >= dailyLimit) {
                return "今日咨询次数已用完（" + dailyLimit + " 次/天），明天再来";
            }
            bucket.count++;
            return null;
        }
    }

    /**
     * 释放 1 次配额：用于调用失败时退还，避免空耗额度。
     */
    public void release(Integer memberId) {
        if (memberId == null) return;
        Bucket bucket = buckets.get(memberId);
        if (bucket == null) return;
        synchronized (bucket) {
            if (bucket.count > 0) bucket.count--;
        }
    }

    /**
     * 返回某会员当前剩余次数，用于前端展示"今日还可咨询 N 次"。
     */
    public int remaining(Integer memberId) {
        if (memberId == null) return 0;
        Bucket bucket = buckets.get(memberId);
        if (bucket == null || !LocalDate.now().equals(bucket.day)) return dailyLimit;
        synchronized (bucket) {
            return Math.max(0, dailyLimit - bucket.count);
        }
    }

    public int getDailyLimit() {
        return dailyLimit;
    }

    private static class Bucket {
        LocalDate day;
        int count;
        Bucket(LocalDate day) { this.day = day; this.count = 0; }
    }
}
