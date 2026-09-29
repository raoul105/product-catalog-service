package org.example.productcatalogservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.productcatalogservice.dtos.ProductRequestDto;
import org.example.productcatalogservice.mappers.ProductMapper;
import org.example.productcatalogservice.models.Product;
import org.example.productcatalogservice.services.IProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ProductController.class)
@Import(ProductMapper.class) // VIP Pass: Forces Spring to load this specific @Component
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IProductService productService;

    // Because of @Import, we can now safely ask Spring for the REAL mapper
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void test_getProductById_HappyPath() throws Exception {
        // 1. ARRANGE
        UUID dummyId = UUID.randomUUID();

        // Build the raw Domain Model exactly as the database would return it
        Product dummyProduct = new Product();
        dummyProduct.setId(dummyId);
        dummyProduct.setTitle("Web Layer Laptop");
        dummyProduct.setPrice(1200.00);

        // Program the hollow service shell to intercept the controller's request
        when(productService.getProductById(dummyId)).thenReturn(dummyProduct);

        // 2. ACT
        // Construct and fire a virtual HTTP GET request at the specific URL
        ResultActions response = mockMvc.perform(get("/products/" + dummyId));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dummyId.toString()))
                .andExpect(jsonPath("$.title").value("Web Layer Laptop"))
                .andExpect(jsonPath("$.price").value(1200.00));
    }


    @Test
    void test_getProductById_NotFoundPath() throws Exception {
        // 1. ARRANGE
        UUID dummyId = UUID.randomUUID();

        // Program the fake service to return null, perfectly simulating an empty database
        when(productService.getProductById(dummyId)).thenReturn(null);

        // 2. ACT
        // Fire the virtual HTTP request
        ResultActions response = mockMvc.perform(get("/products/" + dummyId));

        // 3. ASSERT
        // Verify the GlobalExceptionHandler intercepted the crash and returned a 404 status
        response.andExpect(status().isNotFound())
                // Verify the JSON payload is our custom ErrorResponseDto containing the exact error message
                .andExpect(jsonPath("$.errorMessage").value("Product with ID " + dummyId + " not found."));
    }


    @Test
    void test_createProduct_HappyPath() throws Exception {
        // 1. ARRANGE
        // A. Set up the incoming request payload (What the user sends)
        ProductRequestDto requestDto = new ProductRequestDto();
        requestDto.setTitle("New Web Laptop");
        requestDto.setPrice(1500.00);

        // B. Set up the expected output from the database
        UUID generatedId = UUID.randomUUID();
        Product savedProduct = new Product();
        savedProduct.setId(generatedId);
        savedProduct.setTitle("New Web Laptop");
        savedProduct.setPrice(1500.00);

        // C. Program the fake service
        // We use any(Product.class) because the real ProductMapper will create a brand-new Product object in memory
        when(productService.createProduct(any(Product.class))).thenReturn(savedProduct);

        // 2. ACT
        // Convert our Java DTO into a raw JSON text string
        String jsonPayload = objectMapper.writeValueAsString(requestDto);

        // Fire a POST request, attach the JSON text, and specify the content type
        ResultActions response = mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload));

        // 3. ASSERT
        // Verify the controller assigned the strict 201 CREATED status
        response.andExpect(status().isCreated())
                // Verify the returned JSON contains the newly generated database ID
                .andExpect(jsonPath("$.id").value(generatedId.toString()))
                .andExpect(jsonPath("$.title").value("New Web Laptop"))
                .andExpect(jsonPath("$.price").value(1500.00));
    }


    @Test
    void test_replaceProduct_HappyPath() throws Exception {
        // 1. ARRANGE
        UUID dummyId = UUID.randomUUID();

        // A. Set up the incoming payload (The new data to overwrite the old record)
        ProductRequestDto requestDto = new ProductRequestDto();
        requestDto.setTitle("Replaced Web Laptop");
        requestDto.setPrice(2000.00);

        // B. Set up the expected output from the database
        Product updatedProduct = new Product();
        updatedProduct.setId(dummyId);
        updatedProduct.setTitle("Replaced Web Laptop");
        updatedProduct.setPrice(2000.00);

        // C. Program the fake service using the Mockito Matcher Rule
        when(productService.replaceProduct(eq(dummyId), any(Product.class))).thenReturn(updatedProduct);

        // 2. ACT
        // Convert the Java DTO to a JSON string
        String jsonPayload = objectMapper.writeValueAsString(requestDto);

        // Fire a PUT request with the ID in the URL and the JSON in the body
        ResultActions response = mockMvc.perform(put("/products/" + dummyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload));

        // 3. ASSERT
        // Verify the controller assigned the standard 200 OK status
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dummyId.toString()))
                .andExpect(jsonPath("$.title").value("Replaced Web Laptop"))
                .andExpect(jsonPath("$.price").value(2000.00));
    }


    @Test
    void test_replaceProduct_NotFoundPath() throws Exception {
        // 1. ARRANGE
        UUID dummyId = UUID.randomUUID();

        // Set up a valid incoming payload
        ProductRequestDto requestDto = new ProductRequestDto();
        requestDto.setTitle("Non-existent Laptop");
        requestDto.setPrice(2000.00);

        // Program the fake service to return null, perfectly simulating an empty database response
        when(productService.replaceProduct(eq(dummyId), any(Product.class))).thenReturn(null);

        // 2. ACT
        String jsonPayload = objectMapper.writeValueAsString(requestDto);

        // Fire the PUT request
        ResultActions response = mockMvc.perform(put("/products/" + dummyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload));

        // 3. ASSERT
        // Verify the GlobalExceptionHandler intercepted the crash and returned a 404 status
        response.andExpect(status().isNotFound())
                // Verify the JSON payload contains the exact error message programmed in the controller
                .andExpect(jsonPath("$.errorMessage").value("Product with ID " + dummyId + " not found to update."));
    }


    @Test
    void test_deleteProduct_HappyPath() throws Exception {
        // 1. ARRANGE
        UUID dummyId = UUID.randomUUID();

        // A. Set up the domain model exactly as it would look just before being deleted from the database
        Product productToBeDeleted = new Product();
        productToBeDeleted.setId(dummyId);
        productToBeDeleted.setTitle("Deleted Web Laptop");
        productToBeDeleted.setPrice(9000.00);

        // B. Program the fake service to intercept the delete command and return the target object
        when(productService.deleteProduct(dummyId)).thenReturn(productToBeDeleted);

        // 2. ACT
        // Construct and fire a virtual HTTP DELETE request.
        // Note: We do not need the ObjectMapper or contentType() because DELETE requests do not carry JSON bodies.
        ResultActions response = mockMvc.perform(delete("/products/" + dummyId));

        // 3. ASSERT
        // Verify the routing succeeded and the controller assigned the 200 OK status
        response.andExpect(status().isOk())
                // Verify the ProductMapper successfully translated the domain model's ID to a text string
                .andExpect(jsonPath("$.id").value(dummyId.toString()))
                // Verify the mapper accurately transferred the scalar data to the final JSON payload
                .andExpect(jsonPath("$.title").value("Deleted Web Laptop"))
                .andExpect(jsonPath("$.price").value(9000.00));
    }


    @Test
    void test_deleteProduct_NotFoundPath() throws Exception {
        // 1. ARRANGE
        UUID dummyId = UUID.randomUUID();

        // Program the fake service to return null, simulating the ID was not found in the database
        when(productService.deleteProduct(dummyId)).thenReturn(null);

        // 2. ACT
        // Fire the virtual HTTP DELETE request
        ResultActions response = mockMvc.perform(delete("/products/" + dummyId));

        // 3. ASSERT
        // Verify the GlobalExceptionHandler intercepted the ProductNotFoundException and returned a 404 status
        response.andExpect(status().isNotFound())
                // Verify the JSON payload contains the exact custom error message programmed in the controller
                .andExpect(jsonPath("$.errorMessage").value("Product with ID " + dummyId + " not found to delete."));
    }


    @Test
    void test_updateProduct_HappyPath() throws Exception {
        // 1. ARRANGE
        UUID dummyId = UUID.randomUUID();

        // A. Set up a PARTIAL incoming payload using your DTO
        ProductRequestDto requestDto = new ProductRequestDto();
        // We ONLY set the price. The title will organically remain 'null' in this DTO.
        requestDto.setPrice(1800.00);

        // B. Set up the expected output from the database
        // The service should return the fully populated object with the new price applied
        Product updatedProduct = new Product();
        updatedProduct.setId(dummyId);
        updatedProduct.setTitle("Existing Web Laptop"); // The original title remains safely untouched
        updatedProduct.setPrice(1800.00); // The new price is applied

        // C. Program the fake service using your exact method signature
        when(productService.updateProduct(eq(dummyId), any(Product.class))).thenReturn(updatedProduct);

        // 2. ACT
        // Convert the Java DTO into a partial JSON string: {"title":null,"price":1800.0,"description":null...}
        String jsonPayload = objectMapper.writeValueAsString(requestDto);

        // Fire a PATCH request
        ResultActions response = mockMvc.perform(patch("/products/" + dummyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload));

        // 3. ASSERT
        // Verify the controller assigned the standard 200 OK status
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dummyId.toString()))
                .andExpect(jsonPath("$.title").value("Existing Web Laptop"))
                .andExpect(jsonPath("$.price").value(1800.00));
    }


    @Test
    void test_updateProduct_NotFoundPath() throws Exception {
        // 1. ARRANGE
        UUID dummyId = UUID.randomUUID();

        // Create a dummy payload to satisfy Spring's @RequestBody requirement
        ProductRequestDto requestDto = new ProductRequestDto();
        requestDto.setPrice(1800.00);

        // Program the fake service to updateProduct
        when(productService.updateProduct(eq(dummyId), any(Product.class))).thenReturn(null);

        // 2. ACT
        // Convert the payload to a JSON string
        String jsonPayload = objectMapper.writeValueAsString(requestDto);

        // Fire the PATCH request WITH the attached JSON body
        ResultActions response = mockMvc.perform(patch("/products/" + dummyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload));

        // 3. ASSERT
        // Verify the GlobalExceptionHandler successfully returned the 404 status and exact message
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value("Product with ID " + dummyId + " not found to update."));
    }

}