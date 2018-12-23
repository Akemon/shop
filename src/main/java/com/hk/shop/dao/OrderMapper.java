package com.hk.shop.dao;

import com.hk.shop.pojo.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    //通过用户id和订单id查询订单
    Order getOrderByUserIdAndOrderNo(@Param("userId") Integer userId, @Param("orderNo") Long orderNo);

    //通过订单id查询订单
    Order selectByOrderNo(@Param("orderNo") Long orderNo);

    //获取订单列表
    List<Order> getOrderList(@Param("userId") Integer userId);

    //根据订单号查询订单列表
    List<Order> searchByOrderNo(@Param("orderNo") Long orderNo);
}