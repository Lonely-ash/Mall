package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@Slf4j
@Api(tags = "店铺管理端接口")
@RequestMapping("/admin/shop")
public class ShopController {

    public static final String key = "shop_status";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置店铺状态
     * @param status
     * @return
     */
    @ApiOperation("设置店铺状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺状态为:{}",status == 1 ? "营业中" : "打样");
        redisTemplate.opsForValue().set(key, status);
        return Result.success();
    }

    /**
     * 获取店铺状态
     * @return
     */
    @ApiOperation("获取店铺状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
        log.info("获取店铺状态:{}",status == 1 ? "营业中": "打样");
        return Result.success(status);
    }
}
