package com.byrski.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum ReturnCode {

    // 通用状态码
    SUCCESS(0, "成功"),
    FAIL(-1, "系统繁忙，请稍后再试"),

    // 客户端错误 4xx
    PARAM_EXCEPTION(400, "参数错误"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "您没有相关的权限"),
    NOT_FOUND(404, "您访问的对象不存在"),

    // 服务端错误 5xx
    HTTP_ERROR(500, "HTTP请求失败"),
    DATABASE_ERROR(501, "服务端与数据库访问错误"),

    // 业务错误码 42xxxx
    OTHER_ERROR(420501, "其他错误"),
    ACTIVITY_DEADLINE_ERROR(420502, "活动截止报名且上车点可用，无法退票"),
    ACTIVITY_LOCKED_ERROR(420503, "活动已经进入锁票阶段，无法退票"),
    ACTIVITY_NOT_EXIST(420504, "活动不存在"),
    TICKET_NOT_EXIST(420505, "票不存在"),
    TRADE_REFUNDING(420506, "该订单正在退款中"),
    TRADE_REFUNDED(420507, "该订单已退款"),
    TRADE_CANCELLED(420508, "该订单已取消"),
    TRADE_LOCKED(420509, "该订单已锁定"),
    TRADE_UNPAID(420510, "该订单未付款"),
    TRADE_LEADER(420511, "该订单为领队订单"),
    TRADE_RETURN(420512, "该订单已结束"),
    TRADE_EXISTS(420513, "该活动已存在订单"),
    TRADE_NOT_EXIST(4205014, "订单不存在"),
    TRADE_NOT_FINISH(420515, "订单尚未完成"),
    TRADE_CHECKED(420516, "该订单已核销"),
    ACTIVITY_TEMPLATE_NOT_EXIST(420517, "活动模板不存在"),
    AREA_NOT_EXIST(420518, "区域不存在"),
    STATION_NOT_EXIST(420519, "站点不存在"),
    STATION_INFO_NOT_EXIST(420520, "该站点信息不存在"),
    SNOWFIELD_NOT_EXIST(420521, "雪场不存在"),
    WXGROUP_NOT_EXIST(420522, "微信群不存在"),
    BUS_NOT_EXIST(420523, "车辆不存在"),
    BUS_MOVE_NOT_EXIST(420524, "车次不存在"),
    GUIDE_FINISH(420525, "活动指引已完成"),
    RETURN_TIME_NOT_REACHED(420526, "现在不是返回可用时间"),
    USER_IDENTITY_NOT_EXIST(420527, "用户身份不存在"),
    TOKEN_EMPTY(420528, "token为空"),
    HOTEL_TYPE_NOT_MATCH(420529, "雪票不支持分房"),
    ROOM_ALREADY_ALLOCATED(420530, "房间已分配"),
    ROOM_ALLOCATE_TIME_EXCEED(420531, "不在房间分配时间"),
    ROOM_NOT_EXIST(420532, "房间不存在"),
    ROOM_FULL(420533, "房间已满"),
    TUTORIAL_ID_NOT_EXIST(420534, "教程不存在"),
    TUTORIAL_END(420535, "教程已结束"),
    ROOM_GENDER_INFLICTION(420536, "房间性别不符"),
    ROOM_NOT_MATCH(420537, "房间类型不匹配"),
    PRODUCT_NOT_EXIST(420538, "产品不存在"),
    BUS_FULL(420539, "车辆已满"),
    NOT_STUDENT(420540, "您不是学生"),
    BUS_ALREADY_CHOOSE(420541, "您已经选择大巴车了"),
    ACTIVITY_STATUS_ERROR(420542, "活动状态错误"),
    ACTIVITY_WXGROUP_MISSING(420543, "活动微信群缺失"),
    ACTIVITY_STATION_MISSING(420544, "活动站点缺失"),
    ACTIVITY_PRODUCT_MISSING(420545, "活动产品缺失"),
    ACTIVITY_FULL(420546,"活动报名人数已满"),
    ACTIVITY_DEADLINE(420547, "活动报名已截止"),
    
    // 优惠券相关错误码 4206xx
    COUPON_NOT_EXIST(420601, "优惠券不存在"),
    COUPON_EXPIRED(420602, "优惠券已过期"),
    COUPON_INACTIVE(420603, "优惠券已停用"),
    COUPON_ALREADY_USED(420604, "优惠券已使用"),
    COUPON_NOT_AVAILABLE(420605, "优惠券不可用"),
    COUPON_THRESHOLD_NOT_MET(420606, "未达到优惠券使用门槛"),
    COUPON_PRODUCT_NOT_MATCH(420607, "优惠券不适用于该产品"),
    COUPON_QUANTITY_EXCEEDED(420608, "优惠券数量超出限制"),
    COUPON_RECEIVE_FAILED(420609, "优惠券领取失败"),
    COUPON_BATCH_IMPORT_FAILED(420610, "优惠券批量导入失败"),
    USER_COUPON_NOT_EXIST(420611, "用户优惠券不存在"),
    COUPON_NAME_EXISTS(420612, "优惠券名称已存在"),
    
    // 微信小程序登录错误码
    INVALID_CODE(40029, "无效的 js_code 参数，请检查传入的 code 值"),
    API_RATE_LIMIT(45011, "API 调用频率受限，请稍后再试"),
    HIGH_RISK_USER(40226, "高风险等级用户，小程序登录拦截"),
    INVALID_SECRET(40001, "AppSecret错误或者AppSecret不属于这个公众号，请开发者确认AppSecret的正确性"),
    INVALID_GRANT_TYPE(40002, "请确保grant_type字段值为client_credential"),
    INVALID_IP(40164, "调用接口的IP地址不在白名单中，请在接口IP白名单中进行设置"),
    FROZEN_SECRET(40243, "AppSecret已被冻结，请登录MP解冻后再次调用"),
    IP_NEED_CONFIRM(89503, "此IP调用需要管理员确认,请联系管理员"),
    IP_WAIT_CONFIRM(89501, "此IP正在等待管理员确认,请联系管理员"),
    IP_REFUSED_DAY(89506, "24小时内该IP被管理员拒绝调用两次，24小时内不可再使用该IP调用"),
    IP_REFUSED_HOUR(89507, "1小时内该IP被管理员拒绝调用一次，1小时内不可再使用该IP调用");


    private final int code;
    private final String msg;

    // Code-to-ReturnCode mapping for fast lookup
    private static final Map<Integer, ReturnCode> CODE_MAP = new HashMap<>();

    // Static block to initialize the map
    static {
        for (ReturnCode enumData : ReturnCode.values()) {
            CODE_MAP.put(enumData.getCode(), enumData);
        }
    }

    // Method to get ReturnCode by int code
    public static ReturnCode fromCode(int code) {
        return CODE_MAP.get(code);
    }
}