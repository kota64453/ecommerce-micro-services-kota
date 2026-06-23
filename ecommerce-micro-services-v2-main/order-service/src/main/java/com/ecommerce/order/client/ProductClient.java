package com.ecommerce.order.client;

import com.ecommerce.order.dto.ApiResponse;
import com.ecommerce.order.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", path = "/api/products")
public interface ProductClient {

    @GetMapping("/{id}")
    ApiResponse<ProductDto> getProduct(@PathVariable("id") String id);
}
