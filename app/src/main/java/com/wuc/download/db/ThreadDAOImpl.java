package com.wuc.download.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.wuc.download.entities.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: wuchao
 * @date: 2018/5/22 18:12
 * @desciption: 数据访问接口实现
 */
public class ThreadDAOImpl implements ThreadDAO {

    private DBHelper mDBHelper;

    public ThreadDAOImpl(Context context) {
        mDBHelper = DBHelper.getInstance(context);
    }

    @Override
    public synchronized void insertThread(ThreadInfo threadInfo) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("insert into thread_info(thread_id,url,start,ended,finished) values(?,?,?,?,?)",
                new Object[]{threadInfo.getId(), threadInfo.getUrl(), threadInfo.getStart(),
                        threadInfo.getEnd(), threadInfo.getFinished()});
        db.close();
    }

    @Override
    public synchronized void deleteThread(String url) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ?",
                new Object[]{url});
        db.close();
    }

    @Override
    public synchronized void updateThread(String url, int thread_id, long finished) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("update thread_info set finished = ? where url = ? and thread_id = ?",
                new Object[]{finished, url, thread_id});
        db.close();
    }

    @Override
    public List<ThreadInfo> getThreads(String url) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        List<ThreadInfo> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ?", new String[]{url});
        while (cursor.moveToNext()) {
            ThreadInfo threadInfo = new ThreadInfo();
            threadInfo.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
            threadInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            threadInfo.setStart(cursor.getInt(cursor.getColumnIndex("start")));
            threadInfo.setEnd(cursor.getInt(cursor.getColumnIndex("ended")));
            threadInfo.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
            Log.d("ThreadDAOImpl", "mFinished:" + cursor.getLong(cursor.getColumnIndex("finished")));
            list.add(threadInfo);
        }
        db.close();
        cursor.close();
        return list;
    }

    @Override
    public boolean isExists(String url, int thread_id) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ? and thread_id = ?",
                new String[]{url, thread_id + ""});
        boolean isExists = cursor.moveToNext();
        db.close();
        cursor.close();
        return isExists;
    }
}
