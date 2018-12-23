package com.hk.shop.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author 何康
 * @date 2018/11/5 10:58
 */
public interface IFileService {

    //上传文件
    String upload(MultipartFile file, String path);
}
