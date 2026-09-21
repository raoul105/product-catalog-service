package org.example.productcatalogservice.mappers;

import org.example.productcatalogservice.dtos.CategoryResponseDto;
import org.example.productcatalogservice.dtos.ProductRequestDto;
import org.example.productcatalogservice.dtos.ProductResponseDto;
import org.example.productcatalogservice.models.Category;
import org.example.productcatalogservice.models.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    // 1. Convert incoming Request DTO into our internal Product model
    public Product toProduct(ProductRequestDto productRequestDto) {
        if (productRequestDto == null) {
            return null;
        }

        Product product = new Product();
        product.setTitle(productRequestDto.getTitle());
        product.setDescription(productRequestDto.getDescription());
        product.setImageUrl(productRequestDto.getImageUrl());
        product.setPrice(productRequestDto.getPrice());

        if (productRequestDto.getCategory() != null && productRequestDto.getCategory().getTitle() != null) {
            Category category = new Category();
            category.setTitle(productRequestDto.getCategory().getTitle());
            product.setCategory(category);
        }

        return product;
    }


    public ProductResponseDto toProductResponseDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponseDto productResponseDto = new ProductResponseDto();
        productResponseDto.setId(product.getId());
        productResponseDto.setTitle(product.getTitle());
        productResponseDto.setDescription(product.getDescription());
        productResponseDto.setImageUrl(product.getImageUrl());
        productResponseDto.setPrice(product.getPrice());

        if (product.getCategory() != null) {
            CategoryResponseDto categoryResponseDto = new CategoryResponseDto();
            categoryResponseDto.setId(product.getCategory().getId());
            categoryResponseDto.setTitle(product.getCategory().getTitle());

            productResponseDto.setCategory(categoryResponseDto);
        }

        return productResponseDto;
    }
}
