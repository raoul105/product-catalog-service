package org.example.productcatalogservice.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CategoryResponseDto {
    private UUID id;
    private String title;

    // Notice: We completely omit the List<Product> to prevent infinite JSON loops (Circular references)!
}
