package org.example.productcatalogservice.controllers;

import org.example.productcatalogservice.dtos.ProductRequestDto;
import org.example.productcatalogservice.dtos.ProductResponseDto;
import org.example.productcatalogservice.exceptions.ProductNotFoundException;
import org.example.productcatalogservice.mappers.ProductMapper;
import org.example.productcatalogservice.models.Product;
import org.example.productcatalogservice.services.IProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final IProductService productService;
    private final ProductMapper productMapper;

    // Spring automatically injects the Service and Mapper here
    public ProductController(IProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable("id") UUID productId) {

        // 1. Fail-Fast Validation: Stop illogical inputs at the door
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null.");
        }

        // 2. Fetch the raw domain model from the Service
        Product product = productService.getProductById(productId);

        // 3. Validate: Did we find anything?
        if (product == null) {
            throw new ProductNotFoundException("Product with ID " + productId + " not found.");
        }

        // 4. Shape: Translate the domain model into a safe Output Boundary
        ProductResponseDto productResponseDto = productMapper.toProductResponseDto(product);

        // 5. Return: Send the safe DTO back to the user with a 200 OK
        return new ResponseEntity<>(productResponseDto, HttpStatus.OK);
    }


    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {

        // 1. Fetch the raw list from the Service
        List<Product> products = productService.getAllProducts();

        // 2. Create a new empty box for our safe DTOs
        List<ProductResponseDto> responseDtos = new ArrayList<>();

        // 3. The Assembly Line: Translate each product one by one
        for (Product product : products) {
            ProductResponseDto safeDto = productMapper.toProductResponseDto(product);
            responseDtos.add(safeDto);
        }

        // (Optional: We can also use Java Streams for this!)
        // List<ProductResponseDto> responseDtos = products.stream()
        //         .map(productMapper::toProductResponseDto)
        //         .toList();

        // 4. Return the box of safe DTOs with a 200 OK
        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }


    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductRequestDto productRequestDto) {

        // 1. Fail-Fast Validation: Ensure we actually received a payload
        if (productRequestDto == null) {
            throw new IllegalArgumentException("Product request body cannot be null.");
        }

        // 2. Shape (Input): Translate the strict Request DTO into your Domain Model
        Product product = productMapper.toProduct(productRequestDto);

        // 3. Process: Hand it to the Service layer (FakeStore will generate the ID here)
        Product createdProduct = productService.createProduct(product);

        // 4. Shape (Output): Translate the new Domain Model into a safe Response DTO
        ProductResponseDto productResponseDto = productMapper.toProductResponseDto(createdProduct);

        // 5. Return: Send back the safe DTO with a 201 CREATED status
        return new ResponseEntity<>(productResponseDto, HttpStatus.CREATED);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> replaceProduct(
            @PathVariable("id") UUID productId,
            @RequestBody ProductRequestDto requestDto) {

        // 1. Fail-Fast Validation
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null.");
        }
        if (requestDto == null) {
            throw new IllegalArgumentException("Product request body cannot be null.");
        }

        // 2. Shape (Input)
        Product productRequest = productMapper.toProduct(requestDto);

        // 3. Process: Catch the returned, updated product from the Service!
        Product updatedProduct = productService.replaceProduct(productId, productRequest);

        // 4. Validate: Did the service actually find and replace it?
        if (updatedProduct == null) {
            throw new ProductNotFoundException("Product with ID " + productId + " not found to update.");
        }

        // 5. Shape (Output): Map the updated product, not the input product
        ProductResponseDto responseDto = productMapper.toProductResponseDto(updatedProduct);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }


    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable("id") UUID productId,
            @RequestBody ProductRequestDto requestDto) {

        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null.");
        }
        if (requestDto == null) {
            throw new IllegalArgumentException("Product request body cannot be null.");
        }
        Product productRequest = productMapper.toProduct(requestDto);

        Product updatedProduct = productService.updateProduct(productId, productRequest);

        if (updatedProduct == null) {
            throw new ProductNotFoundException("Product with ID " + productId + " not found to update.");
        }

        ProductResponseDto responseDto = productMapper.toProductResponseDto(updatedProduct);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ProductResponseDto> deleteProduct(@PathVariable("id") UUID productId) {

        // 1. Fail-Fast Validation: Stop illogical inputs at the door
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null.");
        }

        // 2. Fetch the raw domain model from the Service
        Product product = productService.deleteProduct(productId);

        // 3. Validate: Did we find anything?
        if (product == null) {
            throw new ProductNotFoundException("Product with ID " + productId + " not found to delete.");
        }

        // 4. Shape: Translate the domain model into a safe Output Boundary
        ProductResponseDto productResponseDto = productMapper.toProductResponseDto(product);

        // 5. Return: Send the safe DTO back to the user with a 200 OK
        return new ResponseEntity<>(productResponseDto, HttpStatus.OK);
    }


    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {

        // 1. Call the service to get the list of strings
        List<String> categories = productService.getAllCategories();

        // 2. Return the list with a 200 OK status
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }


    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<ProductResponseDto>> getProductsByCategory(
            @PathVariable("categoryName") String categoryName) {

        // 1. Fail-fast validation
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }

        // 2. Fetch internal Product models from the Service
        List<Product> products = productService.getProductsByCategory(categoryName);

        // 3. Convert them to external Response DTOs using your Front Door mapper
        List<ProductResponseDto> responseDtos = new ArrayList<>();
        for (Product product : products) {
            responseDtos.add(productMapper.toProductResponseDto(product));
        }

        // 4. Return the list to the client
        return new ResponseEntity<>(responseDtos, HttpStatus.OK);
    }

}