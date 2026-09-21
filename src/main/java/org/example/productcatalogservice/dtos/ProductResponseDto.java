package org.example.productcatalogservice.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProductResponseDto {
    private UUID id;
    private String title;
    private String description;
    private String imageUrl;
    private Double price;

    // We attach our safe, flattened Category response
    private CategoryResponseDto category;
}
