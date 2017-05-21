package com.cheney.filedownexample.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by cheney on 2017/5/20.
 */

public class DownloadDbHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;

    private static DownloadDbHelper instance;

    public synchronized static void setInstance(DownloadDbHelper downloadDbHelper) {
        if (instance != null) {
            instance.close();
            instance = null;
        }
        instance = downloadDbHelper;
    }

    public synchronized static DownloadDbHelper getInstance() {
        return instance;
    }

    public DownloadDbHelper(Context context) {
        super(context, DownloadDbConstant.DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(DownloadDbConstant.TableCreate.CREATE_DOWNLOAD_TASK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
