package com.lonelyash.service.impl;

import com.lonelyash.domain.po.Address;
import com.lonelyash.mapper.AddressMapper;
import com.lonelyash.service.IAddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {
    @Autowired
    private AddressMapper addressMapper;

        public String selectById(Long id) {
            String city = addressMapper.getBydId(id);
            return city;
        }
}
