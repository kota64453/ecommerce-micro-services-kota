package com.ecommerce.product.repository;

import com.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findByProductCode(String productCode);

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategoryAndActiveTrue(String category, Pageable pageable);

    Page<Product> findByBrandAndActiveTrue(String brand, Pageable pageable);

    Page<Product> findByCategoryAndBrandAndActiveTrue(String category, String brand, Pageable pageable);

    @Query("{ 'active': true, 'price': { $gte: ?0, $lte: ?1 } }")
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @Query("{ 'active': true, 'category': ?0, 'price': { $gte: ?1, $lte: ?2 } }")
    Page<Product> findByCategoryAndPriceBetween(String category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @Query("{ 'active': true, 'brand': ?0, 'price': { $gte: ?1, $lte: ?2 } }")
    Page<Product> findByBrandAndPriceBetween(String brand, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @Query("{ 'active': true, $or: [ { 'name': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } }, { 'brand': { $regex: ?0, $options: 'i' } }, { 'category': { $regex: ?0, $options: 'i' } } ] }")
    Page<Product> searchProducts(String keyword, Pageable pageable);

    List<String> findDistinctCategoriesByActiveTrue();

    List<String> findDistinctBrandsByActiveTrue();

    List<String> findDistinctBrandsByCategoryAndActiveTrue(String category);
}
