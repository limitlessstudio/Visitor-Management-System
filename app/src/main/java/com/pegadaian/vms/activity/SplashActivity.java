package com.pegadaian.vms.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.pegadaian.vms.R;

import maes.tech.intentanim.CustomIntent;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            this.finish();
            CustomIntent.customType(this, "bottom-to-up");
        }, 1500);
    }
}
