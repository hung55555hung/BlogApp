package com.bugbug.blogapp.Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bugbug.blogapp.Activity.LoginActivity;
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

    public ProfileFragment() {}

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
                        profileImage.setImageResource(R.drawable.avt);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
        FollowAdapter adapter=new FollowAdapter(list,getContext());
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        ImageView imageViewSetting = view.findViewById(R.id.imageViewSetting);
        imageViewSetting.setOnClickListener(v -> showSettingDialog(v));

        return view;
    }
    private void showSettingDialog(View anchorView) {
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_settings, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // Click handlers
        popupView.findViewById(R.id.editProfile).setOnClickListener(v -> {
            popupWindow.dismiss();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        popupView.findViewById(R.id.changePassword).setOnClickListener(v -> {
            popupWindow.dismiss();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChangePasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });

        popupView.findViewById(R.id.logout).setOnClickListener(v -> {
            popupWindow.dismiss();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });


        popupWindow.setElevation(10);
        popupWindow.showAsDropDown(anchorView, -476, -50); // chỉnh vị trí menu
    }



}

