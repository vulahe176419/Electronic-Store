package com.example.electronicstore.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Cart implements Parcelable {
    private String id;
    private int cartId;
    private String image;
    private double price;
    private String productId;
    private String productName;
    private int quantity;
    private String userId;

    public Cart() {
    }

    public Cart(String id, int cartId, String image, double price, String productId, String productName, int quantity, String userId) {
        this.id = id;
        this.cartId = cartId;
        this.image = image;
        this.price = price;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.userId = userId;
    }

    protected Cart(Parcel in) {
        id = in.readString();
        cartId = in.readInt();
        image = in.readString();
        price = in.readDouble();
        productId = in.readString();
        productName = in.readString();
        quantity = in.readInt();
        userId = in.readString();
    }

    public static final Creator<Cart> CREATOR = new Creator<Cart>() {
        @Override
        public Cart createFromParcel(Parcel in) {
            return new Cart(in);
        }

        @Override
        public Cart[] newArray(int size) {
            return new Cart[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(cartId);
        dest.writeString(image);
        dest.writeDouble(price);
        dest.writeString(productId);
        dest.writeString(productName);
        dest.writeInt(quantity);
        dest.writeString(userId);
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}