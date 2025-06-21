package com.lonelyash.mapper;

import com.github.pagehelper.Page;
import com.lonelyash.dto.GoodsSalesDTO;
import com.lonelyash.dto.OrdersPageQueryDTO;
import com.lonelyash.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单
     * @param orders
     */
    void insert(Orders orders);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    void update(Orders orders1);

    /**
     * 分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据状态统计订单数量
     * @param status
     * @return
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime time);

    @Select("select * from orders where user_id = #{userId}")
    List<Orders> getByUserId(Long userId);

    /**
     * 动态条件统计营业额
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 动态统计订单数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
