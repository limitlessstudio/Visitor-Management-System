package com.pegadaian.vms.adapter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pegadaian.vms.R;
import com.pegadaian.vms.activity.CheckoutActivity;
import com.pegadaian.vms.activity.VisitorDetailActivity;
import com.pegadaian.vms.model.VisitorData;

import java.util.ArrayList;
import java.util.List;

import maes.tech.intentanim.CustomIntent;

public class VisitorAdapter extends RecyclerView.Adapter<VisitorAdapter.VisitorViewHolder> {

    private Context context;
    private List<VisitorData> visitorDataList;
    private String date;
    private int lastPosition = -1;

    public VisitorAdapter(Context context, List<VisitorData> visitorDataList, String date) {

        this.context = context;
        this.visitorDataList = visitorDataList;
        this.date = date;
    }

    // DATA VIEW HOLDER
    @Override
    public VisitorAdapter.VisitorViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View mView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_visitor, viewGroup, false);

        return new VisitorAdapter.VisitorViewHolder(mView);
    }

    // MENDAPATKAN DATA LOKASI UNTUK DITAMPILKAN
    @Override
    public void onBindViewHolder(@NonNull final VisitorAdapter.VisitorViewHolder visitorViewHolder, int i) {

        Glide.with(context)
                .load(visitorDataList.get(i).getFoto())
                .into(visitorViewHolder.imgVisitor);

        visitorViewHolder.txtNama.setText(visitorDataList.get(i).getNama());
        visitorViewHolder.txtInstansi.setText(visitorDataList.get(i).getInstansi());
        visitorViewHolder.txtTelpVis.setText(visitorDataList.get(i).getTelp());
        visitorViewHolder.txtTujuan.setText(visitorDataList.get(i).getTujuan());

        visitorViewHolder.cvVisitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, VisitorDetailActivity.class);
                intent.putExtra("Foto", visitorDataList.get(visitorViewHolder.getAdapterPosition()).getFoto());
                intent.putExtra("Nama", visitorDataList.get(visitorViewHolder.getAdapterPosition()).getNama());
                intent.putExtra("Email", visitorDataList.get(visitorViewHolder.getAdapterPosition()).getEmail());
                intent.putExtra("Instansi", visitorDataList.get(visitorViewHolder.getAdapterPosition()).getInstansi());
                intent.putExtra("Telp", visitorDataList.get(visitorViewHolder.getAdapterPosition()).getTelp());
                intent.putExtra("Host", visitorDataList.get(visitorViewHolder.getAdapterPosition()).getHost());
                intent.putExtra("Tujuan", visitorDataList.get(visitorViewHolder.getAdapterPosition()).getTujuan());
                intent.putExtra("Qr", visitorDataList.get(visitorViewHolder.getAdapterPosition()).getQr());
                intent.putExtra("Checkin", visitorDataList.get(visitorViewHolder.getAdapterPosition()).getCheckin());
                intent.putExtra("Checkout", visitorDataList.get(visitorViewHolder.getAdapterPosition()).getCheckout());
                intent.putExtra("Rfid", visitorDataList.get(visitorViewHolder.getAdapterPosition()).getRfid());
                intent.putExtra("Key", visitorDataList.get(visitorViewHolder.getAdapterPosition()).getKey());
                intent.putExtra("Date", date);
                context.startActivity(intent);
                CustomIntent.customType(context, "right-to-left");
            }
        });

        setAnimation(visitorViewHolder.itemView, i);
    }

    // ANIMASI LOAD DATA
    public void setAnimation(View viewToAnimate, int position) {

        if (position > lastPosition) {

            ScaleAnimation animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(500);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return visitorDataList.size();
    }

    public void filteredList(ArrayList<VisitorData> filterList) {

        visitorDataList = filterList;

        notifyDataSetChanged();
    }

    public class VisitorViewHolder extends RecyclerView.ViewHolder {

        CardView cvVisitor;
        ImageView imgVisitor;
        TextView txtNama, txtInstansi, txtTelpVis, txtTujuan;

        public VisitorViewHolder(@NonNull View itemView) {
            super(itemView);

            cvVisitor = itemView.findViewById(R.id.cvVisitor);
            imgVisitor = itemView.findViewById(R.id.imgVisitor);
            txtNama = itemView.findViewById(R.id.txtNama);
            txtInstansi = itemView.findViewById(R.id.txtInstansi);
            txtTelpVis = itemView.findViewById(R.id.txtTelpVis);
            txtTujuan = itemView.findViewById(R.id.txtTujuan);
        }
    }
}
