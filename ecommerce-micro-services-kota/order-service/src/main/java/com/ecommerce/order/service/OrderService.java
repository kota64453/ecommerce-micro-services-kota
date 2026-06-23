package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.*;
import com.ecommerce.order.exception.BusinessException;
import com.ecommerce.order.exception.ResourceNotFoundException;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final ProductClient productClient;

    @Transactional
    public ApiResponse<OrderDto> createOrder(Long userId, CreateOrderRequest request) {
        log.info("Creating order for userId: {}", userId);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDto itemDto : request.getItems()) {
            // Validate product exists via Feign
            try {
                ApiResponse<ProductDto> productResponse = productClient.getProduct(itemDto.getProductId());
                ProductDto product = productResponse.getData();

                if (product == null) {
                    throw new BusinessException("Product not found: " + itemDto.getProductId());
                }

                BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
                totalAmount = totalAmount.add(subtotal);

                OrderItem orderItem = OrderItem.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .quantity(itemDto.getQuantity())
                        .price(product.getPrice())
                        .subtotal(subtotal)
                        .build();
                orderItems.add(orderItem);
            } catch (Exception e) {
                log.error("Error validating product {}: {}", itemDto.getProductId(), e.getMessage());
                throw new BusinessException("Error validating product: " + itemDto.getProductId());
            }
        }

        Order order = Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .paymentStatus(PaymentStatus.PENDING)
                .orderStatus(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .orderItems(orderItems)
                .build();

        // Set bidirectional relationship
        orderItems.forEach(item -> item.setOrder(order));

        Order savedOrder = orderRepository.save(order);
        log.info("Order created: {}", savedOrder.getOrderNumber());

        return ApiResponse.success("Order created successfully", orderMapper.toOrderDto(savedOrder));
    }

    @Transactional
    public ApiResponse<OrderDto> updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setOrderStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid order status: " + status);
        }

        order = orderRepository.save(order);
        return ApiResponse.success("Order status updated", orderMapper.toOrderDto(order));
    }

    @Transactional
    public ApiResponse<OrderDto> updatePaymentStatus(Long orderId, String paymentStatus, String paymentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        try {
            PaymentStatus newStatus = PaymentStatus.valueOf(paymentStatus.toUpperCase());
            order.setPaymentStatus(newStatus);
            if (paymentId != null) {
                order.setPaymentId(paymentId);
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid payment status: " + paymentStatus);
        }

        order = orderRepository.save(order);
        return ApiResponse.success("Payment status updated", orderMapper.toOrderDto(order));
    }

    public ApiResponse<OrderDto> getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return ApiResponse.success("Order retrieved successfully", orderMapper.toOrderDto(order));
    }

    public ApiResponse<OrderDto> getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return ApiResponse.success("Order retrieved successfully", orderMapper.toOrderDto(order));
    }

    public ApiResponse<List<OrderDto>> getUserOrders(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return ApiResponse.success("Orders retrieved successfully", orderMapper.toOrderDtoList(orders.getContent()));
    }

    public ApiResponse<List<OrderDto>> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderRepository.findAll(pageable);
        return ApiResponse.success("Orders retrieved successfully", orderMapper.toOrderDtoList(orders.getContent()));
    }
}
