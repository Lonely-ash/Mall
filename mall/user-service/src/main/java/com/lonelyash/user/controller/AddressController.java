package com.lonelyash.user.controller;

import com.lonelyash.common.exception.BadRequestException;
import com.lonelyash.common.utils.BeanUtils;
import com.lonelyash.common.utils.CollUtils;
import com.lonelyash.common.utils.UserContext;
import com.lonelyash.user.domain.dto.AddressDTO;
import com.lonelyash.user.domain.po.Address;
import com.lonelyash.user.service.IAddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@Api(tags = "收货地址管理接口")
public class AddressController {

    private final IAddressService addressService;

    @ApiOperation("根据id查询地址")
    @GetMapping("{addressId}")
    public AddressDTO findAddressById(@ApiParam("地址id") @PathVariable("addressId") Long id) {
        Address address = getAddressByIdAndCheckOwner(id);
        return BeanUtils.copyBean(address, AddressDTO.class);
    }

    @ApiOperation("查询当前用户地址列表")
    @GetMapping
    public List<AddressDTO> findMyAddresses() {
        List<Address> list = addressService.query().eq("user_id", UserContext.getUser()).list();
        if (CollUtils.isEmpty(list)) {
            return CollUtils.emptyList();
        }
        return BeanUtils.copyList(list, AddressDTO.class);
    }

    @ApiOperation("新增地址")
    @PostMapping
    public Long saveAddress(@RequestBody AddressDTO dto) {
        Long userId = UserContext.getUser();
        Address address = BeanUtils.copyBean(dto, Address.class);
        address.setId(null);
        address.setUserId(userId);

        long addressCount = addressService.query().eq("user_id", userId).count();
        if (address.getIsDefault() == null) {
            address.setIsDefault(addressCount == 0 ? 1 : 0);
        }
        if (address.getIsDefault() == 1) {
            clearUserDefaultAddress(userId);
        }

        addressService.save(address);
        return address.getId();
    }

    @ApiOperation("修改地址")
    @PutMapping("{addressId}")
    public void updateAddress(@PathVariable("addressId") Long addressId, @RequestBody AddressDTO dto) {
        Address old = getAddressByIdAndCheckOwner(addressId);
        Address update = BeanUtils.copyBean(dto, Address.class);
        update.setId(old.getId());
        update.setUserId(old.getUserId());

        if (update.getIsDefault() != null && update.getIsDefault() == 1) {
            clearUserDefaultAddress(old.getUserId());
        }
        addressService.updateById(update);
    }

    @ApiOperation("删除地址")
    @DeleteMapping("{addressId}")
    public void deleteAddress(@PathVariable("addressId") Long addressId) {
        Address old = getAddressByIdAndCheckOwner(addressId);
        addressService.removeById(old.getId());
    }

    @ApiOperation("设为默认地址")
    @PutMapping("{addressId}/default")
    public void setDefaultAddress(@PathVariable("addressId") Long addressId) {
        Address old = getAddressByIdAndCheckOwner(addressId);
        Long userId = old.getUserId();
        clearUserDefaultAddress(userId);
        Address update = new Address();
        update.setId(old.getId());
        update.setIsDefault(1);
        addressService.updateById(update);
    }

    private Address getAddressByIdAndCheckOwner(Long addressId) {
        Address address = addressService.getById(addressId);
        if (address == null) {
            throw new BadRequestException("地址不存在");
        }
        Long userId = UserContext.getUser();
        if (!address.getUserId().equals(userId)) {
            throw new BadRequestException("地址不属于当前登录用户");
        }
        return address;
    }

    private void clearUserDefaultAddress(Long userId) {
        Address clear = new Address();
        clear.setIsDefault(0);
        addressService.lambdaUpdate()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .update(clear);
    }
}
