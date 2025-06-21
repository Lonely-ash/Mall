package com.mall.cart.listener;

import com.mall.cart.service.ICartService;
import com.mall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Slf4j
@RequiredArgsConstructor
public class CartClearListener {

        private final ICartService cartService;

        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(name = "cart.clear.queue", durable = "true"),
                exchange = @Exchange(name = "trade.topic"),
                key = "order.create"
        ))
    public void listenCartClear(Collection<Long> itemIds, Message message){
            Long userId = message.getMessageProperties().getHeader("userId");
            log.info("监听到购物车清理消息：{}",userId);
            UserContext.setUser(userId);
            cartService.removeByItemIds(itemIds);
        }
}
