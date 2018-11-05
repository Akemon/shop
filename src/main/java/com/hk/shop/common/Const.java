package com.hk.shop.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author 何康
 * @date 2018/10/30 11:13
 */
public class Const {
    public static final String CURRENT_USER = "currentUser";


    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }
    public enum ProductStatusEnum{
        ON_SELL(1,"销售中");
        private Integer code;
        private String desc;

        ProductStatusEnum(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    public interface Cart{
        int CHECKED = 1;//即购物车选中状态
        int UN_CHECKED = 0;//购物车中未选中状态
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }
    public interface Role{
        int ROLE_CUSTOM = 0; //普通用户
        int ROLE_ADMIN =1 ; //管理员
    }
}
