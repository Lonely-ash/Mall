package com.lonelyash.service;


import com.lonelyash.dto.ShoppingCartDTO;
import com.lonelyash.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车
     * @return
     */
    List<ShoppingCart> showShoppingCart();

    /**
     * 清空购物车
     */
    void clean();

    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
