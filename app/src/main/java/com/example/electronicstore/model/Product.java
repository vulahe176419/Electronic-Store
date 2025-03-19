package com.example.electronicstore.model;

public class Product {
    String pid;
    private String name;
    private String detail;
    private int price;
    private String imageUrl;
    private boolean available;
    private String categoryId;
    private String formattedPrice;

    public Product() {
    }

    public Product(String pid, String name, String detail, int price, String imageUrl, boolean available, String categoryId) {
        this.pid = pid;
        this.name = name;
        this.detail = detail;
        this.price = price;
        this.imageUrl = imageUrl;
        this.available = available;
        this.categoryId = categoryId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDetail() {
        return detail;
    }
    public void setDetail(String detail) {
        this.detail = detail;
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
    public boolean isAvailable() {
        return available;
    }
    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getFormattedPrice() {
        return formattedPrice;
    }

    public void setFormattedPrice(String formattedPrice) {
        this.formattedPrice = formattedPrice;
    }
}