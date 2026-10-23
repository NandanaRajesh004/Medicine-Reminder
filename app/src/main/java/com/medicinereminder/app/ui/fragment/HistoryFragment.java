package com.medicinereminder.app.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.medicinereminder.app.R;
import com.medicinereminder.app.database.DoseLogDao;
import com.medicinereminder.app.model.DoseRecord;
import com.medicinereminder.app.ui.MedicineDetailActivity;
import com.medicinereminder.app.ui.adapter.DoseHistoryAdapter;

import java.util.List;

public class HistoryFragment extends Fragment implements DoseHistoryAdapter.OnHistoryItemClickListener {

    private TextView tvStatTaken;
    private TextView tvStatSkipped;
    private TextView tvStatRate;
    private MaterialButton btnClearHistory;
    private RecyclerView rvHistory;
    private LinearLayout layoutEmpty;

    private DoseLogDao doseLogDao;
    private DoseHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        doseLogDao = new DoseLogDao(requireContext());

        tvStatTaken = view.findViewById(R.id.tv_stat_taken_count);
        tvStatSkipped = view.findViewById(R.id.tv_stat_skipped_count);
        tvStatRate = view.findViewById(R.id.tv_stat_adherence_rate);
        btnClearHistory = view.findViewById(R.id.btn_clear_history);
        rvHistory = view.findViewById(R.id.rv_history);
        layoutEmpty = view.findViewById(R.id.layout_empty_history);

        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DoseHistoryAdapter(requireContext(), this);
        rvHistory.setAdapter(adapter);

        btnClearHistory.setOnClickListener(v -> showClearHistoryDialog());

        loadHistory();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    public void loadHistory() {
        List<DoseRecord> logs = doseLogDao.getAllLogs(200);
        adapter.setList(logs);

        int taken = 0;
        int skipped = 0;
        for (DoseRecord r : logs) {
            if (r.isTaken()) taken++;
            else if (r.isSkipped() || r.isMissed()) skipped++;
        }

        int total = taken + skipped;
        int rate = total > 0 ? (int) Math.round(((double) taken / total) * 100) : 0;

        tvStatTaken.setText(String.valueOf(taken));
        tvStatSkipped.setText(String.valueOf(skipped));
        tvStatRate.setText(rate + "%");

        if (logs.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
            btnClearHistory.setEnabled(false);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            btnClearHistory.setEnabled(true);
        }
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear History")
                .setMessage(R.string.confirm_clear_history)
                .setPositiveButton("Clear", (dialog, which) -> {
                    doseLogDao.clearAllLogs();
                    loadHistory();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onItemClick(DoseRecord record) {
        Intent intent = new Intent(requireContext(), MedicineDetailActivity.class);
        intent.putExtra("medicine_id", record.getMedicineId());
        startActivity(intent);
    }
}
