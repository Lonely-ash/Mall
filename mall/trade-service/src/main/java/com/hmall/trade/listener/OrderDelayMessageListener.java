package com.mall.trade.listener;

import com.mall.api.client.PayClient;
import com.mall.api.dto.PayOrderDTO;
import com.mall.trade.constants.MQConstants;
import com.mall.trade.domain.po.Order;
import com.mall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

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
}
