package com.letsplay.product;

import com.letsplay.exception.*;
import com.letsplay.product.dto.*;
import com.letsplay.user.User;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) { this.repo = repo; }

    public List<ProductResponse> listAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public ProductResponse create(ProductRequest req, User user) {
        Product p = Product.builder()
                .name(req.name())
                .description(req.description())
                .price(req.price())
                .userId(user.getId())
                .build();
        return toResponse(repo.save(p));
    }

    public ProductResponse update(String id, ProductRequest req, User user) {
        Product p = repo.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));

        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole().toString());

        boolean isOwner = p.getUserId().equals(user.getId());
        if (!isAdmin && !isOwner) throw new ForbiddenException("Not allowed to modify this product");

        p.setName(req.name());
        p.setDescription(req.description());
        p.setPrice(req.price());

        return toResponse(repo.save(p));
    }

    public void delete(String id, User user) {
        Product p = repo.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));

        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole().toString());
        boolean isOwner = p.getUserId().equals(user.getId());
        if (!isAdmin && !isOwner) throw new ForbiddenException("Not allowed to delete this product");

        repo.delete(p);
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getUserId());
    }
}
