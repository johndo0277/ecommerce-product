package com.johndo.product.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "products.pagination")
public class PaginationConfig {

    private int defaultPageSize;
    private int defaultPage;
    private int maxPageSize;

    @Override
    public String toString() {
        return "page:" + defaultPage + ":size:" + defaultPageSize;
    }

}
