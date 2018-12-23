package com.hk.shop.service;

import com.hk.shop.common.ServerResponse;
import com.hk.shop.vo.CartVO;

import java.util.Map;

/**
 * @author 何康
 * @date 2018/11/5 21:15
 */
public interface ICartService {
    //获取购物车列表
    ServerResponse<CartVO> getCartList(Integer userId);

    //新增商品到购物车
    ServerResponse<CartVO> addCart(Integer userId, Integer productId, Integer count);

    //更新购物车的数量
    ServerResponse<CartVO> updateCart(Integer userId, Integer productId, Integer count);

    //移除购物车的商品
    ServerResponse<CartVO> deleteCart(Integer userId, String productIds);

    //全选，全反选，单选，单反选
    ServerResponse<CartVO> checkOrUnCheck(Integer userId, Integer productId, Integer check);

    //获取购物车的商品数量
    ServerResponse<String> getCartProductCount(Integer userId);
}
