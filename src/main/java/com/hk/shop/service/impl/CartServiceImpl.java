package com.hk.shop.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hk.shop.common.Const;
import com.hk.shop.common.PropertiesUtil;
import com.hk.shop.common.ResponseCode;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.dao.CartMapper;
import com.hk.shop.dao.ProductMapper;
import com.hk.shop.pojo.Cart;
import com.hk.shop.pojo.Product;
import com.hk.shop.service.ICartService;
import com.hk.shop.service.ICategoryService;
import com.hk.shop.util.BigDecimalUtil;
import com.hk.shop.vo.CartProductVO;
import com.hk.shop.vo.CartVO;
import org.apache.ibatis.annotations.Param;
import org.omg.PortableInterceptor.USER_EXCEPTION;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author 何康
 * @date 2018/11/5 21:16
 */
@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse<CartVO> getCartList(Integer userId) {
        if(StringUtils.isEmpty(userId)) return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                                                                            ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        CartVO cartVO = getCartVOByUserId(userId);
        return ServerResponse.createBySuccess(cartVO);
    }

    /***
     * 新增商品到购物车
     * @param
     * @param productId
     * @param count
     * @return
     */
    @Override
    public ServerResponse<CartVO> addCart(Integer userId, Integer productId, Integer count) {
       if(productId == null|| count == null){
           return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                   ResponseCode.ILLEGAL_ARGUMENT.getDesc());
       }
       //该购物车是否存在
       Cart cart = cartMapper.getCartByUserIdAndProductId(userId,productId);
       if(cart == null){
           //新增购物车
           Cart newCart = new Cart();
           newCart.setUserId(userId);
           newCart.setProductId(productId);
           newCart.setQuantity(count);
           newCart.setChecked(Const.Cart.CHECKED);
           cartMapper.insert(newCart);
       }else{
           cart.setQuantity(cart.getQuantity()+count);
           cartMapper.updateByPrimaryKey(cart);
       }
       //返回一个购物车列表
       return getCartList(userId);
    }

    /***
     * 更新购物车的商品数量
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    @Override
    public ServerResponse<CartVO> updateCart(Integer userId, Integer productId, Integer count) {
        if(productId == null|| count == null){
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.getCartByUserIdAndProductId(userId,productId);
        if(cart != null) {
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        return getCartList(userId);
    }

    /****
     * 移除购物车的商品
     * @param userId
     * @param productIds
     * @return
     */
    @Override
    public ServerResponse<CartVO> deleteCart(Integer userId, String productIds) {
        List<String> productIDList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productIDList)){
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdAndProductIds(userId,productIDList);
        return getCartList(userId);
    }

    /***
     * 全选，全反选，单选，单反选
     * @param userId
     * @param productId
     * @param check
     * @return
     */
    public ServerResponse<CartVO> checkOrUnCheck(Integer userId,Integer productId,Integer check){
        int count = cartMapper.checkOrUncheck(userId,productId,check);
        if(count>0) return getCartList(userId);
        return ServerResponse.createByError("处理失败");
    }

    /****
     * 获取购物车商品数量
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<String> getCartProductCount(Integer userId) {
        Map map = Maps.newHashMap();
        int count = cartMapper.getCartProductCountByUserId(userId);
        return ServerResponse.createBySuccess(count+"");
    }


    /***
     * 封装一个获取购物车vo的方法
     * @param userId
     * @return
     */
    private CartVO getCartVOByUserId(Integer userId){
        List<Cart> carts = cartMapper.getCartListByUserId(userId);
        CartVO cartVO = new CartVO();
        List<CartProductVO> cartProductVOS = Lists.newArrayList();
        //总价格
        BigDecimal cartTotalPrice = new BigDecimal("0");
        if(org.apache.commons.collections.CollectionUtils.isNotEmpty(carts)){
            for(Cart cart:carts){
                CartProductVO cartProductVO = new CartProductVO();
                cartProductVO.setId(cart.getId());
                cartProductVO.setUserId(userId);
                cartProductVO.setProductId(cart.getProductId());
                Product product = productMapper.selectByPrimaryKey(cart.getProductId());
                if(product != null){
                    cartProductVO.setProductMainImage(product.getMainImage());
                    cartProductVO.setProductName(product.getName());
                    cartProductVO.setProductSubtitle(product.getSubtitle());
                    cartProductVO.setProductStatus(product.getStatus());
                    cartProductVO.setProductPrice(product.getPrice());
                    cartProductVO.setProductStock(product.getStock());
                    //关于库存是否足够的问题
                    int buylimitCount = 0;
                    if(cart.getQuantity()>product.getStock()){
                        //库存不足，商品数量即为库存数量，并更新到数据库
                        Cart tempCart = new Cart();
                        tempCart.setId(cart.getId());
                        tempCart.setQuantity(product.getStock());
                        //更新到数据库
                        cartMapper.updateByPrimaryKeySelective(tempCart);
                        //标记数据超过库存
                        cartProductVO.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //重新设置当前购物车的商品数量
                        buylimitCount = product.getStock();
                    }else{
                        //库存充足
                        buylimitCount = cart.getQuantity();
                        cartProductVO.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }
                    //设置购买数量
                    cartProductVO.setQuantity(buylimitCount);
                    //商品选中状态
                    cartProductVO.setProductChecked(cart.getChecked());
                    //当前购物车的商品总价
                    cartProductVO.setProductTotalPrice(BigDecimalUtil.mul(cartProductVO.getQuantity().doubleValue(),cartProductVO.getProductPrice().doubleValue()));
                    //将选中的商品加入到当前用户购物车总价中
                    if(cartProductVO.getProductChecked() == Const.Cart.CHECKED )
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVO.getProductTotalPrice().doubleValue());
                    //将封装好的购物车对象加入列表
                    cartProductVOS.add(cartProductVO);
                }
            }
        }
        //设置总购物车
        cartVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        cartVO.setCartTotalPrice(cartTotalPrice);
        cartVO.setProductVOList(cartProductVOS);
        //是否全选
        cartVO.setAllChecked(getAllCheckedStatus(userId));
        return cartVO;
    }

    //获取当前用户购物车的商品选中状态
    private boolean getAllCheckedStatus(Integer userId){
        if(userId == null){
            return false;
        }
        //没有选中的查出为0，即全选都已经选中
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;

    }
}
