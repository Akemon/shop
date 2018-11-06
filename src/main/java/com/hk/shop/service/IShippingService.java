package com.hk.shop.service;

import com.github.pagehelper.PageInfo;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.pojo.Shipping; /**
 * @author 何康
 * @date 2018/11/6 10:40
 */
public interface IShippingService {
    //增加收货地址
    ServerResponse addShipping(Integer userId, Shipping shipping);

    //删除收货地址
    ServerResponse delShipping(Integer userId, Integer shippingId);

    //更新地址
    ServerResponse updateShipping(Integer userId, Shipping shipping);

    //选中查看具体的地址
    ServerResponse<Shipping> selectShipping(Integer userId, Integer shippingId);

    //地址列表
    ServerResponse<PageInfo> listShipping(Integer userId, Integer pageNum, Integer pageSize);
}
