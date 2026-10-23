package com.medicinereminder.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.medicinereminder.app.model.DoseRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoseLogDao {

    private final DatabaseHelper dbHelper;

    public DoseLogDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insertOrUpdateLog(DoseRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_LOG_MED_ID, record.getMedicineId());
        values.put(DatabaseHelper.COL_LOG_MED_NAME, record.getMedicineName());
        values.put(DatabaseHelper.COL_LOG_DOSAGE, record.getDosage());
        values.put(DatabaseHelper.COL_LOG_FORM, record.getForm());
        values.put(DatabaseHelper.COL_LOG_FOOD_RELATION, record.getFoodRelation());
        values.put(DatabaseHelper.COL_LOG_SCHEDULED_TIME, record.getScheduledTimeMillis());
        values.put(DatabaseHelper.COL_LOG_ACTUAL_TIME, record.getActualTimeMillis());
        values.put(DatabaseHelper.COL_LOG_STATUS, record.getStatus());
        values.put(DatabaseHelper.COL_LOG_DATE_STR, record.getDateStr());
        values.put(DatabaseHelper.COL_LOG_TIME_STR, record.getTimeStr());
        values.put(DatabaseHelper.COL_LOG_NOTES, record.getNotes());

        long id = db.insertWithOnConflict(DatabaseHelper.TABLE_DOSE_LOGS, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
        record.setId(id);
        return id;
    }

    public DoseRecord getLog(long medicineId, String dateStr, String timeStr) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_DOSE_LOGS, null,
                DatabaseHelper.COL_LOG_MED_ID + " = ? AND " +
                        DatabaseHelper.COL_LOG_DATE_STR + " = ? AND " +
                        DatabaseHelper.COL_LOG_TIME_STR + " = ?",
                new String[]{String.valueOf(medicineId), dateStr, timeStr},
                null, null, null);

        DoseRecord record = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                record = fromCursor(cursor);
            }
            cursor.close();
        }
        return record;
    }

    public Map<String, DoseRecord> getLogsMapForDate(String dateStr) {
        Map<String, DoseRecord> map = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_DOSE_LOGS, null,
                DatabaseHelper.COL_LOG_DATE_STR + " = ?",
                new String[]{dateStr},
                null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                DoseRecord record = fromCursor(cursor);
                String key = record.getMedicineId() + "_" + record.getTimeStr();
                map.put(key, record);
            }
            cursor.close();
        }
        return map;
    }

    public List<DoseRecord> getAllLogs(int limit) {
        List<DoseRecord> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String limitStr = limit > 0 ? String.valueOf(limit) : null;
        Cursor cursor = db.query(DatabaseHelper.TABLE_DOSE_LOGS, null,
                null, null, null, null,
                DatabaseHelper.COL_LOG_SCHEDULED_TIME + " DESC",
                limitStr);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(fromCursor(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public void deleteLog(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_DOSE_LOGS,
                DatabaseHelper.COL_LOG_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public void clearAllLogs() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_DOSE_LOGS, null, null);
    }

    private DoseRecord fromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOG_ID));
        long medicineId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOG_MED_ID));
        String medicineName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOG_MED_NAME));
        String dosage = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOG_DOSAGE));
        String form = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOG_FORM));
        String foodRelation = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOG_FOOD_RELATION));
        long scheduledTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOG_SCHEDULED_TIME));
        long actualTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOG_ACTUAL_TIME));
        String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOG_STATUS));
        String dateStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOG_DATE_STR));
        String timeStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOG_TIME_STR));
        String notes = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOG_NOTES));

        return new DoseRecord(id, medicineId, medicineName, dosage, form, foodRelation,
                scheduledTime, actualTime, status, dateStr, timeStr, notes);
    }
}
