package com.lonelyash.mapper;

import com.lonelyash.domain.po.Address;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
public interface AddressMapper extends BaseMapper<Address> {
    @Select("select city from mall.user, #{id}")
    public String getBydId(Long id);


}
