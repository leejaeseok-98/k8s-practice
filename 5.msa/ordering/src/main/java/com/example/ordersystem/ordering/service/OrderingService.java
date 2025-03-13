package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.service.StockRabbitmqService;
import com.example.ordersystem.common.service.StockinventoryService;
import com.example.ordersystem.ordering.controller.SseController;
import com.example.ordersystem.ordering.domain.OrderDetail;
import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.dto.*;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.SimpleTimeZone;

@Service
@Transactional
public class OrderingService {
    private final StockinventoryService stockinventoryService;
    private final StockRabbitmqService stockRabbitmqService;
    private final SseController sseController;
    private final OrderingRepository orderingRepository;
    private final RestTemplate restTemplate;
    private final ProductFeign productFeign;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    public OrderingService(StockinventoryService stockinventoryService, StockRabbitmqService stockRabbitmqService, SseController sseController, OrderingRepository orderingRepository, RestTemplate restTemplate, ProductFeign productFeign, KafkaTemplate<String, Object> kafkaTemplate) {
        this.stockinventoryService = stockinventoryService;
        this.stockRabbitmqService = stockRabbitmqService;
        this.sseController = sseController;
        this.orderingRepository = orderingRepository;
        this.restTemplate = restTemplate;
        this.productFeign = productFeign;
        this.kafkaTemplate = kafkaTemplate;
    }
    public Ordering orderCreate(List<OrderCreateDto> dtos) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .build();
        for (OrderCreateDto o : dtos) {
//            product서버에 api요청을 통해 product객체를 받아와야함. ->동기처리 필수

            String productGetUrl = "http://js-msa-product-service/product/"+o.getProductId();
            String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<ProductDto> response = restTemplate.exchange(productGetUrl, HttpMethod.GET,httpEntity, ProductDto.class);
            ProductDto productDto = response.getBody();
            System.out.println(productDto);
            int quantity = o.getProductCount();
            if ( productDto.getStockQuantity() <quantity) {
                throw new IllegalArgumentException("재고 부족");
            } else {
//                재고감소 api요청을 product서버에 보내야함 -> 비동기 처리 가능
                String productUpdateStockUrl = "http://js-msa-product-service/product/updatestock";
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<ProductUpdateStockDto> updateEntity = new HttpEntity<>(
                        ProductUpdateStockDto.builder().productId(o.getProductId()).productQuantity(o.getProductCount()).build()
                        ,headers);
                restTemplate.exchange(productUpdateStockUrl,HttpMethod.PUT,updateEntity, Void.class);
            }

            OrderDetail orderDetail = OrderDetail.builder()
                    .ordering(ordering)
//                    받아온 product객체를 통해 id값 세팅
                    .productId(o.getProductId())
                    .quantity(o.getProductCount())
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }
        orderingRepository.save(ordering);
        return ordering;
    }

    public Ordering orderFeignKafkaCreate(List<OrderCreateDto> dtos) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .build();
        for (OrderCreateDto o : dtos) {
//            product서버에 feign클라이언트를 통한 api요청 조회
            ProductDto productDto = productFeign.getProductById(o.getProductId());



            int quantity = o.getProductCount();
            if ( productDto.getStockQuantity() <quantity) {
                throw new IllegalArgumentException("재고 부족");
            } else {
//                재고감소 api요청을 product서버에 보내야함 -> kafka에 메시지 발생
//                productFeign.updateProductStock(ProductUpdateStockDto.builder()
//                                .productId(o.getProductId())
//                                .productQuantity(o.getProductCount())
//                        .build());
                ProductUpdateStockDto dto = ProductUpdateStockDto.builder()
                        .productId(o.getProductId()).productQuantity(o.getProductCount())
                        .build();
                kafkaTemplate.send("update-stock-topic", dto);



            }

            OrderDetail orderDetail = OrderDetail.builder()
                    .ordering(ordering)
//                    받아온 product객체를 통해 id값 세팅
                    .productId(o.getProductId())
                    .quantity(o.getProductCount())
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }
        orderingRepository.save(ordering);
        return ordering;
    }

    public List<OrderListResDto> orderList(){
        List<Ordering> orderings = orderingRepository.findAll();
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering o : orderings){
            List<OrderDetailResDto> orderDetailResDtos = new ArrayList<>();
            for(OrderDetail od : o.getOrderDetails()){
                OrderDetailResDto orderDetailResDto = OrderDetailResDto.builder()
                        .detailId(od.getId())
                        .count(od.getQuantity())
                        .build();
                orderDetailResDtos.add(orderDetailResDto);
            }
            OrderListResDto orderDto = OrderListResDto
                    .builder()
                    .id(o.getId())
                    .memberEmail(o.getMemberEmail())
                    .orderStatus(o.getOrderStatus().toString())
                    .orderDetails(orderDetailResDtos)
                    .build();
            orderListResDtos.add(orderDto);
        }
        return orderListResDtos;
    }

    public List<OrderListResDto> myOrders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        for (Ordering o : orderingRepository.findByMemberEmail(email)){
            orderListResDtos.add(o.fromEntity());
        }
        return orderListResDtos;
    }

    public Ordering orderCancel(Long id){
//        상태값 바꾸기
        Ordering ordering = orderingRepository.findById(id).orElseThrow(()->new EntityNotFoundException("d"));
        ordering.cancelStatus();
        return ordering;
//        재고수량바꾸기

    }

}
