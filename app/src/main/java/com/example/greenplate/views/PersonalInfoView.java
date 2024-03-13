package com.example.greenplate.views;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.greenplate.R;
import com.example.greenplate.model.User;
import com.example.greenplate.viewmodels.PersonalInfoViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.greenplate.viewmodels.InputMealViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import android.content.Intent;
import android.view.MenuItem;

public class PersonalInfoView extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener{
    private EditText editHeight;
    private EditText editWeight;
    private EditText editGender;
    private Button enterUserButton;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference root = db.getReference().child("Users");
    private PersonalInfoViewModel viewModel;

    private User user = User.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        viewModel = new ViewModelProvider(this).get(PersonalInfoViewModel.class);
        editHeight = findViewById(R.id.InputHeight);
        editWeight = findViewById(R.id.InputWeight);
        editGender = findViewById(R.id.InputGender);
        enterUserButton = findViewById(R.id.InputInfo);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        enterUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String height =editHeight.getText().toString();
                String weight =editWeight.getText().toString();
                String gender =editGender.getText().toString();

                if (height.isEmpty()) {
                    Toast.makeText(PersonalInfoView.this, "Meal Name cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (weight.isEmpty()) {
                    Toast.makeText(PersonalInfoView.this, "Calories field cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (gender.isEmpty()) {
                    Toast.makeText(PersonalInfoView.this, "Date cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        //int calorieValue = Integer.parseInt(calorieText);
                        //DatabaseReference newMealRef = root.push();


                        String username = user.getUsername().replaceAll("\\.","");
                        DatabaseReference newMealRef = root.child(username);

                        newMealRef.child("Height").setValue(height);
                        newMealRef.child("Weight").setValue(weight);
                        newMealRef.child("Gender").setValue(gender);
                        newMealRef.child("Meals").setValue(db.getReference())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(PersonalInfoView.this, "User saved", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(PersonalInfoView.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } catch (NumberFormatException e) {
                        Toast.makeText(PersonalInfoView.this, "Invalid User Input", Toast.LENGTH_SHORT).show();
                    }


                }

            }

        });
    }
    public boolean onNavigationItemSelected (@NonNull MenuItem item){
        int id = item.getItemId();
        if (id == R.id.Home) {
            startActivity(new Intent(PersonalInfoView.this, Home.class));
            return true;
        } else if (id == R.id.Recipe) {
            startActivity(new Intent(PersonalInfoView.this, RecipeView.class));
            return true;
        } else if (id == R.id.Ingredients) {
            startActivity(new Intent(PersonalInfoView.this, IngredientsView.class));
            return true;
        } else if (id == R.id.ShoppingList) {
            startActivity(new Intent(PersonalInfoView.this, ShoppingListView.class));
            return true;
        }
        return false;
    }
}
