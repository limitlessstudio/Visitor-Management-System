package com.pegadaian.vms.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.text.PageSize;
import com.pegadaian.vms.R;
import com.pegadaian.vms.model.RfidData;

import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import maes.tech.intentanim.CustomIntent;

public class VisitorDetailActivity extends AppCompatActivity {

    @BindView(R.id.imgVisitorDetail) ImageView imgVisitor;
    @BindView(R.id.txtNamaDetail) TextView txtNama;
    @BindView(R.id.txtEmailDetail) TextView txtEmail;
    @BindView(R.id.txtInstansiDetail) TextView txtInstansi;
    @BindView(R.id.txtTelpDetail) TextView txtTelp;
    @BindView(R.id.txtHostDetail) TextView txtHost;
    @BindView(R.id.txtTujuanDetail) TextView txtTujuan;
    @BindView(R.id.imgQrDetail) ImageView imgQr;
    @BindView(R.id.txtCheckinDetail) TextView txtCheckin;
    @BindView(R.id.txtCheckoutDetail) TextView txtCheckout;
    @BindView(R.id.lnPrint) LinearLayout lnPrint;
    @BindView(R.id.imgQrPrint) ImageView imgQrPrint;
    @BindView(R.id.txtNamaPrint) TextView txtNamaPrint;

    public static final String FILE_NAME = "loc.txt";

    DatabaseReference databaseReference;
    Bundle bundleVis;
    ProgressDialog progressDialog;
    String loc, qrUrl, imgUrl, rfid, key, date;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_detailvis);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
        dialog = new Dialog(this);

        getLoc();
    }

    // BTN BACK
    public void btnBackDetail(View v) {

        this.finish();
        CustomIntent.customType(this, "left-to-right");
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
            getBundle();
        } catch (IOException e) {
            e.printStackTrace();

            this.finish();
            Toasty.custom(this, "Pilih lokasi terlebih dahulu",
                    getResources().getDrawable(R.drawable.ic_info_outline_white_24dp),
                    getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary),
                    Toasty.LENGTH_LONG, true, true).show();
        }
    }

    // CEK KONEKSI INTERNET
    public void cekKoneksi() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            dialog.dismiss();
        } else {
            dialog.show();
            dialog.setContentView(R.layout.dialog_offline);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    // BTN COBA KONEKSI
    public void btnCobaKoneksi(View v) {

        dialog.dismiss();
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        new Handler().postDelayed(() -> {
            progressDialog.dismiss();
            cekKoneksi();
        }, 500);
    }

    // AMBIL DATA DARI INTENT
    public void getBundle() {

        bundleVis = getIntent().getExtras();
        if (bundleVis != null) {
            Glide.with(this)
                    .load(bundleVis.getString("Foto"))
                    .into(imgVisitor);
            Glide.with(this)
                    .load(bundleVis.getString("Qr"))
                    .into(imgQr);
            Glide.with(this)
                    .load(bundleVis.getString("Qr"))
                    .into(imgQrPrint);

            imgUrl = bundleVis.getString("Foto");
            qrUrl = bundleVis.getString("Qr");
            txtNama.setText(bundleVis.getString("Nama"));
            txtEmail.setText(bundleVis.getString("Email"));
            txtInstansi.setText(bundleVis.getString("Instansi"));
            txtTelp.setText(bundleVis.getString("Telp"));
            txtHost.setText(bundleVis.getString("Host"));
            txtTujuan.setText(bundleVis.getString("Tujuan"));
            txtCheckin.setText(("Checkin : " + bundleVis.getString("Checkin")));

            if (bundleVis.getString("Checkout").equals("")) {
                txtCheckout.setText("Checkout : -");
            } else {
                txtCheckout.setText(("Checkout : " + bundleVis.getString("Checkout")));
            }

            txtNamaPrint.setText(bundleVis.getString("Nama"));
            rfid = bundleVis.getString("Rfid");
            key = bundleVis.getString("Key");
            date = bundleVis.getString("Date");
        }
    }

    // BTN DIALOG HAPUS
    public void btnDialogHapus(View v) {

        cekKoneksi();
        if (!dialog.isShowing()) {
            dialog.show();
            dialog.setContentView(R.layout.dialog_delete);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    // HAPUS DATA VISITOR
    public void btnHapus(View v) {

        dialog.dismiss();

        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        cekKoneksi();
        if (!dialog.isShowing()) {
            deleteQr();
        }
    }

    // BTN BATAL HAPUS
    public void btnBatal(View v) {

        dialog.dismiss();
    }


    // PRINT QR & NAMA VISITOR
    public void btnPrintVis(View v) {

        lnPrint.setDrawingCacheEnabled(true);
        lnPrint.buildDrawingCache();
        Bitmap bitmapVis = lnPrint.getDrawingCache(); // AMBIL GAMBAR DARI LINEAR LAYOUT

        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        photoPrinter.printBitmap("visitor", bitmapVis);
    }

    // DELETE QR
    public void deleteQr() {

        StorageReference qrReference = FirebaseStorage.getInstance().getReferenceFromUrl(qrUrl);

        databaseReference.child(loc).child("Visitor").child(date).child(key).removeValue()
                .addOnSuccessListener(task -> {
                    qrReference.delete().addOnSuccessListener(task2 -> {
                        deleteFoto();
                    }).addOnFailureListener(e -> Toasty.error(this, "Qr gagal dihapus",
                            Toasty.LENGTH_SHORT, true).show());
                }).addOnFailureListener(e -> Toasty.error(this, "Data gagal dihapus",
                Toasty.LENGTH_SHORT, true).show());
    }

    // DELETE FOTO
    public void deleteFoto() {

        RfidData rfidData = new RfidData("", "", "1");
        StorageReference imgReference = FirebaseStorage.getInstance().getReferenceFromUrl(imgUrl);

        imgReference.delete().addOnSuccessListener(task -> {
            databaseReference.child(loc).child("RFID").child(rfid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {
                                if (snapshot.child("uid").getValue().equals(key)) {
                                    databaseReference.child(loc).child("RFID").child(rfid).setValue(rfidData)
                                            .addOnCompleteListener(task2 -> {
                                                progressDialog.dismiss();
                                                VisitorDetailActivity.this.finish();
                                                CustomIntent.customType(VisitorDetailActivity.this, "left-to-right");
                                                Toasty.success(VisitorDetailActivity.this, "Data visitor telah dihapus",
                                                        Toasty.LENGTH_SHORT, true).show();
                                            }).addOnFailureListener(e -> Toasty.error(VisitorDetailActivity.this, "Rfid gagal diupdate",
                                            Toasty.LENGTH_SHORT, true).show());
                                }
                            } else {
                                Toasty.error(VisitorDetailActivity.this, "Rfid tidak ditemukan", Toasty.LENGTH_SHORT, true).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // NOTHING
                        }
                    });
        }).addOnFailureListener(e -> Toasty.error(this, "Foto gagal dihapus",
                Toasty.LENGTH_SHORT, true).show());
    }

    // BTN BACK
    public void onBackPressed() {

        this.finish();
        CustomIntent.customType(this, "left-to-right");
    }
}
