package com.ecommerce.product.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document(collection = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    private String id;

    @Indexed(unique = true)
    private String productCode;

    @Indexed
    private String name;

    private String description;

    private BigDecimal price;

    private int stock;

    @Indexed
    private String brand;

    @Indexed
    private String category;

    @Builder.Default
    private List<String> images = new ArrayList<>();

    private double rating;

    private int reviewCount;

    private Map<String, String> specifications;

    private boolean active;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ProductCode and active are set manually or via service layer
    // @CreatedDate and @LastModifiedDate handled by MongoAuditing
}
