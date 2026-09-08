package com.jincheng.util;

public final class OrderStatus {
    private OrderStatus() {}

    public static final String PENDING_REVIEW = "待教练查看";
    public static final String RESERVED = "已预约";
    public static final String RESERVED_SUCCESS = "预约成功";
    public static final String REJECTED = "已拒绝";
    public static final String EXPIRED = "已失效";
    public static final String FAILED = "预约失败";
    public static final String ARRIVED = "已到店";
    public static final String NOT_ARRIVED = "未到店";
    public static final String PAID = "已购买";
    public static final String ON_SALE = "上架";
    public static final String REVIEWING = "待审核";

    public static boolean isActiveReservationStatus(String status) {
        return PENDING_REVIEW.equals(status)
                || RESERVED.equals(status)
                || RESERVED_SUCCESS.equals(status)
                || "预约中".equals(status);
    }
}
