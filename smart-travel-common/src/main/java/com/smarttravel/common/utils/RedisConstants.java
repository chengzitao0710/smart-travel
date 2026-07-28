package com.smarttravel.common.utils;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 5L;
    public static final String LOGIN_USER_KEY = "user:token:";
    public static final Long LOGIN_USER_TTL = 36000L;

    public static final String CACHE_SCENIC_KEY = "cache:scenic:";
    public static final Long CACHE_SCENIC_TTL = 30L;

    public static final String CACHE_SCENIC_TYPE_KEY = "cache:scenic:type:";

    public static final String LOCK_SECKILL_KET = "lock:seckill:";
    public static final Long LOCK_SECKILL_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String SECKILL_ORDER_KEY = "seckill:order:";

    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String COMMENT_LIKED_KEY = "comment:liked:";
    public static final String BLOG_HOT_KEY = "blog:hot:";

    public static final String FOLLOW_KEY = "follow:";
    public static final String USER_SIGN_KEY = "user:sign:";

    public static final String SCENIC_GEO_KEY = "scenic:geo:";
}