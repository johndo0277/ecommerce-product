package com.johndo.product.dto.Product;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDTO {

    private Long id;
    private String name;
    private String description;

    @JsonProperty(value = "unit_price")
    private BigDecimal unitPrice;

    private Integer quantity;

    @JsonProperty(value = "category_id")
    private Integer categoryId;

}
