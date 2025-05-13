package com.example.travelapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelapp.Activity.DetailActivity;
import com.example.travelapp.Domain.ItemDomain;
import com.example.travelapp.databinding.ViewholderWishlistBinding;
import java.util.ArrayList;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.Viewholder> {

    private ArrayList<ItemDomain> items;
    private Context context;
    private ViewholderWishlistBinding binding;

    public WishlistAdapter(Context context, ArrayList<ItemDomain> items) {
        this.context = context;
        this.items = items;
        Log.d("WishlistAdapter", "Adapter initialized with " + items.size() + " items");
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ViewholderWishlistBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        ItemDomain item = items.get(position);
        Log.d("WishlistAdapter", "Binding item at position " + position + ": " + item.getTitle());

        binding.titleTxt.setText(item.getTitle());
        binding.priceTxt.setText("$" + item.getPrice());
        binding.addressTxt.setText(item.getAddress());
        binding.scoreTxt.setText(String.valueOf(item.getScore()));

        Glide.with(context)
                .load(item.getPic())
                .into(binding.pic);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", item);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        int count = items.size();
        Log.d("WishlistAdapter", "getItemCount: " + count);
        return count;
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        public Viewholder(ViewholderWishlistBinding binding) {
            super(binding.getRoot());
        }
    }
}