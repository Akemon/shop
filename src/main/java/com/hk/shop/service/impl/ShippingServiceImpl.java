package com.hk.shop.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.hk.shop.common.ResponseCode;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.dao.ShippingMapper;
import com.hk.shop.pojo.Shipping;
import com.hk.shop.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author 何康
 * @date 2018/11/6 10:40
 */
@Service
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;
    /***
     * 增加收货地址
     * @param userId
     * @param shipping
     * @return
     */
    @Override
    public ServerResponse addShipping(Integer userId, Shipping shipping) {
        //防止横向越权问题
        shipping.setUserId(userId);
        int count = shippingMapper.insertSelective(shipping);
        if(count >0){
            int shippingId = shipping.getId();
            Map map = Maps.newHashMap();
            map.put("shippingId",shippingId);
            return ServerResponse.createBySuccess("新建地址成功",map);
        }
        return ServerResponse.createByError("新建地址失败");
    }

    /***
     * 删除收货地址
     * @param
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse delShipping(Integer userId, Integer shippingId) {
        if(shippingId == null)
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        int count = shippingMapper.deleteByUserIdAndShippingId(userId,shippingId);
        if(count > 0){
            return ServerResponse.createBySuccess("删除地址成功");
        }
        return ServerResponse.createByError("删除地址失败");
    }

    /***
     * 更新地址
     * @param userId
     * @param shipping
     * @return
     */
    @Override
    public ServerResponse updateShipping(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int count = shippingMapper.updateByPrimaryKeySelective(shipping);
        if(count > 0){
            return ServerResponse.createBySuccess("更新地址成功");
        }
        return ServerResponse.createByError("更新地址失败");
    }

    /***
     * 选中查看具体的地址
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse<Shipping> selectShipping(Integer userId, Integer shippingId) {
        if(shippingId == null)
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());

        Shipping shipping = shippingMapper.getShippingByUserIdAndShippingId(userId,shippingId);
        if(shipping !=  null){
            return ServerResponse.createBySuccess(shipping);
        }
        return ServerResponse.createByError("查看地址失败");

    }

    /***
     * 地址列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> listShipping(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.getShippingList(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
