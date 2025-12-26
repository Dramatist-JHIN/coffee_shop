package com.coffee.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.coffee.common.OrderStatus;
import com.coffee.common.Result;
import com.coffee.entity.Coffee;
import com.coffee.entity.Order;
import com.coffee.mapper.CoffeeMapper;
import com.coffee.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CoffeeMapper coffeeMapper;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> stats = new HashMap<>();

        // 订单统计
        long totalOrders = orderMapper.selectCount(null);
        long pendingOrders = orderMapper.selectCount(new QueryWrapper<Order>().eq("status", OrderStatus.PENDING.name()));
        long completedOrders = orderMapper.selectCount(new QueryWrapper<Order>().eq("status", OrderStatus.COMPLETED.name()));

        // 营收统计
        List<Order> completedList = orderMapper.selectList(new QueryWrapper<Order>().eq("status", OrderStatus.COMPLETED.name()));
        BigDecimal totalRevenue = completedList.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 咖啡统计
        long totalCoffees = coffeeMapper.selectCount(new QueryWrapper<Coffee>().eq("status", 1));
        List<Coffee> lowStockCoffees = coffeeMapper.selectList(new QueryWrapper<Coffee>().eq("status", 1).lt("stock", 10));

        stats.put("totalOrders", totalOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalCoffees", totalCoffees);
        stats.put("lowStockCoffees", lowStockCoffees);

        return Result.success(stats);
    }
}
