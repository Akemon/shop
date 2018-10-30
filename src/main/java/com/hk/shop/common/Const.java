package com.hk.shop.common;

/**
 * @author 何康
 * @date 2018/10/30 11:13
 */
public class Const {
    public static final String CURRENT_USER = "currentUser";


    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    public interface Role{
        int ROLE_CUSTOM = 0; //普通用户
        int ROLE_ADMIN =1 ; //管理员
    }
}
