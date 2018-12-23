package com.hk.shop.dao;

import com.hk.shop.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    //判断用户名是否存在
    int checkUserName(String username);

    //判断邮箱是否存在
    int checkEmail(String email);

    //登录
    User login(@Param("username") String username, @Param("password") String password);

    //根据用户名获取问题
    String selectQuestionByUsername(String username);

    //判断用户是否回答问题正确
    int forgetCheckAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);

    //设置新密码
    int updatePasswordByUsername(@Param("username") String username, @Param("passwordNew") String passwordNew);

    //检验旧密码是否正确
    int checkPassword(@Param("passwordOld") String passwordOld, @Param("id") Integer id);

    //查询邮箱是否与其他人重复
    int checkEmailByUserId(@Param("email") String email, @Param("id") Integer id);
}