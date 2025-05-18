package com.bugbug.blogapp.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bugbug.blogapp.Adapter.FullScreenImageAdapter;
import com.bugbug.blogapp.R;

import java.util.List;


public class FullScreenImageActivity extends AppCompatActivity {

    ViewPager2 viewPager;
    List<String> imageUrls;
    int startPosition;
    private ImageButton btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_full_screen_image);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewPager = findViewById(R.id.viewPager);
        btnClose = findViewById(R.id.btnClose);
        imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        startPosition = getIntent().getIntExtra("position", 0);

        FullScreenImageAdapter adapter = new FullScreenImageAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startPosition, false);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}