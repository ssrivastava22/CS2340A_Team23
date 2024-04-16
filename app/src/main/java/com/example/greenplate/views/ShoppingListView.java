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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenplate.R;
import com.example.greenplate.model.IngredientsModel;
import com.example.greenplate.model.ShoppingListAdapter;
import com.example.greenplate.model.ShoppingListModel;
import com.example.greenplate.model.User;
import com.example.greenplate.viewmodels.ShoppingListViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingListView extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    private EditText ingredientNameEditText;
    private EditText quantityEditText;
    private EditText caloriesEditText;
    private EditText expirationDateEditText;
    private Button submitButton;
    private User user = User.getInstance();

    private FirebaseDatabase db;
    private ShoppingListViewModel viewModel;

    private DatabaseReference root;
    private RecyclerView recyclerView;
    private ShoppingListAdapter adapter;


    private ArrayList<ShoppingListModel> shoppingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoppinglist);

        initializeViews();
        setupRecyclerView();
        //fetchIngredients();
        fetchIngredients(new ShoppingListView.ShoppingListFetchCallback() {
            @Override
            public void onShoppingListFetched(List<ShoppingListModel> ingredients) {
                shoppingList.clear();
                shoppingList.addAll(ingredients);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ShoppingListView.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchIngredients(ShoppingListView.ShoppingListFetchCallback callback) {
        String username = User.getInstance().getUsername();
        if (username != null && !username.isEmpty()) {
            String sanitizedUsername = username.split("@")[0].replaceAll("[.#$\\[\\]]", "");
            DatabaseReference userRef = root.child(sanitizedUsername);

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<ShoppingListModel> fetchedIngredients = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        String ingredientStr = snapshot.getKey();
                        String quantityStr;
                        Object quantityObj = snapshot.child("quantity").getValue();
                        if (quantityObj instanceof Long || quantityObj instanceof Integer) {
                            quantityStr = String.valueOf(quantityObj);
                        } else if (quantityObj instanceof String) {
                            // Here, you can directly use the string or attempt to parse it as
                            // a long if necessary
                            quantityStr = (String) quantityObj;
                            try {
                                long quantityLong = Long.parseLong(quantityStr);
                                // If parsing is successful but you still need it as a string
                                quantityStr = String.valueOf(quantityLong);
                            } catch (NumberFormatException e) {
                                // Handle the case where the string cannot be parsed to a long
                                quantityStr = "Invalid Format"; // Adjust based on how you want
                                // to handle this
                            }
                        } else {
                            quantityStr = "Unknown Quantity"; // Adjust accordingly
                        }

                        String caloriesStr = snapshot.child("calories").getValue(String.class);
                        String expirationDateStr = snapshot.child("expirationDate")
                                .getValue(String.class);
                        ShoppingListModel ingredient = new ShoppingListModel(ingredientStr,
                                quantityStr, caloriesStr, expirationDateStr);
                        if (ingredient != null) {
                            fetchedIngredients.add(ingredient);
                        }
                    }
                    callback.onShoppingListFetched(fetchedIngredients);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    callback.onError("Failed to load shopping list.");
                }
            });
        } else {
            callback.onError("Username not set or invalid.");
        }
    }

    private void initializeViews() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        ingredientNameEditText = findViewById(R.id.ingredient_name_edit_text);
        quantityEditText = findViewById(R.id.quantity_edit_text);
        caloriesEditText = findViewById(R.id.calories_edit_text);
        expirationDateEditText = findViewById(R.id.expiration_date_edit_text);
        submitButton = findViewById(R.id.submit_button);

        db = FirebaseDatabase.getInstance();
        root = db.getReference().child("ShoppingList");
        viewModel = new ViewModelProvider(this).get(ShoppingListViewModel.class);

        submitButton.setOnClickListener(v -> handleSubmitButtonClick());
        Button scrollUpButton = findViewById(R.id.scrollUpButton1);
        Button scrollDownButton = findViewById(R.id.scrollDownButton1);
        scrollUpButton.setOnClickListener(v -> recyclerView.scrollBy(0, -150));
        scrollDownButton.setOnClickListener(v -> recyclerView.scrollBy(0, 150));

    }


    private void handleSubmitButtonClick() {
        String ingredientName = ingredientNameEditText.getText().toString().trim();
        String quantity = quantityEditText.getText().toString().trim();
        String calories = caloriesEditText.getText().toString().trim();
        String expirationDate = expirationDateEditText.getText().toString().trim();

        if (ingredientName.isEmpty() || quantity.isEmpty() || calories.isEmpty()) {
            Toast.makeText(ShoppingListView.this, "Please fill in all fields.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            double caloriesValue = Double.parseDouble(calories);
            if (caloriesValue <= 0) {
                Toast.makeText(ShoppingListView.this, "Quantity must be positive.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(ShoppingListView.this, "Invalid number of calories entered.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            double quantityValue = Double.parseDouble(quantity);
            if (quantityValue <= 0) {
                Toast.makeText(ShoppingListView.this, "Quantity must be positive.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(ShoppingListView.this, "Invalid quantity entered.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.checkIngredientExists(ingredientName, exists -> {
            if (exists) {
                Toast.makeText(ShoppingListView.this,
                        "Ingredient already exists in shopping list.", Toast.LENGTH_SHORT).show();
            } else {
                addIngredientToShoppingList(ingredientName, quantity, calories, expirationDate);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.Home) {
            startActivity(new Intent(ShoppingListView.this, Home.class));
            return true;
        } else if (id == R.id.Recipe) {
            startActivity(new Intent(ShoppingListView.this, RecipeView.class));
            return true;
        } else if (id == R.id.InputMeal) {
            startActivity(new Intent(ShoppingListView.this, InputMealView.class));
            return true;
        } else if (id == R.id.Ingredients) {
            startActivity(new Intent(ShoppingListView.this, IngredientsView.class));
            return true;
        } else if (id == R.id.ShoppingList) {
            return true;
        }
        return false;
    }

    public void addIngredientToShoppingList(String ingredientName, String quantity, String calories,
                                      String expirationDate) {
        // Retrieve the username (email) from the User singleton instance
        String username = user.getUsername();
        if (username != null && !username.isEmpty()) {
            // Use only the part before the '@' symbol in the email as the key
            // and remove any periods or other illegal characters
            String sanitizedUsername = username.split("@")[0].replaceAll("[.#$\\[\\]]",
                    "");

            // Use the sanitized username to create a reference in your database
            DatabaseReference userRef = root.child(sanitizedUsername);
            DatabaseReference ingredientRef = userRef.child(ingredientName);

            Map<String, Object> ingredientData = new HashMap<>();
            ingredientData.put("quantity", quantity);
            ingredientData.put("calories", calories);
            ingredientData.put("expirationDate", expirationDate);

            // Check if the ingredient already exists
            ingredientRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Ingredient exists, update its values
                        ingredientRef.updateChildren(ingredientData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(ShoppingListView.this,
                                                "Ingredient updated in Shopping List.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ShoppingListView.this,
                                                "Failed to update ingredient in shopping list: "
                                                        + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Ingredient doesn't exist, add it as a new entry
                        ingredientRef.setValue(ingredientData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(ShoppingListView.this,
                                                "Ingredient added to Shopping List.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ShoppingListView.this,
                                                "Failed to add ingredient to shopping list: "
                                                        + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ShoppingListView.this,
                            "Failed to check ingredient existence: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ShoppingListView.this,
                    "Username not set", Toast.LENGTH_SHORT).show();
        }
    }

    /* public void addIngredientToShoppingList(String ingredientName, String quantity, String calories,
                                      String expirationDate) {
        // Retrieve the username (email) from the User singleton instance
        String username = user.getUsername();
        if (username != null && !username.isEmpty()) {
            // Use only the part before the '@' symbol in the email as the key
            // and remove any periods or other illegal characters
            String sanitizedUsername = username.split("@")[0].replaceAll("[.#$\\[\\]]",
                    "");

            // Use the sanitized username to create a reference in your database
            DatabaseReference userRef = root.child(sanitizedUsername);
            DatabaseReference ingredientRef = userRef.child(ingredientName);

            Map<String, Object> ingredientData = new HashMap<>();
            ingredientData.put("quantity", quantity);
            ingredientData.put("calories", calories);
            ingredientData.put("expirationDate", expirationDate);

            ingredientRef.setValue(ingredientData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(ShoppingListView.this,
                                    "Ingredient added to Shopping List.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ShoppingListView.this,
                                    "Failed to add ingredient to shopping list: "
                                            + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(ShoppingListView.this,
                    "Username not set", Toast.LENGTH_SHORT).show();
        }
    } */

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.shopping_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        adapter = new ShoppingListAdapter(shoppingList, root);
        recyclerView.setAdapter(adapter);
    }

    public interface ShoppingListFetchCallback {
        void onShoppingListFetched(List<ShoppingListModel> ingredients);
        void onError(String message);
    }
}
