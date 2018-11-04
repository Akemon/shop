package com.hk.shop.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.hk.shop.common.PropertiesUtil;
import com.hk.shop.common.ResponseCode;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.dao.CategoryMapper;
import com.hk.shop.dao.ProductMapper;
import com.hk.shop.pojo.Category;
import com.hk.shop.pojo.Product;
import com.hk.shop.service.IProductService;
import com.hk.shop.util.DateTimeUtil;
import com.hk.shop.vo.ProductDetailVO;
import com.hk.shop.vo.ProductListVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author 何康
 * @date 2018/11/3 17:55
 */
@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    /***
     * 新增或更新产品
     * @param product
     * @return
     */
    @Override
    public ServerResponse<String> saveOrUpdateProduct(Product product) {
        //将产品的主图进行填充
        if (product != null) {
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String images[] = product.getSubImages().split(",");
                if (images.length > 0) product.setMainImage(images[0]);
            }
        }
        //判断是更新还是新增
        if (product.getId() == null) {
            //新增
            product.setCreateTime(new Date());
            int count = productMapper.insert(product);
            if (count > 0) return ServerResponse.createBySuccess("新增产品成功");
            return ServerResponse.createBySuccess("新增产品失败");
        } else {
            //更新
            int count = productMapper.updateByPrimaryKey(product);
            if (count > 0) return ServerResponse.createBySuccess("更新产品成功");
            return ServerResponse.createBySuccess("更新产品失败");
        }

    }

    /***
     * 修改商品销售状态
     * @param productId
     * @param status
     * @return
     */
    @Override
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if(productId == null || status == null){
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int count = productMapper.updateByPrimaryKeySelective(product);
        if(count>0) return ServerResponse.createBySuccess("修改销售状态成功");
        return ServerResponse.createByError("修改销售状态失败");
    }

    /***
     * 获取商品的详细信息
     * @param productId
     * @return
     */
    @Override
    public ServerResponse<ProductDetailVO> manageProductDetail(Integer productId) {
        if(productId == null)return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null) return ServerResponse.createByError("商品不存在");
        //返回VO对象
        return ServerResponse.createBySuccess(assembleProductDetailVO(product));
    }

    /****
     * 获取商品列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> manageProductList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        //获取商品列表
        List<Product>  productList = productMapper.getAllProduct();
        //封装成商品列表vo对象
        List<ProductListVO> productListVOList = Lists.newArrayList();
        //需要填充初始数据
        PageInfo pageInfo = new PageInfo(productList);
        if(productList == null || productList.size() ==0) {
            return ServerResponse.createByError("商品列表为空");
        }else{
            //将product封装到productListVo
            for(Product product:productList){
                ProductListVO productListVO = new ProductListVO();
                BeanUtils.copyProperties(product,productListVO);
                productListVOList.add(productListVO);
            }
            //修改pageInfo里的数据
            pageInfo.setList(productListVOList);
        }
        return ServerResponse.createBySuccess(pageInfo);
    }

    /****
     * 获取搜索的商品列表
     * @param pageNum
     * @param pageSize
     * @param productId
     * @param productName
     * @return
     */
    @Override
    public ServerResponse<PageInfo> searchProduct(Integer pageNum, Integer pageSize, Integer productId, String productName) {
        PageHelper.startPage(pageNum,pageSize);
        //将产品名称前后加个%
        if(StringUtils.isNotBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        //获取商品列表
        List<Product>  productList = productMapper.getSearchProduct(productId,productName);
        //封装成商品列表vo对象
        List<ProductListVO> productListVOList = Lists.newArrayList();
        //需要填充初始数据
        PageInfo pageInfo = new PageInfo(productList);
        if(productList == null || productList.size() ==0) {
            return ServerResponse.createByError("商品列表为空");
        }else{
            //将product封装到productListVo
            for(Product product:productList){
                ProductListVO productListVO = new ProductListVO();
                BeanUtils.copyProperties(product,productListVO);
                //这里直接将状态置空
                productListVO.setStatus(null);
                productListVOList.add(productListVO);
            }
            //修改pageInfo里的数据
            pageInfo.setList(productListVOList);
        }
        return ServerResponse.createBySuccess(pageInfo);
    }

    /***
     * 装配productDetailVO对象
     * 主要是对图片服务器的域名设置以及父类目id
     * @param product
     * @return
     */
    public ProductDetailVO assembleProductDetailVO(Product product){
        ProductDetailVO productDetailVO = new ProductDetailVO();
        BeanUtils.copyProperties(product,productDetailVO);
        //设置图片服务器的解析域名前缀
        productDetailVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://image.hk.com/"));
        //设置当前商品所在类目的父类目id
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        //没有找到类目，只能说明当前类目的最大的类目，父类目id为0
        if(category == null) productDetailVO.setParentCategoryId(0);
        else productDetailVO.setParentCategoryId(category.getParentId());
        //时间转换
        productDetailVO.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVO.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVO;
    }

}
