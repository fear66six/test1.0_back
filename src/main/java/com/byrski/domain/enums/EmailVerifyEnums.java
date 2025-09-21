package com.byrski.domain.enums;

import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

@Getter
public enum EmailVerifyEnums {

    EMAIL_REGISTER_VERIFY(0, "register", "注册验证码", code -> Pair.of(
            "欢迎来到 ByrSki!",
            "感谢使用 ByrSki！\n" +
            "您的邮箱验证码为： " +
            code +
            "，有效期3分钟。\n" +
            "您接收到这封邮件代表您正在使用此邮箱创建新账号或修改邮箱。" +
            "如果您并未如此操作，请联系我们。")),
    EMAIL_RESET_VERIFY(1, "reset", "重置验证码", code -> Pair.of("重置您在 ByrSki 的密码", "您正在进行密码重置操作，您的邮箱验证码为： " +
            code +
            "，有效期3分钟。\n" +
            "您接收到这封邮件代表您正在重置您 ByrSki 账号的密码" +
            "如果您并未如此操作，请联系我们。")),
    ;


    private final Integer code;

    private final String type;

    private final String desc;

    private final Function<String, Pair<String, String>> template;



    EmailVerifyEnums(Integer code, String type, String desc, Function<String, Pair<String, String>> template) {
        this.code = code;
        this.type = type;
        this.desc = desc;
        this.template = template;
    }

    public static EmailVerifyEnums fromType(String type) {
        for(EmailVerifyEnums typeEnum : EmailVerifyEnums.values()) {
            if(type.equals(typeEnum.getType())) {
                return typeEnum;
            }
        }
        return null;
    }

}
