package com.example.electronicstore.model;

public class OrderDetail {
    private String OrderId;
    private String ProductId;
    private int Quantity;

    public OrderDetail() {

    }

    public OrderDetail(String orderId, String productId, int quantity) {
        this.OrderId = orderId;
        this.ProductId = productId;
        this.Quantity = quantity;
    }

    public String getOrderId() {
        return OrderId;
    }

    public void setOrderId(String orderId) {
        this.OrderId = orderId;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        this.ProductId = productId;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        this.Quantity = quantity;
    }
}