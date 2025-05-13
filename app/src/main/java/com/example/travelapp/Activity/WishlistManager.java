package com.example.travelapp.Activity;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.travelapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WishlistManager {

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private boolean isInWishlist = false;
    private Context context;
    private ImageView favIcon;

    public WishlistManager(Context context, ImageView favIcon) {
        this.context = context;
        this.favIcon = favIcon;
        this.mAuth = FirebaseAuth.getInstance();
        this.dbRef = FirebaseDatabase.getInstance().getReference();
    }

    public void checkIfInWishlist(String itemId) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }

        String userId = user.getUid();
        dbRef.child("users").child(userId).child("wishlist").child(itemId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            isInWishlist = true;
                            favIcon.setImageResource(R.drawable.fav_icon_selected);
                        } else {
                            isInWishlist = false;
                            favIcon.setImageResource(R.drawable.fav_icon);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Failed to check wishlist: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void toggleWishlist(String itemId) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Please sign in to add to wishlist", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        if (isInWishlist) {
            // Xóa khỏi Wishlist
            dbRef.child("users").child(userId).child("wishlist").child(itemId)
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        verifyRemoval(userId, itemId);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to remove from Wishlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Thêm vào Wishlist
            dbRef.child("users").child(userId).child("wishlist").child(itemId).setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        verifyAddition(userId, itemId);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to add to Wishlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void verifyAddition(String userId, String itemId) {
        dbRef.child("users").child(userId).child("wishlist").child(itemId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            isInWishlist = true;
                            favIcon.setImageResource(R.drawable.fav_icon_selected);
                            Toast.makeText(context, "Added to Wishlist", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to add to Wishlist: Data not saved", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Failed to verify addition: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyRemoval(String userId, String itemId) {
        Log.d("WishlistManager", "Checking item ID: " + itemId);
        dbRef.child("users").child(userId).child("wishlist").child(itemId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            isInWishlist = false;
                            favIcon.setImageResource(R.drawable.fav_icon);
                            Toast.makeText(context, "Removed from Wishlist", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to remove from Wishlist: Data still exists", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Failed to verify removal: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}