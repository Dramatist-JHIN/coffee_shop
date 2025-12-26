package com.coffee.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.Result;
import com.coffee.dto.CreateOrderDTO;
import com.coffee.entity.Order;
import com.coffee.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/page")
    public Result<Page<Order>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long baristaId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String coffeeName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return Result.success(orderService.page(pageNum, pageSize, status, customerId, baristaId, customerName, coffeeName, startDate, endDate));
    }

    @GetMapping("/pending")
    public Result<List<Order>> listPending() {
        return Result.success(orderService.listPending());
    }

    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @PostMapping
    public Result<Order> createOrder(HttpServletRequest request, @RequestBody CreateOrderDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        try {
            Order order = orderService.createOrder(userId, dto);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/accept")
    public Result<Void> acceptOrder(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        if (orderService.acceptOrder(id, userId)) {
            return Result.success(null);
        }
        return Result.error("接单失败");
    }

    @PostMapping("/{id}/complete")
    public Result<Void> completeOrder(@PathVariable Long id) {
        try {
            if (orderService.completeOrder(id)) {
                return Result.success(null);
            }
            return Result.error("完成订单失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        if (orderService.cancelOrder(id)) {
            return Result.success(null);
        }
        return Result.error("取消订单失败");
    }
}
