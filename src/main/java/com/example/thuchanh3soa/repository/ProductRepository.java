package com.example.thuchanh3soa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.thuchanh3soa.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
