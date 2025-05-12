package com.bugbug.blogapp.Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bugbug.blogapp.Activity.ChangePasswordActivity;
import com.bugbug.blogapp.Activity.EditProfileActivity;
import com.bugbug.blogapp.Adapter.FollowAdapter;
import com.bugbug.blogapp.Model.Follow;
import com.bugbug.blogapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {
    String imageUrl;
    RecyclerView recyclerView;
    ArrayList<Follow> list;
    TextView textViewName, textViewProfession, textViewBio;
    CircleImageView profileImage;

    FirebaseAuth auth;
    DatabaseReference userRef;


    public ProfileFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getUid());


        View view= inflater.inflate(R.layout.fragment_profile, container, false);
        recyclerView=view.findViewById(R.id.friendRV);
        textViewName = view.findViewById(R.id.textViewName);
        textViewProfession = view.findViewById(R.id.textViewProfession);
        profileImage = view.findViewById(R.id.profile_image);

        textViewBio = view.findViewById(R.id.textViewBio);
        list=new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String profession = snapshot.child("profession").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);
                    imageUrl = snapshot.child("coverPhoto").getValue(String.class);

                    textViewName.setText(name);
                    textViewProfession.setText(profession);
                    textViewBio.setText(bio);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get().load(imageUrl).placeholder(R.drawable.avt).into(profileImage);
                    } else {
                        profileImage.setImageResource(R.drawable.avt); // Set default image
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
//        list.add(new Follow(R.drawable.avatar_image));
//        list.add(new Follow(R.drawable.avatar_image));
//        list.add(new Follow(R.drawable.avatar_image));
//        list.add(new Follow(R.drawable.avatar_image));
//        list.add(new Follow(R.drawable.avatar_image));
        FollowAdapter adapter=new FollowAdapter(list,getContext());
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        ImageView imageViewSetting = view.findViewById(R.id.imageViewSetting);
        imageViewSetting.setOnClickListener(v -> showSettingDialog());

        return view;
    }
    private void showSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Settings");
        String[] options = {"Edit Profile", "Change Password"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                startActivity(new Intent(getContext(), EditProfileActivity.class));
            } else if (which == 1) {
                startActivity(new Intent(getContext(), ChangePasswordActivity.class));
            }
        });
        builder.show();
    }

}

