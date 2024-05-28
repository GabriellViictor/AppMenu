package com.example.appmenu.DataBase;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.appmenu.model.Food;

import java.util.List;

@Dao
public interface FoodDao {
    @Insert
    void insert(Food food);

    @Query("SELECT * FROM food_table")
    List<Food> getAllFoods();

    @Query("SELECT * FROM food_table WHERE name = :name")
    Food getFoodByName(String name);

    @Update()
    void update(Food food);
}
