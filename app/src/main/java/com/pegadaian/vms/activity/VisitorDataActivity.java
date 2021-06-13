package com.pegadaian.vms.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.pegadaian.vms.R;
import com.pegadaian.vms.adapter.VisitorAdapter;
import com.pegadaian.vms.model.VisitorData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

public class VisitorDataActivity extends AppCompatActivity {

    @BindView(R.id.imgFilter) ImageView imgFilter;
    @BindView(R.id.txtActiveDate) TextView txtActive;
    @BindView(R.id.etSearch) EditText etSearch;
    @BindView(R.id.imgSearch) ImageView imgSearch;
    @BindView(R.id.imgClear) ImageView imgClear;
    @BindView(R.id.rvVisitor) RecyclerView rvVisitor;
    @BindView(R.id.lnOfflineData) LinearLayout lnOffline;
    @BindView(R.id.lnNoData) LinearLayout lnNoData;

    public static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static int PERMISSION_ALL = 12;
    public static final String FILE_NAME = "loc.txt";

    List<VisitorData> visitorDataList;
    VisitorAdapter visitorAdapter;
    DatabaseReference databaseReference;
    Bundle bundleLoc;
    ProgressDialog progressDialog;
    SimpleDateFormat dateFormat;
    DatePickerDialog datePickerDialog;
    String loc, dateFilter, namaFile;
    Query query;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_visitor);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        progressDialog = new ProgressDialog(this);
        txtActive.setText(dateFormat.format(new Date()));

        // CEK STORAGE PERMISSION
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        // CUSTOM TOAST
        Toasty.Config.getInstance()
                .setToastTypeface(ResourcesCompat.getFont(this, R.font.montserratbold))
                .setTextSize(12)
                .apply();

        getLoc();
        searchVis();
    }

    // BTN BACK
    public void btnBackVis(View v) {

        this.finish();
        CustomIntent.customType(this, "left-to-right");
    }
    // GET PERMISSION

    public boolean hasPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    // AMBIL DATA LOKASI
    public void getLoc() {

        bundleLoc = getIntent().getExtras();
        if (bundleLoc != null) {
            loc = bundleLoc.getString("Loc");
            tampilVis();
        } else {
            this.finish();
            Toasty.custom(this, "Pilih lokasi terlebih dahulu",
                    getResources().getDrawable(R.drawable.ic_info_outline_white_24dp),
                    getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary),
                    Toasty.LENGTH_LONG, true, true).show();
        }
    }

    // BTN FILTER DATA
    public void btnFilter(View v) {

        Calendar today = Calendar.getInstance();

        datePickerDialog = new DatePickerDialog(this, R.style.DatePicker, (datePicker, year, month, day) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, day);
            dateFilter = dateFormat.format(newDate.getTime());
            btnClearVis(v);
            txtActive.setText(dateFilter);
            tampilVis();
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000); // MENAMPILKAN TGL SEBELUM HARI INI
    }

    // CARI VISITOR
    public void searchVis() {
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
                ArrayList<VisitorData> filterList = new ArrayList<>();

                for (VisitorData item : visitorDataList) {
                    if (item.getNama().toLowerCase().contains(s.toString().toLowerCase())) {
                        filterList.add(item);
                        lnNoData.setVisibility(View.GONE);
                    } else {
                        lnNoData.setVisibility(View.VISIBLE);
                    }
                }
                visitorAdapter.filteredList(filterList);
            }
        });
    }

    // BTN CLEAR SEARCH
    public void btnClearVis(View v) {

        etSearch.setText(null);
        etSearch.clearFocus();

        // HIDE KEYBOARD
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(etSearch.getApplicationWindowToken(), 0);
    }

    // BTN COBA KONEKSI
    public void btnCobaKoneksi(View v) {

        tampilVis();
    }

    // MENAMPILKAN DATA VISITOR
    public void tampilVis() {

        lnNoData.setVisibility(View.GONE);

        // MENAMPILKAN CUSTOM PROGRESS DIALOG
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // SET ADAPTER
        visitorDataList = new ArrayList<>();
        visitorAdapter = new VisitorAdapter(this, visitorDataList, txtActive.getText().toString());
        rvVisitor.setAdapter(visitorAdapter);

        // SET LAYOUT
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        rvVisitor.setLayoutManager(linearLayoutManager);

        // CEK KONEKSI INTERNET
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            // MENAMPILKAN DATA BERDASARKAN TANGGAL
            if (dateFilter != null) {
                query = databaseReference.child(loc).child("Visitor").child(dateFilter);
            } else {
                query = databaseReference.child(loc).child("Visitor").child(dateFormat.format(new Date()));
            }

            lnOffline.setVisibility(View.GONE);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    visitorDataList.clear();
                    visitorAdapter.notifyDataSetChanged();

                    if (snapshot.exists()) {

                        lnNoData.setVisibility(View.GONE);
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {

                            VisitorData visitorData = itemSnapshot.getValue(VisitorData.class);
                            visitorData.setKey(itemSnapshot.getKey());
                            visitorDataList.add(visitorData);
                        }
                    } else {
                        new Handler().postDelayed(() -> lnNoData.setVisibility(View.VISIBLE), 500);
                    }
                    new Handler().postDelayed(() -> progressDialog.dismiss(), 500);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                    Toasty.error(VisitorDataActivity.this, error.getMessage().toString(),
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

    // BTN PRINT REPORT
    public void btnPrint(View v) {

        if (visitorDataList.isEmpty()) { // CEK DATA LIST
            Toasty.custom(this, "Data tidak tersedia",
                    getResources().getDrawable(R.drawable.ic_info_outline_white_24dp),
                    getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary),
                    Toasty.LENGTH_SHORT, true, true).show();
        } else if (!hasPermissions(this, PERMISSIONS)) { // CEK STORAGE PERMISSION
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            // SET FOLDER PENYIMPANAN
            File folder = new File(Environment.getExternalStorageDirectory().toString() + "/Visitor Report");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // SET NAMA FILE
            if (dateFilter != null) {
                namaFile = dateFilter + ".pdf";
            } else {
                namaFile = dateFormat.format(new Date()) + ".pdf";
            }
            File output = new File(folder, namaFile);

            // SET DOC
            BaseColor black = CMYKColor.BLACK;
            BaseColor gray = CMYKColor.LIGHT_GRAY;
            Font fontHeader = new Font(Font.FontFamily.HELVETICA, 8.0f, Font.BOLD, black);
            Font fontCell = new Font(Font.FontFamily.HELVETICA, 8.0f, Font.NORMAL, black);
            Document document = new Document(PageSize.A4);

            // SET TABLE
            PdfPTable table = new PdfPTable(new float[]{4, 35, 25, 15});
            table.setWidthPercentage(100);
            table.setTotalWidth(PageSize.A4.getWidth());
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

            Chunk noText = new Chunk("No.", fontHeader);
            PdfPCell noCell = new PdfPCell(new Phrase(noText));

            Chunk namaText = new Chunk("Nama", fontHeader);
            PdfPCell namaCell = new PdfPCell(new Phrase(namaText));

            Chunk tujuanText = new Chunk("Tujuan", fontHeader);
            PdfPCell tujuanCell = new PdfPCell(new Phrase(tujuanText));

            Chunk telpText = new Chunk("No. Telp", fontHeader);
            PdfPCell telpCell = new PdfPCell(new Phrase(telpText));

            table.addCell(noCell);
            table.addCell(namaCell);
            table.addCell(tujuanCell);
            table.addCell(telpCell);
            table.setHeaderRows(1);

            // SET HEADER
            PdfPCell[] cells = table.getRow(0).getCells();
            for (PdfPCell cell : cells) {
                cell.setBackgroundColor(gray);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            }

            // SET DATA
            for (int i = 0; i < visitorDataList.size(); i++) {
                VisitorData data = visitorDataList.get(i);

                String no = String.valueOf(i + 1);
                String nama = data.getNama();
                String tujuan = data.getTujuan();
                String telp = data.getTelp();

                table.addCell(new Phrase(no + ". ", fontCell));
                table.addCell(new Phrase(nama, fontCell));
                table.addCell(new Phrase(tujuan, fontCell));
                table.addCell(new Phrase(telp, fontCell));
            }

            // BUAT & SIMPAN LAPORAN KE INTERNAL STORAGE
            try {
                PdfWriter.getInstance(document, new FileOutputStream(output));
                document.open();
                document.add(table);
                document.close();

                Toasty.success(this, "Laporan berhasil dibuat",
                        Toasty.LENGTH_LONG, true).show();
            } catch (DocumentException | FileNotFoundException e) {
                e.printStackTrace();

                Toasty.error(this, "Laporan gagal dibuat",
                        Toasty.LENGTH_LONG, true).show();
            }
        }
    }

    // BTN BACK
    public void onBackPressed() {

        this.finish();
        CustomIntent.customType(this, "left-to-right");
    }
}
