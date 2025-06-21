package com.mall.api.client.fallback;

import com.mall.api.client.ItemClient;
import com.mall.api.dto.ItemDTO;
import com.mall.api.dto.OrderDetailDTO;
import com.mall.common.exception.BizIllegalException;
import com.mall.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

@Slf4j
public class ItemClientFallback implements FallbackFactory {//feign服务降级
    @Override
    public Object create(Throwable cause) {
        return new ItemClient(){

            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("远程调用ItemClient#queryItemByIds方法出现异常，参数：{}", ids, cause);
                return CollUtils.emptyList();
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                throw new BizIllegalException(cause);
            }
        };
    }
}
