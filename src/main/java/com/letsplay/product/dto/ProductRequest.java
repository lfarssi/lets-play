package com.letsplay.product.dto;

import jakarta.validation.constraints.*;

public record ProductRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @Positive Double price
) {}
