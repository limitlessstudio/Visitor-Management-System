package com.pegadaian.vms.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pegadaian.vms.R;
import com.pegadaian.vms.adapter.LocationAdapter;
import com.pegadaian.vms.model.LocationData;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import maes.tech.intentanim.CustomIntent;

public class LocationActivity extends AppCompatActivity {

    @BindView(R.id.txtActiveLoc) TextView txtActive;
    @BindView(R.id.etSearch) EditText etSearch;
    @BindView(R.id.imgSearch) ImageView imgSearch;
    @BindView(R.id.imgClear) ImageView imgClear;
    @BindView(R.id.rvLocation) RecyclerView rvLocation;
    @BindView(R.id.lnOfflineLoc) LinearLayout lnOffline;
    @BindView(R.id.lnNoData) LinearLayout lnNoData;

    public static final String FILE_NAME = "loc.txt";

    List<LocationData> locationDataList;
    LocationAdapter locationAdapter;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_location);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        progressDialog = new ProgressDialog(this);

        // SET ADAPTER
        locationDataList = new ArrayList<>();
        locationAdapter = new LocationAdapter(locationDataList, this::recyclerViewListClicked);
        rvLocation.setAdapter(locationAdapter);

        // SET LAYOUT
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvLocation.setLayoutManager(linearLayoutManager);

        // CUSTOM TOAST
        Toasty.Config.getInstance()
                .setToastTypeface(ResourcesCompat.getFont(this, R.font.montserratbold))
                .setTextSize(12)
                .apply();

        tampilLoc();
        searchLoc();
    }

    // BTN BACK
    public void btnBackLoc(View v) {

        startActivity(new Intent(this, MainActivity.class));
        this.finish();
        CustomIntent.customType(this, "left-to-right");
    }

    // CARI LOKASI
    public void searchLoc() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                imgClear.setVisibility(View.GONE);
                imgSearch.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().isEmpty()) {
                    imgClear.setVisibility(View.GONE);
                    imgSearch.setVisibility(View.VISIBLE);
                } else {
                    imgClear.setVisibility(View.VISIBLE);
                    imgSearch.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                // FILTER DATA
                ArrayList<LocationData> filterList = new ArrayList<>();

                for (LocationData item : locationDataList) {
                    if (item.getNama().toLowerCase().contains(s.toString().toLowerCase())) {
                        filterList.add(item);
                        lnNoData.setVisibility(View.GONE);
                    } else {
                        lnNoData.setVisibility(View.VISIBLE);
                    }
                }
                locationAdapter.filteredList(filterList);
            }
        });
    }

    // BTN CLEAR SEARCH
    public void btnClearLoc(View v) {

        etSearch.setText(null);
        etSearch.clearFocus();

        // HIDE KEYBOARD
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(etSearch.getApplicationWindowToken(), 0);
    }

    // BTN COBA KONEKSI
    public void btnCobaKoneksi(View v) {

        tampilLoc();
    }

    // MENAMPILKAN DATA LOKASI
    public void tampilLoc() {

        // MENAMPILKAN CUSTOM PROGRESS DIALOG
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // MEMBACA DATA YG ADA PADA FILE DI INTERNAL STROAGE
        try {
            FileInputStream fileInputStream = openFileInput(FILE_NAME); // DATA YG INGIN DIBACA
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream); // MENCARI DATA DI INTERNAL STORAGE
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader); // MEMBACA FILE DARI INPUT STREAM READER
            StringBuilder stringBuilder = new StringBuilder(); // MENGKONSTRUKSI STRING
            String data;

            while ((data = bufferedReader.readLine()) != null) {
                stringBuilder.append(data);
            }
            txtActive.setText(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();

            Toasty.custom(this, "Pilih lokasi terlebih dahulu",
                    getResources().getDrawable(R.drawable.ic_info_outline_white_24dp),
                    getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary),
                    Toasty.LENGTH_LONG, true, true).show();
        }

        // CEK KONEKSI INTERNET
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            lnOffline.setVisibility(View.GONE);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    locationDataList.clear();
                    locationAdapter.notifyDataSetChanged();

                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {

                        LocationData locationData = itemSnapshot.getValue(LocationData.class);
                        locationDataList.add(locationData);
                    }
                    new Handler().postDelayed(() -> progressDialog.dismiss(), 500);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                    Toasty.error(LocationActivity.this, error.getMessage().toString(),
                            Toasty.LENGTH_SHORT, true).show();
                }
            });
        } else {
            new Handler().postDelayed(() -> {
                progressDialog.dismiss();
                lnOffline.setVisibility(View.VISIBLE);
            }, 500);
        }
    }

    // LOKASI DIKLIK & DISIMPAN DI INTERNAL STORAGE
    public void recyclerViewListClicked(View v, String lokasi) {

        try {
            FileOutputStream fileOutputStream = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fileOutputStream.write(lokasi.getBytes()); // SIMPAN DATA LOKASI KE INTERNAL STORAGE
            fileOutputStream.close();

            startActivity(new Intent(this, MainActivity.class));
            this.finish();
            CustomIntent.customType(this, "left-to-right");
            Toasty.success(this, "Lokasi diubah ke\n" + lokasi,
                    Toasty.LENGTH_LONG, true).show();
        } catch (IOException e) {
            e.printStackTrace();

            Toasty.error(this, "Lokasi gagal diubah",
                    Toasty.LENGTH_LONG, true).show();
        }
    }

    // BTN BACK
    public void onBackPressed() {

        startActivity(new Intent(this, MainActivity.class));
        this.finish();
        CustomIntent.customType(this, "left-to-right");
    }
}
