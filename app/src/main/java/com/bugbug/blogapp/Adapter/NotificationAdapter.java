package com.bugbug.blogapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bugbug.blogapp.Activity.CommentActivity;
import com.bugbug.blogapp.Activity.UserProfileActivity;
import com.bugbug.blogapp.Model.Notification;
import com.bugbug.blogapp.Model.User;
import com.bugbug.blogapp.R;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.viewHolder> {

    List<Notification> list;
    Context context;

    public NotificationAdapter(List<Notification> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.subnotification_sample,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Notification model=list.get(position);
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(model.getSenderId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user=snapshot.getValue(User.class);
                        String coverPhoto = user.getCoverPhoto();
                        if (coverPhoto == null || coverPhoto.isEmpty()) {
                            holder.profile.setImageResource(R.drawable.avatar_default);
                        } else {
                            Picasso.get()
                                    .load(coverPhoto)
                                    .placeholder(R.drawable.avt)
                                    .into(holder.profile);
                        }
                        if(model.getActionType().equals("Like")){
                            holder.notification.setText(Html.fromHtml("<b>"+user.getName()+"</b>"+" liked your post"));
                        }
                        if(model.getActionType().equals("Comment")){
                            holder.notification.setText(Html.fromHtml("<b>"+user.getName()+"</b>"+" commented your post"));
                        }
                        if(model.getActionType().equals("Follow")){
                            holder.notification.setText(Html.fromHtml("<b>"+user.getName()+"</b>"+" started following you"));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        if(model.isCheckOpen()){
            holder.item.setBackgroundResource(R.color.white);
        }else{
            holder.item.setBackgroundResource(R.color.colorNotification);
        }
        holder.time.setText(TimeAgo.using(model.getTimestamp()));
        holder.item.setOnClickListener(v->handleClick(model));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public void handleClick(Notification notification){
        FirebaseDatabase.getInstance().getReference()
                .child("Notification")
                .child(notification.getReceiverId())
                .child(notification.getId())
                .child("checkOpen")
                .setValue(true);
        if(notification.getActionType().equals("Like") || notification.getActionType().equals("Comment")){
            Intent intent=new Intent(context, CommentActivity.class);
            intent.putExtra("postId",notification.getPostId());
            intent.putExtra("postedBy",notification.getReceiverId());
            context.startActivity(intent);
        }else{
            Intent intent=new Intent(context, UserProfileActivity.class);
            intent.putExtra("userId",notification.getSenderId());

            context.startActivity(intent);
        }
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        ImageView profile;
        TextView notification, time;
        ConstraintLayout item;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            profile=itemView.findViewById(R.id.profile_image);
            notification=itemView.findViewById(R.id.notification_tv);
            time=itemView.findViewById(R.id.time_tv);
            item=itemView.findViewById(R.id.item_notification);
        }
    }

}
