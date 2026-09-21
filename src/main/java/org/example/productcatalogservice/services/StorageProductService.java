package org.example.productcatalogservice.services;

import org.example.productcatalogservice.models.Category;
import org.example.productcatalogservice.models.Product;
import org.example.productcatalogservice.repositories.CategoryRepository;
import org.example.productcatalogservice.repositories.ProductRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Primary
public class StorageProductService implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public StorageProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }


    @Override
    public Product getProductById(UUID id) {
        // 1. Call the inherited method from CrudRepository
        // We use Optional because the database might not find a product with that ID.
        Optional<Product> optionalProduct = productRepository.findById(id);

        // 2. Check if the database actually returned a product
        if (optionalProduct.isEmpty()) {
            // If the box is empty, it means the ID doesn't exist.
            // For now, we will just return null. (Later we will throw an exception).
            return null;
        }

        // 3. If the box has something in it, take it out and return it.
        return optionalProduct.get();
    }


    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    @Override
    public Product createProduct(Product product) {
        // 1. Extract the dummy category (Transient object) created by our Phase 1 ProductMapper
        Category dummyCategory = product.getCategory();

        // 2. Defensive Check: Ensure the client actually provided category title before proceeding
        if (dummyCategory != null && dummyCategory.getTitle() != null) {
            // 1. Check if a category with this title already exists in MySQL
            Optional<Category> existingCategoryOptional = categoryRepository.findByTitle(dummyCategory.getTitle());

            if (existingCategoryOptional.isPresent()) {
                // 2A. Category exists: Swap the untrusted dummy category (Transient object) for the real, UUID-backed database record (trusted Managed object)
                Category managedCategory = existingCategoryOptional.get();
                product.setCategory(managedCategory);
            } else {
                // 2B. Category does NOT exist: Create it safely, save it to generate a UUID, and attach it
                Category newCategory = new Category();
                newCategory.setTitle(dummyCategory.getTitle());
                Category savedCategory = categoryRepository.save(newCategory);
                product.setCategory(savedCategory);
            }
        }

        // 3. Save the product to the database, now securely linked to a valid Category record
        Product createdProduct = productRepository.save(product);
        return createdProduct;
    }


    @Override
    public Product replaceProduct(UUID id, Product product) {
        // 1. Check if the product actually exists in the database before trying to replace it.
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return null; // (We will upgrade this to a proper Error response later)
        }

        Product existingProduct = optionalProduct.get();

        // 2. The Total Overwrite for basic text and numbers.
        // Because this is a PUT request, the rule is to replace everything.
        existingProduct.setTitle(product.getTitle());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setImageUrl(product.getImageUrl());

        // 3. The Total Overwrite for the Category relationship.
        Category dummyCategory = product.getCategory();

        // Did the client include a categoryId in their JSON payload?
        if (dummyCategory != null && dummyCategory.getId() != null) {

            // Yes, they did. So we must verify it exists in MySQL before linking it, exactly like createProduct.
            Optional<Category> actualCategoryOptional = categoryRepository.findById(dummyCategory.getId());

            if (actualCategoryOptional.isEmpty()) {
                throw new IllegalArgumentException("Cannot update product: Category ID " + dummyCategory.getId() + " does not exist.");
            }

            // The ID is valid. Attach the real, verified database category to the product.
            Category managedCategory = actualCategoryOptional.get();
            existingProduct.setCategory(managedCategory);

        } else {
            // No. The client did NOT send a category ID.
            // Because the rule of a PUT request is a total wipe, we must disconnect this product from its current category.
            existingProduct.setCategory(null);
        }

        // 4. Save the fully overwritten product back to MySQL.
        return productRepository.save(existingProduct);
    }



    @Override
    public Product updateProduct(UUID id, Product product) {
        // 1. Fetch the existing product
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            return null;
        }

        Product existingProduct = optionalProduct.get();

        // 2. ONLY overwrite scalar fields if they are NOT null (PATCH Semantics)
        if (product.getTitle() != null) {
            existingProduct.setTitle(product.getTitle());
        }
        if (product.getPrice() != null) {
            existingProduct.setPrice(product.getPrice());
        }
        if (product.getDescription() != null) {
            existingProduct.setDescription(product.getDescription());
        }
        if (product.getImageUrl() != null) {
            existingProduct.setImageUrl(product.getImageUrl());
        }

        // 3. Handle Category updates ONLY if a category was sent
        Category dummyCategory = product.getCategory();
        if (dummyCategory != null && dummyCategory.getId() != null) {
            Optional<Category> actualCategoryOptional = categoryRepository.findById(dummyCategory.getId());

            if (actualCategoryOptional.isEmpty()) {
                throw new IllegalArgumentException("Cannot update product: Category ID " + dummyCategory.getId() + " does not exist.");
            }
            // The ID is valid. Attach the real, verified database category to the product.
            Category managedCategory = actualCategoryOptional.get();
            existingProduct.setCategory(managedCategory);
        }
        // Notice we do NOT set the category to null in the 'else' block for PATCH (as opposed to PUT).

        // 4. Save the partially merged product
        return productRepository.save(existingProduct);
    }


    @Override
    public Product deleteProduct(UUID id) {
        // 1. Find the product to make sure it exists
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (optionalProduct.isEmpty()) {
            return null; // Product doesn't exist
        }

        Product productToDelete = optionalProduct.get();

        // 2. Tell the database to delete this specific ID
        productRepository.deleteById(id);

        // 3. Return the data we just deleted as a confirmation to the client.
        return productToDelete;
    }


    @Override
    public List<String> getAllCategories() {
        // 1. Fetch categories and products in a single network trip using your JPQL query
        List<Category> categories = categoryRepository.findAllCategoriesWithProducts();

        // 2. Extract just the titles
        List<String> categoryTitles = new ArrayList<>();
        for (Category category : categories) {
            String title = category.getTitle();
            categoryTitles.add(title);
        }

        return categoryTitles;
    }


    @Override
    public List<Product> getProductsByCategory(String categoryName) {
        // Use the derived query method we just created in the repository
        return productRepository.findByCategoryTitle(categoryName);
    }
}
