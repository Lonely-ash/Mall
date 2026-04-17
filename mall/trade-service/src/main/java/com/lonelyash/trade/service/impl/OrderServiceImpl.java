package com.lonelyash.trade.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lonelyash.api.client.CartClient;
import com.lonelyash.api.client.ItemClient;
import com.lonelyash.api.dto.ItemDTO;
import com.lonelyash.api.dto.OrderDetailDTO;
import com.lonelyash.common.domain.PageDTO;
import com.lonelyash.common.exception.BadRequestException;
import com.lonelyash.common.exception.BizIllegalException;
import com.lonelyash.common.exception.ForbiddenException;
import com.lonelyash.common.utils.BeanUtils;
import com.lonelyash.common.utils.UserContext;
import com.lonelyash.trade.constants.MQConstants;
import com.lonelyash.trade.domain.dto.OrderFormDTO;
import com.lonelyash.trade.domain.po.Order;
import com.lonelyash.trade.domain.po.OrderDetail;
import com.lonelyash.trade.domain.query.OrderPageQuery;
import com.lonelyash.trade.domain.vo.OrderVO;
import com.lonelyash.trade.mapper.OrderMapper;
import com.lonelyash.trade.service.IOrderDetailService;
import com.lonelyash.trade.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private static final Long ADMIN_USER_ID = 1L;

    private final ItemClient itemClient;
    private final IOrderDetailService detailService;
    private final CartClient cartClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @GlobalTransactional
    public Long createOrder(OrderFormDTO orderFormDTO) {
        List<OrderDetailDTO> detailDTOS = orderFormDTO.getDetails();
        if (detailDTOS == null || detailDTOS.isEmpty()) {
            throw new BadRequestException("订单明细不能为空");
        }

        Map<Long, Integer> itemNumMap = detailDTOS.stream()
                .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
        Set<Long> itemIds = itemNumMap.keySet();

        List<ItemDTO> items = itemClient.queryItemByIds(itemIds);
        if (items == null || items.size() < itemIds.size()) {
            throw new BadRequestException("商品不存在");
        }

        for (ItemDTO item : items) {
            Integer buyNum = itemNumMap.get(item.getId());
            if (item.getStatus() == null || item.getStatus() != 1) {
                throw new BizIllegalException("商品已下架: " + item.getName());
            }
            if (item.getStock() == null || item.getStock() < buyNum) {
                throw new BizIllegalException("库存不足: " + item.getName());
            }
        }

        int total = 0;
        for (ItemDTO item : items) {
            total += item.getPrice() * itemNumMap.get(item.getId());
        }

        Order order = new Order();
        order.setTotalFee(total);
        order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(UserContext.getUser());
        order.setStatus(1);
        save(order);

        List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
        detailService.saveBatch(details);

        try {
            rabbitTemplate.convertAndSend("trade.topic", "order.create", itemIds, message -> {
                Long userId = UserContext.getUser();
                message.getMessageProperties().setHeader("userId", userId);
                return message;
            });
        } catch (Exception e) {
            log.error("cart clear message send failed", e);
        }

        try {
            itemClient.deductStock(detailDTOS);
        } catch (Exception e) {
            throw new BizIllegalException("扣减库存失败: " + e.getMessage(), e);
        }

        rabbitTemplate.convertAndSend(
                MQConstants.DELAY_EXCHANGE_NAME,
                MQConstants.DELAY_ORDER_KEY,
                order.getId(),
                message -> {
                    message.getMessageProperties().setDelay(10000);
                    return message;
                }
        );

        return order.getId();
    }

    @Override
    public void markOrderPaySuccess(Long orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void cancleOrder(Long orderId) {
    }

    @Override
    public OrderVO queryOrderById(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BadRequestException("订单不存在");
        }
        Long currentUser = UserContext.getUser();
        if (!ObjectUtil.equal(order.getUserId(), currentUser) && !isAdmin()) {
            throw new ForbiddenException("无权访问该订单");
        }
        return BeanUtils.copyBean(order, OrderVO.class);
    }

    @Override
    public PageDTO<OrderVO> queryMyOrders(OrderPageQuery query) {
        Long currentUser = UserContext.getUser();
        Page<Order> page = lambdaQuery()
                .eq(Order::getUserId, currentUser)
                .eq(query.getStatus() != null, Order::getStatus, query.getStatus())
                .page(query.toMpPage("create_time", false));
        return PageDTO.of(page, OrderVO.class);
    }

    @Override
    public PageDTO<OrderVO> queryOrders(OrderPageQuery query) {
        if (!isAdmin()) {
            throw new ForbiddenException("无管理员权限");
        }
        Page<Order> page = lambdaQuery()
                .eq(query.getUserId() != null, Order::getUserId, query.getUserId())
                .eq(query.getStatus() != null, Order::getStatus, query.getStatus())
                .page(query.toMpPage("create_time", false));
        return PageDTO.of(page, OrderVO.class);
    }

    @Override
    public void updateOrderStatus(Long orderId, Integer status) {
        if (!isAdmin()) {
            throw new ForbiddenException("无管理员权限");
        }
        Order old = getById(orderId);
        if (old == null) {
            throw new BadRequestException("订单不存在");
        }

        Order update = new Order();
        update.setId(orderId);
        update.setStatus(status);
        if (ObjectUtil.equal(status, 3)) {
            update.setConsignTime(LocalDateTime.now());
        }
        if (ObjectUtil.equal(status, 4)) {
            update.setEndTime(LocalDateTime.now());
        }
        if (ObjectUtil.equal(status, 5)) {
            update.setCloseTime(LocalDateTime.now());
        }
        updateById(update);
    }

    private boolean isAdmin() {
        return ObjectUtil.equal(UserContext.getUser(), ADMIN_USER_ID);
    }

    private List<OrderDetail> buildDetails(Long orderId, List<ItemDTO> items, Map<Long, Integer> numMap) {
        List<OrderDetail> details = new ArrayList<>(items.size());
        for (ItemDTO item : items) {
            OrderDetail detail = new OrderDetail();
            detail.setName(item.getName());
            detail.setSpec(item.getSpec());
            detail.setPrice(item.getPrice());
            detail.setNum(numMap.get(item.getId()));
            detail.setItemId(item.getId());
            detail.setImage(item.getImage());
            detail.setOrderId(orderId);
            details.add(detail);
        }
        return details;
    }
}
