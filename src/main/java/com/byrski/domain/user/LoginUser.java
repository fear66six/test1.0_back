package com.byrski.domain.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1126436713468590340L;

    private Long id;
//    private String username;


    private static ThreadLocal<LoginUser> loginUserThreadLocal = new ThreadLocal<>();

    public static LoginUser getInstance() {
        return loginUserThreadLocal.get();
    }

//    public static void initLoginUser(Account account) {
//        LoginUser loginUser = new LoginUser();
//        loginUser.setId(account.getId());
//        loginUser.setUsername(account.getUsername());
//        loginUserThreadLocal.set(loginUser);
//    }

    public static void initLoginUser(Long userId) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(userId);
        loginUserThreadLocal.set(loginUser);
    }

    public static void clearInstance() {
        loginUserThreadLocal.remove();
    }

    public static boolean isLoginUser() {
        LoginUser loginUser = getInstance();
        return loginUser != null;
    }

    public static Long getLoginUserId() {
        return isLoginUser() ? getInstance().id : null;
    }

//    public static String getLoginUsername() {
//        return isLoginUser() ? getInstance().username : null;
//    }

}
