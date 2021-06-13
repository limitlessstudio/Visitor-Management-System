package com.pegadaian.vms.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.print.PrintHelper;

import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.pegadaian.vms.R;
import com.pegadaian.vms.api.JavaMailAPI;
import com.pegadaian.vms.model.HostData;
import com.pegadaian.vms.model.RfidData;
import com.pegadaian.vms.model.VisitorData;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import maes.tech.intentanim.CustomIntent;

public class CheckinActivity extends AppCompatActivity {

    @BindView(R.id.imgAddFoto) ImageView imgAddFoto;
    @BindView(R.id.imgAddQr) ImageView imgAddQr;
    @BindView(R.id.txtAmbil) TextView txtAmbil;
    @BindView(R.id.tilNama) TextInputLayout tilNama;
    @BindView(R.id.etNama) TextInputEditText etNama;
    @BindView(R.id.tilInstansi) TextInputLayout tilInstansi;
    @BindView(R.id.etInstansi) TextInputEditText etInstansi;
    @BindView(R.id.tilTelp) TextInputLayout tilTelp;
    @BindView(R.id.etTelp) TextInputEditText etTelp;
    @BindView(R.id.tilEmailVis) TextInputLayout tilEmailVis;
    @BindView(R.id.etEmailVis) TextInputEditText etEmail;
    @BindView(R.id.spHost) MaterialSpinner spHost;
    @BindView(R.id.tilTujuan) TextInputLayout tilTujuan;
    @BindView(R.id.etTujuan) TextInputEditText etTujuan;
    @BindView(R.id.tilRfid) TextInputLayout tilRfid;
    @BindView(R.id.etRfid) TextInputEditText etRfid;

    public static final int CAMERA_PERM_CODE = 19;
    public static final int CAMERA_REQ_CODE = 45;
    public static final String FILE_NAME = "loc.txt";

    List<String> hostDataList;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    Bundle bundleLoc;
    ProgressDialog progressDialog;
    SimpleDateFormat clockFormat, dateFormat;
    String uid, dataFoto, dataQr, loc, email;
    Query query;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_checkin);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        clockFormat = new SimpleDateFormat("HH:mm");
        progressDialog = new ProgressDialog(this);
        dialog = new Dialog(this);

        // CUSTOM TOAST
        Toasty.Config.getInstance()
                .setToastTypeface(ResourcesCompat.getFont(this, R.font.montserratbold))
                .setTextSize(12)
                .apply();

        getLoc();
        cekInput();
    }

    // BTN BACK
    public void btnBackCheckin(View v) {

        this.finish();
        CustomIntent.customType(this, "up-to-bottom");
    }

    // AMBIL DATA LOKASI
    public void getLoc() {

        bundleLoc = getIntent().getExtras();
        if (bundleLoc != null) {
            loc = bundleLoc.getString("Loc");
            getHost();
            getRfid();
        } else {
            this.finish();
            Toasty.custom(this, "Pilih lokasi terlebih dahulu",
                    getResources().getDrawable(R.drawable.ic_info_outline_white_24dp),
                    getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary),
                    Toasty.LENGTH_SHORT, true, true).show();
        }
    }

    // AMBIL DATA HOST
    public void getHost() {

        // SET SPINNER
        hostDataList = new ArrayList<>();
        spHost.setItems(hostDataList);
        spHost.setBackgroundResource(R.drawable.bg_spinner);
        spHost.getPopupWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_spinner));

        query = databaseReference.child(loc).child("Host");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {

                        HostData hostData = itemSnapshot.getValue(HostData.class);
                        hostDataList.add(hostData.getNama());
                    }
                } else {
                    spHost.setText("Data not found");
                    spHost.setClickable(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // NOTHING
            }
        });
    }

    // AMBIL DATA RFID
    public void getRfid() {

        query = databaseReference.child(loc).child("RFID")
                .orderByChild("status").equalTo("2");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                etRfid.setText(null);
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {

                        String nomor = itemSnapshot.getKey();
                        etRfid.setText(nomor);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // NOTHING
            }
        });
    }

    // BTN AMBIL FOTO
    public void btnAmbilFoto(View V) {

        // CEK PERMISSION KAMERA
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            openCamera();
        }
    }

    // MEMINTA PERMISSION KAMERA
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toasty.custom(this, "Izin kamera dibutuhkan",
                        getResources().getDrawable(R.drawable.ic_info_outline_white_24dp),
                        getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary),
                        Toasty.LENGTH_SHORT, true, true).show();
            }
        }
    }

    // BUKA KAMERA
    public void openCamera() {

        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, CAMERA_REQ_CODE);
    }

    // SETELAH MELAKUKAN FOTO, MAKA...
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQ_CODE && resultCode == RESULT_OK) {
            Bitmap imageVisitor = (Bitmap) data.getExtras().get("data");
            imgAddFoto.setImageBitmap(imageVisitor);
            txtAmbil.setText("Ubah");
        } else {
            if (imgAddFoto.getDrawable() == null) {
                Toasty.error(this, "Foto belum diambil",
                        Toast.LENGTH_SHORT, true).show();
            } else {
                Toasty.custom(this, "Foto tidak diubah",
                        getResources().getDrawable(R.drawable.ic_info_outline_white_24dp),
                        getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary),
                        Toasty.LENGTH_SHORT, true, true).show();
            }
        }
    }

    // CEK INPUT EDIT TEXT
    public void cekInput() {
        // CEK NAMA
        etNama.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    tilNama.setError("    Nama harus diisi");
                } else {
                    tilNama.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        // CEK INSTANSI
        etInstansi.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    tilInstansi.setError("    Instansi harus diisi");
                } else {
                    tilInstansi.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        // CEK TELP
        etTelp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    tilTelp.setError("    No. Telp harus diisi");
                } else {
                    tilTelp.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        // CEK EMAIL
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    tilEmailVis.setError("    Email harus diisi");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(charSequence).matches()) {
                    tilEmailVis.setError("    Email tidak valid");
                } else {
                    tilEmailVis.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        // CEK HOST
        spHost.getPopupWindow().setOnDismissListener(() -> {
            if (spHost.getText().toString().isEmpty()) {
                spHost.setError("    Host harus diisi");
            } else {
                spHost.setError(null);

                // GET EMAIL HOST
                query = databaseReference.child(loc).child("Host").orderByChild("nama").equalTo(spHost.getText().toString());
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {

                            HostData hostData = itemSnapshot.getValue(HostData.class);
                            email = hostData.getEmail();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // NOTHING
                    }
                });
            }
        });
        // CEK TUJUAN
        etTujuan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    tilTujuan.setError("    Tujuan harus diisi");
                } else {
                    tilTujuan.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    // BTN COBA KONEKSI
    public void btnCobaKoneksi(View v) {

        dialog.dismiss();
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        new Handler().postDelayed(() -> {
            progressDialog.dismiss();
            btnSimpan(v);
        }, 500);
    }

    // BTN SIMPAN / CHECKIN
    public void btnSimpan(View v) {

        // CEK KONEKSI INTERNET
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            dialog.dismiss();
            if (imgAddFoto.getDrawable() == null) {
                Toasty.error(this, "Foto belum diambil",
                        Toast.LENGTH_SHORT, true).show();
            } else if (etNama.getText().toString().isEmpty()) {
                etNama.setFocusable(true);
                tilNama.setError("    Nama harus diisi");
            } else if (etInstansi.getText().toString().isEmpty()) {
                etInstansi.setFocusable(true);
                tilInstansi.setError("    Instansi harus diisi");
            } else if (etTelp.getText().toString().isEmpty()) {
                etTelp.setFocusable(true);
                tilTelp.setError("    No. Telp harus diisi");
            } else if (etEmail.getText().toString().isEmpty()) {
                etEmail.setFocusable(true);
                tilEmailVis.setError("    Email harus diisi");
            } else if (spHost.getText().toString().isEmpty()) {
                spHost.setFocusable(true);
                spHost.setError("    Host harus diisi");
            } else if (etTujuan.getText().toString().isEmpty()) {
                etTujuan.setFocusable(true);
                tilTujuan.setError("    Tujuan harus diisi");
            } else if (etRfid.getText().toString().isEmpty()) {
                etRfid.setFocusable(true);
                tilRfid.setError("    Nomor rfid dibutuhkan");
            } else {
                // BUAT UID & UPLOAD DATA VISITOR
                uid = dateFormat.format(new Date()) + clockFormat.format(new Date()) + UUID.randomUUID();
                uploadImageQr();
            }
        } else {
            dialog.show();
            dialog.setContentView(R.layout.dialog_offline);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    // UPLOAD QR KE FIREBASE
    public void uploadImageQr() {

        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // BUAT QR DARI UID
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(uid, BarcodeFormat.QR_CODE, 300, 300);
            Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565);

            // MENGUBAH UKURAN BITMAP QR
            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            imgAddQr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();

            Toasty.error(this, "Qr gagal dibuat",
                    Toast.LENGTH_SHORT, true).show();
        }

        // MENDAPATKAN DATA DARI IMAGE VIEW
        Bitmap bitmapQr = ((BitmapDrawable) imgAddQr.getDrawable()).getBitmap();
        ByteArrayOutputStream streamQr = new ByteArrayOutputStream();

        // KOMPRESS BITMAP KE JPEG
        bitmapQr.compress(Bitmap.CompressFormat.JPEG, 100, streamQr);
        byte[] bytesQr = streamQr.toByteArray();

        // MENENTUKAN FILE PENYIMPANAN
        UploadTask uploadTaskQr = storageReference.child(loc).child("QrImage")
                .child(dateFormat.format(new Date())).child(uid).putBytes(bytesQr);

        uploadTaskQr.addOnSuccessListener(task -> {

            Task<Uri> uriTask = task.getStorage().getDownloadUrl();
            while (!uriTask.isComplete());
            Uri urlImage = uriTask.getResult();
            dataQr = urlImage.toString();
            uploadImageVisitor();
        }).addOnFailureListener(e -> Toasty.error(this, "Qr gagal disimpan",
                Toast.LENGTH_SHORT, true).show());
    }

    // UPLOAD FOTO KE FIREBASE
    public void uploadImageVisitor() {

        // MENDAPATKAN DATA DARI IMAGE VIEW
        Bitmap bitmapVisitor = ((BitmapDrawable) imgAddFoto.getDrawable()).getBitmap();
        ByteArrayOutputStream streamVisitor = new ByteArrayOutputStream();

        // KOMPRESS BITMAP KE JPEG
        bitmapVisitor.compress(Bitmap.CompressFormat.JPEG, 100, streamVisitor);
        byte[] bytesVisitor = streamVisitor.toByteArray();

        // MENENTUKAN FILE PENYIMPANAN
        UploadTask uploadTask = storageReference.child(loc).child("VisitorImage")
                .child(dateFormat.format(new Date())).child(uid).putBytes(bytesVisitor);

        uploadTask.addOnSuccessListener(task -> {

            Task<Uri> uriTask = task.getStorage().getDownloadUrl();
            while (!uriTask.isComplete());
            Uri urlImage = uriTask.getResult();
            dataFoto = urlImage.toString();
            uploadVisitor();
        }).addOnFailureListener(e -> Toasty.error(this, "Foto gagal disimpan",
                Toast.LENGTH_SHORT, true).show());
    }

    // UPLOAD DATA VISITOR KE FIREBASE
    public void uploadVisitor() {

        VisitorData visitorData = new VisitorData(etNama.getText().toString(), etInstansi.getText().toString(),
                etTelp.getText().toString(), etEmail.getText().toString(), spHost.getText().toString(),
                etTujuan.getText().toString(), clockFormat.format(new Date()), "", "Checked In",
                dataQr, dataFoto, etRfid.getText().toString());

        databaseReference.child(loc).child("Visitor").child(dateFormat.format(new Date()))
                .child(uid).setValue(visitorData).addOnCompleteListener(task -> {

                    // UPDATE DATA RFID
                    RfidData rfidData = new RfidData(uid, dateFormat.format(new Date()), "3");
                    databaseReference.child(loc).child("RFID").child(etRfid.getText().toString())
                            .setValue(rfidData).addOnCompleteListener(task2 -> {

                                sendEmailVis();
                                sendEmailHost();
                                progressDialog.dismiss();
                                this.finish();
                                CustomIntent.customType(this, "up-to-bottom");
                                Toasty.success(this, "Data telah tersimpan",
                                        Toast.LENGTH_SHORT, true).show();

                            }).addOnFailureListener(e -> Toasty.error(this, "Rfid gagal diupdate",
                            Toast.LENGTH_SHORT, true).show());
                }).addOnFailureListener(e -> Toasty.error(this, "Data gagal disimpan",
                Toast.LENGTH_SHORT, true).show());
    }

    // KIRIM PESAN KE EMAIL VISITOR
    public void sendEmailVis() {

        String email = etEmail.getText().toString(); // EMAIL PENERIMA
        String subject = "SELAMAT DATANG";
        String message = "Halo, " + etNama.getText().toString() + "\n\nAnda telah masuk area " +
                loc + " Pegadaian dengan tujuan " + etTujuan.getText().toString() + ", ingin bertemu dengan Bapak/Ibu " +
                spHost.getText().toString();

        JavaMailAPI javaMailAPI = new JavaMailAPI(this, email, subject, message);
        javaMailAPI.execute();
    }

    // KIRIM PESAN KE EMAIL HOST
    public void sendEmailHost() {

        // KIRIM PESAN
        String subject = "TAMU ANDA";
        String message = "Halo, " + spHost.getText().toString() + "\n\nTamu anda yang bernama "
                + etNama.getText().toString() + " sedang menunggu di " + loc + " Pegadaian dengan tujuan "
                + etTujuan.getText().toString();

        JavaMailAPI javaMailAPI = new JavaMailAPI(this, email, subject, message);
        javaMailAPI.execute();
    }

    // BTN BACK
    public void onBackPressed() {

        this.finish();
        CustomIntent.customType(this, "up-to-bottom");
    }
}
