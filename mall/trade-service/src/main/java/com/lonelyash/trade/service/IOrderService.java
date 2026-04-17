package com.lonelyash.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lonelyash.common.domain.PageDTO;
import com.lonelyash.trade.domain.dto.OrderFormDTO;
import com.lonelyash.trade.domain.po.Order;
import com.lonelyash.trade.domain.query.OrderPageQuery;
import com.lonelyash.trade.domain.vo.OrderVO;

public interface IOrderService extends IService<Order> {

    Long createOrder(OrderFormDTO orderFormDTO);

    void markOrderPaySuccess(Long orderId);

    void cancleOrder(Long orderId);

    OrderVO queryOrderById(Long orderId);

    PageDTO<OrderVO> queryMyOrders(OrderPageQuery query);

    PageDTO<OrderVO> queryOrders(OrderPageQuery query);

    void updateOrderStatus(Long orderId, Integer status);
}
