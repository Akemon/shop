package com.hk.shop.controller.backend;

import com.hk.shop.common.Const;
import com.hk.shop.common.ResponseCode;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.pojo.Category;
import com.hk.shop.pojo.User;
import com.hk.shop.service.ICategoryService;
import com.hk.shop.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author 何康
 * @date 2018/10/31 11:15
 */
@Controller
@RequestMapping("/manage/category/")
public class CategoryManagerController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ICategoryService categoryService;

    /***
     * 获取品类子节点(平级)
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<List<Category>> getCategory(HttpSession session, @RequestParam(value = "categoryId" ,defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEDD_LOGIN.getCode(),"用户未登录，请登录");
        //判断是否为管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess()){
            //获取品类子节点(平级)
            return categoryService.getCategory(categoryId);
        }else{
            return ServerResponse.createByError("无权限操作,需要管理员登录");
        }
    }

    /***
     * 添加类目
     * @param session
     * @param categoryName
     * @param parenId
     * @return
     */
    @RequestMapping(value = "add_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse addCategory(HttpSession session,String categoryName,@RequestParam(value = "parenId",defaultValue = "0") int parenId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEDD_LOGIN.getCode(),"用户未登录，请登录");
        //判断是否为管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess()){
            //开始增加类目
            return categoryService.addCategory(categoryName,parenId);
        }else{
            return ServerResponse.createByError("无权限操作,需要管理员登录");
        }
    }

    /***
     * 修改品类名称
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping(value = "set_category_name.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session,Integer categoryId,String categoryName){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEDD_LOGIN.getCode(),"用户未登录，请登录");
        //判断是否为管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess()){
            //开始修改类目名称
            return categoryService.modiyfCategoryName(categoryId,categoryName);
        }else{
            return ServerResponse.createByError("无权限操作,需要管理员登录");
        }
    }


    /***
     * 获取当前分类id及递归子节点categoryId
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_deep_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<List<Category>> getCategoryAndDeepChildrenCategory(HttpSession session, @RequestParam(value = "categoryId" ,defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEDD_LOGIN.getCode(),"用户未登录，请登录");
        //判断是否为管理员
        ServerResponse response = userService.checkAdminRole(user);
        if(response.isSuccess()){
            //获取品类子节点以及子节点的类目
            return categoryService.selectCategoryAndChildrenById(categoryId);
        }else{
            return ServerResponse.createByError("无权限操作,需要管理员登录");
        }
    }
}
