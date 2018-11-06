package com.hk.shop.dao;

import com.hk.shop.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    //删除选中的地址
    int deleteByUserIdAndShippingId(@Param("userId") Integer userId,@Param("shippingId") Integer shippingId);

    //选中查看具体的地址
    Shipping getShippingByUserIdAndShippingId(@Param("userId")Integer userId,@Param("shippingId") Integer shippingId);

    //地址列表
    List<Shipping> getShippingList(Integer userId);
}