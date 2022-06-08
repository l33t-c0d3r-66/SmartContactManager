package com.smart.entities;

import javax.persistence.*;

@Entity
@Table(name="orders")
public class PayOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long payOrderId;
    private String orderId;
    private String amount;
    private String receipt;
    private String status;
    private String paymentId;
    @ManyToOne
    private User user;

    public PayOrder() {
    }

    public PayOrder(Long payOrderId, String orderId, String amount, String receipt, String status, String paymentId, User user) {
        this.payOrderId = payOrderId;
        this.orderId = orderId;
        this.amount = amount;
        this.receipt = receipt;
        this.status = status;
        this.paymentId = paymentId;
        this.user = user;
    }

    public Long getPayOrderId() {
        return payOrderId;
    }

    public void setPayOrderId(Long payOrderId) {
        this.payOrderId = payOrderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "PayOrder{" +
                "payOrderId=" + payOrderId +
                ", orderId='" + orderId + '\'' +
                ", amount='" + amount + '\'' +
                ", receipt='" + receipt + '\'' +
                ", status='" + status + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", user=" + user +
                '}';
    }
}
