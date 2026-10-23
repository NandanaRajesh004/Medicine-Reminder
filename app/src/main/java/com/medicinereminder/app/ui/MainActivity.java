package com.medicinereminder.app.ui;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.medicinereminder.app.R;
import com.medicinereminder.app.database.MedicineDao;
import com.medicinereminder.app.model.Medicine;
import com.medicinereminder.app.ui.fragment.HistoryFragment;
import com.medicinereminder.app.ui.fragment.MedicinesFragment;
import com.medicinereminder.app.ui.fragment.TodayDosesFragment;
import com.medicinereminder.app.util.AlarmScheduler;
import com.medicinereminder.app.util.NotificationHelper;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ExtendedFloatingActionButton fabAddMedicine;

    private TodayDosesFragment todayFragment;
    private MedicinesFragment medicinesFragment;
    private HistoryFragment historyFragment;
    private Fragment activeFragment;
    private FragmentManager fragmentManager;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Reminder notifications enabled!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Notification permission is required to receive medicine reminders.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        initViews();
        setupNotificationChannel();
        checkPermissions();
        seedInitialDataIfNeeded();
        setupNavigation(savedInstanceState);
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddMedicine = findViewById(R.id.fab_add_medicine);

        fabAddMedicine.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditMedicineActivity.class);
            startActivity(intent);
        });
    }

    private void setupNotificationChannel() {
        NotificationHelper.createNotificationChannel(this);
    }

    private void checkPermissions() {
        // Android 13+ Notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Android 12+ Exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // Inform user gently if needed
            }
        }
    }

    private void setupNavigation(Bundle savedInstanceState) {
        todayFragment = new TodayDosesFragment();
        medicinesFragment = new MedicinesFragment();
        historyFragment = new HistoryFragment();

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, todayFragment)
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_today) {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, todayFragment)
                        .commit();
                fabAddMedicine.show();
                return true;
            } else if (itemId == R.id.navigation_medicines) {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, medicinesFragment)
                        .commit();
                fabAddMedicine.show();
                return true;
            } else if (itemId == R.id.navigation_history) {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, historyFragment)
                        .commit();
                fabAddMedicine.hide();
                return true;
            }
            return false;
        });
    }

    private void seedInitialDataIfNeeded() {
        MedicineDao dao = new MedicineDao(this);
        if (dao.getAllMedicines().isEmpty()) {
            // Seed 2 default medicines for immediate demonstration
            Medicine m1 = new Medicine();
            m1.setName("Paracetamol");
            m1.setDosage("500 mg");
            m1.setForm("Tablet");
            m1.setFrequency("DAILY");
            m1.setDaysOfWeek("1,2,3,4,5,6,7");
            m1.setTimes("08:00,20:00");
            m1.setStartDate(System.currentTimeMillis());
            m1.setEndDate(-1);
            m1.setFoodRelation("AFTER_MEAL");
            m1.setNotes("Take after breakfast and dinner with a glass of water.");
            m1.setActive(true);
            long id1 = dao.insertMedicine(m1);
            m1.setId(id1);
            AlarmScheduler.scheduleMedicineAlarms(this, m1);

            Medicine m2 = new Medicine();
            m2.setName("Amoxicillin");
            m2.setDosage("250 mg");
            m2.setForm("Capsule");
            m2.setFrequency("DAILY");
            m2.setDaysOfWeek("1,2,3,4,5,6,7");
            m2.setTimes("14:00");
            m2.setStartDate(System.currentTimeMillis());
            m2.setEndDate(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000));
            m2.setFoodRelation("WITH_MEAL");
            m2.setNotes("7-day course. Complete all prescribed doses.");
            m2.setActive(true);
            long id2 = dao.insertMedicine(m2);
            m2.setId(id2);
            AlarmScheduler.scheduleMedicineAlarms(this, m2);

            Medicine m3 = new Medicine();
            m3.setName("Vitamin D3");
            m3.setDosage("1000 IU");
            m3.setForm("Drops");
            m3.setFrequency("DAILY");
            m3.setDaysOfWeek("1,2,3,4,5,6,7");
            m3.setTimes("09:00");
            m3.setStartDate(System.currentTimeMillis());
            m3.setEndDate(-1);
            m3.setFoodRelation("BEFORE_MEAL");
            m3.setNotes("Daily dietary supplement.");
            m3.setActive(true);
            long id3 = dao.insertMedicine(m3);
            m3.setId(id3);
            AlarmScheduler.scheduleMedicineAlarms(this, m3);
        }
    }
}
