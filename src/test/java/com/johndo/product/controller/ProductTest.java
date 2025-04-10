package com.johndo.product.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.johndo.product.dto.Product.ProductRequestDTO;
import com.johndo.product.dto.Product.ProductResponseDTO;
import com.johndo.product.exception.ProductNotFound;
import com.johndo.product.mapper.ProductMapperInterface;
import com.johndo.product.model.Category;
import com.johndo.product.model.Product;
import com.johndo.product.service.ProductService;

@WebMvcTest(ProductController.class) // Load only the web layer
public class ProductTest {

        @Autowired
        private MockMvc mockMvc;
        @MockitoBean
        private ProductMapperInterface productMapper;

        @MockitoBean
        private ProductService mockProductService; // Mocked service for testing

        @BeforeEach
        public void setUp() {
                // Reset the mock before each test
                Mockito.reset(mockProductService, productMapper);
        }

        @Test
        public void testGetProductById() throws Exception {

                var mockProduct = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .description("Test Description")
                                .unitPrice(BigDecimal.valueOf(100.00))
                                .category(Category.builder()
                                                .id(1)
                                                .name("Test Category")
                                                .build())
                                .build();

                var mockResponseDto = ProductResponseDTO.builder()
                                .id(1L)
                                .name("Test Product")
                                .description("Test Description")
                                .unitPrice(BigDecimal.valueOf(100.00))
                                .categoryId(1)
                                .build();

                when(productMapper.toResponseDto(mockProduct)).thenReturn(mockResponseDto);
                when(mockProductService.getProductById(1L)).thenReturn(mockResponseDto);

                mockMvc.perform(get("/api/v1/products/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Test Product"))
                                .andExpect(jsonPath("$.description").value("Test Description"))
                                .andExpect(jsonPath("$.unit_price").value(100.00))
                                .andExpect(jsonPath("$.category_id").value(1));
        }

        @Test
        public void testGetProductByIdNotFound() throws Exception {
                when(mockProductService.getProductById(1L)).thenThrow(new ProductNotFound("Product not found"));

                mockMvc.perform(get("/api/v1/products/1"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value("Product not found"));
        }

        @Test
        public void testGetProductByIdInvalid() throws Exception {
                mockMvc.perform(get("/api/v1/products/invalid"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Invalid argument type"));
        }

        @Test
        public void testGetProductByIdEmpty() throws Exception {
                mockMvc.perform(get("/api/v1/products/"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value("Resource not found"));
        }

        // @Test
        // public void testGetAllProducts() throws Exception {
        // var mockProduct = Product.builder()
        // .id(1L)
        // .name("Test Product")
        // .description("Test Description")
        // .unitPrice(BigDecimal.valueOf(100.00))
        // .category(Category.builder()
        // .id(1)
        // .name("Test Category")
        // .build())
        // .build();

        // var mockResponseDto = ProductResponseDTO.builder()
        // .id(1L)
        // .name("Test Product")
        // .description("Test Description")
        // .unitPrice(BigDecimal.valueOf(100.00))
        // .categoryId(1)
        // .build();
        // // create a mock Page object
        // var mockPage = new PageImpl<>(List.of(mockResponseDto));
        // when(productMapper.toResponseDto(mockProduct)).thenReturn(mockResponseDto);
        // when(mockProductService.getAllProducts(0, 10)).thenReturn(mockPage);
        // mockPage.getContent().forEach(product -> {
        // when(mockProductService.getProductById(product.getId())).thenReturn(product);
        // });

        // mockMvc.perform(get("/api/v1/products?page=0&size=10"))
        // .andExpect(status().isOk())
        // .andExpect(jsonPath("$.content[0].id").value(1))
        // .andExpect(jsonPath("$.content[0].name").value("Test Product"))
        // .andExpect(jsonPath("$.content[0].description").value("Test Description"))
        // .andExpect(jsonPath("$.content[0].unit_price").value(100.00))
        // .andExpect(jsonPath("$.content[0].category_id").value(1));
        // }

        @Test
        public void testCreateProduct() throws Exception {
                var mockProduct = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .description("Test Description")
                                .unitPrice(BigDecimal.valueOf(100.00))
                                .quantity(10)
                                .category(Category.builder()
                                                .id(1)
                                                .name("Test Category")
                                                .build())
                                .build();

                var mockResponseDto = ProductResponseDTO.builder()
                                .id(1L)
                                .name("Test Product")
                                .description("Test Description")
                                .unitPrice(BigDecimal.valueOf(100.00))
                                .quantity(10)
                                .categoryId(1)
                                .build();

                when(productMapper.toResponseDto(mockProduct)).thenReturn(mockResponseDto);
                when(mockProductService.createProduct(Mockito.any(ProductRequestDTO.class)))
                                .thenReturn(mockResponseDto);

                mockMvc.perform(post("/api/v1/products")
                                .contentType("application/json")
                                .content("""
                                                {
                                                    "name": "Test Product",
                                                    "description": "Test Description",
                                                    "unit_price": 100.00,
                                                    "quantity": 10,
                                                    "category_id": 1
                                                }
                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Test Product"))
                                .andExpect(jsonPath("$.description").value("Test Description"))
                                .andExpect(jsonPath("$.unit_price").value(100.00))
                                .andExpect(jsonPath("$.quantity").value(10))
                                .andExpect(jsonPath("$.category_id").value(1));
        }

        @Test
        public void testUpdateProduct() throws Exception {
                var mockProduct = Product.builder()
                                .id(1L)
                                .name("Test Product")
                                .description("Test Description")
                                .unitPrice(BigDecimal.valueOf(100.00))
                                .quantity(10)
                                .category(Category.builder()
                                                .id(1)
                                                .name("Test Category")
                                                .build())
                                .build();

                var mockResponseDto = ProductResponseDTO.builder()
                                .id(1L)
                                .name("Test Product")
                                .description("Test Description")
                                .unitPrice(BigDecimal.valueOf(100.00))
                                .quantity(10)
                                .categoryId(1)
                                .build();

                when(productMapper.toResponseDto(mockProduct)).thenReturn(mockResponseDto);
                when(mockProductService.updateProduct(Mockito.anyLong(), Mockito.any(ProductRequestDTO.class)))
                                .thenReturn(mockResponseDto);

                mockMvc.perform(put("/api/v1/products/1")
                                .contentType("application/json")
                                .content("""
                                                {
                                                    "name": "Test Product",
                                                    "description": "Test Description",
                                                    "unit_price": 100.00,
                                                    "quantity": 10,
                                                    "category_id": 1
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Test Product"))
                                .andExpect(jsonPath("$.description").value("Test Description"))
                                .andExpect(jsonPath("$.unit_price").value(100.00))
                                .andExpect(jsonPath("$.quantity").value(10))
                                .andExpect(jsonPath("$.category_id").value(1));
        }

        @Test
        public void testUpdateProductNotFound() throws Exception {
                when(mockProductService.updateProduct(Mockito.anyLong(), Mockito.any(ProductRequestDTO.class)))
                                .thenThrow(new ProductNotFound("Product not found"));

                mockMvc.perform(put("/api/v1/products/1")
                                .contentType("application/json")
                                .content("""
                                                {
                                                    "name": "Test Product",
                                                    "description": "Test Description",
                                                    "unit_price": 100.00,
                                                    "quantity": 10,
                                                    "category_id": 1
                                                }
                                                """))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value("Product not found"));
        }

        @Test
        public void testDeleteProduct() throws Exception {
                // Mock the service method
                Mockito.doNothing().when(mockProductService).deleteProduct(1L);
                mockMvc.perform(delete("/api/v1/products/1"))
                                .andExpect(status().isNoContent());
        }
}