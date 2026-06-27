package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 */
@Configuration
@Slf4j
public class OssConfiguration
{
    /**
     * 创建阿里云OSS工具类实例
     * @param aliOssProperties
     * @return
     */
    @Bean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties)
    {
        return new AliOssUtil(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getRegion(),
                aliOssProperties.getBucketName());
    }
}
