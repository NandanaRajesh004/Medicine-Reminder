package com.medicinereminder.app.ui.fragment;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.medicinereminder.app.R;
import com.medicinereminder.app.database.DoseLogDao;
import com.medicinereminder.app.database.MedicineDao;
import com.medicinereminder.app.model.DoseRecord;
import com.medicinereminder.app.model.Medicine;
import com.medicinereminder.app.receiver.NotificationActionReceiver;
import com.medicinereminder.app.ui.AddEditMedicineActivity;
import com.medicinereminder.app.ui.MedicineDetailActivity;
import com.medicinereminder.app.ui.adapter.DoseTodayAdapter;
import com.medicinereminder.app.util.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TodayDosesFragment extends Fragment implements DoseTodayAdapter.OnDoseActionListener {

    private ImageButton btnPrevDay;
    private ImageButton btnNextDay;
    private LinearLayout layoutDatePicker;
    private TextView tvHeaderDate;
    private TextView tvJumpToday;
    private TextView tvAdherenceHeadline;
    private TextView tvAdherencePercentage;
    private LinearProgressIndicator progressAdherence;
    private ChipGroup chipGroupFilters;
    private Chip chipFilterAll, chipFilterPending, chipFilterTaken;
    private RecyclerView rvTodayDoses;
    private LinearLayout layoutEmptyState;
    private MaterialButton btnEmptyAddMedicine;

    private DoseTodayAdapter adapter;
    private MedicineDao medicineDao;
    private DoseLogDao doseLogDao;

    private Calendar selectedCalendar;
    private List<DoseRecord> allDosesForDate = new ArrayList<>();

    private final BroadcastReceiver doseUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadDosesForSelectedDate();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today_doses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        medicineDao = new MedicineDao(requireContext());
        doseLogDao = new DoseLogDao(requireContext());
        selectedCalendar = Calendar.getInstance();

        btnPrevDay = view.findViewById(R.id.btn_prev_day);
        btnNextDay = view.findViewById(R.id.btn_next_day);
        layoutDatePicker = view.findViewById(R.id.layout_date_picker);
        tvHeaderDate = view.findViewById(R.id.tv_header_date);
        tvJumpToday = view.findViewById(R.id.tv_jump_today);
        tvAdherenceHeadline = view.findViewById(R.id.tv_adherence_headline);
        tvAdherencePercentage = view.findViewById(R.id.tv_adherence_percentage);
        progressAdherence = view.findViewById(R.id.progress_adherence);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        chipFilterAll = view.findViewById(R.id.chip_filter_all);
        chipFilterPending = view.findViewById(R.id.chip_filter_pending);
        chipFilterTaken = view.findViewById(R.id.chip_filter_taken);
        rvTodayDoses = view.findViewById(R.id.rv_today_doses);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        btnEmptyAddMedicine = view.findViewById(R.id.btn_empty_add_medicine);

        rvTodayDoses.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DoseTodayAdapter(requireContext(), this);
        rvTodayDoses.setAdapter(adapter);

        setupListeners();
        updateDateHeader();
        loadDosesForSelectedDate();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDosesForSelectedDate();

        IntentFilter filter = new IntentFilter(NotificationActionReceiver.ACTION_DOSE_UPDATED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(doseUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(doseUpdateReceiver, filter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            requireContext().unregisterReceiver(doseUpdateReceiver);
        } catch (IllegalArgumentException ignored) {}
    }

    private void setupListeners() {
        btnPrevDay.setOnClickListener(v -> {
            selectedCalendar.add(Calendar.DAY_OF_YEAR, -1);
            updateDateHeader();
            loadDosesForSelectedDate();
        });

        btnNextDay.setOnClickListener(v -> {
            selectedCalendar.add(Calendar.DAY_OF_YEAR, 1);
            updateDateHeader();
            loadDosesForSelectedDate();
        });

        layoutDatePicker.setOnClickListener(v -> showDatePicker());

        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> filterAndDisplayList());

        btnEmptyAddMedicine.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditMedicineActivity.class);
            startActivity(intent);
        });
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateHeader();
                    loadDosesForSelectedDate();
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void updateDateHeader() {
        tvHeaderDate.setText(DateTimeUtils.getHeaderDateDisplay(selectedCalendar));
        boolean isToday = DateTimeUtils.isSameDay(selectedCalendar, Calendar.getInstance());
        tvJumpToday.setText(isToday ? "Tap to change date" : "Tap to pick date (or jump to Today)");
    }

    public void loadDosesForSelectedDate() {
        if (!isAdded()) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(selectedCalendar.getTime());

        List<Medicine> activeMedicines = medicineDao.getActiveMedicines();
        Map<String, DoseRecord> logsMap = doseLogDao.getLogsMapForDate(dateStr);

        allDosesForDate.clear();

        for (Medicine medicine : activeMedicines) {
            if (medicine.isScheduledForDay(selectedCalendar)) {
                List<String> times = medicine.getTimesList();
                for (String timeStr : times) {
                    String key = medicine.getId() + "_" + timeStr;
                    DoseRecord record = logsMap.get(key);

                    if (record == null) {
                        record = new DoseRecord();
                        record.setMedicineId(medicine.getId());
                        record.setMedicineName(medicine.getName());
                        record.setDosage(medicine.getDosage());
                        record.setForm(medicine.getForm());
                        record.setFoodRelation(medicine.getFoodRelation());
                        record.setDateStr(dateStr);
                        record.setTimeStr(timeStr);
                        record.setScheduledTimeMillis(DateTimeUtils.getMillisForDateTime(dateStr, timeStr));
                        record.setStatus(DoseRecord.STATUS_PENDING);
                    }
                    allDosesForDate.add(record);
                }
            }
        }

        // Sort chronologically by scheduled time
        Collections.sort(allDosesForDate, Comparator.comparingLong(DoseRecord::getScheduledTimeMillis));

        updateAdherenceSummary();
        filterAndDisplayList();
    }

    private void updateAdherenceSummary() {
        int total = allDosesForDate.size();
        int taken = 0;
        for (DoseRecord r : allDosesForDate) {
            if (r.isTaken()) taken++;
        }

        int percentage = total > 0 ? (int) Math.round(((double) taken / total) * 100) : 0;
        tvAdherenceHeadline.setText(getString(R.string.doses_taken_count, taken, total));
        tvAdherencePercentage.setText(percentage + "%");
        progressAdherence.setProgress(percentage);
    }

    private void filterAndDisplayList() {
        List<DoseRecord> filtered = new ArrayList<>();
        int checkedId = chipGroupFilters.getCheckedChipId();

        for (DoseRecord r : allDosesForDate) {
            if (checkedId == R.id.chip_filter_taken) {
                if (r.isTaken()) filtered.add(r);
            } else if (checkedId == R.id.chip_filter_pending) {
                if (r.isPending() || r.isMissed()) filtered.add(r);
            } else {
                filtered.add(r);
            }
        }

        adapter.setList(filtered);

        if (filtered.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvTodayDoses.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvTodayDoses.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTakeDose(DoseRecord record, int position) {
        record.setStatus(DoseRecord.STATUS_TAKEN);
        record.setActualTimeMillis(System.currentTimeMillis());
        record.setNotes("Taken on " + DateTimeUtils.formatDate(System.currentTimeMillis(), "hh:mm a"));
        doseLogDao.insertOrUpdateLog(record);

        loadDosesForSelectedDate();
        Snackbar.make(requireView(), "Great! " + record.getMedicineName() + " marked as taken.", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onSkipDose(DoseRecord record, int position) {
        record.setStatus(DoseRecord.STATUS_SKIPPED);
        record.setActualTimeMillis(System.currentTimeMillis());
        record.setNotes("Dose skipped");
        doseLogDao.insertOrUpdateLog(record);

        loadDosesForSelectedDate();
        Snackbar.make(requireView(), record.getMedicineName() + " dose skipped.", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onUndoDose(DoseRecord record, int position) {
        if (record.getId() > 0) {
            doseLogDao.deleteLog(record.getId());
        }
        record.setStatus(DoseRecord.STATUS_PENDING);
        record.setActualTimeMillis(0);

        loadDosesForSelectedDate();
        Snackbar.make(requireView(), "Dose status reset to pending.", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDoseClicked(DoseRecord record) {
        Medicine med = medicineDao.getMedicineById(record.getMedicineId());
        if (med != null) {
            Intent intent = new Intent(requireContext(), MedicineDetailActivity.class);
            intent.putExtra("medicine_id", med.getId());
            startActivity(intent);
        }
    }
}
