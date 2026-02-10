package com.letsplay.product;

import com.letsplay.exception.*;
import com.letsplay.exception.NotFoundException;
import com.letsplay.product.dto.*;
import com.letsplay.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) { this.repo = repo; }

    public List<ProductResponse> listAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public ProductResponse create(ProductRequest req, UserPrincipal principal) {
        Product p = Product.builder()
                .name(req.name())
                .description(req.description())
                .price(req.price())
                .userId(principal.getId())
                .build();
        return toResponse(repo.save(p));
    }

    public ProductResponse update(String id, ProductRequest req, UserPrincipal principal) {
        Product p = repo.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));

        boolean isAdmin = "ROLE_ADMIN".equals(principal.getRole());
        boolean isOwner = p.getUserId().equals(principal.getId());
        if (!isAdmin && !isOwner) throw new ForbiddenException("Not allowed to modify this product");

        p.setName(req.name());
        p.setDescription(req.description());
        p.setPrice(req.price());

        return toResponse(repo.save(p));
    }

    public void delete(String id, UserPrincipal principal) {
        Product p = repo.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));

        boolean isAdmin = "ROLE_ADMIN".equals(principal.getRole());
        boolean isOwner = p.getUserId().equals(principal.getId());
        if (!isAdmin && !isOwner) throw new ForbiddenException("Not allowed to delete this product");

        repo.delete(p);
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getUserId());
    }
}
