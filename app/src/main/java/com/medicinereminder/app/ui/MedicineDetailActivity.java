package com.medicinereminder.app.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.medicinereminder.app.R;
import com.medicinereminder.app.database.MedicineDao;
import com.medicinereminder.app.model.Medicine;
import com.medicinereminder.app.util.AlarmScheduler;
import com.medicinereminder.app.util.DateTimeUtils;

import java.util.List;

public class MedicineDetailActivity extends AppCompatActivity {

    private ImageButton btnBack, btnEdit, btnDeleteTop;
    private FrameLayout iconContainer;
    private ImageView ivIcon;
    private TextView tvName, tvDosage, tvFrequency, tvFood, tvDuration, tvNotes;
    private MaterialSwitch switchActive;
    private ChipGroup chipGroupTimes;
    private MaterialCardView cardNotes;
    private MaterialButton btnDeleteFull;

    private MedicineDao medicineDao;
    private Medicine medicine;
    private long medicineId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_detail);

        medicineDao = new MedicineDao(this);
        medicineId = getIntent().getLongExtra("medicine_id", -1);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedicineDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnEdit = findViewById(R.id.btn_edit_medicine);
        btnDeleteTop = findViewById(R.id.btn_delete_top);
        iconContainer = findViewById(R.id.detail_icon_container);
        ivIcon = findViewById(R.id.detail_iv_icon);
        tvName = findViewById(R.id.detail_tv_name);
        tvDosage = findViewById(R.id.detail_tv_dosage);
        tvFrequency = findViewById(R.id.detail_tv_frequency);
        tvFood = findViewById(R.id.detail_tv_food);
        tvDuration = findViewById(R.id.detail_tv_duration);
        tvNotes = findViewById(R.id.detail_tv_notes);
        switchActive = findViewById(R.id.detail_switch_active);
        chipGroupTimes = findViewById(R.id.detail_chip_group_times);
        cardNotes = findViewById(R.id.card_detail_notes);
        btnDeleteFull = findViewById(R.id.btn_delete_medicine_full);
    }

    private void loadMedicineDetails() {
        medicine = medicineDao.getMedicineById(medicineId);
        if (medicine == null) {
            Toast.makeText(this, "Medicine not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvName.setText(medicine.getName());
        tvDosage.setText(medicine.getDosage() + " • " + medicine.getForm());

        // Icon
        ivIcon.setImageResource(medicine.getFormIconRes());
        ivIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, medicine.getFormColorRes())));
        iconContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, medicine.getFormBgColorRes())));

        // Switch
        switchActive.setOnCheckedChangeListener(null);
        switchActive.setChecked(medicine.isActive());
        switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            medicine.setActive(isChecked);
            medicineDao.setMedicineActive(medicine.getId(), isChecked);
            if (isChecked) {
                AlarmScheduler.scheduleMedicineAlarms(this, medicine);
                Toast.makeText(this, "Reminders enabled.", Toast.LENGTH_SHORT).show();
            } else {
                AlarmScheduler.cancelMedicineAlarms(this, medicine);
                Toast.makeText(this, "Reminders paused.", Toast.LENGTH_SHORT).show();
            }
        });

        // Times chips
        chipGroupTimes.removeAllViews();
        List<String> times = medicine.getTimesList();
        for (String t : times) {
            Chip chip = new Chip(this);
            chip.setText(DateTimeUtils.formatTimeString(t));
            chip.setChipIcon(ContextCompat.getDrawable(this, R.drawable.ic_time));
            chip.setClickable(false);
            chipGroupTimes.addView(chip);
        }

        // Frequency & Food
        tvFrequency.setText(medicine.getFormattedFrequency());
        tvFood.setText(medicine.getFormattedFoodRelation());

        // Duration
        String start = DateTimeUtils.formatDate(medicine.getStartDate(), "MMM d, yyyy");
        if (medicine.getEndDate() > 0) {
            String end = DateTimeUtils.formatDate(medicine.getEndDate(), "MMM d, yyyy");
            tvDuration.setText(start + " to " + end);
        } else {
            tvDuration.setText("From " + start + " (Continuous)");
        }

        // Notes
        if (medicine.getNotes() != null && !medicine.getNotes().trim().isEmpty()) {
            cardNotes.setVisibility(View.VISIBLE);
            tvNotes.setText(medicine.getNotes());
        } else {
            cardNotes.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditMedicineActivity.class);
            intent.putExtra(AddEditMedicineActivity.EXTRA_MEDICINE_ID, medicineId);
            startActivity(intent);
        });

        btnDeleteTop.setOnClickListener(v -> confirmDelete());
        btnDeleteFull.setOnClickListener(v -> confirmDelete());
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirmation_title)
                .setMessage(R.string.delete_confirmation_msg)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    if (medicine != null) {
                        AlarmScheduler.cancelMedicineAlarms(this, medicine);
                        medicineDao.deleteMedicine(medicine.getId());
                        Toast.makeText(this, "Medicine reminder deleted.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
