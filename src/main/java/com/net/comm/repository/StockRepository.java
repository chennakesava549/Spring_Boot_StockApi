package com.net.comm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.net.comm.model.Stock;

import java.util.Optional;

/**
 * Repository for {@link Stock} entities to perform base operations.
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
 
    Optional<Stock> findByName(String name);
}
