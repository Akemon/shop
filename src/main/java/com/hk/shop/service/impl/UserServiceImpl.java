package com.hk.shop.service.impl;

import com.hk.shop.common.Const;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.common.TokenCache;
import com.hk.shop.dao.UserMapper;
import com.hk.shop.pojo.User;
import com.hk.shop.service.IUserService;
import com.hk.shop.util.MD5Util;
import net.sf.jsqlparser.schema.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;

/**
 * @author 何康
 * @date 2018/10/30 10:54
 */
@Service
public class UserServiceImpl  implements IUserService{

    @Autowired
    private UserMapper userMapper;

    /***
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @Override
    public ServerResponse<User> login(String username, String password) {
        //判断用户是否存在
        int count = userMapper.checkUserName(username);
        if(count<=0) return ServerResponse.createByError("用户名不存在");
//      md5加密
        password = MD5Util.MD5(password);
        User user = userMapper.login(username,password);
        //判断密码是否正确
        if(user==null) return ServerResponse.createByError("密码错误");
        //登录成功后将除了密码之外的数据返回
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功",user);
    }

    /***
     * 注册
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> register(User user) {
        //判断用户名和email是否存在
        ServerResponse validResponse = checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()) return validResponse;
        validResponse = checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()) return validResponse;

//        int count = userMapper.checkUserName(user.getUsername());
//        if(count>0) return ServerResponse.createByError("用户名已存在");
//        int emailCount = userMapper.checkEmail(user.getEmail());
//        if(emailCount>0) return ServerResponse.createByError("邮箱已存在");
        //设置默认用户角色
        user.setRole(Const.Role.ROLE_CUSTOM);
        user.setPassword(MD5Util.MD5(user.getPassword()));
        user.setCreateTime(new Date());
        int result = userMapper.insert(user);
        if(result==0) return ServerResponse.createByError("注册失败");
        return ServerResponse.createBySuccess("注册成功");
    }

    /***
     * 参数校验
     * @param str
     * @param type
     * @return
     */
    public ServerResponse<String> checkValid(String str,String type){
        if(org.apache.commons.lang3.StringUtils.isNotBlank(str)){
            if(type.equals(Const.USERNAME)){
                int count = userMapper.checkUserName(str);
                if(count>0) return ServerResponse.createByError("用户名已存在");
            }
            if(type.equals(Const.EMAIL)){
                int emailCount = userMapper.checkEmail(str);
                if(emailCount>0) return ServerResponse.createByError("邮箱已存在");
            }
        }else{
            return ServerResponse.createByError("参数错误");
        }
        return ServerResponse.createBySuccess("校验成功");
    }

    /***
     * 根据用户名获取忘记密码的问题
     * @param username
     * @return
     */
    public ServerResponse<String> getForgetPasswordQuestion(String username){
        //判断用户是否存在
        ServerResponse validResponse = checkValid(username,Const.USERNAME);
        //校验成功，用户名不存在
        if(validResponse.isSuccess()) return ServerResponse.createByError("用户名不存在");
        //查找问题
        String question = userMapper.selectQuestionByUsername(username);
        //判断问题是否为空
        if(org.apache.commons.lang3.StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByError("该用户未设置找回密码问题");
    }

    /***
     * 判断该用户是否通过问题测试
     * @param username
     * @param question
     * @param answer
     * @return
     */
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
        //判断数据库是否有这样一条纪录
        int result = userMapper.forgetCheckAnswer(username,question,answer);
        if(result>0){
            //创建缓存临时存储一个token，用于防止用户的横向越权的问题
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            //将token返回给客户端
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByError("问题的答案错误");
    }

    /***
     * 根据用户名，新密码，Token重置密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        //判断用户名是否存在
        ServerResponse validResponse = checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            return ServerResponse.createByError("用户名不存在");
        }
        //判断token是否传递
        if(org.apache.commons.lang3.StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByError("参数错误，token需要传递");
        }
        //token是否过期并比较是否相同
        String token =  TokenCache.getValue(TokenCache.TOKEN_PREFIX+username);
        if(org.apache.commons.lang3.StringUtils.isBlank(token)){
            return ServerResponse.createByError("token无效或过期");
        }
        if(org.apache.commons.lang3.StringUtils.equals(forgetToken,token)){
            //开始设置新密码
            String passwordMD5 = MD5Util.MD5(passwordNew);
            int count =  userMapper.updatePasswordByUsername(username,passwordMD5);
            if(count>0) return ServerResponse.createBySuccess("密码更新成功");
            return ServerResponse.createByError("密码更新失败");
        }else{
            return ServerResponse.createByError("token不一致，请重新获取");
        }

    }

    /***
     * 登录状态下的重置密码
     * @param passwordOld
     * @param passwordNew
     * @param user
     * @return
     */
    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        //查看旧密码是否正确
        int count = userMapper.checkPassword(MD5Util.MD5(passwordOld),user.getId());
        if(count <= 0) return ServerResponse.createByError("旧密码错误");
        //开始更新密码
        user.setPassword(MD5Util.MD5(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount <=0 ) return ServerResponse.createByError("密码更新失败");
        return ServerResponse.createBySuccess("密码更新成功");
    }

    /***
     * 更新个人信息
     * @param user
     * @return
     */
    @Override
    public ServerResponse<User> updateInformation(User user) {
//        return null;
        //邮箱不能和别人的重复，并且存在的email如果相同的话,不能是我们当前的这个用户的
        int count = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(count>0) return ServerResponse.createByError("email已存在，请更换email重新尝试");
        //开始更新
        int updateCount  = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount>0) return ServerResponse.createBySuccess("更新成功",user);
        return ServerResponse.createByError("更新失败");
    }

    /***
     * 获取个人详细信息
     * @param id
     * @return
     */
    @Override
    public ServerResponse<User> getUserInformation(Integer id) {
        User user = userMapper.selectByPrimaryKey(id);
        if(user == null)return ServerResponse.createByError("找不到当前用户");
        //将密码置空
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    /***
     * 判断是否为管理员
     * @param user
     * @return
     */
    @Override
    public ServerResponse checkAdminRole(User user) {
        if(user!=null&&user.getRole().intValue()==Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }


}
