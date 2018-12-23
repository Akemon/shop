package com.hk.shop.vo;

import com.hk.shop.util.BigDecimalUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 何康
 * @date 2018/11/8 17:41
 */
public class OrderProductVO {
    private List<OrderItemVO> orderItemVoList;
    private String imageHost;
    private BigDecimal productTotalPrice;

    public List<OrderItemVO> getOrderItemVoList() {
        return orderItemVoList;
    }

    public void setOrderItemVoList(List<OrderItemVO> orderItemVoList) {
        this.orderItemVoList = orderItemVoList;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }

    public BigDecimal getProductTotalPrice() {
        return productTotalPrice;
    }

    public void setProductTotalPrice(BigDecimal productTotalPrice) {
        this.productTotalPrice = productTotalPrice;
    }
}
