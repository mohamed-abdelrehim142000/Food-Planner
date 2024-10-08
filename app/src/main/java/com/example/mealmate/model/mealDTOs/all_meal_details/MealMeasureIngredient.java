package com.example.mealmate.model.mealDTOs.all_meal_details;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

import java.io.Serializable;

@Entity
public class MealMeasureIngredient implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "meal_Type")
    public String strType;

    @ColumnInfo(name = "meal_id")
    private String mealId;

    @ColumnInfo(name = "ingredient_name")
    private String ingredientName;

    @ColumnInfo(name = "measure")
    private String measure;


    public MealMeasureIngredient(String strType, String mealId, String ingredientName, String measure) {
        this.mealId = mealId;
        this.ingredientName = ingredientName;
        this.measure = measure;
        this.strType = strType;

    }
// Getters and setters...


    public String getStrType() {
        return strType;
    }

    public void setStrType(String strType) {
        this.strType = strType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMealId() {
        return mealId;
    }

    public void setMealId(String mealId) {
        this.mealId = mealId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }
}
