package com.lonelyash.mapper;

import com.lonelyash.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户
     * @param openId
     * @return
     */
    @Select("select * from pupu.user where openid = #{openId}")
    User getByOpenid(String openId);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);

    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    Integer countByMap(Map map);
}
