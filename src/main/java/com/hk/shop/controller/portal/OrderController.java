package com.hk.shop.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.hk.shop.common.Const;
import com.hk.shop.common.PropertiesUtil;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.pojo.User;
import com.hk.shop.service.IOrderService;
import net.sf.jsqlparser.schema.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * @author 何康
 * @date 2018/11/6 19:22
 */
@Controller
@RequestMapping("/order/")
public class OrderController {

    private static Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService orderService;

    /***
     * 支付
     * @param session
     * @param orderNo
     * @param request
     * @return
     */
    @RequestMapping(value = "pay.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user!= null){
            String path = request.getSession().getServletContext().getRealPath("upload");
            return orderService.pay(user.getId(),path,orderNo);
        }
        return ServerResponse.createByError("用户未登陆,请登录");
    }

    /***
     *处理支付状态的轮询
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "query_order_pay_status.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Boolean> query_order_pay_status(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user!= null){
            ServerResponse serverResponse = orderService.queryOrderPayStatus(user.getId(),orderNo);
            if(serverResponse.isSuccess()) return ServerResponse.createBySuccess(true);
            return ServerResponse.createBySuccess(false);
        }
        return ServerResponse.createByError("用户未登陆,请登录");
    }

    /***
     * 处理支付宝的回调
     * @param request
     * @return
     */
    @RequestMapping(value = "alipay_callback.do",method = RequestMethod.POST)
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        Map<String,String> params = Maps.newHashMap();
        Map parameterMap = request.getParameterMap();
        for(Iterator iterator = parameterMap.keySet().iterator();iterator.hasNext();){
            String name = (String) iterator.next();
            String[] values = (String[]) parameterMap.get(name);
            String valueStr = "";
            for(int i = 0;i<values.length;i++){
                valueStr = (i==values.length-1)?valueStr+values[i]:valueStr+values[i]+",";
            }
            params.put(name,valueStr);
        }
        logger.info("支付宝回调，sign:{},trade_status:{},参数：{}"
                ,params.get("sign")
                ,params.get("trade_status")
                ,params.toString());
        //验证回调是否为支付宝发出，验证签名是否通过,订单数据是否相同
        /**
         * 商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
         * 并判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额）
         * ，同时需要校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email），
         * 上述有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。在上述验证通过后商户必须根据支付宝不同类型的业务通知，正确的进行不同的业务处理，
         * 并且过滤重复的通知结果数据。在支付宝的业务通知中，只有交易通知状态为TRADE_SUCCESS或TRADE_FINISHED时，支付宝才会认定为买家付款成功。
         */
        params.remove("sign_type");
        try {
            boolean alipayRSACheckV2 = AlipaySignature.rsaCheckV2(params,
                    Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if(!alipayRSACheckV2){
               return ServerResponse.createByError("非法请求，验证不通过");
            }
            //更新状态
        } catch (AlipayApiException e) {
            logger.error("支付宝验证异常：{}",e);
        }

        //验证通过后开始处理回调
        ServerResponse serverResponse = orderService.aliCallback(params);
        if(serverResponse.isSuccess()) return Const.AlipayCallback.RESPONSE_SUCCESS;
        return Const.AlipayCallback.RESPONSE_FAILED;
    }


}
