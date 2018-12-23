package com.hk.shop.controller.backend;

import com.hk.shop.common.Const;
import com.hk.shop.common.ResponseCode;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.pojo.User;
import com.hk.shop.service.IOrderService;
import com.hk.shop.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author 何康
 * @date 2018/11/8 18:19
 */
@Controller
@RequestMapping("/manage/order/")
public class OrderManagerController {
    @Autowired
    private IUserService userService;

    @Autowired
    private IOrderService orderService;

    /***
     * 获取订单列表
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse list(HttpSession session,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEDD_LOGIN.getCode(),"用户未登录，请登录");
        //判断是否为管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess()){
            //获取所有订单列表
            return orderService.manageList(pageNum,pageSize);
        }else{
            return ServerResponse.createByError("无权限操作,需要管理员登录");
        }
    }

    /***
     * 按订单号查询
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "search.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse search(HttpSession session,Long orderNo,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                 @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEDD_LOGIN.getCode(),"用户未登录，请登录");
        //判断是否为管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess()){
            //按订单号查询
            return orderService.manageSearch(orderNo,pageNum,pageSize);
        }else{
            return ServerResponse.createByError("无权限操作,需要管理员登录");
        }
    }

    /***
     * 订单详情
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse detail(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEDD_LOGIN.getCode(),"用户未登录，请登录");
        //判断是否为管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess()){
            //按订单号查询
            return orderService.manageDetail(orderNo);
        }else{
            return ServerResponse.createByError("无权限操作,需要管理员登录");
        }
    }

    /**
     * 订单发货
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "send_goods.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse send_goods(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEDD_LOGIN.getCode(),"用户未登录，请登录");
        //判断是否为管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess()){
            //按订单号查询
            return orderService.shippingGoods(orderNo);
        }else{
            return ServerResponse.createByError("无权限操作,需要管理员登录");
        }
    }

}
