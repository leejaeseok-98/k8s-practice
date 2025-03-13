package com.example.ordersystem.common.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class StockinventoryService {
    @Qualifier("stockinventory")
    private final RedisTemplate<String,String> redisTemplate;

    public StockinventoryService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

//    상품등록 주문취소시 increateStock
    public int increaseStock(Long productId, int quantity){
        Long  remains = redisTemplate.opsForValue().increment(String.valueOf(productId),quantity);
        return remains.intValue();
    }

//    상품 주문시 decreaseStock
    public int decreaseStock(Long productId, int quantity){
//        먼저 조회후에 재고감수가 가능할때 decrease
        Object remainsObject =redisTemplate.opsForValue().get(String.valueOf(productId));
        int remains = Integer.parseInt(remainsObject.toString());
        if (remains < quantity){
            return -1;
        }else {
            Long finalRemains  = redisTemplate.opsForValue().decrement(String.valueOf(productId), quantity);
            return finalRemains.intValue();
        }

    }
}
