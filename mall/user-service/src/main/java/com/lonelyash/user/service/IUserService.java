package com.lonelyash.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lonelyash.common.domain.PageDTO;
import com.lonelyash.user.domain.dto.LoginFormDTO;
import com.lonelyash.user.domain.dto.RegisterFormDTO;
import com.lonelyash.user.domain.dto.UpdateUserProfileDTO;
import com.lonelyash.user.domain.po.User;
import com.lonelyash.user.domain.query.UserPageQuery;
import com.lonelyash.user.domain.vo.UserAdminVO;
import com.lonelyash.user.domain.vo.UserLoginVO;
import com.lonelyash.user.domain.vo.UserProfileVO;

public interface IUserService extends IService<User> {

    UserLoginVO login(LoginFormDTO loginFormDTO);

    void register(RegisterFormDTO registerFormDTO);

    UserProfileVO queryMyProfile();

    void updateMyProfile(UpdateUserProfileDTO dto);

    PageDTO<UserAdminVO> queryUsers(UserPageQuery query);

    void updateUserStatus(Long userId, Integer status);

    void deductMoney(String pw, Integer totalFee);
}
