package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.electronicstore.model.Category;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddCategoryActivity extends AppCompatActivity {
    private EditText categoryNameEditText;
    private Button addCategoryButton;
    private ProgressBar progressBar;
    private DatabaseReference categoryRef;
    private ImageView backText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        categoryNameEditText = findViewById(R.id.category_name_edit);
        addCategoryButton = findViewById(R.id.add_category_button);
        progressBar = findViewById(R.id.progress_bar);
        backText = findViewById(R.id.btn_back);

        categoryRef = FirebaseDatabase.getInstance().getReference("categories");

        addCategoryButton.setOnClickListener(v -> addCategory());

        backText.setOnClickListener(v -> {
            startActivity(new Intent(AddCategoryActivity.this, ProductManagerActivity.class));
        });
    }

    private void addCategory() {
        String categoryName = categoryNameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(categoryName)) {
            categoryNameEditText.setError("Category name cannot be empty");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String categoryId = categoryRef.push().getKey();
        Category newCategory = new Category(categoryId, categoryName);

        categoryRef.child(categoryId).setValue(newCategory)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Category added successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to add category!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
