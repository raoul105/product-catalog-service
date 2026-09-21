package org.example.productcatalogservice.exceptions;

public class ProductNotFoundException extends RuntimeException{

    public ProductNotFoundException(String message) {
        super(message); // This hands the custom message up to RuntimeException
    }
}
