package com.example.electronicstore.model;

public class OrderDetail {
    private String OrderId;
    private String ProductId;
    private int Quantity;
    private String productName;
    private int price;
    private String imageUrl;

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


    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}