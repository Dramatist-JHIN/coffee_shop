package com.coffee.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderDTO {
    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemDTO> items;

    private String remark;

    @Data
    public static class OrderItemDTO {
        private Long coffeeId;
        private Integer quantity;
    }
}
