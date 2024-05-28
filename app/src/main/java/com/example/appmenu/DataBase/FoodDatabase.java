package com.example.appmenu.DataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.appmenu.model.Food;

@Database(entities = {Food.class}, version = 1)
public abstract class FoodDatabase extends RoomDatabase {
    private static FoodDatabase instance;

    public abstract FoodDao foodDao();

    public static synchronized FoodDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            FoodDatabase.class, "food_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
