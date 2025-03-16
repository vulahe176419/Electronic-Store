package com.example.electronicstore.model;

import java.io.Serializable;

public class Address implements Serializable {
    private String key;
    private String name;
    private String userId;
    private String addressLine1;
    private String postalCode;
    private boolean isDefault;

    // Constructor rỗng (yêu cầu bởi Firebase)
    public Address() {
    }

    public Address(String name, String userId, String addressLine1, String postalCode, boolean isDefault) {
        this.name = name;
        this.userId = userId;
        this.addressLine1 = addressLine1;
        this.postalCode = postalCode;
        this.isDefault = isDefault;
    }

    // Getters và Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}