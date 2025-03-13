package com.example.ordersystem.product.dtos;

import com.example.ordersystem.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductRegisterDto {
    private String name;
    private String category;
    private int price;
    private int stockQuantity;
    private MultipartFile productImage;

    public Product toEntity(String email){
        return Product.builder()
                .name(this.name)
                .category(this.category)
                .price(this.price)
                .stockQuantity(this.stockQuantity)
                .memberEmail(email)
                .build();
    }
}
