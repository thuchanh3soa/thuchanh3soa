package com.example.thuchanh3soa.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.thuchanh3soa.model.Product;
import com.example.thuchanh3soa.repository.ProductRepository;

/** RESTful API quan ly san pham: xu ly nghiep vu va truy van du lieu. */
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    /** GET /products : lay danh sach tat ca san pham */
    @GetMapping
    public List<Product> getAll() {
        return repository.findAll();
    }

    /** GET /products/{id} : lay chi tiet mot san pham */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> notFound(id));
    }

    /** POST /products : them san pham moi */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Product body) {
        String error = validate(body);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("message", error));
        }
        Product product = new Product();
        copyFields(body, product);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(product));
    }

    /** PUT /products/{id} : cap nhat thong tin san pham */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Product body) {
        return repository.findById(id)
                .<ResponseEntity<?>>map(product -> {
                    String error = validate(body);
                    if (error != null) {
                        return ResponseEntity.badRequest().body(Map.of("message", error));
                    }
                    copyFields(body, product);
                    return ResponseEntity.ok(repository.save(product));
                })
                .orElseGet(() -> notFound(id));
    }

    /** DELETE /products/{id} : xoa san pham */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return notFound(id);
        }
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Da xoa san pham co id = " + id));
    }

    private String validate(Product p) {
        if (p.getName() == null || p.getName().isBlank()) {
            return "Ten san pham khong duoc de trong";
        }
        if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return "Gia san pham phai la so >= 0";
        }
        if (p.getQuantity() == null || p.getQuantity() < 0) {
            return "So luong san pham phai la so >= 0";
        }
        return null;
    }

    private void copyFields(Product from, Product to) {
        to.setName(from.getName());
        to.setDescription(from.getDescription());
        to.setPrice(from.getPrice());
        to.setQuantity(from.getQuantity());
    }

    private ResponseEntity<?> notFound(Integer id) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Khong tim thay san pham co id = " + id));
    }
}
