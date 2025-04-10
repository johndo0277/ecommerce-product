package com.johndo.product.mapper;

import com.johndo.product.dto.Category.CategoryRequestDTO;
import com.johndo.product.dto.Category.CategoryResponseDTO;
import com.johndo.product.model.Category;

public interface CategoryMapperInterface {

    // Map CategoryRequestDTO to Category entity
    Category toEntity(CategoryRequestDTO categoryRequestDTO);

    // Map Category entity to CategoryRequestDTO
    CategoryRequestDTO toDto(Category category);

    // Map Category entity to CategoryResponseDTO
    CategoryResponseDTO toResponseDto(Category category);
}