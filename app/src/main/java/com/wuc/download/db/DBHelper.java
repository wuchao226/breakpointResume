package com.wuc.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author: wuchao
 * @date: 2018/5/22 17:44
 * @desciption:
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "down.db";
    private static final int VERSION = 2;
    private static final String SQL_CREATE = "create table thread_info(_id integer primary key autoincrement," +
            "thread_id integer,url text,start long,ended long,finished long)";
    private static final String SQL_DROP = "drop table if exists thread_info";
    private static DBHelper sDBHelper = null;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public static DBHelper getInstance(Context context) {
        if (sDBHelper == null) {
            synchronized (DBHelper.class) {
                if (sDBHelper == null) {
                    sDBHelper = new DBHelper(context);
                }
            }
        }
        return sDBHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP);
        db.execSQL(SQL_CREATE);
        Log.i("DBHelper", "drop==");
    }

}
