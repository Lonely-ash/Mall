package com.lonelyash.cart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lonelyash.cart.service.ICartService;
import com.lonelyash.api.client.ItemClient;
import com.lonelyash.api.dto.ItemDTO;
import com.lonelyash.cart.config.CartProperties;
import com.lonelyash.cart.domain.dto.CartFormDTO;
import com.lonelyash.cart.domain.po.Cart;
import com.lonelyash.cart.domain.vo.CartVO;
import com.lonelyash.cart.mapper.CartMapper;
import com.lonelyash.common.exception.BizIllegalException;
import com.lonelyash.common.utils.BeanUtils;
import com.lonelyash.common.utils.CollUtils;
import com.lonelyash.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 璁㈠崟璇︽儏琛?鏈嶅姟瀹炵幇绫?
 * </p>
 *
 * @author 铏庡摜
 * @since 2023-05-05
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {


//    private final DiscoveryClient discoveryClient;
    private final ItemClient itemClient;
    private final CartProperties cartProperties;

    @Override
    public void addItem2Cart(CartFormDTO cartFormDTO) {
        // 1.鑾峰彇鐧诲綍鐢ㄦ埛
        Long userId = UserContext.getUser();

        // 2.鍒ゆ柇鏄惁宸茬粡瀛樺湪
        if(checkItemExists(cartFormDTO.getItemId(), userId)){
            // 2.1.瀛樺湪锛屽垯鏇存柊鏁伴噺
            baseMapper.updateNum(cartFormDTO.getItemId(), userId);
            return;
        }
        // 2.2.涓嶅瓨鍦紝鍒ゆ柇鏄惁瓒呰繃璐墿杞︽暟閲?
        checkCartsFull(userId);

        // 3.鏂板璐墿杞︽潯鐩?
        // 3.1.杞崲PO
        Cart cart = BeanUtils.copyBean(cartFormDTO, Cart.class);
        // 3.2.淇濆瓨褰撳墠鐢ㄦ埛
        cart.setUserId(userId);
        // 3.3.淇濆瓨鍒版暟鎹簱
        save(cart);
    }

    @Override
    public List<CartVO> queryMyCarts() {
        // 1.鏌ヨ鎴戠殑璐墿杞﹀垪琛?
        List<Cart> carts = lambdaQuery().eq(Cart::getUserId, UserContext.getUser()).list();
        if (CollUtils.isEmpty(carts)) {
            return CollUtils.emptyList();
        }

        // 2.杞崲VO
        List<CartVO> vos = BeanUtils.copyList(carts, CartVO.class);

        // 3.澶勭悊VO涓殑鍟嗗搧淇℃伅
        handleCartItems(vos);

        // 4.杩斿洖
        return vos;
    }

    private void handleCartItems(List<CartVO> vos) {
        // TODO 1.鑾峰彇鍟嗗搧id
        Set<Long> itemIds = vos.stream().map(CartVO::getItemId).collect(Collectors.toSet());
        // 2.鏌ヨ鍟嗗搧
//        List<ServiceInstance> instances = discoveryClient.getInstances("item-service");
//
//        ServiceInstance instance = instances.get(RandomUtil.randomInt(instances.size()));
//
//        ResponseEntity<List<ItemDTO>> response = restTemplate.exchange(
//                instance.getUri() + "/items?ids={ids}",
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<List<ItemDTO>>() {},
//                CollUtil.join(itemIds, ",")
//        );
//
//        List<ItemDTO> items = null;
//        if(response.getStatusCode().is2xxSuccessful()){items = response.getBody();}
//        if(CollUtils.isEmpty(items)){
//            throw new BadRequestException("璐墿杞︿腑鍟嗗搧涓嶅瓨鍦?);
//        }
        List<ItemDTO> items = itemClient.queryItemByIds(itemIds);
        // 3.杞负 id 鍒?item鐨刴ap
        Map<Long, ItemDTO> itemMap = items.stream().collect(Collectors.toMap(ItemDTO::getId, Function.identity()));
        // 4.鍐欏叆vo
        for (CartVO v : vos) {
            ItemDTO item = itemMap.get(v.getItemId());
            if (item == null) {
                continue;
            }
            v.setNewPrice(item.getPrice());
            v.setStatus(item.getStatus());
            v.setStock(item.getStock());
        }
    }

    @Override
    @Transactional
    public void removeByItemIds(Collection<Long> itemIds) {
        // 1.鏋勫缓鍒犻櫎鏉′欢锛寀serId鍜宨temId
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<Cart>();
        queryWrapper.lambda()
                .eq(Cart::getUserId, UserContext.getUser())
                .in(Cart::getItemId, itemIds);
        // 2.鍒犻櫎
        remove(queryWrapper);
    }

    private void checkCartsFull(Long userId) {
        int count = Math.toIntExact(lambdaQuery().eq(Cart::getUserId, userId).count());
        int maxAmount = cartProperties.getMaxAmount() == null ? 100 : cartProperties.getMaxAmount();
        if (count >= maxAmount) {
            throw new BizIllegalException(StrUtil.format("用户购物车条目不能超过{}", maxAmount));
        }
    }

    private boolean checkItemExists(Long itemId, Long userId) {
        int count = Math.toIntExact(lambdaQuery()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getItemId, itemId)
                .count());
        return count > 0;
    }
}

