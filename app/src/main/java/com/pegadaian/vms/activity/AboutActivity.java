package com.pegadaian.vms.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.pegadaian.vms.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import maes.tech.intentanim.CustomIntent;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.imgBackAbout) ImageView imgBack;
    @BindView(R.id.txtCopyright) TextView txtCopyright;

    SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_about);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        dateFormat = new SimpleDateFormat("yyyy");
        txtCopyright.setText("© " + dateFormat.format(new Date()) + " Pegadaian");
    }

    @Override
    public void onClick(View v) {
        if (v == imgBack) {
            this.finish();
            CustomIntent.customType(this, "left-to-right");
        }
    }

    // BTN BACK
    public void onBackPressed() {

        this.finish();
        CustomIntent.customType(this, "left-to-right");
    }
}
