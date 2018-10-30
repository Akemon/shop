package com.hk.shop.controller.backend;

import com.hk.shop.common.Const;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.pojo.User;
import com.hk.shop.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author 何康
 * @date 2018/10/30 22:35
 */
@Controller
@RequestMapping("/manage/user")
public class UserManagerController {

    @Autowired
    private IUserService userService;

    @RequestMapping(value = "login.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){
        ServerResponse<User> response = userService.login(username,password);
        if(response.isSuccess()){
            int role = response.getData().getRole();
            if(role == Const.Role.ROLE_ADMIN){
                session.setAttribute(Const.CURRENT_USER,response.getData());
                return response;
            }else{
                return ServerResponse.createByError("不是管理员,无法登录");
            }
        }
        return response;
    }
}
