package com.example.lotteryeventsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AdminPosterAdapter extends RecyclerView.Adapter<AdminPosterAdapter.ViewHolder> {

    private List<String>  posterList;

    public AdminPosterAdapter(List<String> posterList) {
        this.posterList = posterList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_poster_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = posterList.get(position);

        Glide.with(holder.posterImage.getContext())
                .load(imageUrl)
                .into(holder.posterImage);
    }

    @Override
    public int getItemCount() {
        return posterList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImage = itemView.findViewById(R.id.posterImage);
        }
    }


}
