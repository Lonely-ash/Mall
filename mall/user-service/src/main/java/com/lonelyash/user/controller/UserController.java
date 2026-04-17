package com.lonelyash.user.controller;

import com.lonelyash.common.domain.PageDTO;
import com.lonelyash.user.domain.dto.LoginFormDTO;
import com.lonelyash.user.domain.dto.RegisterFormDTO;
import com.lonelyash.user.domain.dto.UpdateUserProfileDTO;
import com.lonelyash.user.domain.query.UserPageQuery;
import com.lonelyash.user.domain.vo.UserAdminVO;
import com.lonelyash.user.domain.vo.UserLoginVO;
import com.lonelyash.user.domain.vo.UserProfileVO;
import com.lonelyash.user.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "用户相关接口")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @ApiOperation("用户登录接口")
    @PostMapping("login")
    public UserLoginVO login(@RequestBody @Validated LoginFormDTO loginFormDTO){
        return userService.login(loginFormDTO);
    }

    @ApiOperation("用户注册")
    @PostMapping("register")
    public void register(@RequestBody @Validated RegisterFormDTO registerFormDTO) {
        userService.register(registerFormDTO);
    }

    @ApiOperation("查询当前登录用户")
    @GetMapping("/me")
    public UserProfileVO queryMyProfile() {
        return userService.queryMyProfile();
    }

    @ApiOperation("更新当前登录用户")
    @PutMapping("/me")
    public void updateMyProfile(@RequestBody UpdateUserProfileDTO dto) {
        userService.updateMyProfile(dto);
    }

    @ApiOperation("管理员分页查询用户")
    @GetMapping("/page")
    public PageDTO<UserAdminVO> queryUsers(UserPageQuery query) {
        return userService.queryUsers(query);
    }

    @ApiOperation("管理员更新用户状态")
    @PutMapping("/{id}/status/{status}")
    public void updateUserStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status) {
        userService.updateUserStatus(id, status);
    }

    @ApiOperation("扣减余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pw", value = "支付密码"),
            @ApiImplicitParam(name = "amount", value = "支付金额")
    })
    @PutMapping("/money/deduct")
    public void deductMoney(@RequestParam("pw") String pw,@RequestParam("amount") Integer amount){
        userService.deductMoney(pw, amount);
    }
}
