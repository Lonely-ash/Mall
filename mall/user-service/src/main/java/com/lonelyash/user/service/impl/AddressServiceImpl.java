package com.lonelyash.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lonelyash.user.domain.po.Address;
import com.lonelyash.user.mapper.AddressMapper;
import com.lonelyash.user.service.IAddressService;
import org.springframework.stereotype.Service;


@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {

}
