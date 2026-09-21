// This file is commented out because it is created only for learning purpose. Ignore it.
/*
package org.example.productcatalogservice.config;
import jakarta.annotation.Nullable;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class CustomRestTemplate extends RestTemplate {
    public <T> ResponseEntity<T> putForEntity(String url, @Nullable Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
        RequestCallback requestCallback = httpEntityCallback(request, responseType);
        ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);

        return execute(url, HttpMethod.PUT, requestCallback, responseExtractor, uriVariables);
    }
}
*/