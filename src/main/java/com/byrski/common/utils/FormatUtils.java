package com.byrski.common.utils;

import org.apache.commons.lang3.StringUtils;

public class FormatUtils {

    /**
     * 格式化数字为两位小数
     * @param number 数字
     * @return 格式化后的数字字符串
     */
    public static String formatToDecimal(int number) {
        // 将整数转换为字符串
        String numberStr = String.valueOf(number);
        int length = numberStr.length();

        // 如果整数少于两位，用0补足
        if (length < 2) {
            numberStr = StringUtils.leftPad(numberStr, 2, '0');
            length = 2;
        }

        // 获取整数部分和小数部分
        String integerPart = numberStr.substring(0, length - 2);
        String decimalPart = numberStr.substring(length - 2);

        // 如果整数部分为空，设置为"0"
        if (StringUtils.isEmpty(integerPart)) {
            integerPart = "0";
        }

        // 组合成小数格式的字符串
        return integerPart + "." + decimalPart;
    }
}
