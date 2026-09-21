package org.example.productcatalogservice.services;

import org.example.productcatalogservice.models.Product;

import java.util.List;
import java.util.UUID;

public interface IProductService {

    /**
     * Retrieves a single product by its unique identifier.
     *
     * @param id the unique ID of the product
     * @return the matched Product domain model
     */
    Product getProductById(UUID id);

    /**
     * Retrieves all products currently available in the catalog.
     *
     * @return a List of all Product domain models
     */
    List<Product> getAllProducts();

    /**
     * Creates a new product in the catalog.
     *
     * @param product the domain model containing details of the product to create
     * @return the created Product domain model with its assigned identifier
     */
    Product createProduct(Product product);

    /**
     * Replaces an existing product's entire payload (PUT semantics).
     *
     * @param id the unique ID of the product to replace
     * @param product the new product state to overwrite the existing record
     * @return the updated Product domain model
     */
    Product replaceProduct(UUID id, Product product);

    /**
     * Partially updates specific fields of an existing product (PATCH semantics).
     *
     * @param id the unique ID of the product to update
     * @param product the domain model containing only the fields to be modified
     * @return the updated Product domain model
     */
    Product updateProduct(UUID id, Product product);

    /**
     * Deletes a product from the catalog by its unique identifier (DELETE semantics).
     *
     * @param id the unique ID of the product to remove
     * @return the deleted Product domain model (or confirmation representation)
     */
    Product deleteProduct(UUID id);


    List<String> getAllCategories();


    List<Product> getProductsByCategory(String categoryName);

}