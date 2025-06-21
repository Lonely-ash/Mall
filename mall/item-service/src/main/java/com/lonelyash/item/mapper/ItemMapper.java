package com.lonelyash.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lonelyash.item.domain.dto.ItemDetailDTO;
import com.lonelyash.item.domain.dto.OrderDetailDTO;
import com.lonelyash.item.domain.po.Item;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 商品表 Mapper 接口
 * </p>
 */
public interface ItemMapper extends BaseMapper<Item> {

    @Update("UPDATE item SET stock = stock - #{num} WHERE id = #{itemId}")
    void updateStock(OrderDetailDTO orderDetail);

//  @Select("select content, u.username, i.id,i.name,i.price from `mall-item`.comment as c left join `mall-item`.item i on c.id = i.comment_id left join `mall-user`.user u on c.user_id = u.id where i.id = #{itemId}")

    ItemDetailDTO getItemDetail(Long itemId);

}
