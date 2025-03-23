package com.example.electronicstore.model;

import java.util.List;

public class Review {
    private String id;
    private String userId;
    private String userName;
    private float rating;
    private String productId;
    private String reviewTitle;
    private String date;
    private List<String> mediaUrls;
    private String comment;
    private String variant;
    private String sellerReply;
    private int likes;
    public Review() {}


    public Review(String id, String userId, String userName, float rating, String productId, String reviewTitle, String date,
                  List<String> mediaUrls, String comment, String variant, String sellerReply, int likes) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.productId = productId;
        this.reviewTitle = reviewTitle;
        this.date = date;
        this.mediaUrls = mediaUrls;
        this.comment = comment;
        this.variant = variant;
        this.sellerReply = sellerReply;
        this.likes = likes;
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getReviewTitle() { return reviewTitle; }
    public void setReviewTitle(String reviewTitle) { this.reviewTitle = reviewTitle; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<String> getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }

    public String getSellerReply() { return sellerReply; }
    public void setSellerReply(String sellerReply) { this.sellerReply = sellerReply; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
}