package com.lonelyash.pay.controller;

import com.lonelyash.api.dto.PayOrderDTO;
import com.lonelyash.common.exception.BizIllegalException;
import com.lonelyash.common.utils.BeanUtils;
import com.lonelyash.pay.domain.dto.PayApplyDTO;
import com.lonelyash.pay.domain.dto.PayOrderFormDTO;
import com.lonelyash.pay.domain.po.PayOrder;
import com.lonelyash.pay.domain.vo.PayOrderVO;
import com.lonelyash.pay.enums.PayType;
import com.lonelyash.pay.service.IPayOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "支付相关接口")
@RestController
@RequestMapping("pay-orders")
@RequiredArgsConstructor
public class PayController {

    private final IPayOrderService payOrderService;

    @ApiOperation("生成支付单")
    @PostMapping
    public String applyPayOrder(@RequestBody PayApplyDTO applyDTO) {
        // 双保险：如果前端明确传了余额渠道，则强制按余额支付类型处理。
        if ("balance".equalsIgnoreCase(applyDTO.getPayChannelCode())) {
            applyDTO.setPayType(PayType.BALANCE.getValue());
        }
        if (!PayType.BALANCE.equalsValue(applyDTO.getPayType())) {
            throw new BizIllegalException("抱歉，目前只支持余额支付");
        }
        return payOrderService.applyPayOrder(applyDTO);
    }

    @ApiOperation("尝试基于用户余额支付")
    @ApiImplicitParam(value = "支付单id", name = "id")
    @PostMapping("{id}")
    public void tryPayOrderByBalance(@PathVariable("id") Long id, @RequestBody PayOrderFormDTO payOrderFormDTO) {
        payOrderFormDTO.setId(id);
        payOrderService.tryPayOrderByBalance(payOrderFormDTO);
    }

    @ApiOperation("查询支付单")
    @GetMapping
    public List<PayOrderVO> queryPayOrders() {
        return BeanUtils.copyList(payOrderService.list(), PayOrderVO.class);
    }

    @ApiOperation("根据业务订单id查询支付单")
    @GetMapping("/biz/{id}")
    public PayOrderDTO queryPayOrderByBizOrderNo(@PathVariable("id") Long id) {
        PayOrder payOrder = payOrderService.lambdaQuery().eq(PayOrder::getBizOrderNo, id).one();
        return BeanUtils.copyBean(payOrder, PayOrderDTO.class);
    }
}
