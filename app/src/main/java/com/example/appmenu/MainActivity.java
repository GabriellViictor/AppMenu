package com.example.appmenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.appmenu.DataBase.FoodDao;
import com.example.appmenu.DataBase.FoodDatabase;
import com.example.appmenu.adapter.FoodAdapter;
import com.example.appmenu.databinding.ActivityMainBinding;
import com.example.appmenu.model.Food;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private static FoodAdapter foodAdapter;
    private static ArrayList<Food> foodList = new ArrayList<>();
    private static final String JSON_URL = "https://raw.githubusercontent.com/GabriellViictor/json-m2-mobile/main/menu_data.json"; // URL do arquivo JSON no GitHub

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        RecyclerView recyclerView = binding.RecyclerViewFood;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        foodAdapter = new FoodAdapter(foodList, this);
        recyclerView.setAdapter(foodAdapter);

        if (isOnline()) {
            System.out.println("Device is online, loading data from GitHub.");
            new FetchDataTask().execute(JSON_URL);
        } else {
            System.out.println("Device is offline, loading data from database.");
            loadDataFromDatabase();
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean online = netInfo != null && netInfo.isConnectedOrConnecting();
        System.out.println("Network status: " + (online ? "Online" : "Offline"));
        return online;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void processJsonData(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);

            FoodDatabase db = FoodDatabase.getInstance(this);
            FoodDao foodDao = db.foodDao();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String imageBase64 = jsonObject.getString("image");

                Food food = new Food(
                        jsonObject.getString("name"),
                        jsonObject.getString("description"),
                        jsonObject.getString("price"),
                        imageBase64
                );

                foodList.add(food);
                new InsertFoodAsyncTask(foodDao).execute(food);
            }

            System.out.println("Finished loading data. Updating adapter.");
            foodAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            System.out.println("Error loading JSON: " + e.getMessage());
        }
    }

    private void loadDataFromDatabase() {
        System.out.println("Loading data from database.");
        FoodDatabase db = FoodDatabase.getInstance(this);
        FoodDao foodDao = db.foodDao();
        new LoadFoodAsyncTask(foodDao).execute();
    }

    private static class InsertFoodAsyncTask extends AsyncTask<Food, Void, Void> {
        private FoodDao foodDao;

        private InsertFoodAsyncTask(FoodDao foodDao) {
            this.foodDao = foodDao;
        }

        @Override
        protected Void doInBackground(Food... foods) {
            for (Food food : foods) {
                Food existingFood = foodDao.getFoodByName(food.getName());
                if (existingFood == null) {
                    foodDao.insert(food);
                    System.out.println("Inserted food item: " + food.getName());
                } else {
                    System.out.println("Food item already exists: " + food.getName());
                    existingFood.setPrice("a consultar");
                    foodDao.update(existingFood);
                    System.out.println("Updated price to 'a consultar' for food item: " + existingFood.getName());
                }
            }
            return null;
        }
    }

    private static class LoadFoodAsyncTask extends AsyncTask<Void, Void, List<Food>> {
        private FoodDao foodDao;

        private LoadFoodAsyncTask(FoodDao foodDao) {
            this.foodDao = foodDao;
        }

        @Override
        protected List<Food> doInBackground(Void... voids) {
            List<Food> foods = foodDao.getAllFoods();
            System.out.println("Loaded " + foods.size() + " food items from database.");
            return foods;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(List<Food> foods) {
            if (foods != null && !foods.isEmpty()) {
                System.out.println("Updating adapter with loaded food items.");
                for (Food food : foods) {
                    // Quando offline, o preço deve ser "a consultar"
                    food.setPrice("a consultar");
                }
                foodList.clear();
                foodList.addAll(foods);
                foodAdapter.notifyDataSetChanged();
            } else {
                System.out.println("No food items found in database.");
            }
        }
    }

    private class FetchDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            processJsonData(result);
        }
    }
}
