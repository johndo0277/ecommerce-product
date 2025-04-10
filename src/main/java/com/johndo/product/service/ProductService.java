package com.johndo.product.service;

import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.johndo.product.config.PaginationConfig;
import com.johndo.product.dto.PageWrapper;
import com.johndo.product.dto.Product.ProductRequestDTO;
import com.johndo.product.dto.Product.ProductResponseDTO;
import com.johndo.product.exception.ProductNotFound;
import com.johndo.product.mapper.ProductMapperInterface;
import com.johndo.product.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapperInterface productMapper;
    private final PaginationConfig paginationConfig;
    private final CacheManager cacheManager;

    public ProductService(ProductRepository productRepository,
            ProductMapperInterface productMapper,
            PaginationConfig paginationConfig,
            CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.paginationConfig = paginationConfig;
        this.productRepository = productRepository;
        this.productMapper = productMapper;

    }

    @Cacheable(value = "products", key = "'page:' + #page + ':size:' + #size", unless = "#result == null or #result.content.isEmpty()")
    public PageWrapper<ProductResponseDTO> getAllProducts(int page, int size) {

        size = size <= 0 || size > paginationConfig.getDefaultPageSize() ? paginationConfig.getDefaultPageSize() : size;

        page = page < 0 ? paginationConfig.getDefaultPage() : page;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<ProductResponseDTO> productPage = productRepository.findAll(pageable)
                .map(productMapper::toResponseDto);
        return new PageWrapper<>(productPage);
    }

    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public ProductResponseDTO getProductById(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFound("Product not found"));
        return productMapper.toResponseDto(product);
    }

    @CachePut(value = "products", key = "#result.id") // Cache the newly created product
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        var product = productMapper.toEntity(productRequestDTO);
        var savedProduct = productRepository.save(product);
        // Evict the first page cache to ensure it reflects the new product
        evictFirstPageCache();
        return productMapper.toResponseDto(savedProduct);
    }

    @CachePut(value = "products", key = "#id")
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) {
        var existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFound("Product not found"));
        var updatedProduct = productMapper.toEntity(productRequestDTO);
        updatedProduct.setId(existingProduct.getId());
        var savedProduct = productRepository.save(updatedProduct);
        // Evict the page where the updated product is located
        evictUpdatedProductPageCache(id);
        return productMapper.toResponseDto(savedProduct);
    }

    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFound("Product not found"));
        productRepository.delete(product);
    }

    private void evictFirstPageCache() {
        String cacheKey = paginationConfig.toString();
        var cache = cacheManager.getCache("products");
        if (cache != null) {
            cache.evict(cacheKey);
        }
    }

    private void evictUpdatedProductPageCache(Long productId) {
        var cache = cacheManager.getCache("products");
        if (cache == null) {
            return; // No cache available
        }

        int pageSize = paginationConfig.getDefaultPageSize();
        int low = 0;
        int high = (int) Math.ceil((double) productRepository.count() / pageSize) - 1; // Total number of pages - 1 for
                                                                                       // zero-based index(page start
                                                                                       // from 0)

        // Perform binary search on cached pages

        while (low <= high) {
            int mid = low + (high - low) / 2;
            String cacheKey = "page:" + mid + ":size:" + pageSize;
            var cachedPageWrapper = cache.get(cacheKey, PageWrapper.class); // Retrieve the cached PageWrapper

            if (cachedPageWrapper == null) {
                // If the page is not cached, adjust the search range
                high = mid - 1;
                if (low > high) {
                    break; // No more pages to check
                }
                continue;
            }

            @SuppressWarnings("unchecked")
            List<ProductResponseDTO> productList = (List<ProductResponseDTO>) cachedPageWrapper.getContent();

            // Check if the product ID exists in the cached page
            boolean productFound = productList.stream()
                    .anyMatch(product -> product.getId().equals(productId));

            if (productFound) {
                // Evict the cache for the identified page
                System.out.println("Evicting cache for page: " + mid);
                cache.evict(cacheKey);
                return;
            }

            // Adjust the binary search range based on the product ID
            Long maxIdInPage = productList.get(0).getId(); // First product in descending order
            Long minIdInPage = productList.get(productList.size() - 1).getId(); // Last product in descending order

            if (productId > maxIdInPage) {
                high = mid - 1; // Search in the lower half for the List of products is descending order
            } else if (productId < minIdInPage) {
                low = mid + 1; // Search in the upper half
            } else {
                // Product ID is not in this page, but it should have been; stop searching
                break;
            }
        }

    }

    public void deleteAllProducts() {
        productRepository.deleteAll();

    }
}