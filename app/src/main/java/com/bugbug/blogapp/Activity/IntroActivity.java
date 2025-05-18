package com.bugbug.blogapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bugbug.blogapp.R;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroCustomLayoutFragment;

import com.github.appintro.AppIntroPageTransformerType;

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_intro1_custom));

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_intro2_custom));

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_intro3_custom));

        setTransformer(AppIntroPageTransformerType.Flow.INSTANCE);
        setColorTransitionsEnabled(false);
        setWizardMode(false);
        setIndicatorEnabled(true);
        setSystemBackButtonLocked(true);
        setVibrate(true);
        setColorSkipButton(getResources().getColor(R.color.black));
        setColorDoneText(getResources().getColor(R.color.black));
    }

    @Override
    protected void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}

