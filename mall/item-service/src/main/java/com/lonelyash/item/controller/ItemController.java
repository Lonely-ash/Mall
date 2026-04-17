package com.lonelyash.item.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lonelyash.common.domain.PageDTO;
import com.lonelyash.common.domain.PageQuery;
import com.lonelyash.common.exception.ForbiddenException;
import com.lonelyash.common.utils.BeanUtils;
import com.lonelyash.item.domain.dto.ItemDTO;
import com.lonelyash.item.domain.dto.ItemDetailDTO;
import com.lonelyash.item.domain.dto.OrderDetailDTO;
import com.lonelyash.item.domain.po.Item;
import com.lonelyash.item.domain.query.ItemPageQuery;
import com.lonelyash.item.service.IItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "商品管理相关接口")
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final Long ADMIN_USER_ID = 1L;

    private final IItemService itemService;

    @ApiOperation("商品详情")
    @GetMapping("/detail/{id}")
    public ItemDetailDTO getDetail(@PathVariable("id") long id) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ItemDetailDTO itemDetailDTO = itemService.queryItemDetailById(id);
        stopWatch.stop();
        log.debug("query item detail cost={}ms", stopWatch.getTotalTimeMillis());
        return itemDetailDTO;
    }

    @ApiOperation("买家分页查询上架商品")
    @GetMapping("/page")
    public PageDTO<ItemDTO> queryItemByPage(PageQuery query) {
        Page<Item> result = itemService.lambdaQuery()
                .eq(Item::getStatus, 1)
                .page(query.toMpPage("update_time", false));
        return PageDTO.of(result, ItemDTO.class);
    }

    @ApiOperation("管理员分页查询商品")
    @GetMapping("/page/admin")
    public PageDTO<ItemDTO> queryAdminItemByPage(ItemPageQuery query, @RequestHeader(value = "user-info", required = false) Long userId) {
        assertAdmin(userId);
        Page<Item> result = itemService.lambdaQuery()
                .like(StrUtil.isNotBlank(query.getKey()), Item::getName, query.getKey())
                .eq(StrUtil.isNotBlank(query.getBrand()), Item::getBrand, query.getBrand())
                .eq(StrUtil.isNotBlank(query.getCategory()), Item::getCategory, query.getCategory())
                .eq(query.getStatus() != null, Item::getStatus, query.getStatus())
                .between(query.getMaxPrice() != null, Item::getPrice, query.getMinPrice(), query.getMaxPrice())
                .page(query.toMpPage("update_time", false));
        return PageDTO.of(result, ItemDTO.class);
    }

    @ApiOperation("商品分类列表")
    @GetMapping("/categories")
    public List<String> queryCategories() {
        return itemService.lambdaQuery()
                .select(Item::getCategory)
                .list()
                .stream()
                .map(Item::getCategory)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @ApiOperation("根据id批量查询商品")
    @GetMapping
    public List<ItemDTO> queryItemByIds(@RequestParam("ids") List<Long> ids){
        return itemService.queryItemByIds(ids);
    }

    @ApiOperation("根据id查询商品")
    @GetMapping("{id}")
    public ItemDTO queryItemById(@PathVariable("id") Long id) {
        return BeanUtils.copyBean(itemService.getById(id), ItemDTO.class);
    }

    @ApiOperation("新增商品")
    @PostMapping
    public void saveItem(@RequestBody ItemDTO item) {
        itemService.save(BeanUtils.copyBean(item, Item.class));
    }

    @ApiOperation("更新商品状态")
    @PutMapping("/status/{id}/{status}")
    public void updateItemStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status){
        Item item = new Item();
        item.setId(id);
        item.setStatus(status);
        itemService.updateById(item);
    }

    @ApiOperation("更新商品")
    @PutMapping
    public void updateItem(@RequestBody ItemDTO item) {
        item.setStatus(null);
        itemService.updateById(BeanUtils.copyBean(item, Item.class));
    }

    @ApiOperation("根据id删除商品")
    @DeleteMapping("{id}")
    public void deleteItemById(@PathVariable("id") Long id) {
        itemService.removeById(id);
    }

    @ApiOperation("批量扣减库存")
    @PutMapping("/stock/deduct")
    public void deductStock(@RequestBody List<OrderDetailDTO> items){
        itemService.deductStock(items);
    }

    private void assertAdmin(Long userId) {
        if (!Objects.equals(ADMIN_USER_ID, userId)) {
            throw new ForbiddenException("无管理员权限");
        }
    }
}
