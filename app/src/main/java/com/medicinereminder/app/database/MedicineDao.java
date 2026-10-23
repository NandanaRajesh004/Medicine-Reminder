package com.medicinereminder.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.medicinereminder.app.model.Medicine;

import java.util.ArrayList;
import java.util.List;

public class MedicineDao {

    private final DatabaseHelper dbHelper;

    public MedicineDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insertMedicine(Medicine medicine) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(medicine);
        long id = db.insert(DatabaseHelper.TABLE_MEDICINES, null, values);
        medicine.setId(id);
        return id;
    }

    public int updateMedicine(Medicine medicine) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(medicine);
        return db.update(DatabaseHelper.TABLE_MEDICINES, values,
                DatabaseHelper.COL_MED_ID + " = ?",
                new String[]{String.valueOf(medicine.getId())});
    }

    public int deleteMedicine(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Also clean up dose logs for this medicine
        db.delete(DatabaseHelper.TABLE_DOSE_LOGS,
                DatabaseHelper.COL_LOG_MED_ID + " = ?",
                new String[]{String.valueOf(id)});

        return db.delete(DatabaseHelper.TABLE_MEDICINES,
                DatabaseHelper.COL_MED_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public Medicine getMedicineById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_MEDICINES, null,
                DatabaseHelper.COL_MED_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        Medicine medicine = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                medicine = fromCursor(cursor);
            }
            cursor.close();
        }
        return medicine;
    }

    public List<Medicine> getAllMedicines() {
        List<Medicine> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_MEDICINES, null,
                null, null, null, null,
                DatabaseHelper.COL_MED_IS_ACTIVE + " DESC, " + DatabaseHelper.COL_MED_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(fromCursor(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public List<Medicine> getActiveMedicines() {
        List<Medicine> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_MEDICINES, null,
                DatabaseHelper.COL_MED_IS_ACTIVE + " = 1",
                null, null, null,
                DatabaseHelper.COL_MED_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(fromCursor(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public void setMedicineActive(long id, boolean active) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_MED_IS_ACTIVE, active ? 1 : 0);
        db.update(DatabaseHelper.TABLE_MEDICINES, values,
                DatabaseHelper.COL_MED_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    private ContentValues toContentValues(Medicine medicine) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_MED_NAME, medicine.getName());
        values.put(DatabaseHelper.COL_MED_DOSAGE, medicine.getDosage());
        values.put(DatabaseHelper.COL_MED_FORM, medicine.getForm());
        values.put(DatabaseHelper.COL_MED_FREQUENCY, medicine.getFrequency());
        values.put(DatabaseHelper.COL_MED_DAYS_OF_WEEK, medicine.getDaysOfWeek());
        values.put(DatabaseHelper.COL_MED_INTERVAL_DAYS, medicine.getIntervalDays());
        values.put(DatabaseHelper.COL_MED_TIMES, medicine.getTimes());
        values.put(DatabaseHelper.COL_MED_START_DATE, medicine.getStartDate());
        values.put(DatabaseHelper.COL_MED_END_DATE, medicine.getEndDate());
        values.put(DatabaseHelper.COL_MED_FOOD_RELATION, medicine.getFoodRelation());
        values.put(DatabaseHelper.COL_MED_NOTES, medicine.getNotes());
        values.put(DatabaseHelper.COL_MED_IS_ACTIVE, medicine.isActive() ? 1 : 0);
        values.put(DatabaseHelper.COL_MED_CREATED_AT, medicine.getCreatedAt());
        return values;
    }

    private Medicine fromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_NAME));
        String dosage = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_DOSAGE));
        String form = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_FORM));
        String frequency = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_FREQUENCY));
        String daysOfWeek = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_DAYS_OF_WEEK));
        int intervalDays = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_INTERVAL_DAYS));
        String times = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_TIMES));
        long startDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_START_DATE));
        long endDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_END_DATE));
        String foodRelation = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_FOOD_RELATION));
        String notes = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_NOTES));
        boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_IS_ACTIVE)) == 1;
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_CREATED_AT));

        return new Medicine(id, name, dosage, form, frequency, daysOfWeek, intervalDays,
                times, startDate, endDate, foodRelation, notes, isActive, createdAt);
    }
}
