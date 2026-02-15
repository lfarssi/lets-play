package com.letsplay.product.dto;

import jakarta.validation.constraints.*;

public record ProductRequest(
        @NotBlank @Size(min = 3, max = 70) String name,
        @NotBlank  @Size(min = 3, max = 5000) String description,
        @NotNull(message = "Price is required")  @Positive(message = "Price must be positive") Double price
) {}
