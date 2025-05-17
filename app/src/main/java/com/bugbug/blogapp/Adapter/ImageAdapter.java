package com.bugbug.blogapp.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bugbug.blogapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Uri> imageUris;
    private OnImageRemoveListener removeListener;

    public interface OnImageRemoveListener {
        void onImageRemove(int position);
    }

    public ImageAdapter(Context context, ArrayList<Uri> imageUris, OnImageRemoveListener removeListener) {
        this.context = context;
        this.imageUris = imageUris;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
        ViewGroup.LayoutParams constraintLayoutParams = holder.itemView.getLayoutParams();
        if (imageUris.size()==1) {
            constraintLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            constraintLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = (int) (300 * context.getResources().getDisplayMetrics().density);
            holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        holder.itemView.setLayoutParams(constraintLayoutParams);
        holder.imageView.setLayoutParams(params);
        Uri uri = imageUris.get(position);
        Picasso.get()
                .load(uri.toString())
                .placeholder(R.drawable.avatar_default)
                .error(R.drawable.avatar_default)
                .into(holder.imageView);

        holder.removeButton.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                removeListener.onImageRemove(currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            removeButton = itemView.findViewById(R.id.removeImageBtn);
        }
    }
}