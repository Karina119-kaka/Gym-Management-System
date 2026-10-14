package com.jincheng.util;

/**
 * 课程销售状态（course.sale_status）常量。
 *
 * 全系统状态值统一为中文，禁止再出现 ON_SALE / OFF_SALE 等英文历史值。
 * 状态流转：待审核 --(管理员审核通过)--> 上架 --(管理员/教练下架)--> 下架
 */
public final class CourseStatus {
    private CourseStatus() {}

    /** 待审核：教练发布后等待管理员审核 */
    public static final String REVIEWING = "待审核";
    /** 上架：审核通过，会员可见可购买 */
    public static final String ON_SALE = "上架";
    /** 下架：管理员拒绝或教练主动下架，会员不可见 */
    public static final String OFF_SALE = "下架";

    /** 是否为「已上架」（会员可购买）状态 */
    public static boolean isOnSale(String status) {
        return ON_SALE.equals(status);
    }
}
