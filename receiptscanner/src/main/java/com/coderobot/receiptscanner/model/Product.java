package com.coderobot.receiptscanner.model;

/**
 * Created by the great Tony on 2015/8/13.
 */
public class Product {
    public String description = "";
    public String amount = "";
    public String unitPrice = "";
    public String quantity = "";
    public String rowNum = "";

    public Product() {

    }

    public Product(String description, String quantity, String unitPrice) {
        this.description = description.trim();
        this.quantity = quantity.trim();
        this.unitPrice = unitPrice.trim();
    }

    public int computePrice() {
        return Integer.parseInt(unitPrice) * Integer.parseInt(quantity);
    }
}
