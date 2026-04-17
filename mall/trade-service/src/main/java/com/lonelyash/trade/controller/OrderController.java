package com.lonelyash.trade.controller;

import com.lonelyash.common.domain.PageDTO;
import com.lonelyash.trade.domain.dto.OrderFormDTO;
import com.lonelyash.trade.domain.query.OrderPageQuery;
import com.lonelyash.trade.domain.vo.OrderVO;
import com.lonelyash.trade.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "订单管理接口")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;

    @ApiOperation("根据id查询订单")
    @GetMapping("{id}")
    public OrderVO queryOrderById(@PathVariable("id") Long orderId) {
        return orderService.queryOrderById(orderId);
    }

    @ApiOperation("创建订单")
    @PostMapping
    public Long createOrder(@RequestBody OrderFormDTO orderFormDTO){
        return orderService.createOrder(orderFormDTO);
    }

    @ApiOperation("买家分页查询我的订单")
    @GetMapping("/page/my")
    public PageDTO<OrderVO> queryMyOrders(OrderPageQuery query) {
        return orderService.queryMyOrders(query);
    }

    @ApiOperation("管理员分页查询订单")
    @GetMapping("/page")
    public PageDTO<OrderVO> queryOrders(OrderPageQuery query) {
        return orderService.queryOrders(query);
    }

    @ApiOperation("更新订单状态")
    @ApiImplicitParam(name = "orderId", value = "订单id", paramType = "path")
    @PutMapping("/{orderId}/status/{status}")
    public void updateOrderStatus(@PathVariable("orderId") Long orderId, @PathVariable("status") Integer status) {
        orderService.updateOrderStatus(orderId, status);
    }

    @ApiOperation("标记订单已支付")
    @ApiImplicitParam(name = "orderId", value = "订单id", paramType = "path")
    @PutMapping("/{orderId}")
    public void markOrderPaySuccess(@PathVariable("orderId") Long orderId) {
        orderService.markOrderPaySuccess(orderId);
    }
}
