package com.example.ordersystem.ordering.repository;

import com.example.ordersystem.ordering.domain.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderingRepository extends JpaRepository<Ordering,Long> {
    List<Ordering> findByMemberEmail(String email);
}
