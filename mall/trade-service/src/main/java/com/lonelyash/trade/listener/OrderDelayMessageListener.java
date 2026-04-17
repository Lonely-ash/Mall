package com.lonelyash.trade.listener;

import com.lonelyash.api.client.PayClient;
import com.lonelyash.api.dto.PayOrderDTO;
import com.lonelyash.trade.constants.MQConstants;
import com.lonelyash.trade.domain.po.Order;
import com.lonelyash.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class OrderDelayMessageListener {

    private final IOrderService orderService;
    private final PayClient payClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.DELAY_ORDER_QUEUE_NAME),
            exchange = @Exchange(name = MQConstants.DELAY_EXCHANGE_NAME, delayed = "true"),
            key = MQConstants.DELAY_ORDER_KEY
    ))
    public void listenDelayOrderMessage(Long orderId){
        Order order = orderService.getById(orderId);
        if(order == null || order.getStatus() != 1){
            return;
        }
        PayOrderDTO payOrderDTO = payClient.queryPayOrderByBizOrderNo(orderId);

        if(payOrderDTO != null && payOrderDTO.getStatus() == 3){
            orderService.markOrderPaySuccess(orderId);
        }else {
            orderService.cancleOrder(orderId);
        }
    }

//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(name = MQConstants.DELAY_ORDER_QUEUE_NAME),
//            exchange = @Exchange(name = MQConstants.DELAY_EXCHANGE_NAME, delayed = "true"),
//            key = MQConstants.DELAY_ORDER_KEY
//    ))
//    public void listenDelayOrderMessage(Long orderId) {
//        // 1. 幂等性检查
//        if (orderService.isMessageProcessed(orderId)) return;
//
//        // 2. 查询订单
//        Order order = orderService.getById(orderId);
//        Set<Integer> validStatus = Set.of(OrderStatus.PENDING_PAYMENT, OrderStatus.PART_PAID);
//        if (order == null || !validStatus.contains(order.getStatus())) return;
//
//        try {
//            // 3. 查询支付状态（含重试机制）
//            PayOrderDTO payOrderDTO = payClient.queryPayOrderByBizOrderNo(orderId);
//
//            // 4. 状态机处理
//            if (payOrderDTO == null) {
//                orderService.markManualReview(orderId);
//            } else {
//                processPaymentResult(orderId, payOrderDTO);
//            }
//
//            // 5. 更新消息处理标记
//            orderService.markMessageProcessed(orderId);
//        } catch (Exception e) {
//            // 6. 异常处理（记录日志+触发重试）
//            log.error("处理延迟订单失败 orderId={}", orderId, e);
//            throw new RuntimeException("系统重试", e);
//        }
//    }
//
//    private void processPaymentResult(Long orderId, PayOrderDTO payOrder) {
//        switch (payOrder.getStatus()) {
//            case PayStatus.PROCESSING:
//                orderService.delayCheck(orderId, 5); // 5分钟后再查
//                break;
//            case PayStatus.SUCCESS:
//                orderService.markOrderPaySuccess(orderId);
//                break;
//            default:
//                orderService.cancleOrder(orderId);
//        }
//    }
}
