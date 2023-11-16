package com.cclucky.printserver.controller;

import com.cclucky.printserver.config.MinioConfig;
import com.cclucky.printserver.config.prop.MinioProp;
import com.cclucky.printserver.handle.PrintEventHandle;
import com.cclucky.printserver.utils.MinioUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Calendar;


@RestController
@RequestMapping("/minio")
@Slf4j
public class PrintController {

    @Autowired
    private MinioUtil minioUtils;

    @Autowired
    private MinioProp minioProp;

    @Autowired
    private PrintEventHandle printEventHandle;


    /**
     * 文件上传
     *
     * @param file
     */
    @PostMapping("/upload")
    public String upload(MultipartFile file) {
        String newFileName;
        try {
            //文件名
            // TODO: 2023/11/15 根据实际名称命名文件以减少重复上传
            String fileName = file.getOriginalFilename();
//            newFileName = System.currentTimeMillis() + "." + StringUtils.substringAfterLast(fileName, ".");
            // 获取当前日期
            Calendar instance = Calendar.getInstance();
            int year = instance.get(Calendar.YEAR);
            int month = instance.get(Calendar.MONTH) + 1;
            int date = instance.get(Calendar.DAY_OF_MONTH);
            newFileName = year + "-" + month + "-" + date + "/" + System.currentTimeMillis() + "." + StringUtils.substringAfterLast(fileName, ".");
            //类型
            String contentType = file.getContentType();
            minioUtils.uploadFile(minioProp.getBucketName(), file, newFileName, contentType);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传失败");
            return "上传失败";
        }
        String filePath = minioProp.getEndpoint() + "/" + minioProp.getBucketName() + "/" + newFileName;
        printEventHandle.handle(filePath);
        return filePath.split("/")[1];
    }

    /**
     * 文件上传
     */
    @PostMapping("/upload2")
    public String upload2() {
        return "upload2";
    }
}
