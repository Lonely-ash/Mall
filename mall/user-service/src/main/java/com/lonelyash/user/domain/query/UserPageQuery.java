package com.lonelyash.user.domain.query;

import com.lonelyash.common.domain.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "用户分页查询条件")
public class UserPageQuery extends PageQuery {
    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("状态: 1正常 0冻结")
    private Integer status;
}
