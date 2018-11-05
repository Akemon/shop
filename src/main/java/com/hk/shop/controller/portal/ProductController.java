package com.hk.shop.controller.portal;

import com.github.pagehelper.PageInfo;
import com.hk.shop.common.Const;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.pojo.User;
import com.hk.shop.service.IProductService;
import com.hk.shop.vo.ProductDetailVO;
import net.sf.jsqlparser.schema.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author 何康
 * @date 2018/11/5 15:19
 */
@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService productService;

    /***
     * 获取商品搜索列表
     * @param session
     * @param categoryId
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getList(HttpSession session,
                                 Integer categoryId,
                                 String keyword,
                                 @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                                 @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
                                 @RequestParam(value = "orderBy",defaultValue = "")String orderBy){

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user!= null){
            return productService.getProductByCategoryIdAndKeyword(categoryId,keyword,pageNum,pageSize,orderBy);
        }
        return ServerResponse.createByError("用户未登陆，无法查询产品");
    }

    /***
     * 获取单个商品详情
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVO> getDetail(HttpSession session,Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user!= null){
            return productService.getProductDetail(productId);
        }
        return ServerResponse.createByError("用户未登陆，无法查询产品");
    }
}
