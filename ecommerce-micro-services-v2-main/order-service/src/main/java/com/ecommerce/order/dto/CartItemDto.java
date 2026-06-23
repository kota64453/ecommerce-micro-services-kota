package com.ecommerce.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {

    @NotBlank(message = "Product ID is required")
    private String productId;

    private String productName;

    private String productImage;

    private BigDecimal price;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    private BigDecimal subtotal;
}
