package com.medicinereminder.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "medicine_reminder.db";
    private static final int DATABASE_VERSION = 1;

    private static DatabaseHelper instance;

    // Table names
    public static final String TABLE_MEDICINES = "medicines";
    public static final String TABLE_DOSE_LOGS = "dose_logs";

    // Column names - medicines
    public static final String COL_MED_ID = "id";
    public static final String COL_MED_NAME = "name";
    public static final String COL_MED_DOSAGE = "dosage";
    public static final String COL_MED_FORM = "form";
    public static final String COL_MED_FREQUENCY = "frequency";
    public static final String COL_MED_DAYS_OF_WEEK = "days_of_week";
    public static final String COL_MED_INTERVAL_DAYS = "interval_days";
    public static final String COL_MED_TIMES = "times";
    public static final String COL_MED_START_DATE = "start_date";
    public static final String COL_MED_END_DATE = "end_date";
    public static final String COL_MED_FOOD_RELATION = "food_relation";
    public static final String COL_MED_NOTES = "notes";
    public static final String COL_MED_IS_ACTIVE = "is_active";
    public static final String COL_MED_CREATED_AT = "created_at";

    // Column names - dose_logs
    public static final String COL_LOG_ID = "id";
    public static final String COL_LOG_MED_ID = "medicine_id";
    public static final String COL_LOG_MED_NAME = "medicine_name";
    public static final String COL_LOG_DOSAGE = "dosage";
    public static final String COL_LOG_FORM = "form";
    public static final String COL_LOG_FOOD_RELATION = "food_relation";
    public static final String COL_LOG_SCHEDULED_TIME = "scheduled_time_millis";
    public static final String COL_LOG_ACTUAL_TIME = "actual_time_millis";
    public static final String COL_LOG_STATUS = "status";
    public static final String COL_LOG_DATE_STR = "date_str";
    public static final String COL_LOG_TIME_STR = "time_str";
    public static final String COL_LOG_NOTES = "notes";

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create medicines table
        String createMedicinesTable = "CREATE TABLE " + TABLE_MEDICINES + " ("
                + COL_MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_MED_NAME + " TEXT NOT NULL, "
                + COL_MED_DOSAGE + " TEXT NOT NULL, "
                + COL_MED_FORM + " TEXT, "
                + COL_MED_FREQUENCY + " TEXT, "
                + COL_MED_DAYS_OF_WEEK + " TEXT, "
                + COL_MED_INTERVAL_DAYS + " INTEGER DEFAULT 1, "
                + COL_MED_TIMES + " TEXT NOT NULL, "
                + COL_MED_START_DATE + " INTEGER, "
                + COL_MED_END_DATE + " INTEGER, "
                + COL_MED_FOOD_RELATION + " TEXT, "
                + COL_MED_NOTES + " TEXT, "
                + COL_MED_IS_ACTIVE + " INTEGER DEFAULT 1, "
                + COL_MED_CREATED_AT + " INTEGER"
                + ");";

        // Create dose logs table
        String createDoseLogsTable = "CREATE TABLE " + TABLE_DOSE_LOGS + " ("
                + COL_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_LOG_MED_ID + " INTEGER NOT NULL, "
                + COL_LOG_MED_NAME + " TEXT NOT NULL, "
                + COL_LOG_DOSAGE + " TEXT NOT NULL, "
                + COL_LOG_FORM + " TEXT, "
                + COL_LOG_FOOD_RELATION + " TEXT, "
                + COL_LOG_SCHEDULED_TIME + " INTEGER, "
                + COL_LOG_ACTUAL_TIME + " INTEGER, "
                + COL_LOG_STATUS + " TEXT NOT NULL, "
                + COL_LOG_DATE_STR + " TEXT NOT NULL, "
                + COL_LOG_TIME_STR + " TEXT NOT NULL, "
                + COL_LOG_NOTES + " TEXT, "
                + "UNIQUE (" + COL_LOG_MED_ID + ", " + COL_LOG_DATE_STR + ", " + COL_LOG_TIME_STR + ") ON CONFLICT REPLACE"
                + ");";

        db.execSQL(createMedicinesTable);
        db.execSQL(createDoseLogsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOSE_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICINES);
        onCreate(db);
    }
}
