package com.mall.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.item.domain.dto.ItemDetailDTO;
import com.mall.item.domain.dto.OrderDetailDTO;
import com.mall.item.domain.po.Item;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 商品表 Mapper 接口
 * </p>
 */
public interface ItemMapper extends BaseMapper<Item> {

    @Update("UPDATE item SET stock = stock - #{num} WHERE id = #{itemId}")
    void updateStock(OrderDetailDTO orderDetail);

//  @Select("select content, u.username, i.id,i.name,i.price from `hm-item`.comment as c left join `hm-item`.item i on c.id = i.comment_id left join `hm-user`.user u on c.user_id = u.id where i.id = #{itemId}")

    ItemDetailDTO getItemDetail(Long itemId);

}
