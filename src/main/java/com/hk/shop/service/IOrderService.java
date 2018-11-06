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

}
