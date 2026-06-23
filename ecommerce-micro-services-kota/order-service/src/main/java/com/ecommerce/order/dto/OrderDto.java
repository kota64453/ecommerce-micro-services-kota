package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private Long id;
    private String orderNumber;
    private Long userId;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String orderStatus;
    private String shippingAddress;
    private String billingAddress;
    private String paymentId;
    private LocalDateTime createdAt;
    private List<OrderItemDto> orderItems;
}
