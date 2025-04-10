package com.johndo.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.johndo.product.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

}
