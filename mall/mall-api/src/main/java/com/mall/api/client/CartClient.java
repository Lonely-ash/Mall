package com.mall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient("cart-service")
public interface CartClient {
//    @DeleteMapping
//    public void deleteCartItemByIds(@RequestParam("ids") List<Long> ids){
//        cartService.removeByItemIds(ids);
//    }
    @DeleteMapping("/carts")
    void removeByItemIds(@RequestParam("ids") Collection<Long> ids);
}
