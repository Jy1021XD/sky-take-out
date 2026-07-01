package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺相关接口
 */
@RestController("userShopController")
@Api(tags = "店铺相关接口")
@RequestMapping("/user/shop")
@Slf4j
public class ShopController
{
    @Autowired
    private ShopService shopService;

    /**
     * 获取店铺状态
     * @return
     */
    @GetMapping("status")
    @ApiOperation("获取店铺状态")
    public Result<Integer> getStatus()
    {
        log.info("获取店铺状态");
        Integer status = shopService.getStatus();
        return Result.success(status);
    }

}
