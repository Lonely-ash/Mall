package com.lonelyash.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lonelyash.item.domain.dto.ItemDetailDTO;
import com.lonelyash.item.domain.dto.ItemDTO;
import com.lonelyash.item.domain.dto.OrderDetailDTO;
import com.lonelyash.item.domain.po.Item;

import java.util.Collection;
import java.util.List;


public interface IItemService extends IService<Item> {

    void deductStock(List<OrderDetailDTO> items);

    List<ItemDTO> queryItemByIds(Collection<Long> ids);

    ItemDetailDTO queryItemDetailById(Long id);
}
