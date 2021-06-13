package com.pegadaian.vms.activity;

import android.app.Dialog;
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
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.pegadaian.vms.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import maes.tech.intentanim.CustomIntent;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.tilEmailAdmin) TextInputLayout tilEmail;
    @BindView(R.id.etEmailAdmin) TextInputEditText etEmail;
    @BindView(R.id.tilPassAdmin) TextInputLayout tilPass;
    @BindView(R.id.etPassAdmin) TextInputEditText etPass;

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        dialog = new Dialog(this);

        // CUSTOM TOAST
        Toasty.Config.getInstance()
                .setToastTypeface(ResourcesCompat.getFont(this, R.font.montserratbold))
                .setTextSize(12)
                .apply();

        cekInput();
    }

    // BTN BACK
    public void btnBackLogin(View v) {

        startActivity(new Intent(this, MainActivity.class));
        this.finish();
        CustomIntent.customType(this, "right-to-left");
    }

    public void cekInput() {
        // CEK EMAIL
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    tilEmail.setError("    Email dibutuhkan");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(charSequence).matches()) {
                    tilEmail.setError("    Email tidak valid");
                } else {
                    tilEmail.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        // CEK PASSWORD
        etPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    tilPass.setError("    Password dibutuhkan");
                } else {
                    tilPass.setError(null);
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
            btnLogin(v);
        }, 500);
    }

    // BTN LOGIN
    public void btnLogin(View v) {

        // CEK KONEKSI INTERNET
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            dialog.dismiss();
            if (etEmail.getText().toString().isEmpty()) {
                etEmail.setFocusable(true);
                tilEmail.setError("    Email dibutuhkan");
            } else if (etPass.getText().toString().isEmpty()) {
                etPass.setFocusable(true);
                tilPass.setError("    Password dibutuhkan");
            } else {
                signIn();
            }
        } else {
            dialog.show();
            dialog.setContentView(R.layout.dialog_offline);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    // BTN FORGOT PASSWORD
    public void btnForgot(View v) {

        startActivity(new Intent(this, ForgotPassActivity.class));
        CustomIntent.customType(this, "bottom-to-up");
    }

    // SIGN IN
    public void signIn() {

        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        String email = etEmail.getText().toString();
        String password = etPass.getText().toString();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        startActivity(new Intent(this, MainActivity.class));
                        this.finish();
                        CustomIntent.customType(this, "right-to-left");
                    } else {
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidUserException) {
                        Toasty.error(this, "Email tidak terdaftar",
                                Toasty.LENGTH_SHORT, true).show();
                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        Toasty.error(this, "Password salah",
                                Toasty.LENGTH_SHORT, true).show();
                    }
                });
    }

    // BTN BACK
    public void onBackPressed() {

        startActivity(new Intent(this, MainActivity.class));
        this.finish();
        CustomIntent.customType(this, "right-to-left");
    }
}
