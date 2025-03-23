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
    private final Map<String, Boolean> reviewStatusMap = new HashMap<>(); // Thêm để theo dõi trạng thái đánh giá
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private ValueEventListener ordersListener;
    private ValueEventListener reviewsListener;

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
        setupReviewsListener();

        return view;
    }

    private void setupOrdersListener() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            showToast("Vui lòng đăng nhập để xem đơn hàng");
            loadingProgressBar.setVisibility(View.GONE);
            return;
        }

        new Thread(() -> {
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
                        updateUI();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToast("Lỗi tải đơn hàng: " + error.getMessage());
                    updateUI();
                }
            };
            databaseReference.child("orders")
                    .orderByChild("userId").equalTo(userId)
                    .addListenerForSingleValueEvent(ordersListener); // Chỉ tải một lần
        }).start();
    }

    private void loadOrderDetails() {
        new Thread(() -> {
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
                            showToast("Lỗi tải chi tiết đơn hàng");
                            updateUI();
                        }
                    });
        }).start();
    }

    private void loadProducts() {
        new Thread(() -> {
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
                            updateUI();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            showToast("Lỗi tải sản phẩm");
                            updateUI();
                        }
                    });
        }).start();
    }

    private void setupReviewsListener() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        new Thread(() -> {
            reviewsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    reviewStatusMap.clear();
                    for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                        String productId = reviewSnapshot.child("productId").getValue(String.class);
                        if (productId != null) {
                            reviewStatusMap.put(productId, true);
                        }
                    }
                    updateUI();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToast("Lỗi tải đánh giá: " + error.getMessage());
                }
            };
            databaseReference.child("reviews")
                    .orderByChild("userId").equalTo(userId)
                    .addListenerForSingleValueEvent(reviewsListener);
        }).start();
    }

    private void updateUI() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                loadingProgressBar.setVisibility(View.GONE);
            });
        }
    }

    private void showToast(String message) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
        }
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