package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.domain.dto.LoginFormDTO;
import com.mall.domain.po.User;
import com.mall.domain.vo.UserLoginVO;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
public interface IUserService extends IService<User> {

    UserLoginVO login(LoginFormDTO loginFormDTO);

    void deductMoney(String pw, Integer totalFee);
}
