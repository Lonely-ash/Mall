package com.lonelyash.service.impl;

import com.lonelyash.domain.po.OrderDetail;
import com.lonelyash.mapper.OrderDetailMapper;
import com.lonelyash.service.IOrderDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单详情表 服务实现类
 * </p>

 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements IOrderDetailService {

}
