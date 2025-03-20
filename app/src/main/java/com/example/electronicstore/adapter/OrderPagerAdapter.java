package com.example.electronicstore.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.electronicstore.OrderFragment;

public class OrderPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_TABS = 5;

    public OrderPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return OrderFragment.newInstance("pending"); // Chờ lấy hàng
            case 1: return OrderFragment.newInstance("shipping"); // Chờ giao hàng
            case 2: return OrderFragment.newInstance("delivered"); // Đã giao
            case 3: return OrderFragment.newInstance("returned"); // Trả hàng
            case 4: return OrderFragment.newInstance("canceled"); // Đã hủy
            default: return OrderFragment.newInstance("delivered");
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}