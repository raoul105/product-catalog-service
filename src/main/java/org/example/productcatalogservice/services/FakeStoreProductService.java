package org.example.productcatalogservice.services;

import org.example.productcatalogservice.dtos.FakeStoreProductDto;
import org.example.productcatalogservice.models.Category;
import org.example.productcatalogservice.models.Product;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FakeStoreProductService implements IProductService {
    private final RestTemplate restTemplate;

    public FakeStoreProductService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public Product getProductById(UUID id) {

        // 1. The Network Call: Using the cleaner {id} placeholder syntax
        ResponseEntity<FakeStoreProductDto> responseEntity = restTemplate.getForEntity(
                "https://fakestoreapi.com/products/{id}",
                FakeStoreProductDto.class,
                id
        );

        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            System.out.println("NETWORK ERROR: Server returned status " + responseEntity.getStatusCode());
            return null; // Soon to be: throw new RuntimeException("Server Error")
        }

        if (!responseEntity.hasBody()) {
            System.out.println("DATA WARNING: Server succeeded, but no product data exists for ID " + id);
            return null; // Soon to be: throw new ProductNotFoundException()
        }

        // 2. The Translation: Convert the DTO into our actual Product model
        return convertDtoToProduct(responseEntity.getBody());
    }


    @Override
    public List<Product> getAllProducts() {
        // 1. The Network Call
        ResponseEntity<FakeStoreProductDto[]> responseEntity = restTemplate.getForEntity(
                "https://fakestoreapi.com/products",
                FakeStoreProductDto[].class
        );

        // 2. DEFENSIVE CHECK: Did the server respond with a success code?
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            return new ArrayList<>();
        }

        // 3. DEFENSIVE CHECK: Is the payload empty?
        if (!responseEntity.hasBody()) {
            return new ArrayList<>();
        }

        // 4. Safe Extraction and Translation
        List<Product> products = new ArrayList<>();
        for (FakeStoreProductDto dto : responseEntity.getBody()) {
            products.add(convertDtoToProduct(dto));
        }

        return products;
    }


    @Override
    public Product createProduct(Product product) {
        // 1. Reverse Translation: Convert our Domain Model to the API's expected format
        FakeStoreProductDto requestDto = convertProductToDto(product);

        // 2. The Network Call: Send the payload and capture the sealed response package
        ResponseEntity<FakeStoreProductDto> responseEntity = restTemplate.postForEntity(
                "https://fakestoreapi.com/products",
                requestDto,
                FakeStoreProductDto.class
        );

        // 3. DEFENSIVE CHECK: Did the server accept our creation request?
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            // A production app might log: log.error("Creation failed. Status: {}", responseEntity.getStatusCode());
            return null;
        }

        // 4. DEFENSIVE CHECK: Did the server hand us back the finalized data?
        if (!responseEntity.hasBody()) {
            return null;
        }

        // 5. Safe Extraction and Translation
        return convertDtoToProduct(responseEntity.getBody());
    }


    @Override
    public Product replaceProduct(UUID id, Product product) {
        // 1. Translate
        FakeStoreProductDto requestDto = convertProductToDto(product);

        // 2. Pack the Envelope
        HttpEntity<FakeStoreProductDto> requestEntity = new HttpEntity<>(requestDto);

        // 3. Execute the Master Method
        ResponseEntity<FakeStoreProductDto> responseEntity = restTemplate.exchange(
                "https://fakestoreapi.com/products/{id}",
                HttpMethod.PUT,
                requestEntity,
                FakeStoreProductDto.class,
                id
        );

        // 4. Defensive Checks
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            return null;
        }

        if (!responseEntity.hasBody()) {
            return null;
        }

        // 5. Extract and Translate
        return convertDtoToProduct(responseEntity.getBody());
    }


    @Override
    public Product updateProduct(UUID id, Product product) {
        // 1. Translate the partial data to DTO
        FakeStoreProductDto requestDto = convertProductToDto(product);

        // 2. Pack the Envelope
        HttpEntity<FakeStoreProductDto> requestEntity = new HttpEntity<>(requestDto);

        // 3. The Network Call using PATCH
        ResponseEntity<FakeStoreProductDto> responseEntity = restTemplate.exchange(
                "https://fakestoreapi.com/products/{id}",
                HttpMethod.PATCH,
                requestEntity,
                FakeStoreProductDto.class,
                id
        );

        // 4. Defensive Checks
        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful()) {
            return null;
        }

        if (!responseEntity.hasBody()) {
            return null;
        }

        // 5. Extract and Translate
        return convertDtoToProduct(responseEntity.getBody());
    }


    @Override
    public Product deleteProduct(UUID id) {
        // 1. The Network Call (Notice we pass 'null' for the request payload)
        ResponseEntity<FakeStoreProductDto> responseEntity = restTemplate.exchange(
                "https://fakestoreapi.com/products/{id}",
                HttpMethod.DELETE,
                null,
                FakeStoreProductDto.class,
                id
        );

        // 2. Defensive Checks
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            return null;
        }

        if (!responseEntity.hasBody()) {
            return null;
        }

        // 3. Extract and Translate
        return convertDtoToProduct(responseEntity.getBody());
    }



    @Override
    public List<String> getAllCategories() {
        String url = "https://fakestoreapi.com/products/categories";

        // 1. Fetch the full entity (Status + Headers + Body)
        ResponseEntity<String[]> responseEntity = restTemplate.getForEntity(url, String[].class);

        // 2. Extract just the body from the response
        String[] categoryArray = responseEntity.getBody();

        // 3. Safety check
        if (categoryArray == null) {
            return new ArrayList<>();
        }

        // 4. Convert and return
        return Arrays.asList(categoryArray);
    }



    @Override
    public List<Product> getProductsByCategory(String categoryName) {
        String url = "https://fakestoreapi.com/products/category/{categoryName}";

        ResponseEntity<FakeStoreProductDto[]> responseEntity = restTemplate.getForEntity(
                url,
                FakeStoreProductDto[].class,
                categoryName
        );

        FakeStoreProductDto[] fakeStoreDtos = responseEntity.getBody();

        if (fakeStoreDtos == null) {
            return new ArrayList<>();
        }

        List<Product> products = new ArrayList<>();

        for (FakeStoreProductDto dto : fakeStoreDtos) {
            // Using your existing helper method!
            products.add(convertDtoToProduct(dto));
        }

        return products;
    }











    // --- PRIVATE HELPER METHOD ---
    private Product convertDtoToProduct(FakeStoreProductDto dto) {
        Product product = new Product();
//        product.setId(dto.getId());
        product.setTitle(dto.getTitle());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImage()); // Mapping "image" to "imageUrl"

        // Handling the nested Category object
        Category category = new Category();
        category.setTitle(dto.getCategory());
        product.setCategory(category);

        return product;
    }

    // --- PRIVATE HELPER METHOD FOR SENDING DATA OUT ---
    private FakeStoreProductDto convertProductToDto(Product product) {
        if (product == null) {
            return null;
        }

        FakeStoreProductDto dto = new FakeStoreProductDto();
//        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setImage(product.getImageUrl());

        // Flattening the nested Category object back into a simple String
        if (product.getCategory() != null) {
            dto.setCategory(product.getCategory().getTitle());
        }

        return dto;
    }

}
