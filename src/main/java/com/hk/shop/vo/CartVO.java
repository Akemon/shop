package com.hk.shop.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 何康
 * @date 2018/11/5 21:21
 */
public class CartVO {
    private List<CartProductVO> productVOList;
    private BigDecimal cartTotalPrice;
    private Boolean allChecked;
    private String imageHost;

    public List<CartProductVO> getProductVOList() {
        return productVOList;
    }

    public void setProductVOList(List<CartProductVO> productVOList) {
        this.productVOList = productVOList;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public Boolean getAllChecked() {
        return allChecked;
    }

    public void setAllChecked(Boolean allChecked) {
        this.allChecked = allChecked;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
