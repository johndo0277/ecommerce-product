package com.johndo.product.mapper;

import org.springframework.stereotype.Component;

import com.johndo.product.dto.Category.CategoryRequestDTO;
import com.johndo.product.dto.Category.CategoryResponseDTO;
import com.johndo.product.model.Category;

@Component
public class CategoryMapperImplementation implements CategoryMapperInterface {

    @Override
    public Category toEntity(CategoryRequestDTO categoryRequestDTO) {
        return Category.builder()
                .name(categoryRequestDTO.getName())
                .parent(categoryRequestDTO.getParentId() != null
                        ? Category.builder().id(categoryRequestDTO.getParentId()).build()
                        : null)
                .build();
    }

    @Override
    public CategoryRequestDTO toDto(Category category) {
        return CategoryRequestDTO.builder()
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .build();
    }

    @Override
    public CategoryResponseDTO toResponseDto(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .build();
    }

}
