package com.medicinereminder.app.model;

import com.medicinereminder.app.R;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DoseRecord implements Serializable {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_TAKEN = "TAKEN";
    public static final String STATUS_MISSED = "MISSED";
    public static final String STATUS_SKIPPED = "SKIPPED";

    private long id;
    private long medicineId;
    private String medicineName;
    private String dosage;
    private String form;
    private String foodRelation;
    private long scheduledTimeMillis;
    private long actualTimeMillis;
    private String status; // PENDING, TAKEN, MISSED, SKIPPED
    private String dateStr; // yyyy-MM-dd
    private String timeStr; // HH:mm
    private String notes;

    public DoseRecord() {
        this.status = STATUS_PENDING;
    }

    public DoseRecord(long id, long medicineId, String medicineName, String dosage, String form,
                      String foodRelation, long scheduledTimeMillis, long actualTimeMillis,
                      String status, String dateStr, String timeStr, String notes) {
        this.id = id;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.form = form;
        this.foodRelation = foodRelation;
        this.scheduledTimeMillis = scheduledTimeMillis;
        this.actualTimeMillis = actualTimeMillis;
        this.status = status;
        this.dateStr = dateStr;
        this.timeStr = timeStr;
        this.notes = notes;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getMedicineId() { return medicineId; }
    public void setMedicineId(long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getForm() { return form; }
    public void setForm(String form) { this.form = form; }

    public String getFoodRelation() { return foodRelation; }
    public void setFoodRelation(String foodRelation) { this.foodRelation = foodRelation; }

    public long getScheduledTimeMillis() { return scheduledTimeMillis; }
    public void setScheduledTimeMillis(long scheduledTimeMillis) { this.scheduledTimeMillis = scheduledTimeMillis; }

    public long getActualTimeMillis() { return actualTimeMillis; }
    public void setActualTimeMillis(long actualTimeMillis) { this.actualTimeMillis = actualTimeMillis; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDateStr() { return dateStr; }
    public void setDateStr(String dateStr) { this.dateStr = dateStr; }

    public String getTimeStr() { return timeStr; }
    public void setTimeStr(String timeStr) { this.timeStr = timeStr; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isTaken() {
        return STATUS_TAKEN.equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return STATUS_PENDING.equalsIgnoreCase(status);
    }

    public boolean isMissed() {
        return STATUS_MISSED.equalsIgnoreCase(status);
    }

    public boolean isSkipped() {
        return STATUS_SKIPPED.equalsIgnoreCase(status);
    }

    public String getFormattedScheduledTime() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(timeStr);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (Exception ignored) {}
        return timeStr;
    }

    public int getFormIconRes() {
        if (form == null) return R.drawable.ic_form_tablet;
        switch (form.toLowerCase()) {
            case "capsule": return R.drawable.ic_form_capsule;
            case "syrup":
            case "liquid": return R.drawable.ic_form_syrup;
            case "injection": return R.drawable.ic_form_injection;
            case "drops": return R.drawable.ic_form_drops;
            case "inhaler": return R.drawable.ic_form_inhaler;
            case "other": return R.drawable.ic_form_other;
            case "tablet":
            default: return R.drawable.ic_form_tablet;
        }
    }

    public int getFormColorRes() {
        if (form == null) return R.color.form_tablet;
        switch (form.toLowerCase()) {
            case "capsule": return R.color.form_capsule;
            case "syrup":
            case "liquid": return R.color.form_syrup;
            case "injection": return R.color.form_injection;
            case "drops": return R.color.form_drops;
            case "inhaler": return R.color.form_drops;
            case "other": return R.color.form_other;
            case "tablet":
            default: return R.color.form_tablet;
        }
    }

    public int getFormBgColorRes() {
        if (form == null) return R.color.form_tablet_bg;
        switch (form.toLowerCase()) {
            case "capsule": return R.color.form_capsule_bg;
            case "syrup":
            case "liquid": return R.color.form_syrup_bg;
            case "injection": return R.color.form_injection_bg;
            case "drops": return R.color.form_drops_bg;
            case "inhaler": return R.color.form_drops_bg;
            case "other": return R.color.form_other_bg;
            case "tablet":
            default: return R.color.form_tablet_bg;
        }
    }
}
