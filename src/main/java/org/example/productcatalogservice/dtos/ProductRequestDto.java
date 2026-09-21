package org.example.productcatalogservice.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProductRequestDto {
    private String title;
    private String description;
    private String imageUrl;
    private Double price;

    private CategoryRequestDto category;
}
