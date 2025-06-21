package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    void cancelOrder(OrdersCancelDTO ordersCancelDTO) throws Exception;

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult coconditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    OrderVO details(Long id);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    /**
     * 派送
     * @param id
     */
    void delivery(Long id);

    /**
     * 完成订单
     * @param id
     */
    void complete(Long id);

    /**
     * 用户取消订单
     * @param id
     */
    void userCancelById(long id) throws Exception;

    /**
     * 历史订单查询
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    PageResult pageQuery4User(int page, int pageSize, Integer status);

    /**
     * 再来一单
     *
     * @param id
     */
    void repetition(Long id);

    void payment();


    void reminder(Long id);

//    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;
}
