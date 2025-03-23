package com.example.electronicstore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.electronicstore.adapter.OrderAdapter;
import com.example.electronicstore.model.Order;
import com.example.electronicstore.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderFragment extends Fragment {

    private static final String ARG_STATUS = "status";
    private String status;
    private RecyclerView recyclerView;
    private ProgressBar loadingProgressBar;
    private OrderAdapter adapter;
    private final List<Order> orderList = new ArrayList<>();
    private final Map<String, Product> productMap = new HashMap<>();
    private final Map<String, Integer> totalItemsMap = new HashMap<>();
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private ValueEventListener ordersListener;
    private ValueEventListener reviewsListener; // Thêm listener cho reviews

    public static OrderFragment newInstance(String status) {
        OrderFragment fragment = new OrderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            status = getArguments().getString(ARG_STATUS);
        }
        databaseReference = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        recyclerView = view.findViewById(R.id.orderRecyclerView);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderAdapter(getActivity(), orderList, productMap, totalItemsMap, status);
        recyclerView.setAdapter(adapter);
        setupOrdersListener();
        setupReviewsListener(); // Thêm listener cho reviews
        return view;
    }

    private void setupOrdersListener() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem đơn hàng", Toast.LENGTH_SHORT).show();
            loadingProgressBar.setVisibility(View.GONE);
            return;
        }

        ordersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order != null) {
                        String orderStatus = dataSnapshot.child("status").getValue(String.class);
                        order.setStatus(orderStatus);
                        if (orderStatus != null && status.equals(orderStatus)) {
                            order.setOrderId(dataSnapshot.getKey());
                            orderList.add(order);
                        }
                    }
                }
                if (!orderList.isEmpty()) {
                    loadOrderDetails();
                } else {
                    adapter.notifyDataSetChanged();
                    loadingProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải đơn hàng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                loadingProgressBar.setVisibility(View.GONE);
            }
        };

        databaseReference.child("orders")
                .orderByChild("userId").equalTo(userId)
                .addValueEventListener(ordersListener);
    }

    private void loadOrderDetails() {
        databaseReference.child("orderDetails")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        totalItemsMap.clear();
                        for (Order order : orderList) {
                            int itemCount = 0;
                            for (DataSnapshot detailSnapshot : snapshot.getChildren()) {
                                String orderId = detailSnapshot.child("orderId").getValue(String.class);
                                if (orderId != null && orderId.equals(order.getOrderId())) {
                                    String productId = detailSnapshot.child("productId").getValue(String.class);
                                    if (order.getProductId() == null) {
                                        order.setProductId(productId);
                                    }
                                    itemCount++;
                                }
                            }
                            totalItemsMap.put(order.getOrderId(), itemCount);
                        }
                        loadProducts();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Lỗi tải chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                        loadingProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void loadProducts() {
        databaseReference.child("products")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productMap.clear();
                        for (Order order : orderList) {
                            if (order.getProductId() != null) {
                                DataSnapshot productSnapshot = snapshot.child(order.getProductId());
                                Product product = productSnapshot.getValue(Product.class);
                                if (product != null) {
                                    product.setPid(order.getProductId());
                                    productMap.put(order.getProductId(), product);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        loadingProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
                        loadingProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void setupReviewsListener() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            return;
        }

        reviewsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Khi có thay đổi trong reviews, chỉ cần làm mới giao diện
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải đánh giá: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        databaseReference.child("reviews")
                .orderByChild("userId").equalTo(userId)
                .addValueEventListener(reviewsListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ordersListener != null) {
            databaseReference.child("orders").removeEventListener(ordersListener);
        }
        if (reviewsListener != null) {
            databaseReference.child("reviews").removeEventListener(reviewsListener);
        }
    }

}