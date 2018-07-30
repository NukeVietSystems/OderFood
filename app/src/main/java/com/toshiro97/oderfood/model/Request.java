package com.toshiro97.oderfood.model;

import java.util.List;

public class Request {
    private String phone,name,adress,total,status,comment,paymentState;
    private List<Order> foods;

    public Request(){}

    public Request(String phone, String name, String adress, String total, String status, String comment, String paymentState, List<Order> foods) {
        this.phone = phone;
        this.name = name;
        this.adress = adress;
        this.total = total;
        this.status = status;
        this.comment = comment;
        this.paymentState = paymentState;
        this.foods = foods;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(String paymentState) {
        this.paymentState = paymentState;
    }

    public List<Order> getFoods() {
        return foods;
    }

    public void setFoods(List<Order> foods) {
        this.foods = foods;
    }
}
