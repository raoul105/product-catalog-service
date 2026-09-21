package org.example.productcatalogservice.repositories;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.example.productcatalogservice.models.Category;
import org.example.productcatalogservice.models.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    public void demonstrateLazyFetching() {
        // 1. SETUP: Create a category and a product, then save them to MySQL
        Category category = new Category();
        category.setTitle("Test Category");
        Category savedCategory = categoryRepository.save(category);

        Product product = new Product();
        product.setTitle("Test Product");
        product.setCategory(savedCategory);
        productRepository.save(product);

        // Push the inserts to MySQL
        categoryRepository.flush();

        // Clear the Hibernate memory so we are forced to fetch fresh from the MySQL database
        entityManager.clear();

        System.out.println("--- SETUP COMPLETE. FETCHING CATEGORY ---");

        // 2. THE EXPERIMENT: Fetch all categories using our custom JPQL query
        List<Category> allCategories = categoryRepository.findAllCategoriesWithProducts();
        Category fetchedCategory = allCategories.get(0);

        System.out.println("--- CATEGORY FETCHED. NOW FETCHING PRODUCTS ---");

        // 3. THE TRIGGER: Ask the category for its products
        int productCount = fetchedCategory.getProducts().size();
        System.out.println("Product Count: " + productCount);

        System.out.println("--- TEST COMPLETE ---");
    }
}