package com.sthaabhash.mycheckins.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sthaabhash.mycheckins.model.RecordsModel;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DBNAME = "mycheckins.db",
            RECORD_TABLE = "records";


    private ArrayList<RecordsModel> recordData = new ArrayList<>();
    private Context context;

    public DBHandler(Context context) {
        super(context, DBNAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + RECORD_TABLE + "(id integer primary key autoincrement," +
                "iid varchar," +
                "title TEXT," +
                "place TEXT," +
                "details TEXT," +
                "date TEXT," +
                "location TEXT," +
                "image TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + RECORD_TABLE);
    }

    public void insertRecords(RecordsModel recordsModel) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("iid", generateID());
        contentValues.put("title", recordsModel.getTitle());
        contentValues.put("place", recordsModel.getPlace());
        contentValues.put("details", recordsModel.getDetails());
        contentValues.put("date", recordsModel.getDate());
        contentValues.put("location", recordsModel.getLocation());
        contentValues.put("image", recordsModel.getImage());
        database.insert(RECORD_TABLE, null, contentValues);
    }

    public ArrayList<RecordsModel> getRecords() {
        recordData.clear();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("Select * from " + RECORD_TABLE, null);
        if (cursor.getCount() != 0) {
            if (cursor.moveToFirst()) {
                do {
                    RecordsModel model = new RecordsModel();
                    model.setIid(cursor.getString(cursor.getColumnIndex("iid")));
                    model.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                    model.setPlace(cursor.getString(cursor.getColumnIndex("place")));
                    model.setDetails(cursor.getString(cursor.getColumnIndex("details")));
                    model.setDate(cursor.getString(cursor.getColumnIndex("date")));
                    model.setLocation(cursor.getString(cursor.getColumnIndex("location")));
                    model.setImage(cursor.getString(cursor.getColumnIndex("image")));
                    recordData.add(model);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        database.close();
        return recordData;
    }

    public void deleteRecords(RecordsModel m) {
        SQLiteDatabase database = this.getWritableDatabase();
        int id = getRecordID(m);
        if (id >= 0) {
            database.delete(RECORD_TABLE, "id = " + id, null);
            database.close();
        } else {
            Log.e("DBHandler", "error getting id");
        }
    }

    private int getRecordID(RecordsModel m) {
        int id = -1;
        SQLiteDatabase database = this.getReadableDatabase();
        String query = String.format("select id from %s where iid='%s'", RECORD_TABLE, m.getIid());
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            id = cursor.getInt(0);
        }
        return id;
    }

    private String generateID() {
        int randomNum = new Random().nextInt();
        long timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        return String.format("iid-%s-%s", timeStamp, randomNum);
    }

}
