package com.coffee.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.OrderStatus;
import com.coffee.dto.CreateOrderDTO;
import com.coffee.entity.Coffee;
import com.coffee.entity.Order;
import com.coffee.entity.OrderItem;
import com.coffee.entity.User;
import com.coffee.mapper.CoffeeMapper;
import com.coffee.mapper.OrderItemMapper;
import com.coffee.mapper.OrderMapper;
import com.coffee.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CoffeeMapper coffeeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CoffeeService coffeeService;

    public Page<Order> page(int pageNum, int pageSize, String status, Long customerId, Long baristaId,
                            String customerName, String coffeeName, String startDate, String endDate) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at");

        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        if (customerId != null) {
            wrapper.eq("customer_id", customerId);
        }
        if (baristaId != null) {
            wrapper.eq("barista_id", baristaId);
        }

        // 按顾客名称查询
        if (customerName != null && !customerName.isEmpty()) {
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.like("nickname", customerName).or().like("username", customerName);
            List<User> users = userMapper.selectList(userWrapper);
            if (users.isEmpty()) {
                // 没有匹配的用户，返回空结果
                return new Page<>(pageNum, pageSize);
            }
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            wrapper.in("customer_id", userIds);
        }

        // 按咖啡名称查询
        if (coffeeName != null && !coffeeName.isEmpty()) {
            QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
            itemWrapper.like("coffee_name", coffeeName);
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            if (items.isEmpty()) {
                // 没有匹配的订单项，返回空结果
                return new Page<>(pageNum, pageSize);
            }
            List<Long> orderIds = items.stream().map(OrderItem::getOrderId).distinct().collect(Collectors.toList());
            wrapper.in("id", orderIds);
        }

        // 按时间范围查询
        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            wrapper.ge("created_at", start);
        }
        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            wrapper.le("created_at", end);
        }

        Page<Order> result = orderMapper.selectPage(page, wrapper);

        // 填充订单项和用户名
        for (Order order : result.getRecords()) {
            fillOrderDetails(order);
        }

        return result;
    }

    public List<Order> listPending() {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("status", OrderStatus.PENDING.name()).orderByAsc("created_at");
        List<Order> orders = orderMapper.selectList(wrapper);
        for (Order order : orders) {
            fillOrderDetails(order);
        }
        return orders;
    }

    public Order getById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order != null) {
            fillOrderDetails(order);
        }
        return order;
    }

    private void fillOrderDetails(Order order) {
        // 填充订单项
        QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("order_id", order.getId());
        order.setItems(orderItemMapper.selectList(itemWrapper));

        // 填充顾客名
        User customer = userMapper.selectById(order.getCustomerId());
        if (customer != null) {
            order.setCustomerName(customer.getNickname());
        }

        // 填充咖啡师名
        if (order.getBaristaId() != null) {
            User barista = userMapper.selectById(order.getBaristaId());
            if (barista != null) {
                order.setBaristaName(barista.getNickname());
            }
        }
    }

    @Transactional
    public Order createOrder(Long customerId, CreateOrderDTO dto) {
        // 计算总价
        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderDTO.OrderItemDTO item : dto.getItems()) {
            Coffee coffee = coffeeMapper.selectById(item.getCoffeeId());
            if (coffee == null || coffee.getStatus() != 1) {
                throw new RuntimeException("咖啡不存在或已下架: " + item.getCoffeeId());
            }
            if (coffee.getStock() < item.getQuantity()) {
                throw new RuntimeException("库存不足: " + coffee.getName());
            }
            total = total.add(coffee.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        order.setCustomerId(customerId);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PENDING.name());
        order.setRemark(dto.getRemark());
        orderMapper.insert(order);

        // 创建订单项
        for (CreateOrderDTO.OrderItemDTO item : dto.getItems()) {
            Coffee coffee = coffeeMapper.selectById(item.getCoffeeId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setCoffeeId(item.getCoffeeId());
            orderItem.setCoffeeName(coffee.getName());
            orderItem.setPrice(coffee.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItemMapper.insert(orderItem);
        }

        return order;
    }

    @Transactional
    public boolean acceptOrder(Long orderId, Long baristaId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !OrderStatus.PENDING.name().equals(order.getStatus())) {
            return false;
        }
        order.setBaristaId(baristaId);
        order.setStatus(OrderStatus.MAKING.name());
        return orderMapper.updateById(order) > 0;
    }

    @Transactional
    public boolean completeOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !OrderStatus.MAKING.name().equals(order.getStatus())) {
            return false;
        }

        // 扣减库存
        QueryWrapper<OrderItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        for (OrderItem item : items) {
            if (!coffeeService.decreaseStock(item.getCoffeeId(), item.getQuantity())) {
                throw new RuntimeException("库存扣减失败");
            }
        }

        order.setStatus(OrderStatus.COMPLETED.name());
        return orderMapper.updateById(order) > 0;
    }

    public boolean cancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        if (OrderStatus.COMPLETED.name().equals(order.getStatus())) {
            return false;
        }
        order.setStatus(OrderStatus.CANCELLED.name());
        return orderMapper.updateById(order) > 0;
    }
}
