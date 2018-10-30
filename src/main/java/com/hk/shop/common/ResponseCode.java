package com.hk.shop.common;

/**
 * @author 何康
 * @date 2018/10/30 0:08
 */
public enum ResponseCode {

    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
    NEDD_LOGIN(10,"需要登录"),
    ILLEGAL_ARGUMENT(20,"参数异常");

    private final int code;

    private final String desc;

    ResponseCode(int code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
