package com.example.electronicstore;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.electronicstore.adapter.OrderPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OrderManagementActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isFinishing()) return;
        setContentView(R.layout.activity_order_management);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        OrderPagerAdapter adapter = new OrderPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Cấu hình TabLayoutMediator với văn bản rõ ràng
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Pending orders");
                    tab.setContentDescription("Tab for pending orders");
                    break;
                case 1:
                    tab.setText("Shipping orders");
                    tab.setContentDescription("Tab for shipping orders");
                    break;
                case 2:
                    tab.setText("Delivered orders");
                    tab.setContentDescription("Tab for delivered orders");
                    break;
                case 3:
                    tab.setText("Returned orders");
                    tab.setContentDescription("Tab for returned orders");
                    break;
                case 4:
                    tab.setText("Canceled orders");
                    tab.setContentDescription("Tab for canceled orders");
                    break;
            }
        }).attach();

        int selectedTabPosition = getIntent().getIntExtra("selected_tab_position", 0); // Mặc định là tab 0
        viewPager.setCurrentItem(selectedTabPosition);

        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());
    }
}
