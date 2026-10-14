package com.jincheng.util;

/**
 * 购买记录支付状态（course_buy.pay_status）常量。
 *
 * 全系统状态值统一为中文，禁止再出现 PAID / UNPAID 等英文历史值。
 */
public final class PayStatus {
    private PayStatus() {}

    /** 已购买：会员已完成购买，持有一次可预约课时 */
    public static final String PAID = "已购买";
}
