package com.hk.shop.controller.backend;

import com.github.pagehelper.PageInfo;
import com.hk.shop.common.Const;
import com.hk.shop.common.ResponseCode;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.pojo.Product;
import com.hk.shop.pojo.User;
import com.hk.shop.service.IProductService;
import com.hk.shop.service.IUserService;
import com.hk.shop.vo.ProductDetailVO;
import com.hk.shop.vo.ProductListVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author 何康
 * @date 2018/11/3 9:42
 */
@Controller
@RequestMapping("/manage/product")
public class ProductManagerController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IProductService productService;


    /****
     * 更新或新增产品
     * @param session
     * @param product
     * @return
     */
    @RequestMapping(value = "save.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> productSave(HttpSession session,Product product){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEDD_LOGIN.getCode(),"用户未登录，请登录");
        //判断是否为管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess()){
            //保存或更新商品
            return productService.saveOrUpdateProduct(product);
        }else{
            return ServerResponse.createByError("无权限操作,需要管理员登录");
        }
    }

    /***
     * 修改商品销售状态
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping(value = "set_sale_status.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setSaleStatus(HttpSession session,Integer productId,Integer status){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEDD_LOGIN.getCode(),"用户未登录，请登录");
        //判断是否为管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess()){
            //保存或更新商品
            return productService.setSaleStatus(productId,status);
        }else{
            return ServerResponse.createByError("无权限操作,需要管理员登录");
        }
    }

    /****
     * 获取单个商品详情
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVO> getDetail(HttpSession session, Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEDD_LOGIN.getCode(),"用户未登录，请登录");
        //判断是否为管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess()){
            //获取商品详情
            return productService.manageProductDetail(productId);
        }else{
            return ServerResponse.createByError("无权限操作,需要管理员登录");
        }
    }


    /***
     * 获取商品列表
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> getList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEDD_LOGIN.getCode(),"用户未登录，请登录");
        //判断是否为管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess()){
            //获取商品列表
            return productService.manageProductList(pageNum,pageSize);
        }else{
            return ServerResponse.createByError("无权限操作,需要管理员登录");
        }
    }




    /***
     * 分页搜索商品
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "search.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> getSearchList(HttpSession session, Integer productId,String productName,@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEDD_LOGIN.getCode(),"用户未登录，请登录");
        //判断是否为管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess()){
            //获取商品列表
            return productService.searchProduct(pageNum,pageSize,productId,productName);
        }else{
            return ServerResponse.createByError("无权限操作,需要管理员登录");
        }
    }






}
