package com.toshiro97.oderfood.model;



public class Banner {

    private String foodId, name, image;

    public Banner() {
    }

    public Banner(String foodId, String name, String image) {
        this.foodId = foodId;
        this.name = name;
        this.image = image;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
