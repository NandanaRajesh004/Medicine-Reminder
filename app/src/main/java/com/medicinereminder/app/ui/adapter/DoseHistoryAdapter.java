package com.medicinereminder.app.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.medicinereminder.app.R;
import com.medicinereminder.app.model.DoseRecord;
import com.medicinereminder.app.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class DoseHistoryAdapter extends RecyclerView.Adapter<DoseHistoryAdapter.ViewHolder> {

    public interface OnHistoryItemClickListener {
        void onItemClick(DoseRecord record);
    }

    private final Context context;
    private List<DoseRecord> list = new ArrayList<>();
    private final OnHistoryItemClickListener listener;

    public DoseHistoryAdapter(Context context, OnHistoryItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setList(List<DoseRecord> items) {
        this.list = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dose_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoseRecord item = list.get(position);

        holder.tvMedicineName.setText(item.getMedicineName());

        String displayDate = item.getDateStr();
        if (DateTimeUtils.getTodayDateString().equals(displayDate)) {
            displayDate = "Today (" + displayDate + ")";
        }
        holder.tvDosageAndDate.setText(item.getDosage() + " • " + displayDate);

        String scheduled12 = item.getFormattedScheduledTime();
        if (item.isTaken()) {
            String actualTime12 = DateTimeUtils.formatDate(item.getActualTimeMillis(), "hh:mm a");
            holder.tvTimeDetails.setText("Scheduled: " + scheduled12 + " • Taken at " + actualTime12);

            holder.tvStatusBadge.setText("TAKEN");
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_taken));
            holder.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_taken_bg)));

            holder.ivStatusIcon.setImageResource(R.drawable.ic_check);
            holder.ivStatusIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_taken)));
            holder.statusIconContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_taken_bg)));

        } else if (item.isSkipped()) {
            holder.tvTimeDetails.setText("Scheduled: " + scheduled12 + " • Dose skipped");

            holder.tvStatusBadge.setText("SKIPPED");
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_skipped));
            holder.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_skipped_bg)));

            holder.ivStatusIcon.setImageResource(R.drawable.ic_close);
            holder.ivStatusIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_skipped)));
            holder.statusIconContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_skipped_bg)));

        } else {
            holder.tvTimeDetails.setText("Scheduled: " + scheduled12 + " • Dose missed");

            holder.tvStatusBadge.setText("MISSED");
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_missed));
            holder.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_missed_bg)));

            holder.ivStatusIcon.setImageResource(R.drawable.ic_time);
            holder.ivStatusIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_missed)));
            holder.statusIconContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_missed_bg)));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout statusIconContainer;
        ImageView ivStatusIcon;
        TextView tvMedicineName;
        TextView tvDosageAndDate;
        TextView tvTimeDetails;
        TextView tvStatusBadge;

        ViewHolder(View itemView) {
            super(itemView);
            statusIconContainer = itemView.findViewById(R.id.status_icon_container);
            ivStatusIcon = itemView.findViewById(R.id.iv_status_icon);
            tvMedicineName = itemView.findViewById(R.id.tv_medicine_name);
            tvDosageAndDate = itemView.findViewById(R.id.tv_dosage_and_date);
            tvTimeDetails = itemView.findViewById(R.id.tv_time_details);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
        }
    }
}
