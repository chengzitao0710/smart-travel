package com.smarttravel.common.utils;

public class SystemConstants {
    public static final int MAX_PAGE_SIZE = 5;
    public static final int DEFAULT_PAGE_SIZE = 10;


    public static final String USER_NICK_NAME_PREFIX = "user_";

    public static final String PASSWORD_SALT = "smart_travel_salt";

    public static final Integer SCENIC_STATUS_ON = 1;

    public static final Integer BLOG_STATUS_DRAFT = 0;
    public static final Integer BLOG_STATUS_PUBLISHED = 1;
    public static final Integer BLOG_STATUS_REVIEWING = 2;
    public static final Integer BLOG_STATUS_REJECTED = 3;
    public static final Integer BLOG_STATUS_DELETED = 4;

    public static final Integer BLOG_HOT_START = 0;
    public static final Integer BLOG_HOT_END = 9;

    public static final Long COMMENT_ROOT_PARENT_ID = 0L;
    public static final Long COMMENT_ROOT_ANSWER_ID = 0L;
    public static final Integer COMMENT_DEFAULT_LIKED = 0;
    public static final Integer COMMENT_STATUS_NORMAL = 0;
    public static final Integer COMMENT_STATUS_REPORTED = 1;
    public static final Integer COMMENT_STATUS_BANNED = 2;
    public static final Integer COMMENT_PAGE_SIZE = 20;


    public static final Integer TICKET_STATUS_NORMAL = 1;
    public static final Integer TICKET_STATUS_SECKILL = 2;

    public static final Integer ORDER_STATUS_UNPAID = 1;
    public static final Integer ORDER_STATUS_PAID = 2;
    public static final Integer ORDER_STATUS_VERIFIED = 3;
    public static final Integer ORDER_STATUS_CANCELED = 4;
    public static final Integer ORDER_STATUS_REFUNDING = 5;
    public static final Integer ORDER_STATUS_REFUNDED = 6;

    public static final Long SECKILL_STOCK_ERROR = 1L;
    public static final Long SECKILL_ORDER_ERROR = 2L;

    public static final Integer PAY_TYPE_BALANCE = 1;
    public static final Integer PAY_TYPE_ALIPAY = 2;
    public static final Integer PAY_TYPE_WECHAT = 3;

    public static final Integer ROUTE_STATUS_ON = 1;
    public static final Integer ROUTE_STATUS_OFF = 0;

    public static final Integer IS_HOT_YES = 1;
    public static final Integer IS_HOT_NO = 0;
}