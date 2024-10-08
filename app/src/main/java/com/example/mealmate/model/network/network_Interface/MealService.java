package com.example.mealmate.model.network.network_Interface;

import com.example.mealmate.model.network.CustomMealResponse;
import com.example.mealmate.model.network.MealAreaResponse;
import com.example.mealmate.model.network.MealCategoryResponse;
import com.example.mealmate.model.network.MealIngredientResponse;
import com.example.mealmate.model.network.MealResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MealService {
    @GET("random.php")
    Call<MealResponse> getRandomMeal();

    @GET("search.php")
    Call<MealResponse> searchMealByName(@Query("s") String mealName);

    @GET("search.php")
    Call<MealResponse> listMealsByFirstLetter(@Query("f") String firstLetter);

    @GET("lookup.php")
    Call<MealResponse> lookupMealById(@Query("i") String mealId);

    @GET("lookup.php")
    Call<CustomMealResponse> lookupAllMealDitailsById(@Query("i") String mealId);
    @GET("search.php")
    Call<CustomMealResponse> lookupAllMealDitailsByName(@Query("s") String mealName);



    @GET("categories.php")
    Call<MealCategoryResponse> listAllCategories();

    @GET("list.php?i=list")
    Call<MealIngredientResponse> listAllIngredients();

    @GET("list.php?a=list")
    Call<MealAreaResponse> listAllAreas();


    @GET("list.php")
    Call<MealResponse> listAll(@Query("c") String categoryType);

    @GET("filter.php")
    Call<MealResponse> filterByIngredient(@Query("i") String ingredient);

    @GET("filter.php")
    Call<MealResponse> filterByCategory(@Query("c") String category);

    @GET("filter.php")
    Call<MealResponse> filterByArea(@Query("a") String area);
}
