package com.medicinereminder.app.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.medicinereminder.app.R;
import com.medicinereminder.app.database.MedicineDao;
import com.medicinereminder.app.model.Medicine;
import com.medicinereminder.app.ui.AddEditMedicineActivity;
import com.medicinereminder.app.ui.MedicineDetailActivity;
import com.medicinereminder.app.ui.adapter.MedicineListAdapter;
import com.medicinereminder.app.util.AlarmScheduler;

import java.util.ArrayList;
import java.util.List;

public class MedicinesFragment extends Fragment implements MedicineListAdapter.OnMedicineActionListener {

    private TextInputEditText etSearch;
    private TextView tvMedicinesCount;
    private RecyclerView rvMedicines;
    private LinearLayout layoutEmpty;
    private MaterialButton btnAddFirstMedicine;

    private MedicineDao medicineDao;
    private MedicineListAdapter adapter;
    private List<Medicine> allMedicines = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medicines, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        medicineDao = new MedicineDao(requireContext());

        etSearch = view.findViewById(R.id.et_search_medicines);
        tvMedicinesCount = view.findViewById(R.id.tv_medicines_count);
        rvMedicines = view.findViewById(R.id.rv_medicines);
        layoutEmpty = view.findViewById(R.id.layout_empty_medicines);
        btnAddFirstMedicine = view.findViewById(R.id.btn_add_first_medicine);

        rvMedicines.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MedicineListAdapter(requireContext(), this);
        rvMedicines.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMedicines(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnAddFirstMedicine.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditMedicineActivity.class);
            startActivity(intent);
        });

        loadMedicines();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMedicines();
    }

    public void loadMedicines() {
        allMedicines = medicineDao.getAllMedicines();
        filterMedicines(etSearch.getText() != null ? etSearch.getText().toString() : "");
    }

    private void filterMedicines(String query) {
        List<Medicine> filtered = new ArrayList<>();
        String q = query != null ? query.trim().toLowerCase() : "";

        for (Medicine m : allMedicines) {
            if (q.isEmpty() || m.getName().toLowerCase().contains(q) || m.getDosage().toLowerCase().contains(q)) {
                filtered.add(m);
            }
        }

        adapter.setList(filtered);
        tvMedicinesCount.setText("All Medicines (" + filtered.size() + ")");

        if (filtered.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvMedicines.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvMedicines.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMedicineClick(Medicine medicine) {
        Intent intent = new Intent(requireContext(), MedicineDetailActivity.class);
        intent.putExtra("medicine_id", medicine.getId());
        startActivity(intent);
    }

    @Override
    public void onMedicineToggleActive(Medicine medicine, boolean isActive, int position) {
        medicine.setActive(isActive);
        medicineDao.setMedicineActive(medicine.getId(), isActive);

        if (isActive) {
            AlarmScheduler.scheduleMedicineAlarms(requireContext(), medicine);
        } else {
            AlarmScheduler.cancelMedicineAlarms(requireContext(), medicine);
        }

        adapter.notifyItemChanged(position);
    }
}
