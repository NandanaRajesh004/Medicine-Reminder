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

import com.google.android.material.materialswitch.MaterialSwitch;
import com.medicinereminder.app.R;
import com.medicinereminder.app.model.Medicine;
import com.medicinereminder.app.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class MedicineListAdapter extends RecyclerView.Adapter<MedicineListAdapter.ViewHolder> {

    public interface OnMedicineActionListener {
        void onMedicineClick(Medicine medicine);
        void onMedicineToggleActive(Medicine medicine, boolean isActive, int position);
    }

    private final Context context;
    private List<Medicine> list = new ArrayList<>();
    private final OnMedicineActionListener listener;

    public MedicineListAdapter(Context context, OnMedicineActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setList(List<Medicine> items) {
        this.list = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine item = list.get(position);

        holder.tvMedicineName.setText(item.getName());
        holder.tvDosageForm.setText(item.getDosage() + " • " + item.getForm());

        // Times string formatted to 12h
        List<String> rawTimes = item.getTimesList();
        StringBuilder formattedTimes = new StringBuilder();
        for (int i = 0; i < rawTimes.size(); i++) {
            if (i > 0) formattedTimes.append(", ");
            formattedTimes.append(DateTimeUtils.formatTimeString(rawTimes.get(i)));
        }

        holder.tvFrequencyTimes.setText(item.getFormattedFrequency() + " • " + formattedTimes);

        // Icon
        holder.ivMedicineIcon.setImageResource(item.getFormIconRes());
        holder.ivMedicineIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, item.getFormColorRes())));
        holder.iconContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, item.getFormBgColorRes())));

        // Switch
        holder.switchActive.setOnCheckedChangeListener(null);
        holder.switchActive.setChecked(item.isActive());
        holder.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onMedicineToggleActive(item, isChecked, holder.getAdapterPosition());
            }
        });

        // Dim if inactive
        holder.itemView.setAlpha(item.isActive() ? 1.0f : 0.6f);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMedicineClick(item);
            }
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
        TextView tvDosageForm;
        TextView tvFrequencyTimes;
        MaterialSwitch switchActive;

        ViewHolder(View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.icon_container);
            ivMedicineIcon = itemView.findViewById(R.id.iv_medicine_icon);
            tvMedicineName = itemView.findViewById(R.id.tv_medicine_name);
            tvDosageForm = itemView.findViewById(R.id.tv_dosage_form);
            tvFrequencyTimes = itemView.findViewById(R.id.tv_frequency_times);
            switchActive = itemView.findViewById(R.id.switch_active);
        }
    }
}
