package com.johndo.product.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.johndo.product.dto.Category.CategoryRequestDTO;
import com.johndo.product.dto.Category.CategoryResponseDTO;
import com.johndo.product.exception.CategoryNotFound;
import com.johndo.product.mapper.CategoryMapperImplementation;
import com.johndo.product.mapper.CategoryMapperInterface;
import com.johndo.product.model.Category;
import com.johndo.product.service.CategoryService;

@WebMvcTest(CategoryController.class) // Load only the web layer

public class CategoryTest {

        private CategoryMapperInterface categoryMapper = new CategoryMapperImplementation(); // Use the real mapper for
                                                                                             // testing
        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private CategoryService mockCategoryService; // Mocked service for testing

        @BeforeEach
        public void setUp() {
                // Reset the mock before each test
                Mockito.reset(mockCategoryService);
        }

        @Test
        public void testGetCategoryById() throws Exception {
                /*
                 * Step 1: Create a mock Category object to simulate the service response.
                 * This object should have the same structure as the one returned by the
                 * CategoryService.
                 * 
                 * Step 2: Use Mockito to define the behavior of the mock service when
                 * getCategoryById(1) is called. This will return the mock Category object
                 * created in Step 1.
                 * 
                 * Step 3: Perform a GET request to the endpoint /categories/1 using
                 * MockMvc. This simulates a client request to the API.
                 * 
                 * Note:
                 * if return value is plain text , we need to change the the
                 * ex: jsonPath("$").value("Category not found")
                 * if return json object, we need to change to
                 * ex: jsonPath("$.message").value("Category not found")
                 * Step 4: Use MockMvc to verify the response. We check that the status is OK or
                 * throw Exception (bad request - 400, unique constraint -409, internal server
                 * error - 500)
                 * and that the JSON response contains the expected values for id,... etc
                 * 
                 * TestConfig.class is used to load the test configuration, which includes the
                 * CategoryMapper bean. This allows us to use the real mapper in the test.
                 * 
                 */
                var categoryResponseDTO = CategoryResponseDTO.builder()
                                .id(1)
                                .name("Electronics")
                                .parentId(null)
                                .build();
                when(mockCategoryService.getCategoryById(1)).thenReturn(categoryResponseDTO);

                // Perform the GET request and verify the response
                mockMvc.perform(get("/api/v1/categories/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Electronics"))
                                .andExpect(jsonPath("$.parent_id").value(org.hamcrest.Matchers.nullValue()));
        }

        @Test
        public void testGetCategoryByIdNotFound() throws Exception {
                // Simulate a scenario where the category is not found

                when(mockCategoryService.getCategoryById(999)).thenThrow(new CategoryNotFound("Category not found"));

                // Perform the GET request and verify the response
                mockMvc.perform(get("/api/v1/categories/999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value("Category not found"));
        }

        @Test
        public void testGetCategoryByIdWithParent() throws Exception {
                // Create a mock Category object with a parent
                var parentCategory = Category.builder()
                                .id(2)
                                .name("Home Appliances")
                                .parent(null)
                                .build();

                var category = Category.builder()
                                .id(1)
                                .name("Refrigerators")
                                .parent(parentCategory) // Set parent directly
                                .build();

                // Use the real mapper to convert the category to a response DTO
                var categoryResponseDTO = categoryMapper.toResponseDto(category);

                // Stub the service method
                when(mockCategoryService.getCategoryById(1)).thenReturn(categoryResponseDTO);

                // Perform the GET request and verify the response
                mockMvc.perform(get("/api/v1/categories/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Refrigerators"))
                                .andExpect(jsonPath("$.parent_id").value(2));
        }

        @Test
        public void testCreateCategory() throws Exception {

                var categoryResponseDTO = CategoryResponseDTO.builder()
                                .id(1)
                                .name("Electronics")
                                .parentId(null)
                                .build();

                // Mock the service method
                when(mockCategoryService.createCategory(Mockito.any(CategoryRequestDTO.class)))
                                .thenReturn(categoryResponseDTO);

                // Perform the POST request and verify the response
                mockMvc.perform(post("/api/v1/categories")
                                .contentType("application/json")
                                .content("""
                                                    {
                                                        "name": "Electronics",
                                                        "parentId": null
                                                    }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Electronics"))
                                .andExpect(jsonPath("$.parent_id").value(org.hamcrest.Matchers.nullValue()));
        }

        @Test
        public void testUpdateCategory() throws Exception {
                var categoryResponseDTO = CategoryResponseDTO.builder()
                                .id(1)
                                .name("Electronics")
                                .parentId(null)
                                .build();

                // Mock the service method
                when(mockCategoryService.updateCategory(Mockito.anyInt(), Mockito.any(CategoryRequestDTO.class)))
                                .thenReturn(categoryResponseDTO);

                // Perform the PUT request and verify the response
                mockMvc.perform(put("/api/v1/categories/1")
                                .contentType("application/json")
                                .content("""
                                                    {
                                                        "name": "Electronics",
                                                        "parentId": null
                                                    }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Electronics"))
                                .andExpect(jsonPath("$.parent_id").value(org.hamcrest.Matchers.nullValue()));
        }

        @Test
        public void testDeleteCategory() throws Exception {
                // Mock the service method
                Mockito.doNothing().when(mockCategoryService).deleteCategory(1);

                // Perform the DELETE request and verify the response
                mockMvc.perform(delete("/api/v1/categories/1"))
                                .andExpect(status().isNoContent());
        }

}