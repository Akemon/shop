package com.hk.shop.service.impl;

import ch.qos.logback.classic.gaffer.PropertyUtil;
import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.hk.shop.common.Const;
import com.hk.shop.common.PropertiesUtil;
import com.hk.shop.common.ResponseCode;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.dao.*;
import com.hk.shop.pojo.*;
import com.hk.shop.service.IOrderService;
import com.hk.shop.util.BigDecimalUtil;
import com.hk.shop.util.DateTimeUtil;
import com.hk.shop.util.FTPUtil;
import com.hk.shop.vo.OrderItemVO;
import com.hk.shop.vo.OrderProductVO;
import com.hk.shop.vo.OrderVO;
import com.hk.shop.vo.ShippingVO;
import net.sf.jsqlparser.schema.Server;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author 何康
 * @date 2018/11/6 19:28
 */
@Service
public class OrderServiceImpl implements IOrderService {

    private static Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    // 支付宝当面付2.0服务
    private static AlipayTradeService tradeService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    @Override
    public ServerResponse pay(Integer userId, String path,Long orderNo) {
        //查询这个订单是否存在
        Order order = orderMapper.getOrderByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByError("用户没有该订单");
        }
        Map<String,String> resultMap = Maps.newHashMap();
        resultMap.put("orderNo",order.getOrderNo().toString());
        //开始调用支付


        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("shop扫码支付，订单号：").append(order.getOrderNo()).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单：").append(order.getOrderNo()).append("购买商品共：").append(totalAmount+"元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoAndUserId(orderNo,userId);
        for(OrderItem orderItem:orderItemList){
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods1 = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods1);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);



        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File file =  new File(path);
                if(!file.exists()){
                    file.setWritable(true);
                    file.mkdirs();
                }
                // 需要修改为运行机器上的路径
                String qrPath = String.format(path+"/qr-%s.png",
                        response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                //上传到ftp图片服务器
                File targetFile = new File(path,qrFileName);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    log.error("ftp上传图片异常");
                    e.printStackTrace();
                }
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                resultMap.put("qrUrl",qrUrl);
                return ServerResponse.createBySuccess(resultMap);

            case FAILED:
                log.error("支付宝预下单失败!!!");
                return  ServerResponse.createByError("支付宝预下单失败!!!");

            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return  ServerResponse.createByError("系统异常，预下单状态未知!!!");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return  ServerResponse.createByError("不支持的交易状态，交易返回异常!!!");
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }

    /***
     * 处理支付状态的轮询
     * @param userId
     * @param orderNo
     * @return
     */
    public ServerResponse queryOrderPayStatus(Integer userId,Long orderNo){
        Order order = orderMapper.getOrderByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByError("用户没有该订单");
        }
        //订单支付成功直接返回成功
        if(order.getStatus()>=Const.OrderStatus.PAID.getCode()){
            return  ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    /***
     * 创建订单
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        if(userId == null||shippingId==null)
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        //获取购物车中选中的列表
        List<Cart> cartList = cartMapper.getCartListCheckedByUserId(userId);
        //生成订单详情列表
        ServerResponse serverResponse = getOrderItemListByCartListAndUserId(userId,cartList);
        if(!serverResponse.isSuccess()) return serverResponse;
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        //通过订单详情获取总价格
        BigDecimal totalPayment = getTotalPaymentByItemList(orderItemList);
        //生成订单并插入数据库
        Order order = getOrderByUserIdAndShippingIdAndOrderItemList(userId,shippingId,totalPayment,orderItemList);
        if(order == null) return ServerResponse.createByError("生成订单失败");
        //将订单id插入到订单详情中
        for(OrderItem orderItem :orderItemList) orderItem.setOrderNo(order.getOrderNo());
        //将订单详情批量插入到数据库中
        int count = orderItemMapper.batchInsert(orderItemList);
        if(count <= 0) return  ServerResponse.createByError("订单详情插入失败");
        //减少库存
        reduceStock(orderItemList);
        //清空购物车
        clearCart(cartList);

        //返回给前端数据
        //组装订单主数据对象
        OrderVO orderVO = assembleOrderVO(orderItemList,order);
        return ServerResponse.createBySuccess(orderVO);
    }

    /***
     * 获取订单的商品信息
     * @param userId
     * @return
     */
    @Override
    public ServerResponse getOrderCartProduct(Integer userId) {
        List<OrderItem> orderItemList = orderItemMapper.getByUserId(userId);
        if(CollectionUtils.isEmpty(orderItemList)){
            return ServerResponse.createByError("无订单数据");
        }
        //组装数据
        OrderProductVO orderProductVO = assembleOrderProductVO(orderItemList);
        return ServerResponse.createBySuccess(orderProductVO);

    }

    /***
     * 获取订单列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse getList(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.getOrderList(userId);
        PageInfo pageInfo = new PageInfo(orderList);
        List<OrderVO> orderVOList = assembleOrderVOList(orderList,userId);
        pageInfo.setList(orderVOList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /***
     * 获取订单详情
     * @param userId
     * @return
     */
    @Override
    public ServerResponse getDetail(Integer userId,Long orderNo) {
        Order  order = orderMapper.getOrderByUserIdAndOrderNo(userId,orderNo);
        if(order != null){
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNoAndUserId(orderNo,userId);
            OrderVO orderVO = assembleOrderVO(orderItemList,order);
            return ServerResponse.createBySuccess(orderVO);
        }
        return ServerResponse.createByError("订单不存在");
    }

    /***
     * 取消订单
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse cancelOrder(Integer userId, Long orderNo) {
        Order  order = orderMapper.getOrderByUserIdAndOrderNo(userId,orderNo);
        if(order != null){
           if(order.getStatus()>=Const.OrderStatus.PAID.getCode()){
               return ServerResponse.createByError("订单已完成,无法取消");
           }
           order.setStatus(Const.OrderStatus.CANCELED.getCode());
           order.setUpdateTime(new Date());
           orderMapper.updateByPrimaryKeySelective(order);
           return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError("订单不存在");
    }

    /***
     * 后台获取所有订单列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse manageList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.getOrderList(null);
        System.out.println("订单数量："+orderList.size());
        PageInfo pageInfo = new PageInfo(orderList);
        List<OrderVO> orderVOList = assembleOrderVOList(orderList,null);
        pageInfo.setList(orderVOList);
        return ServerResponse.createBySuccess(pageInfo);
    }


    /**
     * 按订单号查询
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse manageSearch(Long orderNo,int pageNum,int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        if(orderNo == null) return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        List<Order> orderList = orderMapper.searchByOrderNo(orderNo);
        PageInfo pageInfo = new PageInfo(orderList);
        List<OrderVO> orderVOList = assembleOrderVOList(orderList,null);
        pageInfo.setList(orderVOList);
        return ServerResponse.createBySuccess(orderVOList);
    }

    /***
     * 订单详情
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse manageDetail(Long orderNo) {
        return getDetail(null,orderNo);
    }

    /***
     * 订单发货
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse shippingGoods(Long orderNo) {
        Order order= orderMapper.selectByOrderNo(orderNo);
        if(order != null){
            if(order.getStatus() == Const.OrderStatus.PAID.getCode()){
                order.setStatus(Const.OrderStatus.SHIPPED.getCode());
                order.setSendTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
                return ServerResponse.createBySuccess("发货成功");
            }
        }
        return ServerResponse.createByError("订单不存在");
    }

    //组装订单对象列表
    private List<OrderVO> assembleOrderVOList(List<Order> orderList,Integer userId) {
        List<OrderVO> orderVOList = Lists.newArrayList();
        for(Order order:orderList){
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNoAndUserId(order.getOrderNo(),userId);
            OrderVO orderVO = assembleOrderVO(orderItemList,order);
            orderVOList.add(orderVO);
        }
        return orderVOList;
    }

    //组装订单商品对象
    private OrderProductVO assembleOrderProductVO(List<OrderItem> orderItemList) {
        OrderProductVO orderProductVO = new OrderProductVO();
        List<OrderItemVO> orderItemVOList = assembleOrderItemVOList(orderItemList);
        orderProductVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderProductVO.setProductTotalPrice(getTotalPaymentByItemList(orderItemList));
        orderProductVO.setOrderItemVoList(orderItemVOList);
        return orderProductVO;
    }

    //组装订单主数据对象
    private OrderVO assembleOrderVO(List<OrderItem> orderItemList, Order order) {
        //组装订单详情对象
        List<OrderItemVO> orderItemVOList = assembleOrderItemVOList(orderItemList);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());
        orderVO.setStatusDesc(Const.OrderStatus.codeOf(order.getStatus()).getValue());
        //ftp图片地址
        orderVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        //发货地址
        orderVO.setShippingId(order.getShippingId());

        //收件人详情
        Shipping shipping  = shippingMapper.selectByPrimaryKey(order.getShippingId());
        ShippingVO shippingVO = new ShippingVO();
        BeanUtils.copyProperties(shipping,shippingVO);
        if(shipping != null){
            orderVO.setReceiverName(shipping.getReceiverName());
            orderVO.setShippingVo(shippingVO);
        }

        //各种时间
        orderVO.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVO.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVO.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVO.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVO.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));

        orderVO.setOrderItemVoList(orderItemVOList);
        return orderVO;
    }

    //组装前端的订单详情对象
    private List<OrderItemVO> assembleOrderItemVOList(List<OrderItem> orderItemList) {
        List<OrderItemVO> orderItemVOList = Lists.newArrayList();
        for(OrderItem orderItem : orderItemList){
            OrderItemVO orderItemVO = new OrderItemVO();
            BeanUtils.copyProperties(orderItem,orderItemVO);
            orderItemVOList.add(orderItemVO);
        }
        return orderItemVOList;
    }

    //清除购物车信息
    private void clearCart(List<Cart> cartList) {
        for(Cart cart:cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    //减少库存
    private void reduceStock(List<OrderItem> orderItemList) {
        for(OrderItem orderItem :orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock()-orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    /***
     * 生成订单
     * @param userId 用户id
     * @param shippingId 发货id
     * @param totalPayment 总价
     * @param orderItemList 订单详情
     * @return
     */
    private Order getOrderByUserIdAndShippingIdAndOrderItemList(Integer userId, Integer shippingId, BigDecimal totalPayment, List<OrderItem> orderItemList) {
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(totalPayment);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPostage(0);
        order.setStatus(Const.OrderStatus.NO_PAY.getCode());
        order.setShippingId(shippingId);
        //各种时间
        int count =orderMapper.insertSelective(order);
        if(count >0 ) return order;
        return null;
    }
    //获取订单详情的总价格
    private BigDecimal getTotalPaymentByItemList(List<OrderItem> orderItemList) {
        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem :orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    //生成订单号
    private Long generateOrderNo() {
        return System.currentTimeMillis() + new Random().nextInt(100);
    }

    //获取订单详情列表
    private ServerResponse getOrderItemListByCartListAndUserId(Integer userId, List<Cart> cartList) {
        //返回的订单详情列表
        List<OrderItem> orderItemList = Lists.newArrayList();
        //校验购物车是否为空
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByError("购物车为空");
        }
        //校验购物车的商品状态与及库存
        for(Cart cart:cartList){
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if(product == null) return ServerResponse.createByError("商品不存在");
            //状态
            if(product.getStatus() != Const.ProductStatusEnum.ON_SELL.getCode())
                return ServerResponse.createByError("商品不在销售状态");
            //库存
            if(product.getStock()<cart.getQuantity())
                return ServerResponse.createByError("商品库存不足");
            //开始构建OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(cart.getQuantity().doubleValue(),product.getPrice().doubleValue()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    /***
     * 处理支付宝回调
     * @param params
     * @return
     */
    public ServerResponse aliCallback(Map<String,String> params){
        //原支付请求的商户订单号
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        //支付宝交易号
        String trade_no = params.get("trade_no");
        //交易状态
        String tradeStatus = params.get("trade_status");

        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null) return ServerResponse.createByError("订单不存在，回调忽略");
        //交易已成功，为重复调用
        if(order.getStatus() >= Const.OrderStatus.PAID.getCode()){
            return ServerResponse.createBySuccess("支付宝重复调用");
        }
        //交易成功
        if(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            //修改订单状态
            order.setStatus(Const.OrderStatus.PAID.getCode());
            //订单支付时间
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            orderMapper.updateByPrimaryKeySelective(order);
        }
        //生成支付的相关信息
        PayInfo payInfo = new PayInfo();
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setUserId(order.getUserId());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(trade_no);
        payInfo.setPlatformStatus(tradeStatus);
        payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }
}
