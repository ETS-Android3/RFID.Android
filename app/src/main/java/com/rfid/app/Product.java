package com.rfid.app;

public class Product {
    private String productId;
    private String tagId;
    private String name;
    private Integer stock;
    private Integer count;

    public Product() {
    }

    public Product(String productId, String tagId, String name, Integer stock, Integer count) {
        this.productId = productId;
        this.tagId = tagId;
        this.name = name;
        this.stock = stock;
        this.count = count;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
