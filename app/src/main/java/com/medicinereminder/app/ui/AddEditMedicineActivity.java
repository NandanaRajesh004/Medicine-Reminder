package com.medicinereminder.app.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.medicinereminder.app.R;
import com.medicinereminder.app.database.MedicineDao;
import com.medicinereminder.app.model.Medicine;
import com.medicinereminder.app.util.AlarmScheduler;
import com.medicinereminder.app.util.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddEditMedicineActivity extends AppCompatActivity {

    public static final String EXTRA_MEDICINE_ID = "extra_medicine_id";

    private ImageButton btnBack;
    private TextView tvTitle;
    private MaterialButton btnSaveTop, btnSaveBottom;
    private TextInputEditText etMedicineName, etDosage, etNotes, etIntervalDays;
    private ChipGroup chipGroupForm, chipGroupTimes, chipGroupDays, chipGroupFood;
    private MaterialButton btnAddTime;
    private TextView tvNoTimesWarning;
    private RadioGroup rgFrequency;
    private RadioButton rbDaily, rbSpecificDays, rbInterval, rbAsNeeded;
    private LinearLayout layoutDaysOfWeek, layoutInterval;
    private LinearLayout layoutBtnStartDate, layoutBtnEndDate;
    private TextView tvStartDate, tvEndDate;

    private MedicineDao medicineDao;
    private Medicine medicineToEdit;
    private boolean isEditMode = false;

    private final List<String> scheduledTimes = new ArrayList<>();
    private long startDateMillis;
    private long endDateMillis = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_medicine);

        medicineDao = new MedicineDao(this);

        initViews();

        long medId = getIntent().getLongExtra(EXTRA_MEDICINE_ID, -1);
        if (medId != -1) {
            medicineToEdit = medicineDao.getMedicineById(medId);
            if (medicineToEdit != null) {
                isEditMode = true;
                tvTitle.setText(R.string.title_edit_medicine);
                btnSaveBottom.setText(R.string.update_medicine);
            }
        }

        setupDefaults();
        if (isEditMode) {
            populateExistingData();
        }

        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);
        btnSaveTop = findViewById(R.id.btn_save_medicine);
        btnSaveBottom = findViewById(R.id.btn_bottom_save);
        etMedicineName = findViewById(R.id.et_medicine_name);
        etDosage = findViewById(R.id.et_dosage);
        etNotes = findViewById(R.id.et_notes);
        etIntervalDays = findViewById(R.id.et_interval_days);
        chipGroupForm = findViewById(R.id.chip_group_form);
        chipGroupTimes = findViewById(R.id.chip_group_times);
        chipGroupDays = findViewById(R.id.chip_group_days);
        chipGroupFood = findViewById(R.id.chip_group_food);
        btnAddTime = findViewById(R.id.btn_add_time);
        tvNoTimesWarning = findViewById(R.id.tv_no_times_warning);
        rgFrequency = findViewById(R.id.rg_frequency);
        rbDaily = findViewById(R.id.rb_freq_daily);
        rbSpecificDays = findViewById(R.id.rb_freq_specific_days);
        rbInterval = findViewById(R.id.rb_freq_interval);
        rbAsNeeded = findViewById(R.id.rb_freq_as_needed);
        layoutDaysOfWeek = findViewById(R.id.layout_days_of_week);
        layoutInterval = findViewById(R.id.layout_interval);
        layoutBtnStartDate = findViewById(R.id.layout_btn_start_date);
        layoutBtnEndDate = findViewById(R.id.layout_btn_end_date);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);
    }

    private void setupDefaults() {
        startDateMillis = System.currentTimeMillis();
        tvStartDate.setText(DateTimeUtils.formatDate(startDateMillis, "MMM d, yyyy"));
        tvEndDate.setText("Continuous (No End)");

        if (!isEditMode) {
            scheduledTimes.add("08:00");
            refreshTimesChips();
        }
    }

    private void populateExistingData() {
        etMedicineName.setText(medicineToEdit.getName());
        etDosage.setText(medicineToEdit.getDosage());
        etNotes.setText(medicineToEdit.getNotes());

        // Form
        selectFormChip(medicineToEdit.getForm());

        // Times
        scheduledTimes.clear();
        scheduledTimes.addAll(medicineToEdit.getTimesList());
        refreshTimesChips();

        // Frequency
        String freq = medicineToEdit.getFrequency();
        if ("SPECIFIC_DAYS".equalsIgnoreCase(freq)) {
            rbSpecificDays.setChecked(true);
            layoutDaysOfWeek.setVisibility(View.VISIBLE);
            populateDaysChips(medicineToEdit.getDaysOfWeek());
        } else if ("INTERVAL".equalsIgnoreCase(freq)) {
            rbInterval.setChecked(true);
            layoutInterval.setVisibility(View.VISIBLE);
            etIntervalDays.setText(String.valueOf(medicineToEdit.getIntervalDays()));
        } else if ("AS_NEEDED".equalsIgnoreCase(freq)) {
            rbAsNeeded.setChecked(true);
        } else {
            rbDaily.setChecked(true);
        }

        // Food
        selectFoodChip(medicineToEdit.getFoodRelation());

        // Dates
        startDateMillis = medicineToEdit.getStartDate();
        tvStartDate.setText(DateTimeUtils.formatDate(startDateMillis, "MMM d, yyyy"));

        endDateMillis = medicineToEdit.getEndDate();
        if (endDateMillis > 0) {
            tvEndDate.setText(DateTimeUtils.formatDate(endDateMillis, "MMM d, yyyy"));
        } else {
            tvEndDate.setText("Continuous (No End)");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSaveTop.setOnClickListener(v -> saveMedicine());
        btnSaveBottom.setOnClickListener(v -> saveMedicine());

        btnAddTime.setOnClickListener(v -> showTimePicker());

        rgFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_freq_specific_days) {
                layoutDaysOfWeek.setVisibility(View.VISIBLE);
                layoutInterval.setVisibility(View.GONE);
            } else if (checkedId == R.id.rb_freq_interval) {
                layoutDaysOfWeek.setVisibility(View.GONE);
                layoutInterval.setVisibility(View.VISIBLE);
            } else {
                layoutDaysOfWeek.setVisibility(View.GONE);
                layoutInterval.setVisibility(View.GONE);
            }
        });

        layoutBtnStartDate.setOnClickListener(v -> showStartDatePicker());
        layoutBtnEndDate.setOnClickListener(v -> showEndDatePicker());
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        TimePickerDialog picker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            if (!scheduledTimes.contains(timeStr)) {
                scheduledTimes.add(timeStr);
                Collections.sort(scheduledTimes);
                refreshTimesChips();
            } else {
                Toast.makeText(this, "This time is already added.", Toast.LENGTH_SHORT).show();
            }
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);
        picker.show();
    }

    private void refreshTimesChips() {
        chipGroupTimes.removeAllViews();
        tvNoTimesWarning.setVisibility(scheduledTimes.isEmpty() ? View.VISIBLE : View.GONE);

        for (String timeStr : scheduledTimes) {
            Chip chip = new Chip(this);
            chip.setText(DateTimeUtils.formatTimeString(timeStr));
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                scheduledTimes.remove(timeStr);
                refreshTimesChips();
            });
            chipGroupTimes.addView(chip);
        }
    }

    private void showStartDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startDateMillis);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 0, 0, 0);
            startDateMillis = selected.getTimeInMillis();
            tvStartDate.setText(DateTimeUtils.formatDate(startDateMillis, "MMM d, yyyy"));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showEndDatePicker() {
        Calendar cal = Calendar.getInstance();
        if (endDateMillis > 0) cal.setTimeInMillis(endDateMillis);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 23, 59, 59);
            endDateMillis = selected.getTimeInMillis();
            tvEndDate.setText(DateTimeUtils.formatDate(endDateMillis, "MMM d, yyyy"));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        dialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, "Clear (Continuous)", (d, which) -> {
            endDateMillis = -1;
            tvEndDate.setText("Continuous (No End)");
        });

        dialog.show();
    }

    private void selectFormChip(String form) {
        if (form == null) return;
        switch (form.toLowerCase()) {
            case "capsule": chipGroupForm.check(R.id.chip_form_capsule); break;
            case "syrup":
            case "liquid": chipGroupForm.check(R.id.chip_form_syrup); break;
            case "injection": chipGroupForm.check(R.id.chip_form_injection); break;
            case "drops": chipGroupForm.check(R.id.chip_form_drops); break;
            case "inhaler": chipGroupForm.check(R.id.chip_form_inhaler); break;
            case "other": chipGroupForm.check(R.id.chip_form_other); break;
            case "tablet":
            default: chipGroupForm.check(R.id.chip_form_tablet); break;
        }
    }

    private String getSelectedForm() {
        int id = chipGroupForm.getCheckedChipId();
        if (id == R.id.chip_form_capsule) return "Capsule";
        if (id == R.id.chip_form_syrup) return "Syrup";
        if (id == R.id.chip_form_injection) return "Injection";
        if (id == R.id.chip_form_drops) return "Drops";
        if (id == R.id.chip_form_inhaler) return "Inhaler";
        if (id == R.id.chip_form_other) return "Other";
        return "Tablet";
    }

    private void selectFoodChip(String food) {
        if (food == null) return;
        switch (food) {
            case "BEFORE_MEAL": chipGroupFood.check(R.id.chip_food_before); break;
            case "AFTER_MEAL": chipGroupFood.check(R.id.chip_food_after); break;
            case "WITH_MEAL": chipGroupFood.check(R.id.chip_food_with); break;
            case "ANYTIME":
            default: chipGroupFood.check(R.id.chip_food_anytime); break;
        }
    }

    private String getSelectedFood() {
        int id = chipGroupFood.getCheckedChipId();
        if (id == R.id.chip_food_before) return "BEFORE_MEAL";
        if (id == R.id.chip_food_after) return "AFTER_MEAL";
        if (id == R.id.chip_food_with) return "WITH_MEAL";
        return "ANYTIME";
    }

    private void populateDaysChips(String daysOfWeek) {
        if (daysOfWeek == null) return;
        String[] days = daysOfWeek.split(",");
        for (String d : days) {
            try {
                int val = Integer.parseInt(d.trim());
                if (val == 1) ((Chip) findViewById(R.id.chip_day_sun)).setChecked(true);
                if (val == 2) ((Chip) findViewById(R.id.chip_day_mon)).setChecked(true);
                if (val == 3) ((Chip) findViewById(R.id.chip_day_tue)).setChecked(true);
                if (val == 4) ((Chip) findViewById(R.id.chip_day_wed)).setChecked(true);
                if (val == 5) ((Chip) findViewById(R.id.chip_day_thu)).setChecked(true);
                if (val == 6) ((Chip) findViewById(R.id.chip_day_fri)).setChecked(true);
                if (val == 7) ((Chip) findViewById(R.id.chip_day_sat)).setChecked(true);
            } catch (Exception ignored) {}
        }
    }

    private String getSelectedDaysOfWeek() {
        List<String> selected = new ArrayList<>();
        if (((Chip) findViewById(R.id.chip_day_sun)).isChecked()) selected.add("1");
        if (((Chip) findViewById(R.id.chip_day_mon)).isChecked()) selected.add("2");
        if (((Chip) findViewById(R.id.chip_day_tue)).isChecked()) selected.add("3");
        if (((Chip) findViewById(R.id.chip_day_wed)).isChecked()) selected.add("4");
        if (((Chip) findViewById(R.id.chip_day_thu)).isChecked()) selected.add("5");
        if (((Chip) findViewById(R.id.chip_day_fri)).isChecked()) selected.add("6");
        if (((Chip) findViewById(R.id.chip_day_sat)).isChecked()) selected.add("7");

        if (selected.isEmpty()) return "1,2,3,4,5,6,7"; // fallback to daily
        return String.join(",", selected);
    }

    private void saveMedicine() {
        String name = etMedicineName.getText() != null ? etMedicineName.getText().toString().trim() : "";
        String dosage = etDosage.getText() != null ? etDosage.getText().toString().trim() : "";
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        if (name.isEmpty()) {
            etMedicineName.setError("Medicine name is required");
            etMedicineName.requestFocus();
            return;
        }

        if (dosage.isEmpty()) {
            etDosage.setError("Dosage is required (e.g. 500 mg, 1 tablet)");
            etDosage.requestFocus();
            return;
        }

        if (scheduledTimes.isEmpty()) {
            Toast.makeText(this, "Please add at least one reminder time.", Toast.LENGTH_SHORT).show();
            return;
        }

        String form = getSelectedForm();
        String food = getSelectedFood();

        String frequency = "DAILY";
        int freqChecked = rgFrequency.getCheckedRadioButtonId();
        if (freqChecked == R.id.rb_freq_specific_days) frequency = "SPECIFIC_DAYS";
        else if (freqChecked == R.id.rb_freq_interval) frequency = "INTERVAL";
        else if (freqChecked == R.id.rb_freq_as_needed) frequency = "AS_NEEDED";

        String daysOfWeek = getSelectedDaysOfWeek();

        int intervalDays = 1;
        try {
            if (etIntervalDays.getText() != null && !etIntervalDays.getText().toString().isEmpty()) {
                intervalDays = Integer.parseInt(etIntervalDays.getText().toString().trim());
            }
        } catch (NumberFormatException ignored) {}

        String timesJoined = String.join(",", scheduledTimes);

        Medicine medicine = isEditMode ? medicineToEdit : new Medicine();
        medicine.setName(name);
        medicine.setDosage(dosage);
        medicine.setForm(form);
        medicine.setFrequency(frequency);
        medicine.setDaysOfWeek(daysOfWeek);
        medicine.setIntervalDays(intervalDays);
        medicine.setTimes(timesJoined);
        medicine.setStartDate(startDateMillis);
        medicine.setEndDate(endDateMillis);
        medicine.setFoodRelation(food);
        medicine.setNotes(notes);
        medicine.setActive(true);

        if (isEditMode) {
            // Cancel old alarms before updating
            AlarmScheduler.cancelMedicineAlarms(this, medicineToEdit);
            medicineDao.updateMedicine(medicine);
            AlarmScheduler.scheduleMedicineAlarms(this, medicine);
            Toast.makeText(this, "Medicine reminder updated!", Toast.LENGTH_SHORT).show();
        } else {
            long newId = medicineDao.insertMedicine(medicine);
            medicine.setId(newId);
            AlarmScheduler.scheduleMedicineAlarms(this, medicine);
            Toast.makeText(this, "Medicine reminder saved!", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
