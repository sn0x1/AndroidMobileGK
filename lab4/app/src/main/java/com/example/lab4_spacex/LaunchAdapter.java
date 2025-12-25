package com.example.lab4_spacex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab4_spacex.model.LaunchItem;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LaunchAdapter extends RecyclerView.Adapter<LaunchAdapter.ViewHolder> {

    private List<LaunchItem> launches = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LaunchItem item);
    }

    public LaunchAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setLaunches(List<LaunchItem> newLaunches) {
        this.launches = newLaunches;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_launch, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LaunchItem item = launches.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return launches.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMission, tvDate;
        ImageView imgPatch;

        ViewHolder(View itemView) {
            super(itemView);
            tvMission = itemView.findViewById(R.id.tvMissionName);
            tvDate = itemView.findViewById(R.id.tvDate);
            imgPatch = itemView.findViewById(R.id.imgPatch);
        }

        void bind(LaunchItem item, OnItemClickListener listener) {
            tvMission.setText(item.missionName);

            // Форматування дати
            Date date = new Date(item.launchDateUnix * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            tvDate.setText(sdf.format(date));

            // Завантаження картинки
            if (item.patchUrl != null && !item.patchUrl.isEmpty()) {
                Picasso.get().load(item.patchUrl).placeholder(android.R.drawable.ic_menu_gallery).into(imgPatch);
            } else {
                imgPatch.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}