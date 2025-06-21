package com.lonelyash.service;


import com.lonelyash.dto.UserLoginDTO;
import com.lonelyash.entity.User;

public interface UserService {


    /**
     * 用户登录
     *
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);
}
