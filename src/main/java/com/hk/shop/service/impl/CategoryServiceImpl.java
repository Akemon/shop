package com.hk.shop.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hk.shop.common.ServerResponse;
import com.hk.shop.controller.backend.CategoryManagerController;
import com.hk.shop.dao.CategoryMapper;
import com.hk.shop.pojo.Category;
import com.hk.shop.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author 何康
 * @date 2018/10/31 11:24
 */
@Service
public class CategoryServiceImpl implements ICategoryService{

    @Autowired
    private CategoryMapper categoryMapper;

    /***
     * 获取品类子节点(平级)
     * @param categoryId
     * @return
     */
    @Override
    public ServerResponse<List<Category>> getCategory(Integer categoryId) {
        if(categoryId == null) return ServerResponse.createByError("参数错误");
        List<Category> categoryList = categoryMapper.getCategory(categoryId);
        if(categoryList!=null && categoryList.size()!=0) {
            return ServerResponse.createBySuccess(categoryList);
        }
        return ServerResponse.createByError("未找到该品类");
    }

    /****
     * 递归获取所有子节点
     * @param categoryId
     * @return
     */
    @Override
    public ServerResponse selectCategoryAndChildrenById(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();
        findChildrenCategory(categorySet,categoryId);
        //生成categoryId返回
        List<Integer> categoryIdList = Lists.newArrayList();
        if(categoryId!=null){
            for(Category category:categorySet){
                categoryIdList.add(category.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    //递归获取所有子节点
    private Set<Category> findChildrenCategory(Set<Category> categorySet
                                               ,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category!=null){
            categorySet.add(category);
        }
        //获取以该节点作为父节点的所有子节点
        List<Category> categoryList = categoryMapper.getCategory(categoryId);
        //递归调用
        for(Category categoryTemp:categoryList){
            findChildrenCategory(categorySet,categoryTemp.getId());
        }
        return categorySet;

    }

    /***
     * 添加类目
     * @param categoryName
     * @param parenId
     * @return
     */
    public ServerResponse addCategory(String categoryName,Integer parenId){
        if(parenId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByError("参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parenId);
        category.setStatus(true);
        category.setCreateTime(new Date());
        int count = categoryMapper.insert(category);
        if(count>0) return ServerResponse.createBySuccess("添加品类成功");
        return ServerResponse.createByError("添加品类失败");
    }

    /***
     * 修改类目名称
     * @param categoryId
     * @param categoryName
     * @return
     */
    @Override
    public ServerResponse modiyfCategoryName(Integer categoryId, String categoryName) {
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByError("参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int count = categoryMapper.updateByPrimaryKeySelective(category);
        if(count>0) return ServerResponse.createBySuccess("更新品类名字成功");
        return ServerResponse.createByError("更新品类名字失败");
    }


}
