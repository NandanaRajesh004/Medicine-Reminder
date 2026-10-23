package com.medicinereminder.app.model;

import com.medicinereminder.app.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Medicine implements Serializable {
    private long id;
    private String name;
    private String dosage;
    private String form; // Tablet, Capsule, Syrup, Injection, Drops, Inhaler, Other
    private String frequency; // DAILY, SPECIFIC_DAYS, INTERVAL, AS_NEEDED
    private String daysOfWeek; // Comma separated integers: 1=Sun, 2=Mon, 3=Tue, ... 7=Sat
    private int intervalDays; // e.g. 1, 2, 3
    private String times; // Comma separated HH:mm: "08:00,14:00,20:00"
    private long startDate;
    private long endDate; // -1 for ongoing
    private String foodRelation; // ANYTIME, BEFORE_MEAL, AFTER_MEAL, WITH_MEAL
    private String notes;
    private boolean isActive;
    private long createdAt;

    public Medicine() {
        this.form = "Tablet";
        this.frequency = "DAILY";
        this.daysOfWeek = "1,2,3,4,5,6,7";
        this.intervalDays = 1;
        this.times = "08:00";
        this.startDate = System.currentTimeMillis();
        this.endDate = -1;
        this.foodRelation = "ANYTIME";
        this.notes = "";
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
    }

    public Medicine(long id, String name, String dosage, String form, String frequency,
                    String daysOfWeek, int intervalDays, String times, long startDate,
                    long endDate, String foodRelation, String notes, boolean isActive, long createdAt) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.form = form != null ? form : "Tablet";
        this.frequency = frequency != null ? frequency : "DAILY";
        this.daysOfWeek = daysOfWeek != null ? daysOfWeek : "1,2,3,4,5,6,7";
        this.intervalDays = intervalDays > 0 ? intervalDays : 1;
        this.times = times != null ? times : "08:00";
        this.startDate = startDate;
        this.endDate = endDate;
        this.foodRelation = foodRelation != null ? foodRelation : "ANYTIME";
        this.notes = notes != null ? notes : "";
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getForm() { return form; }
    public void setForm(String form) { this.form = form; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public int getIntervalDays() { return intervalDays; }
    public void setIntervalDays(int intervalDays) { this.intervalDays = intervalDays; }

    public String getTimes() { return times; }
    public void setTimes(String times) { this.times = times; }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public long getEndDate() { return endDate; }
    public void setEndDate(long endDate) { this.endDate = endDate; }

    public String getFoodRelation() { return foodRelation; }
    public void setFoodRelation(String foodRelation) { this.foodRelation = foodRelation; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<String> getTimesList() {
        List<String> list = new ArrayList<>();
        if (times != null && !times.trim().isEmpty()) {
            String[] parts = times.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    list.add(trimmed);
                }
            }
        }
        return list;
    }

    /**
     * Checks if this medicine is scheduled on the given calendar day
     */
    public boolean isScheduledForDay(Calendar targetCalendar) {
        if (!isActive) return false;

        Calendar cal = (Calendar) targetCalendar.clone();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long targetDayMillis = cal.getTimeInMillis();

        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(startDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        long startDayMillis = startCal.getTimeInMillis();

        if (targetDayMillis < startDayMillis) {
            return false;
        }

        if (endDate > 0) {
            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis(endDate);
            endCal.set(Calendar.HOUR_OF_DAY, 23);
            endCal.set(Calendar.MINUTE, 59);
            endCal.set(Calendar.SECOND, 59);
            endCal.set(Calendar.MILLISECOND, 999);
            if (targetCalendar.getTimeInMillis() > endCal.getTimeInMillis()) {
                return false;
            }
        }

        if ("DAILY".equalsIgnoreCase(frequency)) {
            return true;
        } else if ("SPECIFIC_DAYS".equalsIgnoreCase(frequency)) {
            int dayOfWeek = targetCalendar.get(Calendar.DAY_OF_WEEK); // 1=Sun, 2=Mon...
            if (daysOfWeek != null && !daysOfWeek.isEmpty()) {
                String[] days = daysOfWeek.split(",");
                for (String d : days) {
                    try {
                        if (Integer.parseInt(d.trim()) == dayOfWeek) {
                            return true;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            return false;
        } else if ("INTERVAL".equalsIgnoreCase(frequency)) {
            long diffDays = (targetDayMillis - startDayMillis) / (24L * 60 * 60 * 1000);
            return (diffDays >= 0) && (diffDays % Math.max(1, intervalDays) == 0);
        } else if ("AS_NEEDED".equalsIgnoreCase(frequency)) {
            return true;
        }

        return true;
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
            case "other": return R.color.form_other_bg;
            case "tablet":
            default: return R.color.form_tablet_bg;
        }
    }

    public String getFormattedFoodRelation() {
        if (foodRelation == null) return "Anytime";
        switch (foodRelation) {
            case "BEFORE_MEAL": return "Before Meal";
            case "AFTER_MEAL": return "After Meal";
            case "WITH_MEAL": return "With Meal";
            case "ANYTIME":
            default: return "Anytime";
        }
    }

    public String getFormattedFrequency() {
        if (frequency == null) return "Daily";
        switch (frequency) {
            case "DAILY": return "Every Day";
            case "SPECIFIC_DAYS": return "Specific Days (" + getDaysSummary() + ")";
            case "INTERVAL": return "Every " + intervalDays + " days";
            case "AS_NEEDED": return "As Needed";
            default: return frequency;
        }
    }

    private String getDaysSummary() {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) return "None";
        String[] map = {"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        StringBuilder sb = new StringBuilder();
        String[] days = daysOfWeek.split(",");
        for (String d : days) {
            try {
                int idx = Integer.parseInt(d.trim());
                if (idx >= 1 && idx <= 7) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(map[idx]);
                }
            } catch (Exception ignored) {}
        }
        return sb.length() > 0 ? sb.toString() : "None";
    }
}
