package com.example.electronicstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ReportIssueActivity extends AppCompatActivity {
    private Button bntSubmitReport;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        bntSubmitReport = findViewById(R.id.btn_report_submit);

        bntSubmitReport.setOnClickListener(v -> {
            Toast.makeText(ReportIssueActivity.this, "Submit success!", Toast.LENGTH_SHORT).show();
            finish();
        });

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }
}
