package org.example.productcatalogservice.services;

import org.example.productcatalogservice.models.Category;
import org.example.productcatalogservice.models.Product;
import org.example.productcatalogservice.repositories.CategoryRepository;
import org.example.productcatalogservice.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class StorageProductServiceTest {

    // 1. Generate the empty shells to block live database calls
    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    // 2. Build the real service and inject the empty shells into it
    @InjectMocks
    private StorageProductService storageProductService;

    @Test
    void test_getProductById_HappyPath() {
        // 1. ARRANGE: Set up the dummy data and program the fake database
        UUID dummyId = UUID.randomUUID();

        Product dummyProduct = new Product();
        dummyProduct.setId(dummyId);
        dummyProduct.setTitle("Mocked Laptop");

        // Program the hollow @Mock repository to return our dummy data
        when(productRepository.findById(dummyId)).thenReturn(Optional.of(dummyProduct));

        // 2. ACT: Execute the real service method
        Product actualProduct = storageProductService.getProductById(dummyId);

        // 3. ASSERT: Verify the service correctly processed and returned the data
        assertNotNull(actualProduct);
        assertEquals(dummyId, actualProduct.getId());
        assertEquals("Mocked Laptop", actualProduct.getTitle());
    }


    @Test
    void test_getProductById_NotFoundPath() {
        // 1. ARRANGE: Create a valid ID but program the fake database to simulate missing data
        UUID dummyId = UUID.randomUUID();

        // We tell the fake repository to return an empty Optional box for this ID
        when(productRepository.findById(dummyId)).thenReturn(Optional.empty());

        // 2. ACT: Execute the real service method
        Product actualProduct = storageProductService.getProductById(dummyId);

        // 3. ASSERT: Verify the service correctly handled the empty box by returning null
        assertNull(actualProduct);
    }


    @Test
    void test_deleteProduct_HappyPath() {
        // 1. ARRANGE
        UUID dummyId = UUID.randomUUID();
        Product dummyProduct = new Product();
        dummyProduct.setId(dummyId);

        // Program the fake to find the product so the service doesn't stop at the 'isEmpty()' check
        when(productRepository.findById(dummyId)).thenReturn(Optional.of(dummyProduct));

        // 2. ACT
        Product deletedProduct = storageProductService.deleteProduct(dummyId);

        // 3. ASSERT
        // Verify the service handed us back the correct deleted object
        assertNotNull(deletedProduct);

        // VERIFY BEHAVIOR: Ask the fake repository if the deleteById method was actually fired exactly one time
        verify(productRepository, times(1)).deleteById(dummyId);
    }


    @Test
    void test_createProduct_WithNewCategory() {
        // 1. ARRANGE
        // A. Set up the incoming request data (What the user sends)
        Category requestCategory = new Category();
        requestCategory.setTitle("Electronics");

        Product requestProduct = new Product();
        requestProduct.setTitle("Smartphone");
        requestProduct.setCategory(requestCategory);

        // B. Set up the expected database return data (What the database generates)
        UUID generatedCategoryId = UUID.randomUUID();
        Category savedCategory = new Category();
        savedCategory.setId(generatedCategoryId);
        savedCategory.setTitle("Electronics");

        UUID generatedProductId = UUID.randomUUID();
        Product savedProduct = new Product();
        savedProduct.setId(generatedProductId);
        savedProduct.setTitle("Smartphone");
        savedProduct.setCategory(savedCategory);

        // C. Program the fakes
        // Rule 1: When asked to find "Electronics", return empty (forcing the 'else' block)
        when(categoryRepository.findByTitle("Electronics")).thenReturn(Optional.empty());

        // Rule 2: When asked to save ANY new category, return our simulated database category (with an ID)
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // Rule 3: When asked to save ANY new product, return our simulated database product (with an ID)
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // 2. ACT
        Product actualProduct = storageProductService.createProduct(requestProduct);

        // 3. ASSERT
        // Verify the final output contains the newly generated database IDs
        assertNotNull(actualProduct);
        assertEquals(generatedProductId, actualProduct.getId());
        assertEquals(generatedCategoryId, actualProduct.getCategory().getId());

        // VERIFY BEHAVIOR
        // Audit the fakes to ensure all three database interactions occurred exactly once
        verify(categoryRepository, times(1)).findByTitle("Electronics");
        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(productRepository, times(1)).save(any(Product.class));
    }

}