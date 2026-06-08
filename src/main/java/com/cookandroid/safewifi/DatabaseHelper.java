package com.cookandroid.safewifi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "safewifi.db";
    private static final int    DB_VERSION = 1;
    public  static final String TABLE      = "wifi_scan_logs";

    public static final String COL_ID            = "id";
    public static final String COL_SCAN_TYPE     = "scan_type";
    public static final String COL_SSID          = "ssid";
    public static final String COL_SECURITY_TYPE = "security_type";
    public static final String COL_RISK_SCORE    = "risk_score";
    public static final String COL_RESULT_TEXT   = "result_text";
    public static final String COL_CREATED_AT    = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable =
                "CREATE TABLE " + TABLE + " (" +
                        COL_ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_SCAN_TYPE     + " TEXT NOT NULL, " +
                        COL_SSID          + " TEXT, " +
                        COL_SECURITY_TYPE + " TEXT, " +
                        COL_RISK_SCORE    + " INTEGER, " +
                        COL_RESULT_TEXT   + " TEXT, " +
                        COL_CREATED_AT    + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public long insert(WifiScanLog log) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SCAN_TYPE,     log.getScanType());
        values.put(COL_SSID,          log.getSsid());
        values.put(COL_SECURITY_TYPE, log.getSecurityType());
        values.put(COL_RISK_SCORE,    log.getRiskScore());
        values.put(COL_RESULT_TEXT,   log.getResultText());
        values.put(COL_CREATED_AT,    log.getCreatedAt());
        long id = db.insert(TABLE, null, values);
        db.close();
        return id;
    }

    public List<WifiScanLog> getAll() {
        List<WifiScanLog> list = new ArrayList<WifiScanLog>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, null, null,
                null, null, COL_CREATED_AT + " DESC");
        if (cursor.moveToFirst()) {
            do {
                list.add(fromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public WifiScanLog getById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null,
                COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        WifiScanLog log = null;
        if (cursor.moveToFirst()) {
            log = fromCursor(cursor);
        }
        cursor.close();
        db.close();
        return log;
    }

    public void delete(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, null, null);
        db.close();
    }

    private WifiScanLog fromCursor(Cursor cursor) {
        WifiScanLog log = new WifiScanLog();
        log.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        log.setScanType(cursor.getString(cursor.getColumnIndexOrThrow(COL_SCAN_TYPE)));
        log.setSsid(cursor.getString(cursor.getColumnIndexOrThrow(COL_SSID)));
        log.setSecurityType(cursor.getString(cursor.getColumnIndexOrThrow(COL_SECURITY_TYPE)));
        log.setRiskScore(cursor.getInt(cursor.getColumnIndexOrThrow(COL_RISK_SCORE)));
        log.setResultText(cursor.getString(cursor.getColumnIndexOrThrow(COL_RESULT_TEXT)));
        log.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));
        return log;
    }
}