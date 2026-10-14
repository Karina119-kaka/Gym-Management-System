package com.jincheng.util;

/**
 * 预约状态（course_order.order_status）常量。
 *
 * 全系统状态值统一为中文，禁止再出现英文历史值。
 * 课程上下架状态见 {@link CourseStatus}，支付状态见 {@link PayStatus}。
 */
public final class OrderStatus {
    private OrderStatus() {}

    /** 待教练查看：会员提交预约后的初始状态 */
    public static final String PENDING_REVIEW = "待教练查看";
    /** 已预约：教练已接单，等待上课 */
    public static final String RESERVED = "已预约";
    /** 预约成功：教练已确认 */
    public static final String RESERVED_SUCCESS = "预约成功";
    /** 预约中 */
    public static final String RESERVING = "预约中";
    /** 已拒绝：教练拒绝了该预约 */
    public static final String REJECTED = "已拒绝";
    /** 已失效：超时未处理或已取消 */
    public static final String EXPIRED = "已失效";
    /** 预约失败：展示用的聚合状态（已拒绝/已失效） */
    public static final String FAILED = "预约失败";
    /** 已到店：教练考勤标记 */
    public static final String ARRIVED = "已到店";
    /** 未到店：教练考勤标记 */
    public static final String NOT_ARRIVED = "未到店";

    /** SQL 片段：仍处于有效状态的预约（会占用一次已购课时） */
    public static final String SQL_ACTIVE_IN =
            "'" + PENDING_REVIEW + "', '" + RESERVED_SUCCESS + "', '" + RESERVED + "', '" + RESERVING + "'";

    /** SQL 片段：等待教练处理的预约 */
    public static final String SQL_PENDING_IN =
            "'" + PENDING_REVIEW + "', '" + RESERVED + "'";

    /** 是否为仍处于有效状态的预约 */
    public static boolean isActiveReservationStatus(String status) {
        return PENDING_REVIEW.equals(status)
                || RESERVED.equals(status)
                || RESERVED_SUCCESS.equals(status)
                || RESERVING.equals(status);
    }
}
