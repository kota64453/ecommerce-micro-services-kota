package com.ecommerce.product.service;

import com.ecommerce.product.dto.ApiResponse;
import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.dto.ProductPageResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.exception.BusinessException;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ApiResponse<ProductDto> createProduct(ProductDto productDto) {
        Product product = productMapper.toProduct(productDto);
        product.setProductCode("P" + System.currentTimeMillis());
        product.setActive(true);
        product = productRepository.save(product);
        log.info("Product created: {} - {}", product.getId(), product.getName());
        return ApiResponse.success("Product created successfully", productMapper.toProductDto(product));
    }

    public ApiResponse<ProductDto> getProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return ApiResponse.success("Product retrieved successfully", productMapper.toProductDto(product));
    }

    public ApiResponse<ProductDto> getProductByCode(String productCode) {
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productCode", productCode));
        return ApiResponse.success("Product retrieved successfully", productMapper.toProductDto(product));
    }

    public ApiResponse<ProductDto> updateProduct(String id, ProductDto productDto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (productDto.getName() != null) existing.setName(productDto.getName());
        if (productDto.getDescription() != null) existing.setDescription(productDto.getDescription());
        if (productDto.getPrice() != null) existing.setPrice(productDto.getPrice());
        existing.setStock(productDto.getStock());
        if (productDto.getBrand() != null) existing.setBrand(productDto.getBrand());
        if (productDto.getCategory() != null) existing.setCategory(productDto.getCategory());
        if (productDto.getImages() != null) existing.setImages(productDto.getImages());
        if (productDto.getSpecifications() != null) existing.setSpecifications(productDto.getSpecifications());
        existing.setActive(productDto.isActive());

        existing = productRepository.save(existing);
        log.info("Product updated: {} - {}", existing.getId(), existing.getName());
        return ApiResponse.success("Product updated successfully", productMapper.toProductDto(existing));
    }

    public ApiResponse<Void> deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft-deleted: {}", id);
        return ApiResponse.success("Product deleted successfully");
    }

    public ApiResponse<ProductPageResponse> getProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findByActiveTrue(pageable);

        ProductPageResponse response = buildPageResponse(productPage);
        return ApiResponse.success("Products retrieved successfully", response);
    }

    public ApiResponse<ProductPageResponse> searchProducts(String keyword, int page, int size,
                                                             String sortBy, String sortDir) {
        String sortField = sortBy.equals("relevance") ? "createdAt" : sortBy;
        Sort sort = buildSort(sortField, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.searchProducts(keyword, pageable);

        ProductPageResponse response = buildPageResponse(productPage);
        return ApiResponse.success("Products retrieved successfully", response);
    }

    public ApiResponse<ProductPageResponse> getProductsByCategory(String category, int page, int size,
                                                                    String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findByCategoryAndActiveTrue(category, pageable);

        ProductPageResponse response = buildPageResponse(productPage);
        return ApiResponse.success("Products retrieved successfully", response);
    }

    public ApiResponse<ProductPageResponse> getProductsByBrand(String brand, int page, int size,
                                                                 String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findByBrandAndActiveTrue(brand, pageable);

        ProductPageResponse response = buildPageResponse(productPage);
        return ApiResponse.success("Products retrieved successfully", response);
    }

    public ApiResponse<ProductPageResponse> getProductsByCategoryAndBrand(String category, String brand,
                                                                            int page, int size,
                                                                            String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findByCategoryAndBrandAndActiveTrue(category, brand, pageable);

        ProductPageResponse response = buildPageResponse(productPage);
        return ApiResponse.success("Products retrieved successfully", response);
    }

    public ApiResponse<ProductPageResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice,
                                                                      int page, int size,
                                                                      String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findByPriceBetween(minPrice, maxPrice, pageable);

        ProductPageResponse response = buildPageResponse(productPage);
        return ApiResponse.success("Products retrieved successfully", response);
    }

    public ApiResponse<List<String>> getCategories() {
        List<String> categories = productRepository.findDistinctCategoriesByActiveTrue();
        return ApiResponse.success("Categories retrieved successfully", categories);
    }

    public ApiResponse<List<String>> getBrands() {
        List<String> brands = productRepository.findDistinctBrandsByActiveTrue();
        return ApiResponse.success("Brands retrieved successfully", brands);
    }

    public ApiResponse<List<String>> getBrandsByCategory(String category) {
        List<String> brands = productRepository.findDistinctBrandsByCategoryAndActiveTrue(category);
        return ApiResponse.success("Brands retrieved successfully", brands);
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String sortField = sortBy;
        if (sortBy == null || sortBy.isEmpty() || sortBy.equals("relevance")) {
            sortField = "createdAt";
        }
        return sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortField).descending() : Sort.by(sortField).ascending();
    }

    private ProductPageResponse buildPageResponse(Page<Product> productPage) {
        return ProductPageResponse.builder()
                .content(productMapper.toProductDtoList(productPage.getContent()))
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .first(productPage.isFirst())
                .empty(productPage.isEmpty())
                .build();
    }
}
