package com.johndo.product.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jayway.jsonpath.JsonPath;
import com.johndo.product.dto.Product.ProductRequestDTO;
import com.johndo.product.dto.Product.ProductResponseDTO;
import com.johndo.product.model.Category;
import com.johndo.product.model.Product;
import com.johndo.product.repository.CategoryRepository;
import com.johndo.product.repository.ProductRepository;
import com.johndo.product.service.ProductService;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class ProductIntegrationTest {

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
        private ProductService productService;

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private MockMvc mockMvc;

        @Test
        void testRepositoryServiceControllerIntegration() throws Exception {

                // Step 1: Test Repository
                // Product entity has a many-to-one relationship with Category
                // create Category entity first
                // then create Product entity

                Category category = Category.builder()
                                .name("Test Category")
                                .build();
                category = categoryRepository.save(category);
                assertThat(category.getId()).isNotNull();
                assertThat(category.getName()).isEqualTo("Test Category");
                assertThat(category.getId()).isEqualTo(1);
                assertThat(category.getParent()).isNull();
                // create Product entity
                Product product = Product.builder()
                                .name("Test Product")
                                .description("Test Description")
                                .unitPrice(BigDecimal.valueOf(100.00))
                                .quantity(100)
                                .category(Category.builder()
                                                .id(1)
                                                .name("Test Category")
                                                .build())
                                .build();

                productRepository.save(product);
                assertThat(product.getId()).isNotNull();
                assertThat(product.getName()).isEqualTo("Test Product");
                assertThat(product.getDescription()).isEqualTo("Test Description");
                assertThat(product.getUnitPrice()).isEqualTo(BigDecimal.valueOf(100.00));
                assertThat(product.getQuantity()).isEqualTo(100);
                assertThat(product.getCategory()).isNotNull();
                assertThat(product.getCategory().getId()).isEqualTo(1);
                assertThat(product.getCategory().getName()).isEqualTo("Test Category");

                Product foundProduct = productRepository.findById(product.getId()).orElse(null);
                assertThat(foundProduct).isNotNull();
                assertThat(foundProduct.getName()).isEqualTo("Test Product");

                // Step 2: Test Service
                ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                                .name("Service Product")
                                .description("Service Description")
                                .unitPrice(BigDecimal.valueOf(200.0))
                                .quantity(50)
                                .categoryId(1)
                                .build();

                ProductResponseDTO createdProduct = productService.createProduct(productRequestDTO);
                assertThat(createdProduct).isNotNull();
                assertThat(createdProduct.getName()).isEqualTo("Service Product");
                assertThat(createdProduct.getDescription()).isEqualTo("Service Description");
                assertThat(createdProduct.getUnitPrice()).isEqualTo(BigDecimal.valueOf(200.0));
                assertThat(createdProduct.getQuantity()).isEqualTo(50);

                assertThat(createdProduct.getCategoryId()).isEqualTo(1);
                // Step 3: Test Controller (POST)
                String postResponse = mockMvc.perform(post("/api/v1/products")
                                .contentType("application/json")
                                .content("""
                                                {
                                                    "name": "Controller Product",
                                                    "description": "Controller Description",
                                                    "unit_price": 300.0,
                                                    "quantity": 30,
                                                    "category_id": 1
                                                }
                                                """))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                // Extract the ID from the POST response (assuming the response contains the
                // product ID)
                Long createdProductId = JsonPath.parse(postResponse).read("$.id", Long.class);

                // Step 4: Test Controller (GET)
                mockMvc.perform(get("/api/v1/products/{id}", createdProductId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Controller Product"));
        }
}