package com.ecommerce.order.mapper;

import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.dto.OrderItemDto;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "paymentStatus", expression = "java(order.getPaymentStatus().name())")
    @Mapping(target = "orderStatus", expression = "java(order.getOrderStatus().name())")
    OrderDto toOrderDto(Order order);

    List<OrderDto> toOrderDtoList(List<Order> orders);

    OrderItemDto toOrderItemDto(OrderItem orderItem);
    List<OrderItemDto> toOrderItemDtoList(List<OrderItem> orderItems);
}
