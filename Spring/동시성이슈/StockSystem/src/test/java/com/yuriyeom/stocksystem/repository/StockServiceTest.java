package com.yuriyeom.stocksystem.repository;

import com.yuriyeom.stocksystem.domain.Stock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;
    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before(){
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    public void after(){
        stockRepository.deleteAll();
    }

    @Test
    public void 재고감소(){
        stockService.decrease(1L, 1L);

        // 100 - 1 = 99
        Stock stock = stockRepository.findById(1L).orElseThrow();

        Assertions.assertEquals(99, stock.getQuantity());
    }

    @Test
    public void 동시에_100개_요청() throws InterruptedException {
        int threadCount = 100;

        // Executor : 비동기로 실행하는 작업을 단순화하여 사용할 수 있게 도와주는 Java의 API
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        // CountDownLatch : 다른 Thread에서 수행중인 작업이 완료될 때까지 대기할 수 있도록 도와주는 클래스
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i=0; i<threadCount; i++){
            executorService.submit(() ->{
                try{
                    stockService.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();
        // 100 - (1 * 100) = 0
        Assertions.assertEquals(0, stock.getQuantity());
    }
}