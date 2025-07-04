package com.mall.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.trade.domain.dto.OrderFormDTO;
import com.mall.trade.domain.po.Order;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
public interface IOrderService extends IService<Order> {

    Long createOrder(OrderFormDTO orderFormDTO);

    void markOrderPaySuccess(Long orderId);

    void cancleOrder(Long orderId);
}
