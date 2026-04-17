package com.lonelyash.pay.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lonelyash.api.client.TradeClient;
import com.lonelyash.api.client.UserClient;
import com.lonelyash.common.exception.BizIllegalException;
import com.lonelyash.common.utils.BeanUtils;
import com.lonelyash.common.utils.UserContext;
import com.lonelyash.pay.domain.dto.PayApplyDTO;
import com.lonelyash.pay.domain.dto.PayOrderFormDTO;
import com.lonelyash.pay.domain.po.PayOrder;
import com.lonelyash.pay.enums.PayStatus;
import com.lonelyash.pay.mapper.PayOrderMapper;
import com.lonelyash.pay.service.IPayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements IPayOrderService {

    private final UserClient userClient;
    private final TradeClient tradeClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public String applyPayOrder(PayApplyDTO applyDTO) {
        PayOrder payOrder = checkIdempotent(applyDTO);
        return payOrder.getId().toString();
    }

    @Override
    @Transactional
    public void tryPayOrderByBalance(PayOrderFormDTO payOrderFormDTO) {
        PayOrder po = queryByIdOrPayOrderNo(payOrderFormDTO.getId());
        if (po == null) {
            throw new BizIllegalException("支付单不存在");
        }

        if (!PayStatus.WAIT_BUYER_PAY.equalsValue(po.getStatus())) {
            throw new BizIllegalException("交易已支付或关闭");
        }

        userClient.deductMoney(payOrderFormDTO.getPw(), po.getAmount());

        boolean success = markPayOrderSuccess(po.getId(), LocalDateTime.now());
        if (!success) {
            throw new BizIllegalException("交易已支付或关闭");
        }

        tradeClient.markOrderPaySuccess(po.getBizOrderNo());
        try {
            rabbitTemplate.convertAndSend("pay.direct", "pay.success", po.getBizOrderNo());
        } catch (Exception e) {
            log.error("pay success message send failed, payOrderId={}, bizOrderNo={}", po.getId(), po.getBizOrderNo(), e);
        }
    }

    public boolean markPayOrderSuccess(Long id, LocalDateTime successTime) {
        return lambdaUpdate()
                .set(PayOrder::getStatus, PayStatus.TRADE_SUCCESS.getValue())
                .set(PayOrder::getPaySuccessTime, successTime)
                .eq(PayOrder::getId, id)
                .in(PayOrder::getStatus, PayStatus.NOT_COMMIT.getValue(), PayStatus.WAIT_BUYER_PAY.getValue())
                .update();
    }

    private PayOrder checkIdempotent(PayApplyDTO applyDTO) {
        PayOrder oldOrder = queryByBizOrderNo(applyDTO.getBizOrderNo());
        if (oldOrder == null) {
            PayOrder payOrder = buildPayOrder(applyDTO);
            payOrder.setPayOrderNo(IdWorker.getId());
            save(payOrder);
            return payOrder;
        }

        if (PayStatus.TRADE_SUCCESS.equalsValue(oldOrder.getStatus())) {
            throw new BizIllegalException("订单已经支付");
        }
        if (PayStatus.TRADE_CLOSED.equalsValue(oldOrder.getStatus())) {
            throw new BizIllegalException("订单已关闭");
        }

        if (!StringUtils.equals(oldOrder.getPayChannelCode(), applyDTO.getPayChannelCode())) {
            PayOrder payOrder = buildPayOrder(applyDTO);
            payOrder.setId(oldOrder.getId());
            payOrder.setQrCodeUrl("");
            updateById(payOrder);
            payOrder.setPayOrderNo(oldOrder.getPayOrderNo());
            return payOrder;
        }
        return oldOrder;
    }

    private PayOrder buildPayOrder(PayApplyDTO payApplyDTO) {
        PayOrder payOrder = BeanUtils.toBean(payApplyDTO, PayOrder.class);
        payOrder.setPayOverTime(LocalDateTime.now().plusMinutes(120L));
        payOrder.setStatus(PayStatus.WAIT_BUYER_PAY.getValue());
        payOrder.setBizUserId(UserContext.getUser());
        return payOrder;
    }

    public PayOrder queryByBizOrderNo(Long bizOrderNo) {
        return lambdaQuery()
                .eq(PayOrder::getBizOrderNo, bizOrderNo)
                .one();
    }

    private PayOrder queryByIdOrPayOrderNo(Long idOrNo) {
        PayOrder po = getById(idOrNo);
        if (po != null) {
            return po;
        }
        po = lambdaQuery().eq(PayOrder::getPayOrderNo, idOrNo).one();
        if (po != null) {
            return po;
        }
        return lambdaQuery().eq(PayOrder::getBizOrderNo, idOrNo).one();
    }
}
