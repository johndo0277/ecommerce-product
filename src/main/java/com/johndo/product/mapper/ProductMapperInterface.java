package com.johndo.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.johndo.product.dto.Product.ProductRequestDTO;
import com.johndo.product.dto.Product.ProductResponseDTO;
import com.johndo.product.model.Category;
import com.johndo.product.model.Product;

@Mapper(componentModel = "spring") // Ensure MapStruct is configured for Spring
public interface ProductMapperInterface {
    // Map ProductRequestDTO to Product entity
    // Product entity has a field category which is a Category object
    // ProductRequestDTO has a field categoryId which is an Integer
    // So we need to map categoryId to category
    // define custom mapping method to convert categoryId to Category object below
    @Mapping(target = "id", ignore = true) // Ignore id when creating a new product
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "mapCategoryIdToCategory")
    Product toEntity(ProductRequestDTO productRequestDTO);

    // Map Product entity to ProductResponseDTO
    // catiegoryId is a field in ProductResponseDTO
    // category is a field in Product entity
    @Mapping(target = "categoryId", source = "category.id") // mapping category to categoryId
    ProductResponseDTO toResponseDto(Product product);

    // custom mapping method to convert categoryId to Category object
    @Named("mapCategoryIdToCategory")
    default Category mapCategoryIdToCategory(Integer categoryId) {
        if (categoryId == null) {
            return null;
        }
        return Category.builder()
                .id(categoryId)
                .build();

    }
}
