package com.byrski.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * 该类提供了一些与 HTTP IP 地址处理相关的实用方法
 */
public class HttpIpUtils {

    /**
     * 此方法用于从 HttpServletRequest 中获取远程 IP 地址。
     * 首先检查请求是否为 null，如果为 null 则返回空字符串。
     * 然后尝试从请求头 "X-Forwarded-For" 获取 IP 地址，如果该地址不为空且包含逗号，
     * 则取逗号前的部分作为 IP 地址。
     * 接着使用 isIpInValid 方法检查该 IP 地址是否有效，如果无效，
     * 则依次从其他请求头（如 "HTTP_CLIENT_IP"、"HTTP_X_FORWARDED_FOR"、"X-Real-IP"）中获取 IP 地址，
     * 直到获取到有效 IP 地址或最终使用 request.getRemoteAddr() 获取 IP 地址。
     * @param request 传入的 HttpServletRequest 对象
     * @return 最终获取到的远程 IP 地址
     */
    public static String getRemoteIpAddress(HttpServletRequest request) {
        if (request == null) return "";

        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(ip) && ip.indexOf(",") > 0) {
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index);
            }
        }
        if (isIpInValid(ip)){
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (isIpInValid(ip)){
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (isIpInValid(ip)){
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (isIpInValid(ip)){
            ip = request.getHeader("X-Real-IP");
        }
        if (isIpInValid(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    /**
     * 此方法用于判断 IP 地址是否无效。
     * 当 IP 地址为空、为 "unknown"、为 "127.0.0.1" 或为内网 IP 时，认为该 IP 地址无效。
     * @param ip 要判断的 IP 地址
     * @return 如果 IP 地址无效返回 true，否则返回 false
     */
    private static boolean isIpInValid(String ip){
        return StringUtils.isBlank(ip)
                || "unknown".equalsIgnoreCase(ip)
                || "127.0.0.1".equalsIgnoreCase(ip)
                || isIntranetIp(ip);
    }

    /**
     * 此方法用于判断 IP 地址是否为内网 IP。
     * 若 IP 地址以 "10." 或 "192.168." 开头，则为内网 IP。
     * 若 IP 地址以 "172." 开头，将 IP 地址按点分割，
     * 若分割后的数组长度为 4 且第二位在 16 到 31 之间，也为内网 IP。
     * @param ip 要判断的 IP 地址
     * @return 如果是内网 IP 则返回 true，否则返回 false
     */
    private static boolean isIntranetIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }
        if (ip.startsWith("172.")) {
            String[] ips = ip.split("\\.");
            if (ips.length == 4) {
                int second = Integer.parseInt(ips[1]);
                return second >= 16 && second <= 31;
            }
        }
        return false;
    }
}

