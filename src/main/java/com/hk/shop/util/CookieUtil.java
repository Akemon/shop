package com.hk.shop.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 何康
 * @date 2019/1/12 11:13
 */
@Slf4j
public class CookieUtil {

    private final static String COOKIE_DOMAIN = ".hk.shop.com";
    private final static String COOKIE_NAME = "user_login_token";

    public static void writeToken(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setDomain(COOKIE_DOMAIN);
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        log.info("write user login token ,name->{},value->{}", COOKIE_NAME, token);
        response.addCookie(cookie);
    }

    public static String readToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase(COOKIE_NAME)) {
                    log.info("读取到用户登陆cookie,value->{}", cookie.getValue());
                    return cookie.getValue();
                }
            }
        } else {
            log.error("读取Cookie失败");
        }
        return null;
    }

    public static void delLoginToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase(COOKIE_NAME)) {
                   cookie.setPath("/");
                   cookie.setDomain(COOKIE_DOMAIN);
                   cookie.setMaxAge(0);
                   log.info("delete user login token");
                   response.addCookie(cookie);
                }
            }
        } else {
            log.error("读取Cookie失败");
            return ;
        }

    }

}
