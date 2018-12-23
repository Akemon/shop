package com.hk.shop.dao;

import com.hk.shop.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    List<OrderItem> getByOrderNoAndUserId(@Param("orderNo") Long orderNo, @Param("userId") Integer userId);

    //批量插入数据
    int batchInsert(@Param("orderItemList") List<OrderItem> orderItemList);

    //获取订单的商品信息
    List<OrderItem> getByUserId(@Param("userId") Integer userId);
}