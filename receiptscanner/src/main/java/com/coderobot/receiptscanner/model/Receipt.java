package com.coderobot.receiptscanner.model;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by the great Tony on 2015/8/13.
 */
public class Receipt {

    public String invNum = "";
    public String invDate = "";
    public String sellerID = "";
    public String sellerName = "";
    public String invStatus = "";
    public String invPeriod = "";
    public String invRandomNum = "";
    public String invTotalCost = "";
    public String invEncrypt = "";
    public String v = "";
    public String code = "";
    public String msg = "";
    public ArrayList<Product> details = new ArrayList<>();


    public Receipt() {

    }

    public boolean isComplete() {
        int total = 0;

        for (Product product : details) total += product.computePrice();

        return Integer.parseInt(invTotalCost) == total;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
