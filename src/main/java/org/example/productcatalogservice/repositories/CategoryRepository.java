package org.example.productcatalogservice.repositories;

import org.example.productcatalogservice.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByTitle(String title);

    @Query("SELECT c FROM Category c JOIN FETCH c.products")
    List<Category> findAllCategoriesWithProducts();
}
