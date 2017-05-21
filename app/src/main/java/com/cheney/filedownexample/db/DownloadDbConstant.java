package com.cheney.filedownexample.db;

/**
 * Created by cheney on 2017/5/20.
 */

public class DownloadDbConstant {
    public static final String DB_NAME = "download.db";

    interface Table {
        String DOWNLOAD_TASK = "download_task";
    }

    public interface DownloadTaskField {
        String ID = "_id";
        String URL = "_url";
        String LOCAL_PATH = "_local_path";
        String FILE_TOTAL_SIZE = "_file_total_size";
        String DOWNLOADED_SIZE = "_downloaded_size";
        String STATUS = "_status";
        String PRIORITY = "_priority";
        String PROGRESS = "_progress";
    }

    interface TableCreate {
        String CREATE_DOWNLOAD_TASK = "CREATE TABLE " + Table.DOWNLOAD_TASK
                + " ( " + DownloadTaskField.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DownloadTaskField.URL + " VARCHAR, "
                + DownloadTaskField.LOCAL_PATH + " VARCHAR, "
                + DownloadTaskField.FILE_TOTAL_SIZE + " BIGINT DEFAULT 0, "
                + DownloadTaskField.DOWNLOADED_SIZE + " BIGINT DEFAULT 0, "
                + DownloadTaskField.STATUS + " SMALLINT DEFAULT 0, "
                + DownloadTaskField.PRIORITY + " SMALLINT DEFAULT 0, "
                + DownloadTaskField.PROGRESS + " SMALLINT DEFAULT 0 "
                + ")";
    }
}
