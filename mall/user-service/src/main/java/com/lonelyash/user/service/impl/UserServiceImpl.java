package com.lonelyash.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lonelyash.common.domain.PageDTO;
import com.lonelyash.common.exception.BadRequestException;
import com.lonelyash.common.exception.BizIllegalException;
import com.lonelyash.common.exception.ForbiddenException;
import com.lonelyash.common.utils.UserContext;
import com.lonelyash.user.config.JwtProperties;
import com.lonelyash.user.domain.dto.LoginFormDTO;
import com.lonelyash.user.domain.dto.RegisterFormDTO;
import com.lonelyash.user.domain.dto.UpdateUserProfileDTO;
import com.lonelyash.user.domain.po.User;
import com.lonelyash.user.domain.query.UserPageQuery;
import com.lonelyash.user.domain.vo.UserAdminVO;
import com.lonelyash.user.domain.vo.UserLoginVO;
import com.lonelyash.user.domain.vo.UserProfileVO;
import com.lonelyash.user.enums.UserStatus;
import com.lonelyash.user.mapper.UserMapper;
import com.lonelyash.user.service.IUserService;
import com.lonelyash.user.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private static final String ADMIN_USERNAME = "admin";

    private final PasswordEncoder passwordEncoder;

    private final JwtTool jwtTool;

    private final JwtProperties jwtProperties;

    @Override
    public UserLoginVO login(LoginFormDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        User user = query().eq("username", username).one();
        if (user == null) {
            log.warn("login failed, username not found: {}", username);
            throw new BadRequestException("用户名或密码错误");
        }

        if (user.getStatus() == UserStatus.FROZEN) {
            throw new ForbiddenException("用户被冻结");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("login failed, bad password: username={}", username);
            throw new BadRequestException("用户名或密码错误");
        }

        String token = jwtTool.createToken(user.getId(), jwtProperties.getTokenTTL());
        log.info("login success: userId={}, username={}", user.getId(), username);

        UserLoginVO vo = new UserLoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setBalance(user.getBalance());
        vo.setToken(token);
        vo.setRole(resolveRole(user));
        return vo;
    }

    @Override
    public void register(RegisterFormDTO registerFormDTO) {
        String username = StrUtil.trim(registerFormDTO.getUsername());
        if (StrUtil.isBlank(username)) {
            throw new BadRequestException("用户名不能为空");
        }
        long count = query().eq("username", username).count();
        if (count > 0) {
            throw new BadRequestException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(registerFormDTO.getPassword()));
        user.setPhone(StrUtil.emptyToNull(StrUtil.trim(registerFormDTO.getPhone())));
        user.setStatus(UserStatus.NORMAL);
        user.setBalance(0);
        save(user);
    }

    @Override
    public UserProfileVO queryMyProfile() {
        User user = getLoginUser();
        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setBalance(user.getBalance());
        vo.setStatus(user.getStatus() == null ? null : user.getStatus().getValue());
        vo.setRole(resolveRole(user));
        return vo;
    }

    @Override
    public void updateMyProfile(UpdateUserProfileDTO dto) {
        User user = getLoginUser();
        User update = new User();
        update.setId(user.getId());

        if (dto.getPhone() != null) {
            update.setPhone(StrUtil.emptyToNull(StrUtil.trim(dto.getPhone())));
        }

        if (StrUtil.isNotBlank(dto.getNewPassword())) {
            if (StrUtil.isBlank(dto.getOldPassword())) {
                throw new BadRequestException("修改密码时必须提供旧密码");
            }
            if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
                throw new BadRequestException("旧密码错误");
            }
            update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        updateById(update);
    }

    @Override
    public PageDTO<UserAdminVO> queryUsers(UserPageQuery query) {
        assertAdmin();
        Page<User> result = lambdaQuery()
                .like(StrUtil.isNotBlank(query.getUsername()), User::getUsername, query.getUsername())
                .eq(query.getStatus() != null, User::getStatus, UserStatus.of(query.getStatus()))
                .page(query.toMpPage("create_time", false));

        return PageDTO.of(result, user -> {
            UserAdminVO vo = new UserAdminVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setPhone(user.getPhone());
            vo.setBalance(user.getBalance());
            vo.setStatus(user.getStatus() == null ? null : user.getStatus().getValue());
            vo.setCreateTime(user.getCreateTime());
            vo.setUpdateTime(user.getUpdateTime());
            return vo;
        });
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        assertAdmin();
        User user = getById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        if (ADMIN_USERNAME.equalsIgnoreCase(user.getUsername()) && status == UserStatus.FROZEN.getValue()) {
            throw new BadRequestException("管理员账号不可冻结");
        }
        User update = new User();
        update.setId(userId);
        update.setStatus(UserStatus.of(status));
        updateById(update);
    }

    @Override
    public void deductMoney(String pw, Integer totalFee) {
        User user = getLoginUser();
        if (!passwordEncoder.matches(pw, user.getPassword())) {
            throw new BizIllegalException("用户密码错误");
        }

        try {
            baseMapper.updateMoney(user.getId(), totalFee);
        } catch (Exception e) {
            throw new RuntimeException("扣款失败，可能是余额不足", e);
        }
    }

    private User getLoginUser() {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new ForbiddenException("未登录");
        }
        User user = getById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        return user;
    }

    private void assertAdmin() {
        User user = getLoginUser();
        if (!ADMIN_USERNAME.equalsIgnoreCase(user.getUsername())) {
            throw new ForbiddenException("无管理员权限");
        }
    }

    private String resolveRole(User user) {
        return ADMIN_USERNAME.equalsIgnoreCase(user.getUsername()) ? "admin" : "buyer";
    }
}
