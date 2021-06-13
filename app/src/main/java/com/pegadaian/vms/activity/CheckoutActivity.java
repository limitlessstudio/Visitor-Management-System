package com.pegadaian.vms.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pegadaian.vms.R;
import com.pegadaian.vms.model.RfidData;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import maes.tech.intentanim.CustomIntent;

public class CheckoutActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {

    @BindView(R.id.qrReader) QRCodeReaderView qrReader;

    public static final int CAMERA_PERM_CODE = 19;
    public static final String FILE_NAME = "loc.txt";

    DatabaseReference databaseReference;
    Bundle bundleLoc;
    ProgressDialog progressDialog;
    SimpleDateFormat clockFormat, dateFormat;
    ConnectivityManager connectivityManager;
    String loc, rfid;
    Query query;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_checkout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        clockFormat = new SimpleDateFormat("HH:mm");
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        progressDialog = new ProgressDialog(this);
        dialog = new Dialog(this);

        // MEMINTA PERMISSION KAMERA
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }

        // SET QR
        qrReader.setOnQRCodeReadListener(this);
        qrReader.setQRDecodingEnabled(true);
        qrReader.setAutofocusInterval(2000);
        qrReader.setBackCamera();

        // CUSTOM TOAST
        Toasty.Config.getInstance()
                .setToastTypeface(ResourcesCompat.getFont(this, R.font.montserratbold))
                .setTextSize(12)
                .apply();

        getLoc();
    }

    // BTN BACK
    public void btnBackCheckout(View v) {

        this.finish();
        CustomIntent.customType(this, "up-to-bottom");
    }

    // AMBIL DATA LOKASI
    public void getLoc() {

        bundleLoc = getIntent().getExtras();
        if (bundleLoc != null) {
            loc = bundleLoc.getString("Loc");
        } else {
            finish();
            Toasty.custom(this, "Pilih lokasi terlebih dahulu",
                    getResources().getDrawable(R.drawable.ic_info_outline_white_24dp),
                    getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary),
                    Toasty.LENGTH_SHORT, true, true).show();
        }
    }

    // CEK PERMISSION KAMERA
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.finish();
                CustomIntent.customType(this, "up-to-bottom");
                startActivity(getIntent());
            } else {
                this.finish();
                CustomIntent.customType(this, "up-to-bottom");
                Toasty.custom(this, "Izin kamera dibutuhkan",
                        getResources().getDrawable(R.drawable.ic_info_outline_white_24dp),
                        getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary),
                        Toasty.LENGTH_SHORT, true, true).show();
            }
        }
    }

    // BTN COBA KONEKSI
    public void btnCobaKoneksi(View v) {

        dialog.dismiss();
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            new Handler().postDelayed(() -> progressDialog.dismiss(), 500);
        } else {
            new Handler().postDelayed(() -> {
                progressDialog.dismiss();
                dialog.show();
                dialog.setContentView(R.layout.dialog_offline);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }, 500);
        }
    }

    // MEMBACA QR CODE
    @Override
    public void onQRCodeRead(String text, PointF[] points) {

        // CEK KONEKSI INTERNET
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            dialog.dismiss();

            databaseReference.child(loc).child("Visitor").child(dateFormat.format(new Date())).child(text)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {
                                rfid = snapshot.child("rfid").getValue().toString();
                                databaseReference.child(loc).child("Visitor").child(dateFormat.format(new Date()))
                                        .child(text).child("checkout").setValue(clockFormat.format(new Date()))
                                        .addOnSuccessListener(task -> {
                                            databaseReference.child(loc).child("Visitor").child(dateFormat.format(new Date()))
                                                    .child(text).child("status").setValue("Checked Out");
                                            updateRfid();
                                        }).addOnFailureListener(e -> {
                                            CheckoutActivity.this.finish();
                                            CustomIntent.customType(CheckoutActivity.this, "up-to-bottom");
                                            Toasty.error(CheckoutActivity.this, "Checkout gagal",
                                                    Toast.LENGTH_SHORT, true).show();
                                        });
                            } else {
                                CheckoutActivity.this.finish();
                                CustomIntent.customType(CheckoutActivity.this, "up-to-bottom");
                                Toasty.error(CheckoutActivity.this,"Checkout kedaluwarsa",
                                        Toast.LENGTH_SHORT, true).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // NOTHING
                        }
                    });
            qrReader.stopCamera(); // STOP KAMERA, JIKA QR BERHASIL DI SCAN
        } else {
            dialog.show();
            dialog.setContentView(R.layout.dialog_offline);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrReader.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrReader.stopCamera();
    }

    // UPDATE DATA RFID
    public void updateRfid() {

        RfidData rfidData = new RfidData("", "", "1");
        databaseReference.child(loc).child("RFID").child(rfid).setValue(rfidData)
                .addOnSuccessListener(task -> {
                    this.finish();
                    CustomIntent.customType(CheckoutActivity.this, "up-to-bottom");
                    Toasty.success(CheckoutActivity.this, "Checkout berhasil",
                            Toast.LENGTH_SHORT, true).show();
                }).addOnFailureListener(e -> {
                    this.finish();
                    CustomIntent.customType(CheckoutActivity.this, "up-to-bottom");
                    Toasty.error(this, "Rfid gagal diupdate",
                            Toast.LENGTH_SHORT, true).show();
                });
    }

    // BTN BACK
    public void onBackPressed() {

        this.finish();
        CustomIntent.customType(this, "up-to-bottom");
    }
}
