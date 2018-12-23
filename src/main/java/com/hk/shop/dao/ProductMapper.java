package com.hk.shop.dao;

import com.hk.shop.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    //获取所有商品
    List<Product> getAllProduct();

    //获取搜索商品
    List<Product> getSearchProduct(@Param("productId") Integer productId, @Param("productName") String productName);

    //根据id列表和商品名称搜索商品
    List<Product> selectByKeywordAndCategoryIds(@Param("keyword") String keyword, @Param("categoryIdList") List<Integer> categoryIdList);
}