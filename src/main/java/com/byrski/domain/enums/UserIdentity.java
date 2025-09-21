package com.byrski.domain.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum UserIdentity {
    USER(0, "user"),
    LEADER(1, "leader"),
    ADMIN(2, "admin"),
    GUEST(3, "guest")
    ;

    private final Integer code;
    private final String role;

    private static final Map<Integer, UserIdentity> CODE_MAP = new HashMap<>();

    static {
        for (UserIdentity enumData : UserIdentity.values()) {
            CODE_MAP.put(enumData.getCode(), enumData);
        }
    }

    public static UserIdentity fromCode(int code) {
        return CODE_MAP.get(code);
    }
    
    /**
     * 从 Integer 类型的 code 获取 UserIdentity，处理 null 值的情况
     * @param code Integer 类型的身份代码
     * @return UserIdentity 枚举值，如果 code 为 null 或找不到对应的枚举，返回 USER
     */
    public static UserIdentity fromCode(Integer code) {
        if (code == null) {
            return USER;
        }
        UserIdentity identity = CODE_MAP.get(code);
        return identity != null ? identity : USER;
    }
}
