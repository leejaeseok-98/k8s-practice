package com.example.ordersystem.product.dtos;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductResDto {
    private Long id;
    private String name;
    private String category;
    private int price;
    private int stockQuantity;
    private String imagePath;

   public ProductResDto fromEntity(){
       return ProductResDto.builder()
               .id(this.id)
               .name(this.name)
               .category(this.category)
               .price(this.price)
               .stockQuantity(this.stockQuantity)
               .imagePath(this.imagePath)
               .build();
   }
}
