package com.example.appmenu.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_table")
public class Food {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String description;
    private String price;
    private String imageBase64;

    public Food(String name, String description, String price, String imageBase64) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageBase64 = imageBase64;
    }

    // Getters e setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}
