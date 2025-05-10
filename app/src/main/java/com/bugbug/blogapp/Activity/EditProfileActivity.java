package com.bugbug.blogapp.Activity;
import com.bugbug.blogapp.Util.CloudinaryUtil;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bugbug.blogapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {
    String imageUrl;
    EditText editName, editProfession, editBio;
    Button saveBtn;
    CircleImageView profileImage;
    ImageView btnAddImg;
    Uri uri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    FirebaseUser currentUser;
    DatabaseReference userRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        uri = result.getData().getData();
                        Picasso.get().load(uri).into(profileImage); // Load image using Picasso
                    }
                }
        );
        editName = findViewById(R.id.editName);
        editProfession = findViewById(R.id.editProfession);
        editBio = findViewById(R.id.editBio);
        saveBtn = findViewById(R.id.saveBtn);
        btnAddImg = findViewById(R.id.edit_icon);
        profileImage = findViewById(R.id.profile_image);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // Get the values from the snapshot
                String name = snapshot.child("name").getValue(String.class);
                String profession = snapshot.child("profession").getValue(String.class);
                String bio = snapshot.child("bio").getValue(String.class);
                imageUrl = snapshot.child("coverPhoto").getValue(String.class); // Retrieve cover photo URL


                editName.setText(name);
                editProfession.setText(profession);
                editBio.setText(bio);

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).placeholder(R.drawable.avt).into(profileImage);
                } else {
                    profileImage.setImageResource(R.drawable.avt); // Set default image
                }
            }
        });
        saveBtn.setOnClickListener(v -> {
            String name = editName.getText().toString();
            String profession = editProfession.getText().toString();
            String bio = editBio.getText().toString();


            if (uri != null) {
                CloudinaryUtil.uploadImage(EditProfileActivity.this, uri, new CloudinaryUtil.UploadResultListener() {
                    @Override
                    public void onSuccess(String uploadedImageUrl) {

                        saveDataToFirebase(name, profession, bio, uploadedImageUrl);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(EditProfileActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {

                saveDataToFirebase(name, profession, bio, imageUrl);
            }
        });
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        btnAddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(intent);
            }
        });

    }
    private void saveDataToFirebase(String name, String profession, String bio, String profileImageUrl) {
        userRef.child("name").setValue(name);
        userRef.child("profession").setValue(profession);
        userRef.child("bio").setValue(bio);
        userRef.child("coverPhoto").setValue(profileImageUrl); // Save image URL
        Toast.makeText(EditProfileActivity.this, "Updated", Toast.LENGTH_SHORT).show();
        finish();
    }
}