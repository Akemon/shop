package com.hk.shop.service;

import com.hk.shop.common.ServerResponse;

import java.util.Map;

/**
 * @author 何康
 * @date 2018/11/6 19:27
 */
public interface IOrderService {

    //发出支付请求
    ServerResponse pay(Integer userId,String path, Long orderNo);

    //处理支付宝回调
    ServerResponse aliCallback(Map<String,String> params);

    //处理支付状态的轮询
    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);

    //创建订单
    ServerResponse createOrder(Integer userId, Integer shippingId);

    //获取订单的商品信息
    ServerResponse getOrderCartProduct(Integer userId);

    //获取订单列表
    ServerResponse getList(Integer userId, Integer pageNum, Integer pageSize);

    //获取订单详情
    ServerResponse getDetail(Integer userId,Long orderNo);

    //取消订单
    ServerResponse cancelOrder(Integer userId, Long orderNo);




    //后台获取所有订单列表
    ServerResponse manageList(int pageNum, int pageSize);
    //按订单号查询
    ServerResponse manageSearch(Long orderNo,int pageNum,int pageSize);

    //订单详情
    ServerResponse manageDetail(Long orderNo);

    //订单发货
    ServerResponse shippingGoods(Long orderNo);
}
