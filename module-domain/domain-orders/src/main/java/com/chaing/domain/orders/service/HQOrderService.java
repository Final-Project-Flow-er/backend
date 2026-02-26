package com.chaing.domain.orders.service;

import com.chaing.domain.orders.dto.response.HQOrderResponse;
import com.chaing.domain.orders.entity.HeadOfficeOrder;
import com.chaing.domain.orders.entity.HeadOfficeOrderItem;
import com.chaing.domain.orders.repository.HeadOfficeOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HQOrderService {

    private final HeadOfficeOrderRepository orderRepository;

    public List<HQOrderResponse> getAllOrders(Long hqId, String username) {
        List<HeadOfficeOrder> orders = orderRepository.findAllByHqIdAndUsername(hqId, username);
        Map<HeadOfficeOrder, List<HeadOfficeOrderItem>> orderAndOrderItem = new HashMap<>();
    }
}
