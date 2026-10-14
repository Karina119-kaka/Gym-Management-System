-- =====================================================================
-- 迁移脚本：AI 调用日志表
-- 用于追踪每次 AI 问答的成本、Token、是否回退、错误信息等
-- 适用：已部署的系统升级，不影响现有数据
-- 使用方法：mysql -uroot -p gym_management_system < migration_ai_call_log.sql
-- =====================================================================

USE gym_management_system;

CREATE TABLE IF NOT EXISTS ai_call_log (
    log_id             BIGINT AUTO_INCREMENT COMMENT '日志ID' PRIMARY KEY,
    member_id          INT             NOT NULL COMMENT '会员ID',
    question           VARCHAR(500)    NOT NULL COMMENT '原始问题（最多200字）',
    question_summary   VARCHAR(120)    NULL     COMMENT '问题摘要（前100字+…）',
    answer             MEDIUMTEXT      NULL     COMMENT 'AI回答（异常时存错误摘要，正常为空以控制表大小）',
    source             VARCHAR(20)    NULL     COMMENT 'llm / local / fallback',
    model              VARCHAR(50)     NULL     COMMENT '调用的模型名',
    prompt_tokens      INT             NULL     COMMENT '输入token数',
    completion_tokens  INT             NULL     COMMENT '输出token数',
    total_tokens       INT             NULL     COMMENT '总token数',
    cost_yuan          DECIMAL(10, 4)  NULL     COMMENT '估算成本（元）',
    elapsed_ms         INT             NOT NULL COMMENT '本次调用耗时(毫秒)',
    success            TINYINT(1)      NOT NULL COMMENT '1=成功 0=异常',
    error_message      VARCHAR(500)    NULL     COMMENT '失败时的错误信息',
    history_turns      INT             NULL     COMMENT '本次请求带的历史轮数',
    create_time        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
    INDEX idx_member_day (member_id, create_time),
    INDEX idx_create_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI 调用日志表';
