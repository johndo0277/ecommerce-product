package com.johndo.product.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import com.johndo.product.dto.Category.CategoryRequestDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CategoryValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testNameIsBlank() {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("") // Blank name
                .parentId(1) // Null parentId
                .build();

        Set<ConstraintViolation<CategoryRequestDTO>> violations = validator.validate(request);

        // Expect 2 violations: one for @NotBlank and one for @Positive
        assertEquals(2, violations.size());

        // Check that the expected messages are present
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Name is required")));

    }

    @Test
    public void testNameTooShort() {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("AB")
                .parentId(1)
                .build();

        Set<ConstraintViolation<CategoryRequestDTO>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("Name must be at least 3 characters", violations.iterator().next().getMessage());
    }

    @Test
    public void testNameExceedsMaxLength() {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("This is a very long category name that exceeds fifty characters")
                .parentId(1)
                .build();

        Set<ConstraintViolation<CategoryRequestDTO>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("Name must be less than 50 characters", violations.iterator().next().getMessage());
    }

    @Test
    public void testParentIdNotPositive() {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("Electronics")
                .parentId(-1)
                .build();

        Set<ConstraintViolation<CategoryRequestDTO>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("Parent ID must be a positive integer", violations.iterator().next().getMessage());
    }
}