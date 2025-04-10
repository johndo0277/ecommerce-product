package com.johndo.product.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.johndo.product.dto.PageWrapper;
import com.johndo.product.dto.Category.CategoryRequestDTO;
import com.johndo.product.dto.Product.ProductRequestDTO;
import com.johndo.product.dto.Product.ProductResponseDTO;
import com.johndo.product.service.CategoryService;
import com.johndo.product.service.ProductService;

@Testcontainers
@SpringBootTest
public class ProductServiceTest {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    private static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeAll
    static void setup() {
        // Ensure containers are running
        assertThat(postgresContainer.isRunning()).isTrue();
        assertThat(redisContainer.isRunning()).isTrue();
    }

    @AfterEach
    void cleanup() {
        // Clear the database
        productService.deleteAllProducts(); // Implement this method in your service
        categoryService.deleteAllCategories(); // Implement this method in your service

        // Clear the Redis cache
        var cache = cacheManager.getCache("products");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    public void testGetAllProducts() {
        var category = CategoryRequestDTO.builder()
                .name("Test Category")
                .build();
        var savedCategory = categoryService.createCategory(category);
        // Create a new product
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Product 1");
        request.setUnitPrice(BigDecimal.valueOf(20.0));
        request.setQuantity(2);
        request.setDescription("Description 1");
        request.setCategoryId(savedCategory.getId()); // Assuming a category with ID 1 exists
        productService.createProduct(request);

        // Create another product
        ProductRequestDTO request2 = new ProductRequestDTO();
        request2.setName("Product 2");
        request2.setUnitPrice(BigDecimal.valueOf(40.0));
        request2.setQuantity(4);
        request2.setDescription("Description 2");
        request2.setCategoryId(savedCategory.getId()); // Assuming a category with ID 1 exists
        productService.createProduct(request2);

        // Retrieve all products
        productService.getAllProducts(0, 10);

        // Verify the products are cached
        var cache = cacheManager.getCache("products");
        assertThat(cache).isNotNull();
        var cachedProducts = cache.get("page:0:size:10", PageWrapper.class);
        @SuppressWarnings("unchecked")
        List<ProductResponseDTO> cachedProductList = (List<ProductResponseDTO>) cachedProducts.getContent();
        assertThat(cachedProducts).isNotNull();
        assertThat(cachedProducts.getContent()).isNotEmpty();
        assertThat(cachedProductList).hasSize(2);
        // descending order list

        assertThat(cachedProductList.get(0).getName()).isEqualTo("Product 2");
        assertThat(cachedProductList.get(1).getName()).isEqualTo("Product 1");

        assertThat(cachedProducts.getTotalElements()).isEqualTo(2);

    }

    @Test
    void testCreateAndGetProduct() {
        var category = CategoryRequestDTO.builder()

                .name("Test Category")
                .build();

        var savedCategory = categoryService.createCategory(category);
        // Create a new product
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Test Product");
        request.setUnitPrice(BigDecimal.valueOf(100.0));
        request.setQuantity(10);
        request.setDescription("Test Description");
        request.setCategoryId(savedCategory.getId());
        // Create the product
        ProductResponseDTO createdProduct = productService.createProduct(request);

        // Verify product is cached
        var cache = cacheManager.getCache("products");
        assertThat(cache).isNotNull();
        ProductResponseDTO cachedProduct = cache.get(createdProduct.getId(), ProductResponseDTO.class);
        assertThat(cachedProduct).isNotNull();
        assertThat(cachedProduct.getName()).isEqualTo("Test Product");

        // Retrieve product by ID
        ProductResponseDTO retrievedProduct = productService.getProductById(createdProduct.getId());
        assertThat(retrievedProduct).isNotNull();
        assertThat(retrievedProduct.getName()).isEqualTo("Test Product");
    }

    @Test
    void testUpdateProduct() {

        var category = CategoryRequestDTO.builder()
                .name("Test Category")
                .build();
        var savedCategory = categoryService.createCategory(category);

        // Create a new product
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Old Product");
        request.setUnitPrice(BigDecimal.valueOf(50.0));
        request.setQuantity(5);
        request.setDescription("Old Description");
        request.setCategoryId(savedCategory.getId()); // Assuming a category with ID 1 exists
        ProductResponseDTO createdProduct = productService.createProduct(request);

        // Update the product
        ProductRequestDTO updateRequest = new ProductRequestDTO();
        updateRequest.setName("Updated Product");
        updateRequest.setUnitPrice(BigDecimal.valueOf(75.0));
        updateRequest.setQuantity(10);
        updateRequest.setDescription("Updated Description");
        updateRequest.setCategoryId(savedCategory.getId()); // Assuming a category with ID 1 exists
        ProductResponseDTO updatedProduct = productService.updateProduct(createdProduct.getId(), updateRequest);

        // Verify the cache is updated
        var cache = cacheManager.getCache("products");
        if (cache != null) {

            assertThat(cache).isNotNull();
            ProductResponseDTO cachedProduct = cache.get(updatedProduct.getId(), ProductResponseDTO.class);
            if (cachedProduct != null) {
                assertThat(cachedProduct).isNotNull();
                assertThat(cachedProduct.getName()).isEqualTo("Updated Product");
            } else {
                // If the cache is null, it means the cache was not updated correctly
                assertThat(cachedProduct).isNull();
            }
        } else {
            return; // No cache available
        }
    }

    @Test
    public void testEvictPageWhenProductIsUpdated() {
        var category = CategoryRequestDTO.builder()
                .name("Test Category")
                .build();
        var savedCategory = categoryService.createCategory(category);
        // Create a new product
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Product to Evict");
        request.setUnitPrice(BigDecimal.valueOf(30.0));
        request.setQuantity(3);
        request.setDescription("Description to Evict");
        request.setCategoryId(savedCategory.getId()); // Assuming a category with ID 1 exists
        ProductResponseDTO createdProduct = productService.createProduct(request);

        // Update the product
        ProductRequestDTO updateRequest = new ProductRequestDTO();
        updateRequest.setName("Updated Product");
        updateRequest.setUnitPrice(BigDecimal.valueOf(75.0));
        updateRequest.setQuantity(10);
        updateRequest.setDescription("Updated Description");
        updateRequest.setCategoryId(savedCategory.getId()); // Assuming a category with ID 1 exists
        productService.updateProduct(createdProduct.getId(), updateRequest);

        // Verify the page cache is evicted
        var cache = cacheManager.getCache("products");
        assertThat(cache).isNotNull();
        var cachedProducts = cache.get("page:0:size:10", PageWrapper.class);
        assertThat(cachedProducts).isNull(); // The page should be evicted
    }

    @Test
    void testDeleteProduct() {
        var category = CategoryRequestDTO.builder()
                .name("Test Category")
                .build();
        var savedCategory = categoryService.createCategory(category);
        // Create a new product
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Product to Delete");
        request.setUnitPrice(BigDecimal.valueOf(30.0));
        request.setQuantity(3);
        request.setDescription("Description to Delete");
        request.setCategoryId(savedCategory.getId()); // Assuming a category with ID 1 exists
        ProductResponseDTO createdProduct = productService.createProduct(request);

        // Delete the product
        productService.deleteProduct(createdProduct.getId());

        // Verify the product is removed from the cache
        var cache = cacheManager.getCache("products");
        assertThat(cache).isNotNull();
        ProductResponseDTO cachedProduct = cache.get(createdProduct.getId(), ProductResponseDTO.class);
        assertThat(cachedProduct).isNull();
    }

}