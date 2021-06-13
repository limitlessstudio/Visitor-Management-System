package com.pegadaian.vms.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pegadaian.vms.R;
import com.pegadaian.vms.model.LocationData;
import com.pegadaian.vms.utils.RecyclerViewClickListener;

import java.util.ArrayList;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<LocationData> locationDataList;
    private int lastPosition = -1;
    private RecyclerViewClickListener itemListener;

    public LocationAdapter(List<LocationData> locationDataList, RecyclerViewClickListener itemListener) {

        this.locationDataList = locationDataList;
        this.itemListener = itemListener;
    }

    // DATA VIEW HOLDER
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View mView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_location, viewGroup, false);

        return new LocationViewHolder(mView);
    }

    // MENDAPATKAN DATA LOKASI UNTUK DITAMPILKAN
    @Override
    public void onBindViewHolder(@NonNull final LocationViewHolder locationViewHolder, int i) {

        locationViewHolder.txtLokasi.setText(locationDataList.get(i).getNama());
        locationViewHolder.txtAlamat.setText(locationDataList.get(i).getAlamat());
        locationViewHolder.txtTelp.setText(locationDataList.get(i).getTelp());

        setAnimation(locationViewHolder.itemView, i);
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
        return locationDataList.size();
    }

    public void filteredList(ArrayList<LocationData> filterList) {

        locationDataList = filterList;
        notifyDataSetChanged();
    }

    public class LocationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtLokasi, txtAlamat, txtTelp;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);

            txtLokasi = itemView.findViewById(R.id.txtLokasi);
            txtAlamat = itemView.findViewById(R.id.txtAlamat);
            txtTelp = itemView.findViewById(R.id.txtTelpLoc);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            itemListener.recyclerViewListClicked(v, txtLokasi.getText().toString());
        }
    }
}
