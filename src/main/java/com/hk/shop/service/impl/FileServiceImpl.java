package com.hk.shop.service.impl;

import com.google.common.collect.Lists;
import com.hk.shop.service.IFileService;
import com.hk.shop.util.FTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author 何康
 * @date 2018/11/5 10:58
 */
@Service
public class FileServiceImpl implements IFileService {
    private Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    /***
     * 上传文件
     * @param file 文件名
     * @param path 上传路径
     * @return 返回上传到服务器的文件名
     */
    public String upload(MultipartFile file,String path){
        //上传的文件名
        String fileName = file.getOriginalFilename();
        //文件拓展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        //服务器的文件名
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        log.info("开始上传文件，上传文件名为：{},上传路径为：{},新文件名为：{}",fileName,path,uploadFileName);
        //新建文件
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.mkdirs();
            //设置可写权限
            fileDir.setWritable(true);
        }
        File targetFile = new File(path,uploadFileName);
        try {
            //上传到项目目录下
            file.transferTo(targetFile);
            //上传到ftp服务器下
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //删除upload下的文件
            targetFile.delete();
        } catch (IOException e) {
            log.error("上传文件失败");
            e.printStackTrace();
        }
        return uploadFileName;
    }

//    public static void main(String[] args) {
//        String fileName ="abc.jpg";
//        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
//        System.out.println(fileExtensionName);
//    }
}
