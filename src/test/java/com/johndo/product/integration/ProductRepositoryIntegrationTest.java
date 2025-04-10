package com.johndo.product.integration;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.johndo.product.model.Category;
import com.johndo.product.model.Product;
import com.johndo.product.repository.CategoryRepository;
import com.johndo.product.repository.ProductRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@Testcontainers
public class ProductRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15.2")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @Transactional
    void testSaveAndRetrieveProduct() {
        // Save a category
        Category category = categoryRepository.save(Category.builder().name("Test Category").build());

        // Save a product
        Product product = productRepository.save(Product.builder()
                .name("Test Product")
                .description("Test Description")
                .unitPrice(BigDecimal.valueOf(100.00))
                .quantity(100)
                .category(category)
                .build());

        // Retrieve the product
        Product foundProduct = productRepository.findById(product.getId()).orElse(null);
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getName()).isEqualTo("Test Product");
        assertThat(foundProduct.getCategory().getName()).isEqualTo("Test Category");
    }

    @Test
    void testDeleteProduct() {
        // Save a category
        Category category = categoryRepository.save(Category.builder().name("Test Category").build());

        // Save a product
        Product product = productRepository.save(Product.builder()
                .name("Test Product")
                .description("Test Description")
                .unitPrice(BigDecimal.valueOf(100.00))
                .quantity(100)
                .category(category)
                .build());

        productRepository.deleteById(product.getId());

    }

    @Test
    void testSaveAndRetriveProductList() {
        // Save a category
        Category category = categoryRepository.save(Category.builder().name("Test Category").build());

        // Save a product
        Product product = productRepository.save(Product.builder()
                .name("Test Product")
                .description("Test Description")
                .unitPrice(BigDecimal.valueOf(100.00))
                .quantity(100)
                .category(category)
                .build());
        List<Product> products = productRepository.findAll();
        assertThat(products.size()).isEqualTo(1);
        assertThat((products.stream().anyMatch(p -> p.getName().equals(("Test Product")))));
    }
}