package com.johndo.product.service;

import org.springframework.stereotype.Service;

import com.johndo.product.dto.Category.CategoryRequestDTO;
import com.johndo.product.dto.Category.CategoryResponseDTO;
import com.johndo.product.exception.CategoryNotFound;
import com.johndo.product.mapper.CategoryMapperInterface;
import com.johndo.product.repository.CategoryRepository;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapperInterface categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapperInterface categoryMapper) {
        this.categoryMapper = categoryMapper;
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponseDTO getCategoryById(Integer id) {
        var category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFound("Category not found"));
        return categoryMapper.toResponseDto(category);
    }

    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO) {
        var category = categoryMapper.toEntity(categoryRequestDTO);
        var savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDto(savedCategory);
    }

    public CategoryResponseDTO updateCategory(Integer id, CategoryRequestDTO categoryRequestDTO) {
        var existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFound("Category not found"));
        var updatedCategory = categoryMapper.toEntity(categoryRequestDTO);
        updatedCategory.setId(existingCategory.getId());
        var savedCategory = categoryRepository.save(updatedCategory);
        return categoryMapper.toResponseDto(savedCategory);
    }

    public void deleteCategory(Integer id) {
        var category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFound("Category not found"));
        categoryRepository.delete(category);
    }

    public void deleteAllCategories() {
        categoryRepository.deleteAll();
    }

}
