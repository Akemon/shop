package com.hk.shop.dao;

import com.hk.shop.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    //获取购物车列表
    List<Cart> getCartListByUserId(Integer userId);

    //购物车是否全选状态
    int selectCartProductCheckedStatusByUserId(Integer userId);

    //通过用户id和商品id获取购物车
    Cart getCartByUserIdAndProductId(@Param("userId") Integer userId,@Param("productId") Integer productId);

    //删除购物车
    int deleteByUserIdAndProductIds(@Param("userId")Integer userId,@Param("productIDList") List<String> productIDList);

    //全选，全反选，单选，单反选
    int checkOrUncheck(@Param("userId")Integer userId,@Param("productId") Integer productId,@Param("check") Integer check);

    //获取购物车的商品数量
    int getCartProductCountByUserId(Integer userId);

    //获取选中的购物车信息
    List<Cart> getCartListCheckedByUserId(Integer userId);
}