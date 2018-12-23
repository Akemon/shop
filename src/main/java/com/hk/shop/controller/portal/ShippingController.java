package com.hk.shop.controller.portal;

import com.github.pagehelper.PageInfo;
import com.google.common.base.Splitter;
import com.hk.shop.common.Const;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.pojo.Shipping;
import com.hk.shop.pojo.User;
import com.hk.shop.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author 何康
 * @date 2018/11/6 10:39
 */
@Controller
@RequestMapping("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService shippingService;

    @RequestMapping(value = "add.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse add(HttpSession session, Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user != null){
            return shippingService.addShipping(user.getId(),shipping);
        }else{
            return ServerResponse.createByError("未登录，请先先登录");
        }
    }

    /***
     * 删除收货地址
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping(value = "del.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse del(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user != null){
            return shippingService.delShipping(user.getId(),shippingId);
        }else{
            return ServerResponse.createByError("未登录，请先先登录");
        }
    }

    /***
     * 更新地址
     * @param session
     * @param
     * @return
     */
    @RequestMapping(value = "update.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse update(HttpSession session, Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user != null){
            return shippingService.updateShipping(user.getId(),shipping);
        }else{
            return ServerResponse.createByError("未登录，请先先登录");
        }
    }

    /***
     * 选中查看具体的地址
     * @param session
     * @param
     * @return
     */
    @RequestMapping(value = "select.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Shipping> select(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user != null){
            return shippingService.selectShipping(user.getId(),shippingId);
        }else{
            return ServerResponse.createByError("未登录，请先先登录");
        }
    }

    /***
     * 地址列表
     * @param session
     * @return
     */
    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> list(HttpSession session,
                                         @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user != null){
            return shippingService.listShipping(user.getId(),pageNum,pageSize);
        }else{
            return ServerResponse.createByError("未登录，请先先登录");
        }
    }


}
