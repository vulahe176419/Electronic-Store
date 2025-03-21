package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.electronicstore.adapter.CategoryAdapter;
import com.example.electronicstore.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.electronicstore.model.Category;

import java.util.ArrayList;
import java.util.List;

public class EditProductActivity extends AppCompatActivity {
    private EditText nameEdit, detailEdit, priceEdit, imageUrlEdit;
    private CheckBox availableCheckBox;
    private Button saveButton, deleteButton;
    private DatabaseReference productRef;
    private String targetPid;
    private ImageView backText;
    private Spinner categorySpinner;
    private DatabaseReference categoryRef;
    private String selectedCategoryId;
    private List<Category> categoryList;
    private List<String> categoryNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        nameEdit = findViewById(R.id.name_edit);
        detailEdit = findViewById(R.id.detail_edit);
        priceEdit = findViewById(R.id.price_edit);
        imageUrlEdit = findViewById(R.id.imageUrl_edit);
        availableCheckBox = findViewById(R.id.available);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);
        backText = findViewById(R.id.btn_back);
        categorySpinner = findViewById(R.id.category_spinner);

        categoryList = new ArrayList<>();

        targetPid = getIntent().getStringExtra("product_pid");
        if (targetPid == null) {
            Toast.makeText(this, "No product selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        productRef = FirebaseDatabase.getInstance().getReference("products").child(targetPid);
        categoryRef = FirebaseDatabase.getInstance().getReference("categories");

        loadCategories();

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        nameEdit.setText(product.getName());
                        detailEdit.setText(product.getDetail());
                        priceEdit.setText(String.valueOf(product.getPrice()));
                        imageUrlEdit.setText(product.getImageUrl());
                        if (snapshot.hasChild("available")) {
                            availableCheckBox.setChecked(product.isAvailable());
                        } else {
                            availableCheckBox.setChecked(true);
                        }
                    }
                } else {
                    Toast.makeText(EditProductActivity.this, "Product data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProductActivity.this, "Failed to load product data", Toast.LENGTH_SHORT).show();
            }
        });

        saveButton.setOnClickListener(v -> saveProduct());
        deleteButton.setOnClickListener(v -> deleteProduct());

        backText.setOnClickListener(v -> {
            startActivity(new Intent(EditProductActivity.this, ProductManagerActivity.class));
        });

        priceEdit.addTextChangedListener(new TextWatcher() {
            private final boolean isEditing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void saveProduct() {
        String newName = nameEdit.getText().toString().trim();
        String newDetail = detailEdit.getText().toString().trim();
        String newPriceStr = priceEdit.getText().toString().trim();
        String newImageUrl = imageUrlEdit.getText().toString().trim();
        boolean newAvailable = availableCheckBox.isChecked();

        if (TextUtils.isEmpty(newName)) {
            nameEdit.setError("Name cannot be empty");
            return;
        }
        if (TextUtils.isEmpty(newPriceStr)) {
            priceEdit.setError("Price cannot be empty");
            return;
        }

        if (TextUtils.isEmpty(newImageUrl)) {
            imageUrlEdit.setError("Image URL cannot be empty");
            return;
        }
        int newPrice = Integer.parseInt(newPriceStr);
        int selectedCategoryIndex = categorySpinner.getSelectedItemPosition();
        String selectedCategoryId = categoryList.get(selectedCategoryIndex).getId();

        Product updatedProduct = new Product(targetPid, newName, newDetail, newPrice, newImageUrl, newAvailable, selectedCategoryId);

        productRef.setValue(updatedProduct)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Product updated successfully.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(EditProductActivity.this, ProductManagerActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update product: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void deleteProduct() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    productRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Product deleted.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(EditProductActivity.this, ProductManagerActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to delete: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void loadCategories() {
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (categoryList == null) {
                    categoryList = new ArrayList<>();
                }
                categoryList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Category category = data.getValue(Category.class);
                    if (category != null) {
                        categoryList.add(category);
                    }
                }
                List<String> categoryNames = new ArrayList<>();
                for (Category category : categoryList) {
                    categoryNames.add(category.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProductActivity.this, android.R.layout.simple_spinner_item, categoryNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProductActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

}