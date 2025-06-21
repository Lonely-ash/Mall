package com.lonelyash.controller.admin;

import com.lonelyash.dto.OrdersCancelDTO;
import com.lonelyash.dto.OrdersConfirmDTO;
import com.lonelyash.dto.OrdersPageQueryDTO;
import com.lonelyash.dto.OrdersRejectionDTO;
import com.lonelyash.result.PageResult;
import com.lonelyash.result.Result;
import com.lonelyash.service.OrderService;
import com.lonelyash.vo.OrderStatisticsVO;
import com.lonelyash.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@Api(tags = "管理端订单相关接口")
@Slf4j
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单搜索:{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.coconditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable("id") Long id) {
        log.info("查询订单详情:{}",id);
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    /**
     *各个状态的订单数量统计
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics() {
        log.info("各个状态的订单数量统计");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单:{}",ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        log.info("拒单:{}",ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
            log.info("取消订单:{}", ordersCancelDTO);
            orderService.cancelOrder(ordersCancelDTO);
            return Result.success();
    }

    /**
     * 派送订单
     * @param id
     * @return
     * @throws Exception
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable("id") Long id) throws Exception {
        log.info("派送订单:{}", id);
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 完成订单
     * @param id
     * @return
     * @throws Exception
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable("id") Long id) throws Exception {
        log.info("完成订单:{}", id);
        orderService.complete(id);
        return Result.success();
    }
}
