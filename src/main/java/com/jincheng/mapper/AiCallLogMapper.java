package com.jincheng.mapper;

import com.jincheng.entity.AiCallLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * AI 调用日志 Mapper。
 * 记录每次 AI 问答的元数据（不存全量 answer，只存摘要以控制表大小）。
 */
@Mapper
public interface AiCallLogMapper {

    /**
     * 插入一条调用日志，返回自增主键。
     * answer 字段只在异常时存错误摘要，正常调用不存全量答案以避免表膨胀。
     */
    @Insert("""
            INSERT INTO ai_call_log
              (member_id, question, question_summary, answer, source, model,
               prompt_tokens, completion_tokens, total_tokens, cost_yuan,
               elapsed_ms, success, error_message, history_turns, create_time)
            VALUES
              (#{memberId}, #{question}, #{questionSummary}, #{answer}, #{source}, #{model},
               #{promptTokens}, #{completionTokens}, #{totalTokens}, #{costYuan},
               #{elapsedMs}, #{success}, #{errorMessage}, #{historyTurns}, #{createTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "logId")
    int insert(AiCallLog log);

    /**
     * 统计某会员今天的调用次数，用于限流兜底校验（实际限流在内存层）。
     */
    @Select("""
            SELECT COUNT(1)
            FROM ai_call_log
            WHERE member_id = #{memberId}
              AND DATE(create_time) = CURDATE()
            """)
    int countTodayByMemberId(@Param("memberId") Integer memberId);

    /**
     * 取最近一条调用记录的时间，可用于"上次咨询"展示。
     */
    @Select("""
            SELECT MAX(create_time)
            FROM ai_call_log
            WHERE member_id = #{memberId}
            """)
    Date selectLastCallTime(@Param("memberId") Integer memberId);

    /**
     * 今日 + 本月聚合：调用量、成功/失败/回退次数、成本、token、平均耗时、活跃会员数。
     * 单行结果，前端按 key 读取。
     */
    @Select("""
            SELECT
              SUM(CASE WHEN DATE(create_time) = CURDATE() THEN 1 ELSE 0 END) AS todayCount,
              SUM(CASE WHEN DATE(create_time) = CURDATE() AND success = 1 THEN 1 ELSE 0 END) AS todaySuccess,
              SUM(CASE WHEN DATE(create_time) = CURDATE() AND success = 0 THEN 1 ELSE 0 END) AS todayFail,
              SUM(CASE WHEN DATE(create_time) = CURDATE() AND source = 'fallback' THEN 1 ELSE 0 END) AS todayFallback,
              COALESCE(SUM(CASE WHEN DATE(create_time) = CURDATE() THEN cost_yuan ELSE 0 END), 0) AS todayCost,
              COALESCE(SUM(CASE WHEN DATE(create_time) = CURDATE() THEN total_tokens ELSE 0 END), 0) AS todayTokens,
              COUNT(DISTINCT CASE WHEN DATE(create_time) = CURDATE() THEN member_id END) AS todayMembers,
              AVG(CASE WHEN DATE(create_time) = CURDATE() THEN elapsed_ms END) AS todayAvgMs,
              SUM(CASE WHEN DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m') THEN 1 ELSE 0 END) AS monthCount,
              SUM(CASE WHEN DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m') AND success = 0 THEN 1 ELSE 0 END) AS monthFail,
              COALESCE(SUM(CASE WHEN DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m') THEN cost_yuan ELSE 0 END), 0) AS monthCost,
              COALESCE(SUM(CASE WHEN DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m') THEN total_tokens ELSE 0 END), 0) AS monthTokens
            FROM ai_call_log
            """)
    Map<String, Object> selectSummary();

    /**
     * 最近 N 天每日调用量与成本，用于趋势图。返回固定天数行（无数据日期由前端补零）。
     */
    @Select("""
            SELECT
              DATE(create_time) AS day,
              COUNT(1) AS count,
              COALESCE(SUM(cost_yuan), 0) AS cost,
              COALESCE(SUM(total_tokens), 0) AS tokens,
              SUM(CASE WHEN success = 0 THEN 1 ELSE 0 END) AS failCount
            FROM ai_call_log
            WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)
            GROUP BY DATE(create_time)
            ORDER BY day
            """)
    List<Map<String, Object>> selectDailySeries(@Param("days") int days);

    /**
     * 最近调用明细（联表取会员名/账号），用于管理端日志列表。
     */
    @Select("""
            SELECT
              l.log_id        AS logId,
              l.member_id     AS memberId,
              m.Member_no     AS memberNo,
              m.Member_name   AS memberName,
              l.question_summary AS questionSummary,
              l.source        AS source,
              l.model         AS model,
              l.total_tokens  AS totalTokens,
              l.cost_yuan     AS costYuan,
              l.elapsed_ms    AS elapsedMs,
              l.success       AS success,
              l.error_message AS errorMessage,
              l.history_turns AS historyTurns,
              l.create_time   AS createTime
            FROM ai_call_log l
            LEFT JOIN member m ON m.Member_id = l.member_id
            ORDER BY l.create_time DESC, l.log_id DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectRecent(@Param("limit") int limit);

    /**
     * 本月调用次数最多的会员排行。
     */
    @Select("""
            SELECT
              l.member_id AS memberId,
              m.Member_no AS memberNo,
              m.Member_name AS memberName,
              COUNT(1) AS count,
              COALESCE(SUM(l.cost_yuan), 0) AS cost
            FROM ai_call_log l
            LEFT JOIN member m ON m.Member_id = l.member_id
            WHERE DATE_FORMAT(l.create_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')
            GROUP BY l.member_id, m.Member_no, m.Member_name
            ORDER BY count DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectTopMembers(@Param("limit") int limit);
}
