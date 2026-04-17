package com.lonelyash.trade.domain.query;

import com.lonelyash.common.domain.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "订单分页查询条件")
public class OrderPageQuery extends PageQuery {
    @ApiModelProperty("订单状态")
    private Integer status;

    @ApiModelProperty("用户ID(管理员查询使用)")
    private Long userId;
}
