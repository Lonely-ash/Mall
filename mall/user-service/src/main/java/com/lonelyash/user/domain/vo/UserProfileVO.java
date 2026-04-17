package com.lonelyash.user.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "当前用户信息")
public class UserProfileVO {
    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("账户余额(分)")
    private Integer balance;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("角色: admin/buyer")
    private String role;
}
