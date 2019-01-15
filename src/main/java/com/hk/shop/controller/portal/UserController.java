package com.hk.shop.controller.portal;

import com.hk.shop.common.Const;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.pojo.User;
import com.hk.shop.service.IUserService;
import net.sf.jsqlparser.schema.Server;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author 何康
 * @date 2018/10/30 10:44
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService userService;

    /***
     * 登录
     * @param userName
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(@RequestParam("username")String userName,
                                      @RequestParam("password") String password,
                                      HttpSession session){
//        return null;
        ServerResponse<User> response = userService.login(userName,password);
        if(response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    /***
     * 登出
     * @param session
     * @return
     */
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session){
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess("退出成功");
    }

    /***
     * 注册
     * @param user
     * @return
     */
    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){
        return userService.register(user);
    }

    /***
     * 校验参数
     * @param str
     * @param type
     * @return
     */
    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str,String type){
        return userService.checkValid(str,type);
    }

    /***
     * 获取用户详细信息
     * @param session
     * @return
     */
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);
//        if(user!= null){
//            return ServerResponse.createBySuccess(user);
//        }
//        return ServerResponse.createByError("用户未登陆，无法查询用户信息");
        return ServerResponse.createBySuccess((User)session.getAttribute(Const.CURRENT_USER));
    }

    /***
     * 根据用户名获取忘记密码的问题
     * @param username
     * @return
     */
    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> getForgetPasswordQuestion(String username){
        return userService.getForgetPasswordQuestion(username);
    }

    /***
     * 查看用户是否回答问题正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
        return userService.forgetCheckAnswer(username,question,answer);
    }

    /***
     * 根据用户名，密码，token设定新的密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        return userService.forgetResetPassword(username,passwordNew,forgetToken);
    }

    /***
     * 登录状态下的重置密码
     * @param passwordOld
     * @param passwordNew
     * @param session
     * @return
     */
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,
                                                HttpSession session){
        //判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null) return ServerResponse.createByError("用户未登录");
        return userService.resetPassword(passwordOld,passwordNew,user);
    }

    /***
     * 更新个人信息
     * @param session
     * @param user
     * @return
     */
    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInformation(HttpSession session,User user){
        //判断用户是否登录
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser==null) return ServerResponse.createByError("用户未登录");
        //用户名不能修改，从缓存里取
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        //开始更新个人信息,注：更新后的用户个人信息重新存入session中
        ServerResponse<User> response = userService.updateInformation(user);
        session.setAttribute(Const.CURRENT_USER,response.getData());
        //将user设置为空
        response.setData(null);
        return response;
    }

    /***
     * 获取用户个人信息
     * @param session
     * @return
     */
    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpSession session){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser==null) return ServerResponse.createByError(10,"用户未登录,无法获取当前用户信息,status=10,强制登录");
        return userService.getUserInformation(currentUser.getId());
    }


}
