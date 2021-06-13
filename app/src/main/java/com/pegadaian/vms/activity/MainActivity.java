
package com.pegadaian.vms.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pegadaian.vms.BuildConfig;
import com.pegadaian.vms.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import maes.tech.intentanim.CustomIntent;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.imgMenu) ImageView imgMenu;
    @BindView(R.id.imgLogin) ImageView imgLogin;
    @BindView(R.id.txtGreeting) TextView txtGreeting;
    @BindView(R.id.cvCheckin) CardView cvCheckin;
    @BindView(R.id.cvCheckout) CardView cvCheckout;
    @BindView(R.id.lnCheckedIn) LinearLayout lnCheckedIn;
    @BindView(R.id.lnCheckedOut) LinearLayout lnCheckedOut;
    @BindView(R.id.txtCheckedIn) TextView txtCheckedIn;
    @BindView(R.id.txtCheckedOut) TextView txtCheckedOut;
    @BindView(R.id.dlMenu) DrawerLayout dlMenu;
    @BindView(R.id.imgCancel) ImageView imgCancel;
    @BindView(R.id.lnLoc) LinearLayout lnLoc;
    @BindView(R.id.lnData) LinearLayout lnData;
    @BindView(R.id.lnAbout) LinearLayout lnAbout;
    @BindView(R.id.txtVersion) TextView txtVersion;

    public static final String FILE_NAME = "loc.txt";
    boolean doubleBack = false;

    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    SimpleDateFormat dateFormat;
    String loc, uid;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        txtVersion.setText(("v " + BuildConfig.VERSION_NAME)); // READ APP VERSION
        uid = FirebaseAuth.getInstance().getUid(); // GET UID USER
        databaseReference = FirebaseDatabase.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dialog = new Dialog(this);

        // CUSTOM TOAST
        Toasty.Config.getInstance()
                .setToastTypeface(ResourcesCompat.getFont(this, R.font.montserratbold))
                .setTextSize(12)
                .apply();

        getLoc();
        greeting();
    }

    // AMBIL DATA LOKASI
    public void getLoc() {

        // MEMBACA DATA YG ADA DI INTERNAL STROAGE
        try {
            FileInputStream fileInputStream = openFileInput(FILE_NAME); // DATA YG INGIN DIBACA
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream); // MENCARI DATA DI INTERNAL STORAGE
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader); // MEMBACA FILE DARI INPUT STREAM READER
            StringBuilder stringBuilder = new StringBuilder(); // MENGKONSTRUKSI STRING
            String data;

            while ((data = bufferedReader.readLine()) != null) {
                stringBuilder.append(data);
            }
            loc = stringBuilder.toString();
            getCheck();
        } catch (IOException e) {
            e.printStackTrace();

            Toasty.custom(this, "Pilih lokasi terlebih dahulu",
                    getResources().getDrawable(R.drawable.ic_info_outline_white_24dp),
                    getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary),
                    Toasty.LENGTH_LONG, true, true).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == imgMenu) {
            dlMenu.openDrawer(GravityCompat.START);
        } else if (v == imgCancel) {
            dlMenu.closeDrawer(GravityCompat.START);
        } else if (v == lnAbout) {
            startActivity(new Intent(this, AboutActivity.class));
            CustomIntent.customType(this, "right-to-left");
            new Handler().postDelayed(() -> dlMenu.closeDrawer(GravityCompat.START), 1200);
        } else if (loc != null) {
            if (v == imgLogin) {
                if (uid != null) {
                    dialog.show();
                    dialog.setContentView(R.layout.dialog_signout);
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                    this.finish();
                    CustomIntent.customType(this, "left-to-right");
                }
            } else if (v == cvCheckin) {
                startActivity(new Intent(this, CheckinActivity.class).putExtra("Loc", loc));
                CustomIntent.customType(this, "bottom-to-up");
            } else if (v == cvCheckout) {
                startActivity(new Intent(this, CheckoutActivity.class).putExtra("Loc", loc));
                CustomIntent.customType(this, "bottom-to-up");
            } else if (v == lnLoc) {
                if (uid != null) {
                    startActivity(new Intent(this, LocationActivity.class));
                    this.finish();
                    CustomIntent.customType(this, "right-to-left");
                    new Handler().postDelayed(() -> dlMenu.closeDrawer(GravityCompat.START), 1200);
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                    this.finish();
                    CustomIntent.customType(this, "left-to-right");
                    new Handler().postDelayed(() -> dlMenu.closeDrawer(GravityCompat.START), 1200);
                }
            } else if (v == lnData) {
                startActivity(new Intent(this, VisitorDataActivity.class).putExtra("Loc", loc));
                CustomIntent.customType(this, "right-to-left");
                new Handler().postDelayed(() -> dlMenu.closeDrawer(GravityCompat.START), 1200);
            }
        } else {
            startActivity(new Intent(this,  LocationActivity.class));
            this.finish();
            CustomIntent.customType(this, "right-to-left");
            new Handler().postDelayed(() -> dlMenu.closeDrawer(GravityCompat.START), 1200);
        }
    }

    // SET WAKTU GREETING
    private void greeting() {
        Calendar calendar = Calendar.getInstance();
        int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 0 && timeOfDay < 12) {
            txtGreeting.setText("Selamat\nPagi.");
        } else if (timeOfDay >= 12 && timeOfDay < 15) {
            txtGreeting.setText("Selamat\nSiang.");
        } else if (timeOfDay >= 15 && timeOfDay < 18) {
            txtGreeting.setText("Selamat\nSore.");
        } else if (timeOfDay >= 18 && timeOfDay < 24) {
            txtGreeting.setText("Selamat\nMalam.");
        }
    }

    // CEK DATA CHECKIN & CHECKOUT
    public void getCheck() {

        if (uid != null) {
            lnCheckedIn.setVisibility(View.VISIBLE);
            lnCheckedOut.setVisibility(View.VISIBLE);
        } else {
            lnCheckedIn.setVisibility(View.GONE);
            lnCheckedOut.setVisibility(View.GONE);
        }

        // CHECKED IN
        databaseReference.child(loc).child("Visitor").child(dateFormat.format(new Date()))
                .orderByChild("status").equalTo("Checked In").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            txtCheckedIn.setText(String.valueOf(snapshot.getChildrenCount()) + " Visitor");
                        } else {
                            txtCheckedIn.setText("0 Visitor");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toasty.error(MainActivity.this, error.getMessage().toString(),
                                Toasty.LENGTH_SHORT, true).show();
                    }
                });

        // CHECKED OUT
        databaseReference.child(loc).child("Visitor").child(dateFormat.format(new Date()))
                .orderByChild("status").equalTo("Checked Out").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            txtCheckedOut.setText(String.valueOf(snapshot.getChildrenCount()) + " Visitor");
                        } else {
                            txtCheckedOut.setText("0 Visitor");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toasty.error(MainActivity.this, error.getMessage().toString(),
                                Toasty.LENGTH_SHORT, true).show();
                    }
                });
    }

    // BTN SIGNOUT
    public void btnSignout(View v) {

        dialog.dismiss();
        FirebaseAuth.getInstance().signOut(); // HAPUS SESSION
        startActivity(getIntent());
        this.finish();
        CustomIntent.customType(this, "fadein-to-fadeout");
    }

    // BTN BATAL
    public void btnBatal(View v) {

        dialog.dismiss();
    }

    // BTN BACK
    public void onBackPressed() {

        if (doubleBack) {
            super.onBackPressed();
            return;
        }

        this.doubleBack = true;
        Toasty.custom(this, "Tekan sekali lagi untuk keluar",
                getResources().getDrawable(R.drawable.ic_info_outline_white_24dp),
                getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary),
                Toasty.LENGTH_SHORT, true, true).show();

        new Handler().postDelayed(() -> doubleBack = false, 2000);
    }
}