package com.example.ordersystem.product.dtos;

import jakarta.persistence.Access;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdateStockDto {
    private Long productId;
    protected Integer productQuantity;
}
