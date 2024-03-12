package com.example.greenplate.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.greenplate.R;
import com.example.greenplate.viewmodels.InputMealViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InputMealView extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private EditText editMealText;
    private EditText editCalorieText;
    private Button enterMealButton;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference root = db.getReference().child("Meals");
    private InputMealViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_meal);
        viewModel = new ViewModelProvider(this).get(InputMealViewModel.class);
        editMealText = findViewById(R.id.InputMealName);
        editCalorieText = findViewById(R.id.InputCalories);
        enterMealButton = findViewById(R.id.InputMealButton);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        enterMealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String calorieText = editCalorieText.getText().toString();
                String mealName = editMealText.getText().toString();
                if (mealName.isEmpty()) {
                    Toast.makeText(InputMealView.this, "The 'Meal Name' cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (calorieText.isEmpty()) {
                    Toast.makeText(InputMealView.this, "The 'Calories' field cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        int calorieValue = Integer.parseInt(calorieText);
                        DatabaseReference newMealRef = root.push();
                        newMealRef.child("Meal Name").setValue(mealName);
                        newMealRef.child("Calories").setValue(calorieValue)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(InputMealView.this, "Meal saved!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(InputMealView.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } catch (NumberFormatException e) {
                        Toast.makeText(InputMealView.this, "Invalid Calorie Input", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.Home) {
            startActivity(new Intent(InputMealView.this, Home.class));
            return true;
        } else if (id == R.id.Recipe) {
            startActivity(new Intent(InputMealView.this, RecipeView.class));
            return true;
        } else if (id == R.id.InputMeal) {
            return true;
        } else if (id == R.id.Ingredients) {
            startActivity(new Intent(InputMealView.this, IngredientsView.class));
            return true;
        } else if (id == R.id.ShoppingList) {
            startActivity(new Intent(InputMealView.this, ShoppingListView.class));
            return true;
        } else if (id == R.id.PersonalInfo) {
            startActivity(new Intent(InputMealView.this, PersonalInfoView.class));
            return true;
        }
        return false;
    }
}
