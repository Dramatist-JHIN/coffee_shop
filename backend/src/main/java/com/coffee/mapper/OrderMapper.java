package com.coffee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffee.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT DATE(created_at) as date, SUM(total_amount) as amount, COUNT(*) as count " +
            "FROM `order` WHERE status = 'COMPLETED' AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> getSalesStats();

    @Select("SELECT oi.coffee_name, SUM(oi.quantity) as total_quantity " +
            "FROM order_item oi JOIN `order` o ON oi.order_id = o.id " +
            "WHERE o.status = 'COMPLETED' " +
            "GROUP BY oi.coffee_id, oi.coffee_name ORDER BY total_quantity DESC LIMIT 5")
    List<Map<String, Object>> getPopularCoffees();
}
