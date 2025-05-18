package com.bugbug.blogapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bugbug.blogapp.Activity.FullScreenImageActivity;
import com.bugbug.blogapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostImageAdapter extends RecyclerView.Adapter<PostImageAdapter.ViewHolder> {
    private Context context;
    private List<String> imageUrls;
    private boolean isSingleImage;
    private static final int MAX_HEIGHT_DP = 200;

    public PostImageAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.isSingleImage = imageUrls.size() == 1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        ViewGroup.LayoutParams constraintLayoutParams = holder.itemView.getLayoutParams();
        float density = context.getResources().getDisplayMetrics().density;
        if (isSingleImage) {
            constraintLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            ((ViewGroup.MarginLayoutParams) constraintLayoutParams).setMarginEnd(0);
        } else {
            constraintLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            ((ViewGroup.MarginLayoutParams) constraintLayoutParams).setMarginEnd((int) (5 * density));
        }
        holder.itemView.setLayoutParams(constraintLayoutParams);

        ViewGroup.LayoutParams imageParams = holder.imageView.getLayoutParams();

        if (isSingleImage) {
            imageParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            holder.imageView.setMaxHeight((int) (400 * density));
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //holder.imageView.setMaxHeight((int) (MAX_HEIGHT_DP * density));
        } else {
            imageParams.width =  ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.imageView.setMaxWidth((int) (300 * density));
            holder.imageView.setMaxHeight((int) (MAX_HEIGHT_DP * density));
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        holder.imageView.setLayoutParams(imageParams);
        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullScreenImageActivity.class);
            intent.putStringArrayListExtra("imageUrls", new ArrayList<>(imageUrls));
            intent.putExtra("position", position);
            context.startActivity(intent);
        });
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}