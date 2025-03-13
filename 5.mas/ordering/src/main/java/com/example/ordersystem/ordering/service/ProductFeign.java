package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.config.FeignTokenConfig;
import com.example.ordersystem.ordering.dto.ProductDto;
import com.example.ordersystem.ordering.dto.ProductUpdateStockDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
//name은 유레카의 서비스명이고 url은 쿠베네티스의 서비스명임
@FeignClient(name = "product-service", url="http://js-msa-product-service", configuration = FeignTokenConfig.class)
public interface ProductFeign {
    @GetMapping(value = "/product/{id}")
    ProductDto getProductById(@PathVariable("id") Long id);

    @PutMapping(value = "/product/updatestock")
    void updateProductStock(@RequestBody ProductUpdateStockDto dto);
}
