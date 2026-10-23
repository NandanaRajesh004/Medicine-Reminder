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

import com.google.android.material.button.MaterialButton;
import com.medicinereminder.app.R;
import com.medicinereminder.app.model.DoseRecord;

import java.util.ArrayList;
import java.util.List;

public class DoseTodayAdapter extends RecyclerView.Adapter<DoseTodayAdapter.ViewHolder> {

    public interface OnDoseActionListener {
        void onTakeDose(DoseRecord record, int position);
        void onSkipDose(DoseRecord record, int position);
        void onUndoDose(DoseRecord record, int position);
        void onDoseClicked(DoseRecord record);
    }

    private final Context context;
    private List<DoseRecord> list = new ArrayList<>();
    private final OnDoseActionListener listener;

    public DoseTodayAdapter(Context context, OnDoseActionListener listener) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_dose_today, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoseRecord item = list.get(position);

        holder.tvMedicineName.setText(item.getMedicineName());
        holder.tvDosage.setText(item.getDosage() != null ? item.getDosage() : "");
        holder.tvScheduledTime.setText(item.getFormattedScheduledTime());

        String food = item.getFoodRelation();
        if ("BEFORE_MEAL".equals(food)) {
            holder.tvFoodInstruction.setText("Before Meal");
        } else if ("AFTER_MEAL".equals(food)) {
            holder.tvFoodInstruction.setText("After Meal");
        } else if ("WITH_MEAL".equals(food)) {
            holder.tvFoodInstruction.setText("With Meal");
        } else {
            holder.tvFoodInstruction.setText("Anytime");
        }

        // Icon & colors
        holder.ivMedicineIcon.setImageResource(item.getFormIconRes());
        holder.ivMedicineIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, item.getFormColorRes())));
        holder.iconContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, item.getFormBgColorRes())));

        // Status Styling & Button configurations
        String status = item.getStatus();
        if (DoseRecord.STATUS_TAKEN.equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setText("TAKEN");
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_taken));
            holder.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_taken_bg)));

            holder.btnSkipDose.setVisibility(View.GONE);
            holder.btnTakeDose.setText("Undo");
            holder.btnTakeDose.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_close));
            holder.btnTakeDose.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_secondary)));
            holder.btnTakeDose.setOnClickListener(v -> {
                if (listener != null) listener.onUndoDose(item, holder.getAdapterPosition());
            });

        } else if (DoseRecord.STATUS_SKIPPED.equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setText("SKIPPED");
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_skipped));
            holder.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_skipped_bg)));

            holder.btnSkipDose.setVisibility(View.GONE);
            holder.btnTakeDose.setText("Undo");
            holder.btnTakeDose.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_close));
            holder.btnTakeDose.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_secondary)));
            holder.btnTakeDose.setOnClickListener(v -> {
                if (listener != null) listener.onUndoDose(item, holder.getAdapterPosition());
            });

        } else {
            // PENDING or MISSED
            if (item.getScheduledTimeMillis() < System.currentTimeMillis() - (30 * 60 * 1000L)) {
                holder.tvStatusBadge.setText("MISSED");
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_missed));
                holder.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_missed_bg)));
            } else {
                holder.tvStatusBadge.setText("PENDING");
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_pending));
                holder.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_pending_bg)));
            }

            holder.btnSkipDose.setVisibility(View.VISIBLE);
            holder.btnSkipDose.setOnClickListener(v -> {
                if (listener != null) listener.onSkipDose(item, holder.getAdapterPosition());
            });

            holder.btnTakeDose.setText("Take Dose");
            holder.btnTakeDose.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_check));
            holder.btnTakeDose.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary)));
            holder.btnTakeDose.setOnClickListener(v -> {
                if (listener != null) listener.onTakeDose(item, holder.getAdapterPosition());
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDoseClicked(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconContainer;
        ImageView ivMedicineIcon;
        TextView tvMedicineName;
        TextView tvDosage;
        TextView tvStatusBadge;
        TextView tvScheduledTime;
        TextView tvFoodInstruction;
        MaterialButton btnSkipDose;
        MaterialButton btnTakeDose;

        ViewHolder(View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.icon_container);
            ivMedicineIcon = itemView.findViewById(R.id.iv_medicine_icon);
            tvMedicineName = itemView.findViewById(R.id.tv_medicine_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvScheduledTime = itemView.findViewById(R.id.tv_scheduled_time);
            tvFoodInstruction = itemView.findViewById(R.id.tv_food_instruction);
            btnSkipDose = itemView.findViewById(R.id.btn_skip_dose);
            btnTakeDose = itemView.findViewById(R.id.btn_take_dose);
        }
    }
}
