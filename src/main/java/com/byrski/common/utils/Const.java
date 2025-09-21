package com.byrski.common.utils;

import com.byrski.domain.enums.TradeStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

public class Const {

    public static final String OSS_BUCKET = "byrski";

    public static final String JWT_BLACK_LIST = "jwt:blacklist:";
    public static final int ORDER_CORS = -102;


    //邮件验证码
    public final static String VERIFY_EMAIL_LIMIT = "verify:email:limit:";
    public final static String VERIFY_EMAIL_DATA = "verify:email:data:";

    // 邮箱注册锁
    public final static String RESET_EMAIL_LOCK = "reset:email:lock:";
    public final static String REGISTER_EMAIL_LOCK = "register:email:lock:";
    public final static String VERIFY_EMAIL_SEND_LOCK = "verify:email:send:";

    // IP黑名单
    public static final String IP_BLACKLIST_KEY = "ip:blacklist:";
    public static final String IP_REQUEST_COUNT_KEY = "ip:request:count:";
    public static final String IP_BLOCKED_KEY = "ip:blocked:";

    public static final String TRADE_STATION_RECORD = "trade:station:record:";

    // 消息队列
    public static final String QUEUE_TRADE_TTL = "trade.ttl";
    public static final String QUEUE_TRADE_DEAD = "trade.dead";
    public static final String QUEUE_EMAIL = "mail";

    // 默认过期时间
    public static final int DEFAULT_EXPIRE_TIME = 300;

    public static final String API_STATS_KEY_PREFIX = "api:stats:";
    public static final String API_DAILY_STATS_KEY = API_STATS_KEY_PREFIX + "daily:";
    public static final String API_TOTAL_STATS_KEY = API_STATS_KEY_PREFIX + "total";
    public static final int STATS_EXPIRE_DAYS = 30;

    // 不支持退款的状态
    public static final Set<Integer> invalidRefundStatuses = Set.of(
            TradeStatus.REFUNDING.getCode(),
            TradeStatus.CANCELLED.getCode(),
            TradeStatus.LOCKED.getCode(),
            TradeStatus.REFUNDED.getCode(),
            TradeStatus.LEADER.getCode(),
            TradeStatus.RETURN.getCode()
    );

    public static final Map<Integer, Set<Integer>> STATUS_MAPPING = new HashMap<>(Map.ofEntries(
            entry(1, Set.of(TradeStatus.UNPAID.getCode())),
            entry(2, Set.of(TradeStatus.PAID.getCode(), TradeStatus.LOCKED.getCode(), TradeStatus.CONFIRMING.getCode(), TradeStatus.RETURN.getCode())),
            entry(3, Set.of(TradeStatus.PAID.getCode(), TradeStatus.LOCKED.getCode(), TradeStatus.CONFIRMING.getCode(), TradeStatus.RETURN.getCode())),
            entry(4, Set.of(TradeStatus.REFUNDING.getCode(), TradeStatus.REFUNDED.getCode(), TradeStatus.CANCELLED.getCode()))
    ));
}
