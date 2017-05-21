package com.cheney.filedownexample.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cheney.filedownexample.download.DownloadTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheney on 2017/5/20.
 */

public class TaskDBHandler {
    private SQLiteDatabase db;
    public TaskDBHandler() {
        db = DownloadDbHelper.getInstance().getWritableDatabase();
    }

    public void addTask(DownloadTask task) {
        if (task == null) {
            return;
        }
        insertOrUpdate(task);
    }

    public void updateTask(DownloadTask task) {
        if (task == null) {
            return;
        }
        insertOrUpdate(task);
    }

    public void deleteTask(DownloadTask task) {
        if (task == null) {
            return;
        }
        db.delete(DownloadDbConstant.Table.DOWNLOAD_TASK, DownloadDbConstant.DownloadTaskField.URL + "=?", new String[]{task.getUrl()});
    }

    public List<DownloadTask> getAllTasks() {
        List<DownloadTask> tasks = new ArrayList<>();
        Cursor cursor = db.query(DownloadDbConstant.Table.DOWNLOAD_TASK, null, null, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    DownloadTask task = new DownloadTask();
                    task.fromCursor(cursor);
                    tasks.add(task);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return tasks;
    }

    public List<DownloadTask> getUnFinishedTasks() {
        List<DownloadTask> tasks = new ArrayList<>();
        Cursor cursor = db.query(DownloadDbConstant.Table.DOWNLOAD_TASK, null,
                DownloadDbConstant.DownloadTaskField.STATUS + " IN (?, ?, ?)",
                new String[]{String.valueOf(DownloadTask.Status.IDLE), String.valueOf(DownloadTask.Status.WAITING),
                        String.valueOf(DownloadTask.Status.DOWNLOADING)}, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    DownloadTask task = new DownloadTask();
                    task.fromCursor(cursor);
                    tasks.add(task);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return tasks;
    }

    private synchronized void insertOrUpdate(DownloadTask task) {
        Cursor cursor = db.query(DownloadDbConstant.Table.DOWNLOAD_TASK, null, DownloadDbConstant.DownloadTaskField.URL + "=?", new String[]{task.getUrl()}, null, null, null);
        boolean needInsert = true;
        ContentValues contentValues = task.getContentValues();
        try {
            if (cursor != null && cursor.getCount() > 0) {
                // update task
                int rows = db.update(DownloadDbConstant.Table.DOWNLOAD_TASK, contentValues, DownloadDbConstant.DownloadTaskField.URL + "=?", new String[]{task.getUrl()});
                if (rows > 0) {
                    needInsert = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        if (needInsert) {
            db.insert(DownloadDbConstant.Table.DOWNLOAD_TASK, null, contentValues);
        }
    }
}
