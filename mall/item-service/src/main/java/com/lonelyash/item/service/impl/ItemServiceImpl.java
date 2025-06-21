package com.lonelyash.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lonelyash.item.domain.dto.ItemDetailDTO;
import com.lonelyash.item.mapper.ItemMapper;
import com.lonelyash.item.service.IItemService;
import com.lonelyash.common.exception.BizIllegalException;
import com.lonelyash.common.utils.BeanUtils;
import com.lonelyash.item.domain.dto.ItemDTO;
import com.lonelyash.item.domain.dto.OrderDetailDTO;
import com.lonelyash.item.domain.po.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author 虎哥
 */
@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Override
    @Transactional
    public void deductStock(List<OrderDetailDTO> items) {
        String sqlStatement = "mapper.com.lonelyash.item.ItemMapper.updateStock";
        boolean r = false;
        try {
            r = executeBatch(items, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
        } catch (Exception e) {
            throw new BizIllegalException("更新库存异常，可能是库存不足!", e);
        }
        if (!r) {
            throw new BizIllegalException("库存不足！");
        }
    }

    @Override
    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }

    @Override
    public ItemDetailDTO queryItemDetailById(Long id) {
        return itemMapper.getItemDetail(id);
    }

}
