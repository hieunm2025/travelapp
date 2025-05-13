package com.example.travelapp.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travelapp.Adapter.WishlistAdapter;
import com.example.travelapp.Domain.ItemDomain;
import com.example.travelapp.databinding.FragmentWishlistBinding;
import com.example.travelapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class WishlistFragment extends Fragment {

    private RecyclerView recyclerView;
    private FragmentWishlistBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWishlistBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        recyclerView = binding.wishlistRecyclerView;
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        initWishlist();

        return view;
    }

    private void initWishlist() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.d("WishlistFragment", "User not logged in, hiding wishlist");
            if (binding != null) {
                binding.wishlistRecyclerView.setVisibility(View.GONE);
                binding.progressBarWishlist.setVisibility(View.GONE);
            }
            return;
        }

        String userId = user.getUid();
        Log.d("WishlistFragment", "User logged in with UID: " + userId);

        DatabaseReference wishlistRef = dbRef.child("users").child(userId).child("wishlist");
        if (binding != null) {
            binding.wishlistRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBarWishlist.setVisibility(View.VISIBLE);
        }

        ArrayList<ItemDomain> list = new ArrayList<>();

        wishlistRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null || binding == null) {
                    return;
                }

                if (snapshot.exists()) {
                    List<String> itemIds = new ArrayList<>();
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        String itemId = itemSnapshot.getKey();
                        if (itemId != null && !itemId.isEmpty()) {
                            itemIds.add(itemId);
                        }
                    }

                    Log.d("WishlistFragment", "Found " + itemIds.size() + " items in wishlist");

                    if (itemIds.isEmpty()) {
                        binding.wishlistRecyclerView.setVisibility(View.GONE);
                        binding.progressBarWishlist.setVisibility(View.GONE);
                        return;
                    }

                    for (String itemId : itemIds) {
                        dbRef.child("Item").child(itemId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot itemSnapshot) {
                                        ItemDomain item = itemSnapshot.getValue(ItemDomain.class);
                                        if (item != null) {
                                            Log.d("WishlistFragment", "Loaded item: " + item.getTitle());
                                            list.add(item);
                                        }

                                        if (list.size() == itemIds.size()) {
                                            Log.d("WishlistFragment", "All items loaded, setting adapter with " + list.size() + " items");
                                            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                                            RecyclerView.Adapter adapter = new WishlistAdapter(requireContext(), list);
                                            recyclerView.setAdapter(adapter);
                                            binding.progressBarWishlist.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("WishlistFragment", "Error loading item: " + error.getMessage());
                                        if (binding != null) {
                                            binding.wishlistRecyclerView.setVisibility(View.GONE);
                                            binding.progressBarWishlist.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                } else {
                    Log.d("WishlistFragment", "Wishlist snapshot does not exist");
                    binding.wishlistRecyclerView.setVisibility(View.GONE);
                    binding.progressBarWishlist.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("WishlistFragment", "Error loading wishlist: " + error.getMessage());
                if (binding != null) {
                    binding.wishlistRecyclerView.setVisibility(View.GONE);
                    binding.progressBarWishlist.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh memory leak
    }
}