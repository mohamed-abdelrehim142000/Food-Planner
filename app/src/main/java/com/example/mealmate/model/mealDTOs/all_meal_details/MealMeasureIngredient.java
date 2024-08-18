package com.example.mealmate.model.mealDTOs.all_meal_details;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity
public class MealMeasureIngredient {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "meal_id")
    private String mealId;

    @ColumnInfo(name = "ingredient_name")
    private String ingredientName;

    @ColumnInfo(name = "measure")
    private String measure;

    public MealMeasureIngredient(int id, String mealId, String ingredientName, String measure) {
        this.id = id;
        this.mealId = mealId;
        this.ingredientName = ingredientName;
        this.measure = measure;
    }
// Getters and setters...


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
