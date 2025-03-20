package com.example.electronicstore.model;

public class Purchase {
    private String Pid;
    private String Pname;
    private String Pimage;
    private double Price;
    private int Quantity;
    private String Status;

    public Purchase() {}

    public Purchase(String pid, String pname, String pimage, double price, int quantity, String status) {
        this.Pid = pid;
        this.Pname = pname;
        this.Pimage = pimage;
        this.Price = price;
        this.Quantity = quantity;
        this.Status = status;
    }

    // Getters and setters
    public String getPid() { return Pid; }
    public void setPid(String pid) { this.Pid = pid; }
    public String getPname() { return Pname; }
    public void setPname(String pname) { this.Pname = pname; }
    public String getPimage() { return Pimage; }
    public void setPimage(String pimage) { this.Pimage = pimage; }
    public double getPrice() { return Price; }
    public void setPrice(double price) { this.Price = price; }
    public int getQuantity() { return Quantity; }
    public void setQuantity(int quantity) { this.Quantity = quantity; }
    public String getStatus() { return Status; }
    public void setStatus(String status) { this.Status = status; }
}
