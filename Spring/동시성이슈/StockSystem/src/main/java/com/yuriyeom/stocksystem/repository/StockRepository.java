package com.yuriyeom.stocksystem.repository;

import com.yuriyeom.stocksystem.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
}
