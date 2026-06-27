package com.sky.service.impl;

import com.sky.service.CommonService;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService
{
    @Autowired
    private AliOssUtil aliOssUtil;

    @Override
    public String upload(MultipartFile file)
    {
        try
        {
            // 获取文件名
            String fileName = file.getOriginalFilename();
            // 获取文件名后缀
            String suffix = fileName.substring(fileName.lastIndexOf("."));
            // 构造新文件名
            String newFileName = UUID.randomUUID().toString() + suffix;
            // 文件上传
            return aliOssUtil.upload(file.getBytes(), newFileName);
        } catch (Exception e)
        {
            log.error("文件上传失败：{}", e.getMessage());
        }
        return null;
    }
}
