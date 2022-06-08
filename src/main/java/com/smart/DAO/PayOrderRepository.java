package com.smart.DAO;

import com.smart.entities.PayOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayOrderRepository extends JpaRepository<PayOrder,Long> {
    public PayOrder findPayOrderByOrderId(String orderId);
}
