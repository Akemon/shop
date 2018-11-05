package com.hk.shop.controller.backend;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.hk.shop.common.Const;
import com.hk.shop.common.PropertiesUtil;
import com.hk.shop.common.ResponseCode;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.pojo.Product;
import com.hk.shop.pojo.User;
import com.hk.shop.service.IFileService;
import com.hk.shop.service.IProductService;
import com.hk.shop.service.IUserService;
import com.hk.shop.vo.ProductDetailVO;
import com.hk.shop.vo.ProductListVO;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

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

    @Autowired
    private IFileService fileService;


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

    /***
     * 上传文件
     * @param session
     * @param upload_file
     * @param request
     * @return
     */
    @RequestMapping(value = "upload.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse upload(HttpSession session, MultipartFile upload_file, HttpServletRequest request){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);
//        if(user == null) return ServerResponse.createByError(ResponseCode.NEDD_LOGIN.getCode(),"用户未登录，请登录");
//        //判断是否为管理员
//        ServerResponse response = userService.checkAdminRole(user);
//        if(response.isSuccess()){
            //上传路径
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetName = fileService.upload(upload_file,path);
            Map fileMap = Maps.newHashMap();
            fileMap.put("uri",targetName);
            fileMap.put("url", PropertiesUtil.getProperty("ftp.server.http.prefix")+targetName);
            return ServerResponse.createBySuccess(fileMap);
//        }else{
//            return ServerResponse.createByError("无权限操作,需要管理员登录");
//        }
    }

    /****
     * 富文本文件上传
     * @param session
     * @param upload_file
     * @param request
     * @return
     */
    @RequestMapping(value = "richtext_img_upload.do",method = RequestMethod.POST)
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, MultipartFile upload_file, HttpServletRequest request, HttpServletResponse response){
        Map map = Maps.newHashMap();
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            map.put("success",false);
            map.put("msg","请登录管理员");
            return map;
        }
        //判断是否为管理员
        ServerResponse result = userService.checkAdminRole(user);
        if(result.isSuccess()){
        //上传路径
        String path = request.getSession().getServletContext().getRealPath("upload");
        String targetName = fileService.upload(upload_file,path);
        if(StringUtils.isBlank(targetName)){
            map.put("success",false);
            map.put("msg","上传失败");
            return map;
        }
        String url = PropertiesUtil.getProperty( PropertiesUtil.getProperty("ftp.server.http.prefix")+targetName);
            map.put("success",true);
            map.put("msg","上传成功");
            map.put("file_path",url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return map;
        }else{
            map.put("success",false);
            map.put("msg","无权限操作");
            return map;
        }
    }

}
