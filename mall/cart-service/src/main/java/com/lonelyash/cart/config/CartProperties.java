package com.lonelyash.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mall.cart")
public class CartProperties {
    /**
     * 单个用户购物车最大条目数，配置缺失时使用默认值。
     */
    private Integer maxAmount = 100;
}
