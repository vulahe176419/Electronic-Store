package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.electronicstore.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {
    private EditText nameEditText, detailEditText, priceEditText, imageUrlEditText;
    private Button submitButton;
    private DatabaseReference databaseReference;
    private ImageView backText;
    private Spinner categorySpinner;
    private final List<String> categoryNames = new ArrayList<>();
    private final List<String> categoryIds = new ArrayList<>();
    private String selectedCategoryId = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        nameEditText = findViewById(R.id.name);
        detailEditText = findViewById(R.id.detail);
        priceEditText = findViewById(R.id.price);
        imageUrlEditText = findViewById(R.id.imageUrl);
        submitButton = findViewById(R.id.submit_button);
        backText = findViewById(R.id.btn_back);

        categorySpinner = findViewById(R.id.category_spinner);
        loadCategories();


        databaseReference = FirebaseDatabase.getInstance().getReference("products");

        submitButton.setOnClickListener(v -> createProduct());

        backText.setOnClickListener(v -> {
            startActivity(new Intent(AddProductActivity.this, ProductManagerActivity.class));
        });

        priceEditText.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) {
                    return;
                }
                isEditing = true;
                String originalString = s.toString().replaceAll("\\.", "");
                if (!originalString.isEmpty()) {
                    String formattedString = formatPrice(originalString);
                    priceEditText.setText(formattedString);
                    priceEditText.setSelection(formattedString.length());
                }
                isEditing = false;
            }
        });

    }

    private void createProduct() {
        String name = nameEditText.getText().toString().trim();
        String detail = detailEditText.getText().toString().trim();
        String priceStr = priceEditText.getText().toString().trim();
        String imageUrl = imageUrlEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            priceEditText.setError("Price is required");
            return;
        }
        if (TextUtils.isEmpty(imageUrl)) {
            imageUrlEditText.setError("Image URL is required");
            return;
        }

        priceStr = priceStr.replace(".", "");
        int price;
        try {
            price = Integer.parseInt(priceStr);
        } catch (NumberFormatException e) {
            priceEditText.setError("Invalid price format");
            return;
        }

        DatabaseReference newProductRef = databaseReference.push();
        String pid = newProductRef.getKey();

        Product newProduct = new Product(pid, name, detail, price, imageUrl, true, selectedCategoryId);

        newProductRef.setValue(newProduct)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Product added successfully.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to add product: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCategories() {
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("categories");

        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryNames.clear();
                categoryIds.clear();

                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String id = categorySnapshot.child("id").getValue(String.class);
                    String name = categorySnapshot.child("name").getValue(String.class);

                    if (id != null && name != null) {
                        categoryIds.add(id);
                        categoryNames.add(name);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddProductActivity.this,
                        android.R.layout.simple_spinner_item, categoryNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddProductActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategoryId = categoryIds.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = null;
            }
        });
    }

    private String formatPrice(String price) {
        StringBuilder formatted = new StringBuilder();
        int count = 0;
        for (int i = price.length() - 1; i >= 0; i--) {
            formatted.append(price.charAt(i));
            count++;
            if (count % 3 == 0 && i != 0) {
                formatted.append('.');
            }
        }
        return formatted.reverse().toString();
    }

}