package com.chaing.domain.settlements;

import com.chaing.domain.sales.repository.FranchiseSalesItemRepository;
import com.chaing.domain.orders.repository.FranchiseOrderRepository;
import com.chaing.domain.transports.repository.TransitRepository;
import com.chaing.domain.returns.repository.FranchiseReturnRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test") // Assuming there's a test profile, or just use default
public class DataVolumeTest {

    @Autowired
    private FranchiseSalesItemRepository salesItemRepository;

    @Autowired
    private FranchiseOrderRepository orderRepository;

    @Autowired
    private TransitRepository transitRepository;

    @Autowired
    private FranchiseReturnRepository returnRepository;

    @Test
    void checkDataForMarch6th() {
        LocalDate date = LocalDate.of(2026, 3, 6);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        System.out.println("====== Data Volume Check for 2026-03-06 ======");
        
        // Sales Items
        long salesCount = salesItemRepository.findAll().stream()
                .filter(item -> item.getCreatedAt() != null && item.getCreatedAt().toLocalDate().equals(date))
                .count();
        System.out.println("Sales Items: " + salesCount);

        // Orders
        long orderCount = orderRepository.findAll().stream()
                .filter(order -> order.getCreatedAt() != null && order.getCreatedAt().toLocalDate().equals(date))
                .count();
        System.out.println("Franchise Orders: " + orderCount);

        // Transit
        long transitCount = transitRepository.findAll().stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().equals(date))
                .count();
        System.out.println("Transit Records: " + transitCount);

        // Returns
        long returnCount = returnRepository.findAll().stream()
                .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().toLocalDate().equals(date))
                .count();
        System.out.println("Returns: " + returnCount);
        
        System.out.println("==============================================");
    }
}
